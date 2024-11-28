package vaulsys.eft.base.terminalTypeProcessor;

import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.authorization.exception.NotPaperReceiptException;
import vaulsys.authorization.exception.NotSubsidiaryAccountException;
import vaulsys.authorization.exception.OpKeyUndefinedException;
import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.billpayment.MCIBillPaymentUtil;
import vaulsys.calendar.DateTime;
import vaulsys.message.ScheduleMessage;
import vaulsys.message.exception.MessageAlreadyReversedException;
import vaulsys.message.exception.ReversalOriginatorNotFoundException;
import vaulsys.migration.MigrationData;
import vaulsys.network.NetworkManager;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.exception.exception.InvalidBusinessDateException;
import vaulsys.protocols.exception.exception.ReferenceTransactionNotFoundException;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.constants.LastStatusIssued;
import vaulsys.protocols.ndc.constants.ReceiptOptionType;
import vaulsys.scheduler.SchedulerService;
import vaulsys.security.SecurityService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.ATMProducer;
import vaulsys.terminal.atm.ATMRequest;
import vaulsys.terminal.atm.device.ReceiptPrinter;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.ClearingInfo;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.util.List;

import org.apache.log4j.Logger;

public class ATMProcessor extends TerminalTypeProcessor {

	transient Logger logger = Logger.getLogger(ATMProcessor.class);


	public static final ATMProcessor Instance = new ATMProcessor();
	private ATMProcessor(){};

	@Override
	public void messageValidation(Ifx ifx, Long messageId/*, ProtocolMessage protocolMsg*/) throws Exception {
		Terminal endPointTerminal = ifx.getEndPointTerminal();
		ATMTerminal atm = null;


		//		atm = (ATMTerminal) ifx.getOriginatorTerminal();
		if (TerminalType.ATM.equals(endPointTerminal.getTerminalType()))
			atm = (ATMTerminal) endPointTerminal;
		else{
			atm = TerminalService.findTerminal(ATMTerminal.class, Long.valueOf(ifx.getTerminalId()));
			ifx.setOriginatorTerminal(atm);
		}

		if (atm == null)
			return;

		if (ifx.getIfxType() == null) {
			if (atm.getId().equals(endPointTerminal.getId())) {
				//				ATMRequest atmRequest = ATMTerminalService.findATMRequest(atm, ifx.getOpkey());
				ATMRequest atmRequest = ProcessContext.get().getATMRequest(atm.getOwnOrParentConfigurationId(), ifx.getOpkey());
				if (atmRequest == null) {
					throw new OpKeyUndefinedException("Not found any request by opkey: " + ifx.getOpkey());
				}
			}
		}

		if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
			//			if (atm.getId().equals(endPointTerminal.getId())) {
			////				if (!Util.hasText(atm.getIP())) {
			////					IoSession session = NetworkManager.getInstance().getResponseOnSameSocketConnectionById(messageId);
			////					String remoteAddress = session.getRemoteAddress().toString();
			////					if (Util.hasText(remoteAddress)) {
			////						atm.setIP(remoteAddress.substring(1, remoteAddress.indexOf(":")));
			////						GeneralDao.Instance.saveOrUpdate(atm);
			////					}
			////				}
			//				IoSession session = NetworkManager.getInstance().getResponseOnSameSocketConnectionById(messageId);
			//				String remoteAddress = session.getRemoteAddress().toString();
			//				String ip = "";
			//				if (Util.hasText(remoteAddress)) {
			//					ip = remoteAddress.substring(1, remoteAddress.indexOf(":"));
			//				}
			//				
			//				if (Util.hasText(ip)) {
			//					String atmIP = atm.getIP();
			//					if (!Util.hasText(atmIP)){
			//						atm.setIP(ip);
			//						GeneralDao.Instance.saveOrUpdate(atm);
			//					} else {
			//						if(!ip.trim().equals(atmIP.trim())) {
			//							throw new AuthorizationException("last atm IP: " + atmIP + ", incomming IP: " + ip);
			//						}
			//					}
			//				}
			//				
			//				if (atm.getKeySet() == null || atm.getKeySet().isEmpty())
			//					ATMTerminalService.addDefaultKeySetForTerminal(atm);
			//				
			//				if(!ATMState.IN_SERIVCE.equals(atm.getState())){
			//					atm.setATMState(ATMState.IN_SERIVCE);
			//					GeneralDao.Instance.saveOrUpdate(atm);
			//				}
			//			}
		}

		/**** comment for Receipt Option ****/
		/*if (ShetabFinalMessageType.isReceiptOrShowRsMessage(ifx.getIfxType()) && ErrorCodes.APPROVED.equals(ifx.getRsCode())) {
			if (Util.hasText(ifx.getBufferC()) && NDCConstants.BAL_TYPE_SHOW.equals(Long.parseLong(ifx.getBufferC())))
				throw new NotPaperReceiptException();
		}*/

		ReceiptPrinter printer = atm.getDevice(ReceiptPrinter.class);
		if (printer != null && printer.getErrorSeverity() != null
				&& ErrorSeverity.FATAL.equals(printer.getErrorSeverity())) {

			if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType()) &&
					!ISOFinalMessageType.isReversalMessage(ifx.getIfxType())) {

				if (ISOFinalMessageType.isPurchaseChargeMessage(ifx.getIfxType())
						//						|| ShetabFinalMessageType.isDepositMessage(ifx.getIfxType())
						) {
					/****************/
					NetworkManager.getInstance().removeResponseOnSameSocketConnectionById(messageId);
					logger.info("removing removeResponseOnSameSocketConnectionById: " + messageId);
					/****************/
					throw new NotPaperReceiptException();
				} else if (ISOFinalMessageType.isTransferMessage(ifx.getIfxType())
						|| ISOFinalMessageType.isBillPaymentMessage(ifx.getIfxType())
						|| TrnType.CHANGEINTERNETPINBLOCK.equals(ifx.getTrnType())
						|| ISOFinalMessageType.isOnlineBillPaymentMessage(ifx.getIfxType())
						|| ISOFinalMessageType.isThirdPartyPurchaseMessage(ifx.getIfxType())
						) {

					if(ISOFinalMessageType.isTransferCheckAccountMessage(ifx.getIfxType()) ||
							ISOFinalMessageType.isTransferToacChechAccountMessage(ifx.getIfxType()))
						;
					else if (ifx.getForceReceipt() != null && !ifx.getForceReceipt())
						;
					else {
						/****************/
						if (ifx.getReceiptOption() == null || ReceiptOptionType.WITH_RECEIPT.equals(ifx.getReceiptOption())){
							NetworkManager.getInstance().removeResponseOnSameSocketConnectionById(messageId);
							logger.info("removing removeResponseOnSameSocketConnectionById: " + messageId);
							/****************/
							throw new NotPaperReceiptException();
						}
					}
				}
			} else if (ISOFinalMessageType.isForceShowIfReceiptErrorRsMessage(ifx.getIfxType()) && ISOResponseCodes.APPROVED.equals(ifx.getRsCode())) {
				if (ifx.getForceReceipt() != null && !ifx.getForceReceipt())
					;
				else {
					/****************/
					if (ReceiptOptionType.WITHOUT_RECEIPT.equals(ifx.getReceiptOption()))  //TASK Task019 : Receipt Option
						;
					else {
						NetworkManager.getInstance().removeResponseOnSameSocketConnectionById(messageId);
						logger.info("removing removeResponseOnSameSocketConnectionById: " + messageId);
						/****************/
						throw new NotPaperReceiptException();
					}
				}
			}
		}

		if (IfxType.GET_ACCOUNT_RS.equals(ifx.getIfxType()) && ISOResponseCodes.APPROVED.equals(ifx.getRsCode())
				&& (ifx.getCardAccountInformation() == null || ifx.getCardAccountInformation().isEmpty())) {

			if (reversalMessageHasBeenRecieved(ifx.getTransaction().getFirstTransaction())) {
				throw new MessageAlreadyReversedException("Message already reversed - message is reversing.", true);
			}

			throw new NotSubsidiaryAccountException();
		}
		if(IfxType.GET_ACCOUNT_RQ.equals(ifx.getSecIfxType()) 
				&& !Util.hasText(ifx.getSubsidiaryAccFrom())&& Util.hasText(atm.getLastIncomingTransaction().getIncomingIfx().getBufferB())){
			throw new MandatoryFieldException("Invalid GeneralBufferB: " + atm.getLastIncomingTransaction().getIncomingIfx().getBufferB());
		}	
		if (SecurityService.isTranslatePIN(ifx)) {
			if (!Util.hasText(ifx.getPINBlock()))
				throw new MandatoryFieldException("ifx: " + ifx.getId() + " must be has PinBlock!");
		}
		//		super.messageValidation(ifx, messageId);
	}


	@Override
	protected Transaction ifxTypeBindingProcess(Ifx ifx) throws Exception {

		Transaction referenceTransaction = null;
		ATMTerminal atm = (ATMTerminal) ifx.getEndPointTerminal();

		if (IfxType.GET_ACCOUNT_RQ.equals(ifx.getIfxType())){
			if (atm.getLastTransaction()!= null 
					&& atm.getLastTransaction().getOutgoingIfx()!= null){

				Ifx lastIfx = atm.getLastTransaction().getOutgoingIfx()/*getOutputMessage().getIfx()*/;

				if (ISOFinalMessageType.isPrepareBillPaymentRqMessage(lastIfx.getIfxType())
						&& ifx.getSecTrnType().equals(TrnType.BILLPAYMENT) ){
					referenceTransaction = atm.getLastTransaction();

				} else if (ISOFinalMessageType.isPrepareTranferCardToAccountMessage(lastIfx.getIfxType())
						&& ifx.getSecTrnType().equals(TrnType.TRANSFER_CARD_TO_ACCOUNT)){
					referenceTransaction = atm.getLastTransaction();

				} else if (ISOFinalMessageType.isTransferCheckAccountMessage(lastIfx.getIfxType())
						&& ifx.getSecTrnType().equals(TrnType.TRANSFER)){
					referenceTransaction = atm.getLastTransaction();

				} else if (ISOFinalMessageType.isTransferToacChechAccountMessage(lastIfx.getIfxType())//TASK Task002 : Transfer Card To Account
						&& ifx.getSecTrnType().equals(TrnType.TRANSFER_CARD_TO_ACCOUNT)){ 
					referenceTransaction = atm.getLastTransaction(); 

				} else if(ISOFinalMessageType.isPrepareOnlineBillPayment(lastIfx.getIfxType())
						&& ifx.getSecTrnType().equals(TrnType.ONLINE_BILLPAYMENT)){
					referenceTransaction = atm.getLastTransaction();
				} else if (IfxType.PREPARE_THIRD_PARTY_PURCHASE.equals(lastIfx.getIfxType())
						&& ifx.getSecTrnType().equals(TrnType.THIRD_PARTY_PAYMENT))
					referenceTransaction = atm.getLastTransaction();
				/***this has been added beacuse of wrong pin and not having customer receipt paper problem:**/
				else if (IfxType.GET_ACCOUNT_RS.equals(lastIfx.getIfxType())
						&& ISOResponseCodes.HOST_LINK_DOWN.equals(lastIfx.getRsCode())){
					if(ifx.getSecTrnType().equals(TrnType.TRANSFER)
							|| ifx.getSecTrnType().equals(TrnType.TRANSFER_CARD_TO_ACCOUNT)
							|| ifx.getSecTrnType().equals(TrnType.BILLPAYMENT)
							|| ifx.getSecTrnType().equals(TrnType.ONLINE_BILLPAYMENT)
							|| ifx.getSecTrnType().equals(TrnType.PREPARE_THIRD_PARTY_PAYMENT)
							|| ifx.getSecTrnType().equals(TrnType.WITHDRAWAL_CUR)	//Mirkamali(Task179)
							)
						referenceTransaction = atm.getLastTransaction().getReferenceTransaction();
					else
						referenceTransaction = atm.getLastTransaction();
				} else if(IfxType.PREPARE_WITHDRAWAL.equals(lastIfx.getIfxType())	//Mirkamali(Task179): Currecny ATM
						&& ifx.getSecTrnType().equals(TrnType.WITHDRAWAL_CUR))
					referenceTransaction = atm.getLastTransaction();
			}
			//		}else if(IfxType.ONLINE_BILLPAYMENT_REV_REPEAT_RQ.equals(ifx.getIfxType())){
			////			Ifx lastIfx= atm.getLastTransaction().getOutputMessage().getIfx();
			//			Ifx refIncomingIfx = getPartialReversalOriginatorTransaction(ifx);
			//			if(refIncomingIfx.getOnliBillPaymentData()!=null && refIncomingIfx.getOnliBillPaymentData().getOnlineBillPayment() != null)
			//				if(!OnlineBillPaymentStatus.NOT_PAID.equals(refIncomingIfx.getOnliBillPaymentData().getOnlineBillPayment().getPaymentStatus())){
			//					refIncomingIfx.getOnliBillPaymentData().getOnlineBillPayment().setPaymentStatus(OnlineBillPaymentStatus.NOT_PAID);
			//				}
		}
		/*else if (TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(ifx.getTrnType())){
			referenceTransaction = atm.getLastTransaction();
			if (referenceTransaction == null || !IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT.equals(referenceTransaction.getIncomingIfx()getInputMessage().getIfx().getIfxType())){
				throw new ReferenceTransactionNotFoundException(
						"No Reference Transaction was found for the "+TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT+".(trx: "+ ifx.getTransaction().getId() + ")");
			}

			ifx.setAppPAN(referenceTransaction.getIncomingIfx()getInputMessage().getIfx().getAppPAN()+ifx.getAppPAN());
			copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()getOutputMessage().getIfx());

		}*/ 
		else if (IfxType.PARTIAL_DISPENSE_RQ.equals(ifx.getIfxType())) {
			//			ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, Util.longValueOf(ifx.getTerminalId()));
			referenceTransaction = atm.getLastTransaction();
			if (referenceTransaction == null)
				throw new ReferenceTransactionNotFoundException(
						"No Reference Transaction was found for the Partial dispense request.(trx: "+ ifx.getTransaction().getId() + ")");
			if (referenceTransaction.getOutgoingIfx() != null 
					&& TrnType.WITHDRAWAL.equals(referenceTransaction.getOutgoingIfx().getTrnType()/*getOutputMessage().getIfx().getTrnType()*/)
					&& referenceTransaction.getOutgoingIfx().getTotalStep()/*getOutputMessage().getIfx().getTotalStep()*/>1
					&& referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getAppPAN().equals(ifx.getAppPAN())
					&& referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getAuth_Amt().equals(ifx.getAuth_Amt())
					) {
				copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/);
				ifx.getAtmSpecificData().setNextOpkey(null);
				ifx.getAtmSpecificData().setSecIfxType(null);
				ifx.getAtmSpecificData().setSecTrnType(null);
			}

			ifx.getTransaction().setReferenceTransaction(referenceTransaction);
			ifx.getTransaction().setFirstTransaction(ifx.getTransaction());

		} else if (IfxType.PARTIAL_DISPENSE_REV_REPEAT_RQ.equals(ifx.getIfxType()) && ifx.getOriginalDataElements() != null ){

			Ifx refIncomingIfx = getPartialReversalOriginatorTransaction(ifx);
			if (refIncomingIfx != null)
				referenceTransaction = refIncomingIfx.getTransaction();

		} /*else if (ShetabFinalMessageType.isReversalRqMessage(ifx.getIfxType()) && ifx.getOriginalDataElements() != null) {

			Ifx refIncomingIfx = getReversalOriginatorTransaction(ifx);
			if (refIncomingIfx != null){
				if((ifx.getSecTrnType() == null && refIncomingIfx.getSecTrnType() == null) ||  
						(ifx.getSecTrnType() != null && ifx.getSecTrnType().equals(refIncomingIfx.getSecTrnType())))
					referenceTransaction = refIncomingIfx.getTransaction();
			}

		}*/else if (IfxType.TRANSFER_RQ.equals(ifx.getIfxType()) || IfxType.TRANSFER_TO_ACCOUNT_RQ.equals(ifx.getIfxType())) {

			Transaction lastTransaction = atm.getLastTransaction();

			if (lastTransaction != null && lastTransaction.getFirstTransaction() != null){

				if (ISOFinalMessageType.isTransferCheckAccountMessage(lastTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType())) {
					referenceTransaction = lastTransaction;					

				} else {
					Transaction lastCheckAccount = lastTransaction.getFirstTransaction().getReferenceTransaction();
					if (lastCheckAccount!= null
							&& !ISOResponseCodes.APPROVED.equals(lastTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getRsCode())
							&& ISOFinalMessageType.isTransferCheckAccountMessage(lastCheckAccount.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType())) {

						MigrationData migData = lastCheckAccount.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getMigrationData();
						MigrationData secMigData = lastCheckAccount.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getMigrationSecondData();

						boolean secAppPan = false;
						boolean appPan = false;

						if (ifx.getSecondAppPan().equals(lastCheckAccount.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getAppPAN())) {
							secAppPan = true;
						}

						if (ifx.getAppPAN().equals(lastCheckAccount.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getSecondAppPan())) {
							appPan = true;
						}

						if (appPan && secAppPan) {
							referenceTransaction = lastCheckAccount;

						} else if (!appPan && secMigData != null) {
							if (ifx.getAppPAN().equals(secMigData.getNeginAppPan())) {
								appPan = true;
							}	
						}

						if (appPan && secAppPan) {
							referenceTransaction = lastCheckAccount;

						} else if (!secAppPan && migData != null) {
							if (ifx.getSecondAppPan().equals(migData.getNeginAppPan())) {
								secAppPan = true;
							}	
						}

						if (appPan && secAppPan) {
							referenceTransaction = lastCheckAccount;
						}

					}else if(IfxType.GET_ACCOUNT_RS.equals(lastTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType())
							&& lastTransaction.getReferenceTransaction()!= null
							&& IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(lastTransaction.getReferenceTransaction().getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType())
							&& ifx.getAppPAN().equals(lastTransaction.getReferenceTransaction().getOutgoingIfx()/*getOutputMessage().getIfx()*/.getSecondAppPan())){
						referenceTransaction = lastTransaction.getReferenceTransaction();
					}
				}
			}
			if (referenceTransaction == null)
				throw new ReferenceTransactionNotFoundException(
						"No Check Account Transaction was found for the transfer request.(trx: " + ifx.getTransaction().getId()+ ")");

			copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/);

			/*if (TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getTrnType()))
				ifx.setSecondAppPan(referenceTransaction.getOutgoingIfx().getAppPAN());*/

		}else if (IfxType.BILL_PMT_RQ.equals(ifx.getIfxType())) {
			referenceTransaction = atm.getLastTransaction();


			/*** Bug: bind hesab farei ba moshkele receipt: Start ***/
			if (IfxType.BILL_PMT_RQ.equals(atm.getLastTransaction().getIncomingIfx().getIfxType()) &&  
					IfxType.GET_ACCOUNT_RQ.equals(atm.getLastTransaction().getIncomingIfx().getSecIfxType()) 
					&& atm.getLastTransaction().getIncomingIfx().getStatus() != null
					&& atm.getLastTransaction().getIncomingIfx().getStatus().getStatusDesc().startsWith("NotPaperReceiptException")){
				String shenaseGhabz = atm.getLastTransaction().getIncomingIfx().getBillID();  	

				String billIdLong = "";
				try {
					billIdLong = String.valueOf(Long.parseLong(shenaseGhabz));
				} catch(Exception e) {
					logger.warn("BillID is null!");
				}
				try {
					ifx.setBillID(billIdLong);
					ifx.setBillCompanyCode(BillPaymentUtil.extractCompanyCode(billIdLong));
					ifx.setThirdPartyTerminalId(BillPaymentUtil.getThirdPartyTerminalId(billIdLong));
					ifx.setBillOrgType(BillPaymentUtil.extractBillOrgType(billIdLong));
				} catch(Exception e) {
					logger.warn("Exception in setting Bill Data...");
				}            
			}
			/*** Bug: bind hesab farei ba moshkele receipt: End ***/

			if (referenceTransaction == null || !IfxType.PREPARE_BILL_PMT.equals(referenceTransaction.getIncomingIfx().getIfxType())){
				if (referenceTransaction!=null){
					if (IfxType.BILL_PMT_RS.equals(referenceTransaction.getOutgoingIfx().getIfxType()) 
							&& !ISOResponseCodes.APPROVED.equals(referenceTransaction.getOutgoingIfx().getRsCode())){
						referenceTransaction = referenceTransaction.getReferenceTransaction();

						if(referenceTransaction != null && !IfxType.PREPARE_BILL_PMT.equals(referenceTransaction.getIncomingIfx().getIfxType())){
							if(! compareBillInfo(ifx, referenceTransaction.getOutgoingIfx()))
								throw new ReferenceTransactionNotFoundException(
										"bill info of reference ifx is different from current ifx! (trx id: "+ ifx.getTransaction().getId() + ")");
						}
					}else if (IfxType.GET_ACCOUNT_RS.equals(referenceTransaction.getOutgoingIfx().getIfxType())){
						referenceTransaction = referenceTransaction.getReferenceTransaction();
						if (referenceTransaction == null || !IfxType.PREPARE_BILL_PMT.equals(referenceTransaction.getIncomingIfx().getIfxType())){
							referenceTransaction = null;
						}
					}else
						referenceTransaction = null;
				}

				if (referenceTransaction == null)
					throw new ReferenceTransactionNotFoundException(
							"No Prepare BillPaymenet Transaction was found for the billPaymenet request.(trx: "+ ifx.getTransaction().getId() + ")");
			}

			if (referenceTransaction.getOutgoingIfx() != null) {
				copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/);
			}

		} else if (IfxType.ONLINE_BILLPAYMENT_RQ.equals(ifx.getIfxType())) {
			referenceTransaction = atm.getLastTransaction();

			if (referenceTransaction == null || !IfxType.PREPARE_ONLINE_BILLPAYMENT.equals(referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType())){
				if (referenceTransaction!=null){
					if (IfxType.ONLINE_BILLPAYMENT_RS.equals(referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType()) 
							&& !ISOResponseCodes.APPROVED.equals(referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getRsCode())){
						referenceTransaction = referenceTransaction.getReferenceTransaction();

						if (referenceTransaction == null 
								|| !IfxType.PREPARE_ONLINE_BILLPAYMENT.equals(referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType())
								|| !ifx.getOnlineBillPaymentRefNum().equals(referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getOnlineBillPaymentRefNum())
								|| !ifx.getAppPAN().equals(referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getAppPAN())){
							referenceTransaction = null;
						}
					}else if (IfxType.GET_ACCOUNT_RS.equals(referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType())){
						referenceTransaction = referenceTransaction.getReferenceTransaction();
						if (referenceTransaction == null || !IfxType.PREPARE_ONLINE_BILLPAYMENT.equals(referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType())){
							referenceTransaction = null;
						}
					}else
						referenceTransaction = null;
				}

				if (referenceTransaction == null)
					throw new ReferenceTransactionNotFoundException(
							"No Prepare Online BillPaymenet Transaction was found for the online billPaymenet request.(trx: "+ ifx.getTransaction().getId() + ")");
			}

			if (referenceTransaction.getOutgoingIfx() != null) {
				copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/);
			}

		} else if (IfxType.THIRD_PARTY_PURCHASE_RQ.equals(ifx.getIfxType())){
			referenceTransaction = atm.getLastTransaction();

			if (referenceTransaction == null || !IfxType.PREPARE_THIRD_PARTY_PURCHASE.equals(referenceTransaction.getIncomingIfx().getIfxType())){
				if (referenceTransaction!=null){
					if (IfxType.THIRD_PARTY_PURCHASE_RS.equals(referenceTransaction.getOutgoingIfx().getIfxType()) 
							&& !ISOResponseCodes.APPROVED.equals(referenceTransaction.getOutgoingIfx().getRsCode())){
						referenceTransaction = referenceTransaction.getReferenceTransaction();

						if (referenceTransaction == null 
								|| !IfxType.PREPARE_THIRD_PARTY_PURCHASE.equals(referenceTransaction.getIncomingIfx().getIfxType())
								//							|| !ifx.getBillID().equals(referenceTransaction.getOutgoingIfx().getBillID())
								|| !ifx.getAppPAN().equals(referenceTransaction.getOutgoingIfx().getAppPAN())){
							referenceTransaction = null;
						}
					}else if (IfxType.GET_ACCOUNT_RS.equals(referenceTransaction.getOutgoingIfx().getIfxType())){
						referenceTransaction = referenceTransaction.getReferenceTransaction();
						if (referenceTransaction == null || !IfxType.PREPARE_THIRD_PARTY_PURCHASE.equals(referenceTransaction.getIncomingIfx().getIfxType())){
							referenceTransaction = null;
						}
					}else
						referenceTransaction = null;
				}

				if (referenceTransaction == null)
					throw new ReferenceTransactionNotFoundException(
							"No Prepare BillPaymenet Transaction was found for the billPaymenet request.(trx: "+ ifx.getTransaction().getId() + ")");
			}

			if (referenceTransaction.getOutgoingIfx() != null) {
				copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx());
			}

		}

		//Mirkamali(Task179): Currency ATM
		else if(IfxType.WITHDRAWAL_CUR_RQ.equals(ifx.getIfxType())) {
			referenceTransaction = atm.getLastTransaction();
			if (referenceTransaction == null || !IfxType.PREPARE_WITHDRAWAL.equals(referenceTransaction.getIncomingIfx().getIfxType())){
				if (referenceTransaction != null){
					if (IfxType.WITHDRAWAL_CUR_RS.equals(referenceTransaction.getOutgoingIfx().getIfxType()) 
							&& !ISOResponseCodes.APPROVED.equals(referenceTransaction.getOutgoingIfx().getRsCode())){
						referenceTransaction = referenceTransaction.getReferenceTransaction();

						if (referenceTransaction == null 
								|| !IfxType.PREPARE_WITHDRAWAL.equals(referenceTransaction.getIncomingIfx().getIfxType())
								|| !ifx.getAppPAN().equals(referenceTransaction.getOutgoingIfx().getAppPAN())){
							referenceTransaction = null;
						}
					}else if (IfxType.GET_ACCOUNT_RS.equals(referenceTransaction.getOutgoingIfx().getIfxType())){
						referenceTransaction = referenceTransaction.getReferenceTransaction();
						if (referenceTransaction == null || !IfxType.PREPARE_WITHDRAWAL.equals(referenceTransaction.getIncomingIfx().getIfxType())){
							referenceTransaction = null;
						}
					}else
						referenceTransaction = null;
				}

				if (referenceTransaction == null)
					throw new ReferenceTransactionNotFoundException(
							"No Prepare Withdrawal Transaction was found for the Withdrawal request.(trx: "+ ifx.getTransaction().getId() + ")");
			}

			if (referenceTransaction.getOutgoingIfx() != null) {
				copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx());
				ifx.setAuth_Amt(referenceTransaction.getOutgoingIfx().getAuth_Amt());
			}
		}
		/*** TransferToAccount:START ***/
		else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifx.getIfxType()) || 
				IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ.equals(ifx.getIfxType())) {

			Transaction lastTransaction = atm.getLastTransaction();

			if (lastTransaction != null && lastTransaction.getFirstTransaction() != null){
				if (ISOFinalMessageType.isTransferToacChechAccountMessage(lastTransaction.getOutgoingIfx().getIfxType())) {
					referenceTransaction = lastTransaction;					

				} else {//AldTODO Task002 : Aya in Ghesmat lazem ast ? (az Mrs.Pakravan Porside Shavad)
					Transaction lastCheckAccount = lastTransaction.getFirstTransaction().getReferenceTransaction();
					if (lastCheckAccount!= null
							&& !ISOResponseCodes.APPROVED.equals(lastTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getRsCode())
							&& ISOFinalMessageType.isTransferToacChechAccountMessage(lastCheckAccount.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType())) {
						referenceTransaction = lastCheckAccount;	//TASK Task002 : Transfer Card To Account 
						//AldTODO Task002 : Felan Comment Shod ta bad ba Mrs.Pakravan Sohbat konim						
						//						
						//						MigrationData migData = lastCheckAccount.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getMigrationData();
						//						MigrationData secMigData = lastCheckAccount.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getMigrationSecondData();
						//						
						//						boolean secAppPan = false;
						//						boolean appPan = false;
						//						
						//						if (ifx.getSecondAppPan().equals(lastCheckAccount.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getAppPAN())) {
						//							secAppPan = true;
						//						}
						//						
						//						if (ifx.getAppPAN().equals(lastCheckAccount.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getSecondAppPan())) {
						//							appPan = true;
						//						}
						//						
						//						if (appPan && secAppPan) {
						//							referenceTransaction = lastCheckAccount;
						//							
						//						} else if (!appPan && secMigData != null) {
						//							if (ifx.getAppPAN().equals(secMigData.getNeginAppPan())) {
						//								appPan = true;
						//							}	
						//						}
						//						
						//						if (appPan && secAppPan) {
						//							referenceTransaction = lastCheckAccount;
						//							
						//						} else if (!secAppPan && migData != null) {
						//							if (ifx.getSecondAppPan().equals(migData.getNeginAppPan())) {
						//								secAppPan = true;
						//							}	
						//						}
						//						
						//						if (appPan && secAppPan) {
						//							referenceTransaction = lastCheckAccount;
						//						}
					}else if(IfxType.GET_ACCOUNT_RS.equals(lastTransaction.getOutgoingIfx().getIfxType())
							&& lastTransaction.getReferenceTransaction()!= null
							&& IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(lastTransaction.getReferenceTransaction().getOutgoingIfx().getIfxType())
							&& ifx.getAppPAN().equals(lastTransaction.getReferenceTransaction().getOutgoingIfx().getSecondAppPan())){
						referenceTransaction = lastTransaction.getReferenceTransaction();
					}
				}
			}
			if (referenceTransaction == null)
				throw new ReferenceTransactionNotFoundException(
						"No Check Account Transaction was found for the transfer request.(trx: " + ifx.getTransaction().getId()+ ")");

			copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx());
			if (TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getTrnType()))
				ifx.setSecondAppPan(referenceTransaction.getOutgoingIfx().getAppPAN());

		}		
		//TASK Task002 : Transfer Card To Account 
		else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType()) ) {

			Transaction lastTransaction = atm.getLastTransaction();

			if (lastTransaction != null && lastTransaction.getFirstTransaction() != null){

				if (ISOFinalMessageType.isPrepareTranferCardToAccountMessage(lastTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType())) {
					referenceTransaction = lastTransaction;
					copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/);
					ifx.setAppPAN(referenceTransaction.getOutgoingIfx().getAppPAN()+"."+ifx.getBufferB());
					ifx.setActualAppPAN(referenceTransaction.getOutgoingIfx().getActualAppPAN()+"."+ifx.getBufferB());						
					ifx.setDestBankId(ifx.getBankId());
				} 
			}
			if (referenceTransaction == null)
				throw new ReferenceTransactionNotFoundException(
						"No Check Account Transaction was found for the transfer request.(trx: " + ifx.getTransaction().getId()+ ")");


			//TASK Task002 : Transfer Card To Account //AldTODO Please Check shavad
			//			if (TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getTrnType()))
			//				ifx.setSecondAppPan(referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getAppPAN());

		} 

		//TASK Task002 : Transfer Card To Account //AldComment TransferToAccount For 4 Step
		else if (IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP.equals(ifx.getIfxType()) || 
				IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_REV_REPEAT.equals(ifx.getIfxType())) { //TODO AD TransferToAccount Aya Niaz hast baraye  IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT ham check shavad

			ifx.setAppPAN(ifx.getBufferB()!= null ? ifx.getBufferB() : " ");//AldComment Task002 : Ager BufferB khali bood " " ra bazarim chon dar gheire insorat shomare kart ro miferestad
			ifx.setActualAppPAN(ifx.getBufferB()!= null ? ifx.getBufferB() : " ");//AldComment Task002 : Ager BufferB khali bood " " ra bazarim chon dar gheire insorat shomare kart ro miferestad 
		} 
		//TASK Task002 : Transfer Card To Account 
		else if (IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT.equals(ifx.getIfxType()) || 
				IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT.equals(ifx.getIfxType())) {
			//AldComment Task002 : in if niaz hast chon agar bufferB null bashad dar hengame copyfields bufferB last Transaction ra jaygozin mikonad ke dorost nist
			if (ifx.getBufferB() == null) 
				ifx.setBufferB(" ");

			Transaction lastTransaction = atm.getLastTransaction();

			if (lastTransaction != null && lastTransaction.getFirstTransaction() != null){					
				if (ISOFinalMessageType.isPrepareTranferCardToAccountMessage(lastTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType())) {
					referenceTransaction = lastTransaction;		
					copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/);
					ifx.setAppPAN(referenceTransaction.getOutgoingIfx().getAppPAN()+"."+ifx.getBufferB());
					ifx.setActualAppPAN(referenceTransaction.getOutgoingIfx().getActualAppPAN()+"."+ifx.getBufferB());
				} 
			}
			if (referenceTransaction == null)
				throw new ReferenceTransactionNotFoundException(
						"No Check Account Transaction was found for the transfer request.(trx: " + ifx.getTransaction().getId()+ ")");
		}
		//AldTODO Task002 : move here
		else if (ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType()) && ifx.getOriginalDataElements() != null) {

			Ifx refIncomingIfx = getReversalOriginatorTransaction(ifx); 
			if (refIncomingIfx != null){  
				if((ifx.getSecTrnType() == null && refIncomingIfx.getSecTrnType() == null) 
						||   //TASK Task002 : Transfer to account //AldComment Task002 : Add in 92.03.27
						(
								(ifx.getSecTrnType() != null && ifx.getSecTrnType().equals(refIncomingIfx.getSecTrnType()))
								|| 
								(ISOFinalMessageType.isContinueIfReceiptErrorRevMessage(ifx.getIfxType())
										&& 
										ifx.getSecTrnType() != null 
										&& 
										refIncomingIfx.getSecTrnType() == null )
								)
						)
					referenceTransaction = refIncomingIfx.getTransaction();

				//Original				
				//				if((ifx.getSecTrnType() == null && refIncomingIfx.getSecTrnType() == null) ||  
				//						(ifx.getSecTrnType() != null && ifx.getSecTrnType().equals(refIncomingIfx.getSecTrnType())))
				//					referenceTransaction = refIncomingIfx.getTransaction();

			}

		}

		/*** TransferToAccount:END ***/

		else if (atm.getLastTransaction()!= null 
				&& atm.getLastTransaction().getOutgoingIfx()!= null
				&& IfxType.GET_ACCOUNT_RS.equals(atm.getLastTransaction().getOutgoingIfx().getIfxType())
				&& atm.getLastTransaction().getOutgoingIfx().getAppPAN().equals(ifx.getAppPAN())
				//				&& atm.getLastTransaction().getOutputMessage().getIfx().getReceivedDt().getDayDate().equals(ifx.getReceivedDt().getDayDate())
				&& (Util.hasText(ifx.getSubsidiaryAccFrom()) || ifx.getAccTypeFrom().equals(AccType.SUBSIDIARY_ACCOUNT))
				) {

			/*** less 5 min (hhmmss, 1 sa@ & 1 min: 010100)***/
			DateTime lastReceivedDt = atm.getLastTransaction().getOutgoingIfx().getReceivedDt();
			int compareDay = ifx.getReceivedDt().getDayDate().compareTo(lastReceivedDt.getDayDate());
			if (compareDay == 0) {
				if (ifx.getReceivedDt().compareTo(lastReceivedDt) < 500) { 
					referenceTransaction = atm.getLastTransaction();

				}
			} else if (compareDay == 1 && 
					(lastReceivedDt.getDayTime().compareTo(ifx.getReceivedDt().getDayTime()) / 10000) == 23 && 
					(lastReceivedDt.getDayTime().compareTo(ifx.getReceivedDt().getDayTime()) % 10000 > 5500)) {
				referenceTransaction = atm.getLastTransaction();

			}

			if (referenceTransaction != null && referenceTransaction.getOutgoingIfx() != null) {
				copyFieldToIncomingIfx(ifx, referenceTransaction.getOutgoingIfx());
			}
		}

		return referenceTransaction;
	}

	private boolean compareBillInfo(Ifx ifx, Ifx referenceIfx) {
		if (!MCIBillPaymentUtil.isBillPaymentWithMobileNumber(ifx))
			if (!ifx.getBillID().equals(referenceIfx.getBillID()))
				return false;
		if (!ifx.getAppPAN().equals(referenceIfx.getAppPAN()))
			return false;
		return true;
	}

	@Override
	protected Transaction setRefereneTrx(Ifx incomingIfx, Transaction transaction)throws Exception {
		Transaction refTrnx = ifxTypeBindingProcess(incomingIfx);
		transaction.setReferenceTransaction(refTrnx);

		/*** agar trx ghabli getAcc bashad, dar methode setRefTrx lifeCycle in trx ba getAcc barabar mishavad, 
		 * dar marhaleye badi ref ra null mikonim ***/
		if (refTrnx!= null && refTrnx.getOutgoingIfx() != null && refTrnx.getOutgoingIfx()/*getOutputMessage().getIfx()*/!= null && IfxType.GET_ACCOUNT_RS.equals(refTrnx.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType())){
			transaction.setReferenceTransaction(null);
			refTrnx = null;
		}
		return refTrnx;
	}



	protected void setRRN(Ifx incomingIfx, Transaction transaction, Transaction refTrnx) {
		if (!Util.hasText(incomingIfx.getNetworkRefId())) {
			incomingIfx.setNetworkRefId(incomingIfx.getSrc_TrnSeqCntr());
		}
		if (transaction.getLifeCycle() == null) {
			LifeCycle lifeCycle = new LifeCycle();
			GeneralDao.Instance.saveOrUpdate(lifeCycle);
			transaction.setLifeCycle(lifeCycle);
		}
	}


	@Override
	protected Ifx getReversalOriginatorTransaction(Ifx incomingIfx)throws Exception {
		Ifx refIncomingIfx = null;
		ATMTerminal atm = (ATMTerminal) incomingIfx.getEndPointTerminal();

		Transaction lastTransaction = atm.getLastTransaction();
		DateTime lastReceivedDate = null;

		if (lastTransaction != null && lastTransaction.getFirstTransaction() != null) {			
			Transaction refTrnx = lastTransaction.getFirstTransaction();									
			if (isReferenceTransaction(lastTransaction, refTrnx, incomingIfx))
				refIncomingIfx = refTrnx.getIncomingIfx();

			else if (IfxType.TRANSFER_REV_REPEAT_RQ.equals(incomingIfx.getIfxType())){
				refTrnx = lastTransaction.getReferenceTransaction();									
				if (refTrnx!= null && isReferenceTransaction(lastTransaction, refTrnx, incomingIfx))
					refIncomingIfx = refTrnx.getIncomingIfx();

			} else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ.equals(incomingIfx.getIfxType())) { 
				refTrnx = lastTransaction.getReferenceTransaction();									
				if (refTrnx!= null && isReferenceTransaction(lastTransaction, refTrnx, incomingIfx))
					refIncomingIfx = refTrnx.getIncomingIfx();
			}

			if (lastTransaction.getInputMessage() instanceof ScheduleMessage) {
				lastReceivedDate = lastTransaction.getReferenceTransaction().getIncomingIfx().getReceivedDt();

			} else {
				lastReceivedDate = lastTransaction.getIncomingIfx().getReceivedDt();
			}
		}

		if (refIncomingIfx == null)
			refIncomingIfx =  TransactionService.getReversalOriginatorTransactionForAcqTerminal(incomingIfx, lastReceivedDate);

		incomingIfx = checkOriginalData(incomingIfx, refIncomingIfx);

		//TASK Task067 : ATM Withdrawal Bug 
		if (refIncomingIfx != null && IfxType.WITHDRAWAL_REV_REPEAT_RQ.equals(incomingIfx.getIfxType()) && !incomingIfx.getLast_TrnSeqCntr().equals(refIncomingIfx.getLast_TrnSeqCntr())){
			incomingIfx.getTransaction().setLifeCycle(null);
			return null;
		}		

		return refIncomingIfx;
	}

	private Ifx getPartialReversalOriginatorTransaction(Ifx incomingIfx) throws Exception {
		Transaction transaction = incomingIfx.getTransaction();
		Ifx refIncomingIfx = null;

		ATMTerminal atm = (ATMTerminal) incomingIfx.getEndPointTerminal();

		Transaction lastTransaction = atm.getLastTransaction();
		DateTime lastReceivedDate = null;

		if (lastTransaction != null /*&& lastTransaction.getFirstTransaction() != null*/) {

			if (isReferenceTransaction(lastTransaction, lastTransaction, incomingIfx)){
				refIncomingIfx = lastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/;
			}

			if (lastTransaction.getInputMessage() instanceof ScheduleMessage) {
				lastReceivedDate = lastTransaction.getReferenceTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/.getReceivedDt();

			} else {
				lastReceivedDate = lastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getReceivedDt();
			}
		}
		if (refIncomingIfx == null)
			refIncomingIfx = TransactionService.getReversalOriginatorTransactionForAcqTerminal(incomingIfx, lastReceivedDate);


		incomingIfx = checkOriginalData(incomingIfx, refIncomingIfx);


		if (refIncomingIfx!= null && refIncomingIfx.getTransaction() != null) {

			Transaction referenceTransaction = refIncomingIfx.getTransaction();

			if (referenceTransaction.getReferenceTransaction() != null
					&& referenceTransaction.getReferenceTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/!= null	
					&& ISOFinalMessageType.isWithdrawalOrPartialMessage(referenceTransaction.getReferenceTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType())) {
				transaction.setFirstTransaction(referenceTransaction);
				transaction.setReferenceTransaction(referenceTransaction.getReferenceTransaction());
				referenceTransaction = transaction.getReferenceTransaction();
			} else{
				transaction.setReferenceTransaction(referenceTransaction.getFirstTransaction());
				referenceTransaction = transaction.getReferenceTransaction();
			}

			//			if (transaction.getFirstTransaction()== null || transaction.getFirstTransaction().getId() == transaction.getId())
			//				transaction.setFirstTransaction(TransactionService.findLastPartialRequest(referenceTransaction.getLifeCycle()));
			refIncomingIfx = referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/;


			try {
				if (!Util.hasText(incomingIfx.getNew_AmtAcqCur()) 
						&& refIncomingIfx != null && !refIncomingIfx.getAuth_Amt().equals(refIncomingIfx.getReal_Amt())) {
					logger.debug("A time-out request for partial_dispense has been received! newAmount = realAmount: "
							+ refIncomingIfx.getReal_Amt());
					incomingIfx.setNew_AmtAcqCur(refIncomingIfx.getReal_Amt() + "");
					incomingIfx.setNew_AmtIssCur(incomingIfx.getNew_AmtAcqCur());
				}


				if (transaction.getFirstTransaction().getOutgoingIfx()!= null) {
					Ifx firstOutIfx = transaction.getFirstTransaction().getOutgoingIfx()/*getOutputMessage().getIfx()*/;
					if (!Util.longValueOf(incomingIfx.getLast_TrnSeqCntr())
							.equals(
									Util.longValueOf(firstOutIfx
											.getSrc_TrnSeqCntr()))) {
						logger
						.debug("A time-out request for partial_dispense has been received! newAmount = actualAmount: "
								+ firstOutIfx.getActualDispenseAmt());
						incomingIfx.setNew_AmtAcqCur(firstOutIfx
								.getActualDispenseAmt()
								+ "");
						incomingIfx.setNew_AmtIssCur(incomingIfx
								.getNew_AmtAcqCur());
					}
					if (Util.longValueOf(incomingIfx.getLast_TrnSeqCntr())
							.equals(
									Util.longValueOf(firstOutIfx
											.getSrc_TrnSeqCntr()))) {
						logger
						.debug("A time-out request for partial_dispense has been received! newAmount = actualAmount + currentAmount: "
								+ ((firstOutIfx.getActualDispenseAmt() == null ? 0
										: firstOutIfx
										.getActualDispenseAmt()) + firstOutIfx
										.getCurrentDispenseAmt()));
						incomingIfx
						.setNew_AmtAcqCur(((firstOutIfx
								.getActualDispenseAmt() == null ? 0
										: firstOutIfx.getActualDispenseAmt()) + firstOutIfx
										.getCurrentDispenseAmt())
										+ "");
						incomingIfx.setNew_AmtIssCur(incomingIfx
								.getNew_AmtAcqCur());
					}
				}
			} catch (Exception e) {
				logger.error("Encounter with an Exception in PARTIAL_DISPENSE_REV_REPEAT_RQ: "
						+ e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}

		return refIncomingIfx;
	}

	private Boolean isReferenceTransaction(Transaction lastTransaction, Transaction referenceTransaction, Ifx ifx) throws Exception {
		String terminalId = ifx.getTerminalId();
		String bankId = ifx.getBankId();
		String orgIdNum = ifx.getOrgIdNum();
		String appPAN = ifx.getAppPAN();
		TrnType trnType = ifx.getTrnType();
		Long amount = ifx.getAuth_Amt();
		DateTime refOrigDt = ifx.getSafeOriginalDataElements().getOrigDt();
		String networkTrnInfo = ifx.getSafeOriginalDataElements().getNetworkTrnInfo();
		String refTrnSeqCounter = ifx.getSafeOriginalDataElements().getTrnSeqCounter();
		Ifx refIncomingIfx = null;
		String secAppPan = ifx.getSecondAppPan();

		refIncomingIfx = referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/;
		Ifx myLastIfx = lastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/;

		if (refIncomingIfx != null
				&& !ISOFinalMessageType.isReversalMessage(refIncomingIfx.getIfxType())
				&& bankId.equals(refIncomingIfx.getBankId()) 
				&& terminalId.equals(refIncomingIfx.getTerminalId())
				&& orgIdNum.equals(refIncomingIfx.getOrgIdNum())

				//				&& appPAN.equals(refIncomingIfx.getAppPAN())
				&& (
						(!TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(trnType) && appPAN.equals(refIncomingIfx.getAppPAN()))
						|| 
						(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(trnType) && secAppPan.equals(refIncomingIfx.getSecondAppPan()))
						)				
						&& trnType.equals(refIncomingIfx.getTrnType())
						//Mirkamali(Task179): Currency ATM
						&& (amount == null || amount.equals(refIncomingIfx.getAuth_Amt())|| TrnType.BILLPAYMENT.equals(trnType) || (TrnType.WITHDRAWAL_CUR.equals(trnType) && ifx.getReal_Amt().equals(refIncomingIfx.getReal_Amt())))
						// ATM doesn't send refOrigDt, networkTrnInfo and
						// refTrnSeqCounter in reverse messages
						&& (refOrigDt == null || refOrigDt.equals(refIncomingIfx.getOrigDt()))
						&& (!Util.hasText(networkTrnInfo) || networkTrnInfo.equals(refIncomingIfx.getNetworkRefId()))
						&& (!Util.hasText(refTrnSeqCounter) || refTrnSeqCounter.equals(refIncomingIfx.getSrc_TrnSeqCntr()))) {

			String myLastSeqCntr = myLastIfx.getSrc_TrnSeqCntr();
			String terminalLastSeqCntr = ifx.getLast_TrnSeqCntr();

			if (!Util.hasText(myLastSeqCntr) 
					|| !Util.hasText(terminalLastSeqCntr)
					|| Integer.parseInt(myLastSeqCntr) != Integer.parseInt(terminalLastSeqCntr)
					|| IfxType.PARTIAL_DISPENSE_REV_REPEAT_RQ.equals(ifx.getIfxType())
					|| !LastStatusIssued.GOOD_TERMINATION_SENT.equals(ifx.getLastTrxStatusIssue())
					) {
				//TASK Task141 : NCR
				if (LastStatusIssued.NONE_SENT.equals(ifx.getLastTrxStatusIssue()) && !ATMProducer.NCR.equals(((ATMTerminal)ifx.getEndPointTerminal()).getProducer())) {
					//					if (ifx.getEndPointTerminal() != null && ATMProducer.GRG.equals(((ATMTerminal)ifx.getEndPointTerminal()).getProducer())) {
					logger.fatal("F with NONE_SENT from GRG, ignore it! ");
					return false;
					//					}
				}

				ifx.getTransaction().setReferenceTransaction(referenceTransaction);
				return true;

			}
			//			if (!(Util.hasText(myLastSeqCntr) && Util.hasText(terminalLastSeqCntr)
			//					&& Integer.parseInt(myLastSeqCntr) == Integer.parseInt(terminalLastSeqCntr)
			//					&& LastStatusIssued.GOOD_TERMINATION_SENT.equals(ifx.getLastTrxStatusIssue()) 
			//					&& !IfxType.PARTIAL_DISPENSE_REV_REPEAT_RQ.equals(ifx.getIfxType()))) {
			//				
			//				ifx.getTransaction().setReferenceTransaction(referenceTransaction);
			//				return true;
			//			}
		}

		return false;
	}

	@Override
	protected Ifx checkOriginalData(Ifx incomingIfx, Ifx refIncomingIfx) throws InvalidBusinessDateException {
		if (refIncomingIfx == null)
			return incomingIfx; 

		Transaction referenceTransaction = refIncomingIfx.getTransaction();

		if (TransactionService.isReferenceTrxSettled(referenceTransaction)){
			throw new InvalidBusinessDateException("Originator Transaction already settled.(refTrx: "
					+ referenceTransaction.getId() + ")");
		}

		if (ISOFinalMessageType.isReturnRq(incomingIfx.getIfxType())) {
			incomingIfx.setAuth_Amt(refIncomingIfx.getAuth_Amt());
			incomingIfx.setReal_Amt(incomingIfx.getAuth_Amt());
			incomingIfx.setTrx_Amt(incomingIfx.getAuth_Amt());
			incomingIfx.setSec_Amt(refIncomingIfx.getSec_Amt());
		}
		if (incomingIfx.getOriginalDataElements().getFwdBankId() == null)
			incomingIfx.getOriginalDataElements().setFwdBankId(refIncomingIfx.getDestBankId());
		if (incomingIfx.getOriginalDataElements().getBankId() == null)
			incomingIfx.getOriginalDataElements().setBankId(refIncomingIfx.getBankId());
		if (incomingIfx.getOriginalDataElements().getOrigDt() == null)
			incomingIfx.getOriginalDataElements().setOrigDt(refIncomingIfx.getOrigDt());
		if (incomingIfx.getOriginalDataElements().getTrnSeqCounter() == null)
			incomingIfx.getOriginalDataElements().setTrnSeqCounter(refIncomingIfx.getSrc_TrnSeqCntr());
		if (incomingIfx.getOriginalDataElements().getTerminalId() == null)
			incomingIfx.getOriginalDataElements().setTerminalId(refIncomingIfx.getTerminalId());
		if (incomingIfx.getOriginalDataElements().getAppPAN() == null)
			incomingIfx.getOriginalDataElements().setAppPAN(refIncomingIfx.getAppPAN());

		if (!Util.hasText(incomingIfx.getPINBlock())) {
			if (Util.hasText(incomingIfx.getAppPAN())
					&& incomingIfx.getAppPAN().equals(refIncomingIfx.getAppPAN())) {
				incomingIfx.setPINBlock(refIncomingIfx.getPINBlock());
			}
		}
		return incomingIfx;
	}

	@Override
	public void checkValidityOfLastTransactionStatus(Ifx incomingIfx) {
		try {
			//			ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, Util.longValueOf(incomingIfx.getTerminalId()));
			ATMTerminal atm = (ATMTerminal) incomingIfx.getEndPointTerminal();
			TransactionService.checkValidityOfLastTransactionStatus(atm, incomingIfx);

			//Mirkamali(Task139): If flag is disagreement, reverse it!
			Transaction lastTransaction = atm.getLastTransaction();
			if (lastTransaction != null) {
				ClearingInfo srcClrInfo = lastTransaction.getSourceClearingInfo();
				if(srcClrInfo != null && ClearingState.DISAGREEMENT.equals(srcClrInfo.getClearingState()) && lastTransaction.getIncomingIfx() != null) {

					List<Ifx> result = TransactionService.getATMPowerFailure(lastTransaction.getIncomingIfx().getReceivedDtLong(), incomingIfx.getReceivedDtLong(),
							lastTransaction.getIncomingIfx().getSrc_TrnSeqCntr(), incomingIfx.getTerminalId());

					if(result != null && result.size() > 0)
						logger.debug("A power failure is received befor for terminal " + incomingIfx.getTerminalId() + ": Don't reverse it!");
					else {
						logger.debug("trx should be revers:" + lastTransaction.getFirstTransaction() + ", " + lastTransaction);
						if(lastTransaction.getLifeCycle() != null && lastTransaction.getLifeCycle().getIsFullyReveresed() == null)
							SchedulerService.processReversalJob(lastTransaction.getFirstTransaction(), lastTransaction, ISOResponseCodes.APPROVED, null, false);
						else
							logger.debug("trx shouldnt be revers:" + lastTransaction.getFirstTransaction() + ", " + lastTransaction);

					}

				}
			}

		} catch (ReversalOriginatorNotFoundException e) {
			logger.error("Check Validity of Last Transaction "+ e.getClass().getSimpleName()+": "+ e.getMessage());
		}
	}
}
