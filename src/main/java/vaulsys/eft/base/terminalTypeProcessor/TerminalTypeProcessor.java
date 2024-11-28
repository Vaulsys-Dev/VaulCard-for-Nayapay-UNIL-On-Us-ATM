
package vaulsys.eft.base.terminalTypeProcessor;

import vaulsys.authentication.exception.AuthenticationException;
import vaulsys.caching.CheckAccountParamsForCache;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.message.Message;
import vaulsys.message.exception.DuplicateMessageException;
import vaulsys.message.exception.MessageAlreadyReversedException;
import vaulsys.message.exception.ReturnOfTransactionNotAllowed;
import vaulsys.message.exception.ReversalOriginatorNotApproved;
import vaulsys.message.exception.ReversalOriginatorNotFoundException;
import vaulsys.migration.MigrationDataService;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.exception.exception.InvalidBusinessDateException;
import vaulsys.protocols.exception.exception.ReferenceTransactionNotFoundException;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.LifeCycleStatus;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;

public abstract class TerminalTypeProcessor {

	private Logger logger = Logger.getLogger(this.getClass());

	 public void messageValidation(Ifx ifx, Long messageId) throws Exception {
		if (!Util.isAccount(ifx.getAppPAN()) && Util.hasText(ifx.getAppPAN()) && !ifx.getAppPAN().substring(0, 6).equals(ifx.getDestBankId().toString()))
			throw new AuthenticationException(ifx.getAppPAN() + " doesn't belong to bank " + ifx.getDestBankId(), true);

		if (Util.hasText(ifx.getTrk2EquivData()) && !ISOFinalMessageType.isTransferMessage(ifx.getIfxType())) {
			if (!ifx.getTrk2EquivData().contains(ifx.getAppPAN()))
				throw new AuthenticationException(ifx.getTrk2EquivData() + " doesn't correspond with "
						+ ifx.getAppPAN(), true);
		}
	}

	
	public void bindMessage(Message message) throws Exception {
		logger.debug("Message binder...");

		if (!message.isIncomingMessage())
			throw new Exception("input message is not of applicable type (Message.INCOMING)");

		Ifx ifx = message.getIfx();
		Transaction transaction = message.getTransaction();
	
		if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
			bindResponseMessage(ifx);
			if (message.getChannel().getInstitutionId().equals("639347"))
				try {
				MigrationDataService.fillOnLineData(ifx, transaction.getId());
				} catch(Exception e) {
					logger.error("Exception in filling migration data, " + e, e);
				}

		} else if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) ||
				ISOFinalMessageType.isPrepareMessage(ifx.getIfxType()) ||
				ISOFinalMessageType.isPrepareReversalMessage(ifx.getIfxType())) {
			bindRequestMessage(ifx);
		}

		TransactionService.updateLifeCycleStatus(transaction, transaction.getIncomingIfx()/*getInputMessage().getIfx()*/);
		/**
		 * @author k.khodadi
		 * for sorush
		 */
//		TransactionService.checkReverseSorush(transaction, ifx);
		
	}

	protected void bindResponseMessage(Ifx ifx) throws Exception {
		Transaction transaction = ifx.getTransaction();
		Transaction referenceTransaction = null;
		
		try {
			referenceTransaction = getFirstTransaction(ifx);

			if (referenceTransaction == null) {
				logger.error("No Reference for transaction " + transaction.getDebugTag() + "(" + transaction.getId()+ ")");
				throw new ReferenceTransactionNotFoundException("No Reference for transaction "+ transaction.getDebugTag() + "(" + transaction.getId() + ")");
			}
			logger.debug("referenceTransaction found:" + referenceTransaction.getId());

			// TODO: change refTransaction
			transaction.setFirstTransaction(referenceTransaction);
			transaction.setReferenceTransaction(referenceTransaction.getReferenceTransaction());

			copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/);
			
			postBindingResponseMessage(ifx, referenceTransaction);
			if (!ISOFinalMessageType.isReversalRsMessage(ifx.getIfxType())
					//m.rehman: adding check for advice response message
					&& !ISOMessageTypes.isFinancialAdviceResponseMessage(ifx.getMti())
					//m.rehman: adding check for Loro advice response message
					&& !ISOMessageTypes.isLoroAdviceResponseMessage(ifx.getMti())
					&& ISOResponseCodes.APPROVED.equals(ifx.getRsCode())
					&& reversalMessageHasBeenRecieved(transaction.getFirstTransaction())) {
				throw new MessageAlreadyReversedException("Message already reversed - message is reversing.", true);
				
			} else if (!ISOResponseCodes.APPROVED.equals(ifx.getRsCode()) &&
//					GlobalContext.getInstance().getMyInstitution().getBin().equals(ifx.getBankId()) &&
					ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId()) &&
					TerminalType.ATM.equals(ifx.getTerminalType()) 
//					&&
//					TerminalType.ATM.equals(ifx.getTransaction().getFirstTransaction().getInputMessage().getIfx().getEndPointTerminal().getTerminalType())
					) {
				
				if (reversalMessageHasBeenRecieved(transaction.getFirstTransaction())) {
					throw new MessageAlreadyReversedException("Message already reversed - message is reversing.", true);
				}
				
			}
			
			if (ISOFinalMessageType.isReversalRsMessage(ifx.getIfxType()) /*&& Util.hasText(transaction.getLifeCycle().getReversalRsCode())*/) {
				if (ISOResponseCodes.isReplaceRsCode(transaction.getLifeCycle().getReversalRsCode(), ifx.getRsCode()))
					transaction.getLifeCycle().setReversalRsCode(ifx.getRsCode());
			}

			if (reversalMessageHasBeenSent(transaction)) {
				throw new MessageAlreadyReversedException("Message already reversed - not notifying.", false);
			}
			
			if (isMessageExpired(ifx)) {
				throw new MessageAlreadyReversedException("Message expired - don't forward to terminal.", false);
			}
			
		} catch (DuplicateMessageException e) {
			throw e;
		} catch (MessageAlreadyReversedException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}
	}

	private boolean isMessageExpired(Ifx ifx) {
		Transaction transaction = ifx.getTransaction();
		Transaction firstTransaction = transaction.getFirstTransaction();
		Ifx firstIfx = firstTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/;

		Terminal endPointTerminal = null;
		
		if (IfxType.TRANSFER_TO_ACCOUNT_RS.equals(ifx.getIfxType()) &&
				IfxType.TRANSFER_FROM_ACCOUNT_RS.equals(firstIfx.getIfxType())) {
			endPointTerminal = firstTransaction.getFirstTransaction().getIncomingIfxOrMessageEndpoint();
			
		} else {
			endPointTerminal = firstTransaction.getIncomingIfxOrMessageEndpoint();
			
		}
		
		if (endPointTerminal != null &&
				endPointTerminal.getLastIncomingTransaction() != null) {
			Transaction lastInTrx = endPointTerminal.getLastIncomingTransaction();
			
			if (firstTransaction == lastInTrx) {
				logger.debug("received RS message is response of terminal_last incoming trx, OK ");
				return false;
			}
			
			if (lastInTrx.getIncomingIfx().getOrigDt().after(ifx.getOrigDt())) {
				logger.debug("received RS message is NOT response of terminal_last incoming trx, NOK ");
				return true;
				
			} else {
				logger.debug("terminal(" + endPointTerminal.getId() + ") last incoming trx: " + lastInTrx.getId() + ", incoming RS trx: " + transaction.getId() + " , OK ");
				return false;
			}
		}
			
		return false;
		
	}


	protected void postBindingResponseMessage(Ifx incomingIfx, Transaction referenceTrx) {
		if (!incomingIfx.getRequest()
				&& TerminalType.ATM.equals(incomingIfx.getTerminalType())
//				&& GlobalContext.getInstance().getMyInstitution().getBin().equals(incomingIfx.getBankId())
				&& ProcessContext.get().getMyInstitution().getBin().equals(incomingIfx.getBankId())
//				&& FinancialEntityRole.MY_SELF.equals(GlobalContext.getInstance().getMyInstitution().getRole())
				&& FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())
//				&& !TerminalService.isOriginatorSwitchTerminal(incomingMessage.getTransaction().getFirstTransaction().getInputMessage())
				){
			long currentTimeMillis = System.currentTimeMillis();
			logger.debug("Try to lock atm terminal " + incomingIfx.getTerminalId()+" on receiving response message!");
			ATMTerminal atm = null;
			try {
				atm = TerminalService.findTerminal(ATMTerminal.class, Long.valueOf(incomingIfx.getTerminalId()));
//				GeneralDao.Instance.synchObject(atm);
				TerminalService.lockTerminal(incomingIfx.getTerminalId(), LockMode.UPGRADE);
				
				logger.debug("terminal locked.... " + incomingIfx.getTerminalId() + ", " + (System.currentTimeMillis()-currentTimeMillis));
			} catch (Exception e) {
				logger.error("Encounter an exception to lock atm terminal", e);
			}
		}
//		else if(TerminalType.INTERNET.equals(incomingIfx.getTerminalType()) &&
//				ProcessContext.get().getMyInstitution().getBin().equals(incomingIfx.getBankId())&&
//				FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())){
//			EpayProcessor.Instance.postBindingResponseMessage(incomingIfx, referenceTrx);
//		}
		
	}


	private boolean reversalMessageHasBeenSent(Transaction transaction) {

		if (ISOFinalMessageType.isReversalOrRepeatRsMessage(transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType()))
			return false;

		logger.debug("Try to get Lock of LifeCycle[" + transaction.getLifeCycleId() + ")");
		
		transaction.getAndLockLifeCycle(LockMode.UPGRADE);
		
		long currentTimeMillis = System.currentTimeMillis();
		GeneralDao.Instance.optimizedSynchObject(transaction.getLifeCycle());
		logger.debug("LifeCycle[" + transaction.getLifeCycleId() + ") has beeb locked and it's reloaded!, " + (System.currentTimeMillis()-currentTimeMillis));

		if (!LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsFullyReveresed())
				|| !LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsPartiallyReveresed())
				|| !LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsReturnReversed()))
			return true;

		return false;
	}
	
	public boolean reversalMessageHasBeenRecieved(Transaction transaction) {

		Message inputMessage = transaction.getInputMessage();
		Ifx ifx = inputMessage.getIfx();
//		Long myBin = FinancialEntityService.getMyInstitution().getBin();
		if (inputMessage.getEndPointTerminal() == null || !TerminalType.ATM.equals(inputMessage.getEndPointTerminal().getTerminalType())) {
			if (ISOFinalMessageType.isMessageNotToBeReverse(transaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType()))
				return false;
		}

//		if (!myBin.equals(ifx.getBankId()))
//			return false;

		logger.debug("Try to get Lock of LifeCycle[" + transaction.getLifeCycleId() + ")");
		
		transaction.getAndLockLifeCycle(LockMode.UPGRADE);
		
		long currentTimeMillis = System.currentTimeMillis();
		GeneralDao.Instance.optimizedSynchObject(transaction.getLifeCycle());
		logger.debug("LifeCycle[" + transaction.getLifeCycleId() + ") has beeb locked and it's reloaded!, " + (System.currentTimeMillis()-currentTimeMillis));

		if (!LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsFullyReveresed())
				|| !LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsPartiallyReveresed())
				|| !LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsReturnReversed()))
			return true;

		logger.debug("Searching for reversal Message (BankId: " + ifx.getBankId() + ", terminalId: "
				+ ifx.getTerminalId() + ", orgId: " + ifx.getOrgIdNum() + ", appPAN: " + ifx.getAppPAN() + ", OrigDt: "
				+ ifx.getOrigDt() + ", NetworkRefId: " + ifx.getNetworkRefId() + ", TrnSecCntr: "
				+ ifx.getSrc_TrnSeqCntr() + ", TrnType: " + ifx.getTrnType());

		List<Long> reversalRequests = TransactionService.hasReversalRequest(inputMessage.getIfx());

		if (reversalRequests != null && reversalRequests.size() > 0) {
			logger.error(reversalRequests.size() + " Reversal requests are found for Trx " + transaction.getDebugTag()
					+ "[" + transaction.getId() + "] ifxIds["+reversalRequests.toString()+"]");
			return true;
		} else {
//			logger.debug("No Reversal request is found for Trx " + transaction.getDebugTag() + "["
//					+ transaction.getId() + "]");
			return false;
		}
	}
	
	private Transaction getFirstTransaction(Ifx ifx) throws DuplicateMessageException {
		List<Transaction> transactions = TransactionService.findFirstTransactionsWithoutQuery(ifx);
		if (transactions == null || transactions.size() == 0)
			return null;

		if (transactions.size() > 1) {
			List<Transaction> goodTransactions = new ArrayList<Transaction>();
			if (!ISOFinalMessageType.isReversalRsMessage(ifx.getIfxType())
					&& !ISOMessageTypes.isFinancialAdviceResponseMessage(ifx.getMti())  //m.rehman: for advice
					&& !ISOMessageTypes.isLoroAdviceResponseMessage(ifx.getMti())) {   //m.rehman: for loro
				String detailMessage = "Duplicate requests found for a response: " + transactions.size() + "(";
				for (Transaction t : transactions) {
					detailMessage += t.getId() + ":" + t.getDebugTag() + ", ";
					if (t.getInputMessage() != null && t.getIncomingIfx()/*getInputMessage().getIfx()*/ != null) {
						if (!ISOFinalMessageType.isGetAccountMessage(t.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType()))
							goodTransactions.add(t);
					}
				}
				logger.warn(detailMessage + ")");
				if(goodTransactions != null && goodTransactions.size()>0)
					return goodTransactions.get(0);
				else 
					return null;

			} else {
				if(transactions != null && transactions.size() > 0)
					return transactions.get(0);
				else 
					return null;
			}

		} else if (transactions.size() == 1) {
			return transactions.get(0);
		} else {
			return null;
		}
	}
	
	protected void bindRequestMessage(Ifx ifx) throws Exception {
		Transaction transaction = ifx.getTransaction();
		ifx.setFirstTrxId(transaction.getId());
		Transaction referenceTransaction = null;
		try {
			setRRNForRequest(ifx);
		/*	*//**
			 * @author k.khodadi
			 * for sorush
			 *//*
			TransactionService.checkReverseSorush(transaction, ifx);*/
			
		} catch (InvalidBusinessDateException e) {
			logger.error(e);
			throw e;
		} catch (ReferenceTransactionNotFoundException e) {
			logger.error(e);
			throw e;
		} catch (Exception e) {
			logger.error("Can't set RRN: " + e, e);
		}
		
		/**
		 * @author k.khodadi
		 * for sorush
		 */
		TransactionService.checkReverseSorush(transaction, ifx);
		
		
		logger.debug("RRNForRequest set...");

		
		if ((ISOFinalMessageType.isReturnReverseMessage(ifx.getIfxType())
			|| (ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType()) && !alreadyReturn(transaction))
			|| ISOFinalMessageType.isReturnRq(ifx.getIfxType()))
				//m.rehman
			|| ifx.getIfxType().equals(IfxType.PREAUTH_COMPLET_ADVICE_RQ)) {

			referenceTransaction = transaction.getReferenceTransaction();
			Transaction refereceTransactionRs = null;

			if (referenceTransaction == null) {
				logger.info("ReversalOriginatorNotFoundException: " + "(NetworkRefId= " + ifx.getNetworkRefId()
						+ ", TrnSecCntr= " + ifx.getSafeOriginalDataElements().getTrnSeqCounter() + ", BankId= "
						+ ifx.getBankId() + ", OrigDt= " + ifx.getSafeOriginalDataElements().getOrigDt());
				throw new ReversalOriginatorNotFoundException("(NetworkRefId= " + ifx.getNetworkRefId()
						+ ", TrnSecCntr= " + ifx.getSafeOriginalDataElements().getTrnSeqCounter() + ", BankId= "
						+ ifx.getBankId() + ", OrigDt= " + ifx.getSafeOriginalDataElements().getOrigDt());

			}

			if (referenceTransaction.getOutputMessage() != null)
				copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/);

			try {
				logger.debug("Try to get Lock of LifeCycle[" + transaction.getLifeCycleId() + ")");
				
				transaction.getAndLockLifeCycle(LockMode.UPGRADE);
				
				long currentTimeMillis = System.currentTimeMillis();
				GeneralDao.Instance.optimizedSynchObject(transaction.getLifeCycle());
				logger.debug("LifeCycle[" + transaction.getLifeCycleId() + ") has beeb locked and it's reloaded!, " + (System.currentTimeMillis()-currentTimeMillis));
				
				refereceTransactionRs = TransactionService.findcorrespondingResponse(referenceTransaction);
			} catch (Exception e) {
				throw new ReversalOriginatorNotFoundException("(NetworkRefId= " + ifx.getNetworkRefId()
						+ ", TrnSecCntr= " + ifx.getSafeOriginalDataElements().getTrnSeqCounter() + ", BankId= "
						+ ifx.getBankId() + ", OrigDt= " + ifx.getSafeOriginalDataElements().getOrigDt());
			}

			// TODO: double check this
			if (refereceTransactionRs == null && !ISOFinalMessageType.isDepositTypeRevMessage(ifx.getIfxType()))
				throw new ReversalOriginatorNotApproved();

			if (refereceTransactionRs != null
					&& (refereceTransactionRs.getInputMessage().isScheduleMessage()
							|| (!ISOResponseCodes.isSuccess(refereceTransactionRs.getIncomingIfx().getRsCode()) &&
									!ISOResponseCodes.shouldBeRepeated(refereceTransactionRs.getIncomingIfx().getRsCode()))))
				throw new ReversalOriginatorNotApproved();

			if (ISOFinalMessageType.isReturnRq(ifx.getIfxType())
					&& !ISOFinalMessageType.isReturnable(referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType())) {
				throw new ReturnOfTransactionNotAllowed(referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType()+ "");
			}

			if (ISOFinalMessageType.isReturnRq(ifx.getIfxType()))
				transaction.setReferenceTransaction(referenceTransaction);
		
			if (alreadyReversed(transaction)) {
				//TODO NOTE: Only Reversal Or Return Message may be already reversed/returned! so we can check this condition 
				//in the reversal/ return part! (above condition) 
				logger.debug("alreadyReversed");
				
				if (referenceTransaction == null){
					Ifx referenceIfx = getReversalOriginatorTransaction(ifx);
					referenceTransaction = (referenceIfx!= null)? referenceIfx.getTransaction(): null;
				}
				
				String rsCode = "";
				/** ******** */
				if (referenceTransaction != null)
//					rsCode = TransactionService.getPreviousReversalRsCode(referenceTransaction, true);
					rsCode = referenceTransaction.getLifeCycle().getReversalRsCode();
				if (ISOResponseCodes.APPROVED.equals(rsCode))
					rsCode = ISOResponseCodes.INVALID_ACCOUNT;
				
				if (ISOResponseCodes.INVALID_ACCOUNT.equals(rsCode))
					throw new MessageAlreadyReversedException("Message already reversed:" + rsCode, rsCode);
			}
			
		} else {
			// Don't check duplicate Request for reversal messages!
			if (ConfigUtil.getBoolean(ConfigUtil.CHECK_DUPLICATE_MESSAGE)) {
				if (duplicateRequest(ifx)) {
					throw new DuplicateMessageException("Duplicate message.");
				}
			}
		}


		if (transaction.getFirstTransaction() == null)
			transaction.setFirstTransaction(transaction);
	}
	
	protected boolean alreadyReturn(Transaction transaction) {
		transaction.getAndLockLifeCycle(LockMode.UPGRADE);
		if (LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getIsReturned()))
			return true;
		return false;
	}
	
	protected boolean alreadyReversed(Transaction transaction) {
		if (ISOFinalMessageType.isReversalRsMessage(transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType()))
			return false;
		//TODO: MNS Performance
		//TODO: Noroozi I think there's no need to lock lifecycle here! it's been locked before!
		logger.debug("Try to get Lock of LifeCycle[" + transaction.getLifeCycleId() + ")");
		
		transaction.getAndLockLifeCycle(LockMode.UPGRADE);
		
		long currentTimeMillis = System.currentTimeMillis();
		GeneralDao.Instance.optimizedSynchObject(transaction.getLifeCycle());
		logger.debug("LifeCycle[" + transaction.getLifeCycleId() + ") has beeb locked and it's reloaded!, " + (System.currentTimeMillis()-currentTimeMillis));

		if (ISOFinalMessageType.isReturnReverseMessage(transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType())) {
			if (LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getIsReturnReversed()))
				return true;
			else
				return false;
		}

		// TODO Partial_reverse and Full_reverse?!!

		if (LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getIsFullyReveresed())
				|| LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getIsPartiallyReveresed())
				|| LifeCycleStatus.RESPONSE.equals(transaction.getLifeCycle().getIsReturned()))
			return true;

		return false;
	}
	
	protected boolean duplicateRequest(Ifx ifx) {
		String queryString = "select i.id from Ifx as i inner join " + " i.networkTrnInfo as n "
				+ " where n.NetworkRefId= :NetworkRefId " 
				+ " and n.Src_TrnSeqCntr = :TrnSeqCntr "
				+ " and n.BankId = :BankId " 
				+ " and n.DestBankId = :DestBankId " 
				+ " and n.OrigDt = :OrigDt "
				+ " and n.TerminalId = :terminalId " 
				+ " and not (n.id = :id) " 
				+ " and i.ifxType = :ifxType "
				+ " and i.ifxDirection =:IfxDirection";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("NetworkRefId", ifx.getNetworkRefId());
		params.put("TrnSeqCntr", ifx.getSrc_TrnSeqCntr());
		params.put("BankId", ifx.getBankId());
		params.put("DestBankId", ifx.getDestBankId());
		params.put("OrigDt", ifx.getOrigDt());
		params.put("terminalId", ifx.getTerminalId());
		params.put("id", ifx.getNetworkTrnInfo().getId());
		/*************/
		params.put("IfxDirection", IfxDirection.INCOMING);
		params.put("ifxType", ifx.getIfxType());
		/*************/

		List list = GeneralDao.Instance.find(queryString, params);
		return list.size() > 0;
	}
	
	protected Transaction ifxTypeBindingProcess(Ifx ifx) throws Exception {
		Transaction referenceTransaction = null;
		IfxType ifxType = ifx.getIfxType();
		
		/******* SORUSH REVERSAL ******/
		/*if(TransactionService.IsSorush(ifx)){
			
		}*/
		
	/*	if (TransactionService.IsSorush(ifx)) {
			List<Transaction> result = TransactionService.getReferenceOfSorushTransaction(ifx);
			if (result == null || result.size() <= 0) {
				throw new ReferenceTransactionNotFoundException("No Reference Transaction was found for the SORUSH request: "+ ifx.getTransaction().getId());

				
			} else if (result.size() != 1) {
				
//				List<Transaction> trxOk = new ArrayList<Transaction>();
//				for (Transaction transaction: result) {
//					Transaction responseTrx = TransactionService.findResponseTrx(transaction.getLifeCycleId(), transaction);
//					if (responseTrx != null && responseTrx.getOutgoingIfx() != null) {
//						if (ErrorCodes.APPROVED.equals(responseTrx.getOutgoingIfx().getRsCode())) {
//							trxOk.add(transaction);
//						}
//					}
//				}
//
//				if (trxOk.size() <= 0) {
//					throw new ReferenceTransactionNotFoundException("No Reference Transaction was found for the SORUSH request: "+ ifx.getTransaction().getId());
//
//				} else if (trxOk.size() != 1) {
					throw new ReferenceTransactionNotFoundException("more than one Reference Transaction were found for the SORUSH request: "+ ifx.getTransaction().getId() +
							", " + ToStringBuilder.reflectionToString(result.toArray(), ToStringStyle.MULTI_LINE_STYLE));
					
//				} else {
//
//					referenceTransaction = trxOk.get(0);
//					ifx = setOriginalData(ifx, referenceTransaction);
//
//				}
			} else {
				
				//uncomment two lines
//				if (ifx.getTrx_Amt() != null && ifx.getTrx_Amt().intValue() > 10000) {
//					throw new FITControlNotAllowedException("Failed: BankID has not allowed FIT :936450 for amount > 10000", ErrorCodes.WALLET_IN_PROVISIONAL_STATE);
//				} 
				
				referenceTransaction = result.get(0);
				ifx = setOriginalData(ifx, referenceTransaction);
				
			}
			
	   


			List<Transaction> result = TransactionService.getReferenceOfSorushTransaction(ifx);
			if (result == null || result.size() <= 0) {
				throw new ReferenceTransactionNotFoundException("No Reference Transaction was found for the SORUSH request: "+ ifx.getTransaction().getId());

				
			} else if (result.size() != 1) {
				
				List<Transaction> trxOk = new ArrayList<Transaction>();
				for (Transaction transaction: result) {
					Transaction responseTrx = TransactionService.findResponseTrx(transaction.getLifeCycleId(), transaction);
					if (responseTrx != null && responseTrx.getOutgoingIfx() != null) {
						if (ErrorCodes.APPROVED.equals(responseTrx.getOutgoingIfx().getRsCode())) {
							trxOk.add(transaction);
						}
					}
				}
				
				if (trxOk.size() <= 0) {
					throw new ReferenceTransactionNotFoundException("No Reference Transaction was found for the SORUSH request: "+ ifx.getTransaction().getId());
					
				} else if (trxOk.size() != 1) {
					throw new ReferenceTransactionNotFoundException("more than one Reference Transaction were found for the SORUSH request: "+ ifx.getTransaction().getId() +
							", " + ToStringBuilder.reflectionToString(result.toArray(), ToStringStyle.MULTI_LINE_STYLE));
					
				} else {
					
					//uncomment two lines
//					if (ifx.getTrx_Amt() != null && ifx.getTrx_Amt().intValue() > 10000) {
//						throw new FITControlNotAllowedException("Failed: BankID has not allowed FIT :936450 for amount > 10000", ErrorCodes.WALLET_IN_PROVISIONAL_STATE);
//					} 
					referenceTransaction = trxOk.get(0);
					ifx = setOriginalData(ifx, referenceTransaction);
					
				}
			} else {
				
				//uncomment two lines
//				if (ifx.getTrx_Amt() != null && ifx.getTrx_Amt().intValue() > 10000) {
//					throw new FITControlNotAllowedException("Failed: BankID has not allowed FIT :936450 for amount > 10000", ErrorCodes.WALLET_IN_PROVISIONAL_STATE);
//				} 
				
				referenceTransaction = result.get(0);
				ifx = setOriginalData(ifx, referenceTransaction);
				
			}
			
	
		
			} else */ if ((	ISOFinalMessageType.isReversalRqMessage(ifxType)
				|| ISOFinalMessageType.isReturnRq(ifxType)
				|| IfxType.PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT.equals(ifxType)
				//m.rehman: for void/pre-auth completion transaction from NAC
				|| IfxType.VOID_RQ.equals(ifxType)
				|| IfxType.PREAUTH_COMPLET_ADVICE_RQ.equals(ifxType))
			&& ifx.getOriginalDataElements() != null) {

			Ifx refIncomingIfx = getReversalOriginatorTransaction(ifx);
			ifx = checkOriginalData(ifx, refIncomingIfx);
			
			if (refIncomingIfx != null)
				referenceTransaction = refIncomingIfx.getTransaction();
			
			
		}else if (IfxType.TRANSFER_RQ.equals(ifxType) || IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(ifxType)
				|| IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifxType) || IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ.equals(ifxType)) {
			String networkRefIdIncoming = ifx.getNetworkRefId();
            /******************* get checkAccount from map or with query *******************/
            try {
                CheckAccountParamsForCache checkAccount = TransactionService.createCheckAccountObjForAddOrGet(ifx);

                Transaction referenceTransaction2 = null;
                Long refTrxId = GlobalContext.getInstance().getCheckAccountForTransfer(checkAccount);


                logger.debug("checkAccountCache: RefTrxId from cache is: " + refTrxId);
                if(refTrxId != null) {
                    referenceTransaction2 = GeneralDao.Instance.load(Transaction.class, refTrxId);
                    referenceTransaction = referenceTransaction2;
                    GlobalContext.getInstance().removeCheckAccountForTransfer(checkAccount);
                    logger.debug("checkAccountCache: checkAccount for transafer from map is : " + referenceTransaction2.getId());
                } else {
                    referenceTransaction = TransactionService.getCheckAccountTransactionOfTransfer(ifx);
                    logger.debug("checkAccountCache: checkAccount for transafer from query is : " + referenceTransaction.getId());
                }
//                if(referenceTransaction2 != null)
//                    logger.debug("checkAccountCache: checkAccount for transafer from map is : " + referenceTransaction2.getId());
//                else
//                    logger.debug("checkAccountCache: checkAccount for transafer from map is NULL");
//
//
//                if(referenceTransaction != null)
//                    logger.debug("checkAccountCache: checkAccount for transafer from query is : " + referenceTransaction.getId());
//                else
//                    logger.debug("checkAccountCache: checkAccount for transafer from query is NULL");
//
//                if(referenceTransaction != null && referenceTransaction2 != null &&
//                        referenceTransaction.getId() != null && referenceTransaction2.getId() != null &&
//                        referenceTransaction.getId().equals(referenceTransaction2.getId()))
//                    logger.debug("checkAccountCache: checkAccount for transafer from map and from query are equal");
//
//                if(referenceTransaction == null){
//                    referenceTransaction = referenceTransaction2;
//                    logger.debug("checkAccountCache: Because of checkAccount of query being null, copying checkAccount from map to it!");
//                }
            } catch (Exception e) {
                logger.error("checkAccountCache: An error occures in geting checkAccount from map, but continue!");
            }

            /*******************************************************************************/

            if (referenceTransaction == null){
				if ((IfxType.TRANSFER_RQ.equals(ifxType) || IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifxType))
						|| ((IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(ifxType) || IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ.equals(ifxType))
								&& ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId())
							&& ifx.getEndPointTerminal() != null
								// NEGIN TRANSFER
							&& !(!TerminalType.INTERNET.equals(ifx.getTerminalType()) && TerminalType.SWITCH.equals(ifx.getEndPointTerminal().getTerminalType()))
							)
							) {
						throw new ReferenceTransactionNotFoundException(
								"No Check Account Transaction was found for the transfer request.(trx: "
										+ ifx.getTransaction().getId() + ")");
					}
			}				
			

			if (referenceTransaction!=null && referenceTransaction.getOutputMessage() != null) {
				copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/);
				ifx.setNetworkRefId(networkRefIdIncoming);
			}
		}else if (IfxType.ONLINE_BILLPAYMENT_RQ.equals(ifxType)){
			referenceTransaction = TransactionService.getPrepareOnlineBillPayment(ifx);
			if(referenceTransaction == null){
				throw new ReferenceTransactionNotFoundException("NO Prepare Online BillPaymnet TransactionFound for online bill paymnet request: "+ifx.getTransaction().getId());
			}
		}/*else if (IfxType.THIRD_PARTY_PAYMENT_RQ.equals(ifx.getIfxType()) && !TerminalType.INTERNET.equals(ifx.getTerminalType())){
			referenceTransaction = TransactionService.getPrepareThirdPartyPayment(ifx);
			if(referenceTransaction == null){
				throw new ReferenceTransactionNotFoundException("No Prepare thirdpartypayment for thirdpartypayment request: " + ifx.getTransactionId());
			}
			if (referenceTransaction!=null && referenceTransaction.getOutputMessage() != null ) {
				copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()getOutputMessage().getIfx());
//				ifx.setNetworkRefId(networkRefIdIncoming);
			}
		}*/
		return referenceTransaction;
	}

	protected Ifx getReversalOriginatorTransaction(Ifx incomingIfx) throws Exception {
		Ifx refIncomingIfx = null;
		Transaction refTrnx = null;
		List<Transaction> refTrxList = null;
		try {
			refTrxList = TransactionService.getReversalOriginatorTransaction(incomingIfx);

			if(refTrxList == null || refTrxList.size() == 0) {
				logger.info("Ref Transaction is Null"); //Raza TEMP
				return null;
			}
			
			if (IfxType.RETURN_RQ.equals(incomingIfx.getIfxType())) {
				refTrnx = getValidOriginatorTransaction(refTrxList);
				
			} else 
				refTrnx = refTrxList.get(0);
				
			if (refTrnx == null)
				return null;
			
			refIncomingIfx = refTrnx.getIncomingIfx()/*getInputMessage().getIfx()*/;
		} catch (Exception e) {
			if (!Util.hasText(incomingIfx.getNetworkRefId())) {
				incomingIfx.setNetworkRefId(incomingIfx.getSrc_TrnSeqCntr());
				logger.info("incomingIfx NetworkRefId has been set to: " + incomingIfx.getSrc_TrnSeqCntr()
						+ "(incomingIfx.Src_TrnSeqCntr)");
			}

			throw new ReferenceTransactionNotFoundException(" (trnSeqCntr= "
					+ incomingIfx.getSafeOriginalDataElements().getTrnSeqCounter() + ", terminalId= "
					+ incomingIfx.getTerminalId() + ", OrigDt= "
					+ incomingIfx.getSafeOriginalDataElements().getOrigDt() + ")" + e, e);
		}
		incomingIfx.getTransaction().setReferenceTransaction(refTrnx);
		return refIncomingIfx;
	}


	private Transaction getValidOriginatorTransaction(List<Transaction> refTrxList) {
		for (Transaction trx: refTrxList) {
			Transaction rsTrx = TransactionService.findResponseTrx(trx.getLifeCycleId(), trx);
			if (rsTrx != null) {
				if (ISOResponseCodes.APPROVED.equals(rsTrx.getIncomingIfx()/*getInputMessage().getIfx()*/.getRsCode()))
					return trx;
			}
		}
		return null;
	}


	protected Ifx checkOriginalData(Ifx incomingIfx, Ifx refIncomingIfx) throws InvalidBusinessDateException {
		if (refIncomingIfx == null)
			return incomingIfx;
		Transaction refTrnx = refIncomingIfx.getTransaction(); 
		
/*		if (refTrnx != null && refTrnx.getSourceSettleInfo() != null
				&& !SettledState.NOT_SETTLED.equals(refTrnx.getSourceSettleInfo().getSettledState())) {
			throw new InvalidBusinessDateException("Originator Transaction already settled.(refTrx: "+ refTrnx.getId() + ")");
		}
*/
		if (TransactionService.isReferenceTrxSettled(refTrnx))
			throw new InvalidBusinessDateException("Originator Transaction already settled.(refTrx: "
					+ refTrnx.getId() + ")");
		
		return setOriginalData(incomingIfx, refTrnx);
	}


	private Ifx setOriginalData(Ifx incomingIfx, Transaction refTrnx) {
		Ifx refIncomingIfx;
		if (refTrnx != null) {
			refIncomingIfx = refTrnx.getIncomingIfx()/*getInputMessage().getIfx()*/;
			
			if (ISOFinalMessageType.isReturnRq(incomingIfx.getIfxType())) {
				incomingIfx.setAuth_Amt(refIncomingIfx.getAuth_Amt());
				incomingIfx.setReal_Amt(incomingIfx.getReal_Amt());
				incomingIfx.setTrx_Amt(incomingIfx.getTrx_Amt());
				incomingIfx.setSec_Amt(refIncomingIfx.getSec_Amt());
			}
			
			if (incomingIfx.getOriginalDataElements().getFwdBankId() == null)
				incomingIfx.getOriginalDataElements().setFwdBankId(refIncomingIfx.getDestBankId());
			if (incomingIfx.getOriginalDataElements().getBankId() == null || 
					incomingIfx.getOriginalDataElements().getBankId().equals(Util.longValueOf(ConfigUtil.getProperty(ConfigUtil.SHAPARAK_BIN).trim())))
				incomingIfx.getOriginalDataElements().setBankId(refIncomingIfx.getBankId());
			if (incomingIfx.getOriginalDataElements().getOrigDt() == null)
				incomingIfx.getOriginalDataElements().setOrigDt(refIncomingIfx.getOrigDt());
			if (incomingIfx.getOriginalDataElements().getTrnSeqCounter() == null)
				incomingIfx.getOriginalDataElements().setTrnSeqCounter(refIncomingIfx.getSrc_TrnSeqCntr());
			if(incomingIfx.getOriginalDataElements().getNetworkTrnInfo() == null)
				incomingIfx.getOriginalDataElements().setNetworkTrnInfo(refIncomingIfx.getNetworkTrnInfo().toString());

			if (!Util.hasText(incomingIfx.getPINBlock())) {
				if (Util.hasText(incomingIfx.getAppPAN())
						&& incomingIfx.getAppPAN().equals(refIncomingIfx.getAppPAN())) {
					incomingIfx.setPINBlock(refIncomingIfx.getPINBlock());
				}
			}
		}
		
		return incomingIfx;
	}
	
	protected void copyFieldToIncomingIfx(Ifx incomingIfx, Ifx refIfx) {
		incomingIfx.copyFields(refIfx);
	}

	protected void setRRNForRequest(Ifx incomingIfx) throws Exception {
		/*** the following condition is repeated before ***/
//		if(	ShetabFinalMessageType.isRequestMessage(incomingIfx.getIfxType()) || 
//			ShetabFinalMessageType.isPrepareMessage(incomingIfx.getIfxType()) ||
//			ShetabFinalMessageType.isPrepareReversalMessage(incomingIfx.getIfxType())) {

			Transaction transaction = incomingIfx.getTransaction();
			Transaction refTrnx = null;
	
			try {
				refTrnx = setRefereneTrx(incomingIfx, transaction);
			}catch(ReferenceTransactionNotFoundException e1){
				if(IfxType.LAST_PURCHASE_CHARGE_RQ.equals(incomingIfx.getIfxType()))
					logger.warn("Encounter with an Exception in setRRNForRequest "+ e1.getClass().getSimpleName()+": "+ e1.getMessage());
				else
					logger.error("Encounter with an Exception in setRRNForRequest "+ e1.getClass().getSimpleName()+": "+ e1.getMessage());
				throw e1;
			} catch (Exception e) {
				logger.error("Encounter with an Exception in setRRNForRequest "+ e.getClass().getSimpleName()+": "+ e.getMessage());
				throw e;
			}finally{
				setRRN(incomingIfx, transaction, refTrnx);	
				checkValidityOfLastTransactionStatus(incomingIfx);
				
			}
//		}
	}


	protected Transaction setRefereneTrx(Ifx incomingIfx, Transaction transaction)throws Exception {
		transaction.setReferenceTransaction(ifxTypeBindingProcess(incomingIfx));
		return transaction.getReferenceTransaction();
	}

	protected void setRRN(Ifx incomingIfx, Transaction transaction, Transaction refTrnx) {
		String refTranx_RRN;
		if (refTrnx != null) {
			try {
				refTranx_RRN = refTrnx.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getNetworkRefId();
			} catch (Exception e) {
				refTranx_RRN = refTrnx.getIncomingIfx()/*getInputMessage().getIfx()*/.getNetworkRefId();
			}
			transaction.setLifeCycle(refTrnx.getLifeCycle());

			if (refTranx_RRN != null && !refTranx_RRN.equals(incomingIfx.getNetworkRefId()))
				incomingIfx.setNetworkRefId(refTranx_RRN);

		} else {
			if (incomingIfx.getNetworkRefId() == null || "".equals(incomingIfx.getNetworkRefId())) {
				incomingIfx.setNetworkRefId(incomingIfx.getSrc_TrnSeqCntr());
			}
			if (transaction.getLifeCycle() == null) {
				LifeCycle lifeCycle = new LifeCycle();
				GeneralDao.Instance.saveOrUpdate(lifeCycle);
				transaction.setLifeCycle(lifeCycle);
			}
		}
		
	}
	
	public void checkValidityOfLastTransactionStatus(Ifx incomingIfx) {
	}
}
