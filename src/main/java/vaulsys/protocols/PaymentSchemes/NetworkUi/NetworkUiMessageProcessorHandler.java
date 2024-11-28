package vaulsys.protocols.PaymentSchemes.NetworkUi;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.cms.base.CMSProductKeys;
import vaulsys.cms.components.CMSStatusCodes;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.exception.SwitchSystemException;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.scheduler.SchedulerService;
import vaulsys.security.hsm.HardwareSecurityModule;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wallet.components.WalletDBOperations;
import vaulsys.wallet.exception.WalletCardValidationException;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;
//import org.apache.xalan.xslt.Process;
import org.hibernate.LockMode;

import java.util.*;
import vaulsys.customer.Currency;

public class NetworkUiMessageProcessorHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(NetworkUiMessageProcessorHandler.class);

	public static final NetworkUiMessageProcessorHandler Instance = new NetworkUiMessageProcessorHandler();
	
	private NetworkUiMessageProcessorHandler(){
	}

	@Override
	public void execute(ProcessContext processContext) throws Exception {
		Message incomingMessage = processContext.getInputMessage();
		IfxType ifxType = incomingMessage.getIfx().getIfxType();

		if (IfxType.SIGN_OFF_RQ.equals(ifxType) || IfxType.SIGN_ON_RQ.equals(ifxType) ||
				IfxType.ECHO_RQ.equals(ifxType) || IfxType.KEY_EXCHANGE_RQ.equals(ifxType)) {
			ProcessNetworkManagementRequest(processContext);

		} else if (IfxType.WALLET_TOPUP_RQ.equals(ifxType)) {
			ProcessWalletTopupRequest(processContext);

		} else if (IfxType.WALLET_TOPUP_REV_REPEAT_RQ.equals(ifxType)) {
			CreateWalletTopupReversal(processContext);

		} else if (IfxType.CVV_GENERATION_RQ.equals(ifxType)) {
			ProcessCVVGenerationRequest(processContext);

		} else
		{
			throw new SwitchSystemException("Not Supported IfxType: " + ifxType);
		}

		GeneralDao.Instance.saveOrUpdate(incomingMessage.getIfx());
		GeneralDao.Instance.saveOrUpdate(incomingMessage);
		GeneralDao.Instance.saveOrUpdate(incomingMessage.getMsgXml());
	}

	public void ProcessNetworkManagementRequest(ProcessContext processContext) throws Exception {
		Message incomingMessage, outMessage;
		String channelId;
		Channel outChannel;
		Terminal endpointTerminal;
		Ifx outIfx;
		Transaction transaction;
		MessageObject messageObject;

		incomingMessage = processContext.getInputMessage();
		transaction = incomingMessage.getTransaction();
		messageObject = (MessageObject)incomingMessage.getProtocolMessage();

		outMessage = null;
		try {
			channelId = messageObject.getChannelId();
			outChannel = ProcessContext.get().getChannel(channelId);

			outIfx = MsgProcessor.processor(incomingMessage.getIfx());
			outIfx.setReceivedDt(incomingMessage.getStartDateTime());
			outIfx.setIfxDirection(IfxDirection.OUTGOING);
			outIfx.setTerminalType(TerminalType.SWITCH);
			outIfx.setRequest(true);
			outIfx.setTransaction(transaction);

			outMessage = new Message(MessageType.OUTGOING);
			outMessage.setChannel(outChannel);
			outMessage.setTransaction(transaction);
			outMessage.setRequest(false);
			outMessage.setNeedResponse(false);
			outMessage.setNeedToBeInstantlyReversed(false);
			outMessage.setNeedToBeSent(true);
			outMessage.setIfx(outIfx);
			outMessage.setStartDateTime(incomingMessage.getStartDateTime());
			outMessage.setXML(messageObject.toString());
			endpointTerminal = TerminalService.findEndpointTerminal(outMessage, outMessage.getIfx(),
					incomingMessage.getChannel().getEndPointType());
			outMessage.setEndPointTerminal(endpointTerminal);

			LifeCycle lifeCycle = new LifeCycle();
			lifeCycle.setIsComplete(true);
			GeneralDao.Instance.saveOrUpdate(lifeCycle);
			transaction.setLifeCycle(lifeCycle);

			transaction.addOutputMessage(outMessage);
			GeneralDao.Instance.saveOrUpdate(outMessage);
			GeneralDao.Instance.saveOrUpdate(outMessage.getMsgXml());
			GeneralDao.Instance.saveOrUpdate(outIfx);

		} catch (Exception e) {
			logger.error("Exception in producing UIMessage: "+e , e);
			throw e;
		}
	}

	public void ProcessWalletTopupRequest(ProcessContext processContext) throws Exception {
		try {
			Message incomingMessage, outMessage;
			Channel outChannel;
			Terminal endpointTerminal;
			Ifx ifx, outIfx;
			String addData;
			Integer iretval;
			Transaction transaction;
			MessageObject messageObject;
			Currency currency;

			incomingMessage = processContext.getInputMessage();
			transaction = incomingMessage.getTransaction();
			messageObject = (MessageObject)incomingMessage.getProtocolMessage();

			ifx = incomingMessage.getIfx();
			addData = ifx.getAddDataPrivate();
			if (Util.hasText(addData) && addData.substring(0, 1).equals("W")) {
				iretval = WalletDBOperations.Instance.ValidateWalletByOtherInfo(ifx);
			} else {
				iretval = WalletDBOperations.Instance.ValidateWalletByPan(ifx);
			}

			if (iretval > 0) {
				logger.debug("Wallet validated successfully");

				if (!messageObject.getAmount().contains(".")) {
					currency = ProcessContext.get().getCurrency(
							Integer.parseInt(
									ifx.getWalletCardRelation().getAccount().getCurrency()));
					ifx.setAuth_Amt(ifx.getAuth_Amt() * (long) Math.pow(10, currency.getDecimalPosition()));
				}
				WalletDBOperations.Instance.TopupWalletAmount(ifx, Boolean.FALSE);

			} else if (iretval == 0) {
				//Off-Us transaction flow
				logger.info("OFFUS Wallet Card not allowed. Error!!!");
				ifx.setRsCode(ISOResponseCodes.TRANSACTION_TIMEOUT); //Raza verify this return code
				throw new WalletCardValidationException();

			} else {
				logger.error("Card Validation Failed");
				ifx.setRsCode(ISOResponseCodes.LIMIT_EXCEEDED); //Raza verify this return code
				throw new WalletCardValidationException();
			}

			if(ifx.getRsCode() == null || ifx.getRsCode()=="") {
				ifx.setRsCode(ISOResponseCodes.APPROVED);
			}

			//outChannel = processContext.getChannel(incomingMessage.getChannel().getOriginatorChannelId());
			outChannel = incomingMessage.getChannel();

			outIfx = MsgProcessor.processor(ifx);
			outIfx.setIfxType(IfxType.getResponseIfxType(ifx.getIfxType()));
			outIfx.setMti(ISOMessageTypes.getResponseMTI(ifx.getMti()));
			outIfx.setReceivedDt(incomingMessage.getStartDateTime());
			outIfx.setIfxDirection(IfxDirection.OUTGOING);
			outIfx.setTerminalType(TerminalType.SWITCH);
			outIfx.setRequest(false);
			outIfx.setTransaction(transaction);

			outMessage = new Message(MessageType.OUTGOING);
			outMessage.setChannel(outChannel);
			outMessage.setTransaction(transaction);
			outMessage.setRequest(false);
			outMessage.setNeedResponse(false);
			outMessage.setNeedToBeInstantlyReversed(true);
			outMessage.setNeedToBeSent(true);
			outMessage.setIfx(outIfx);
			outMessage.setStartDateTime(incomingMessage.getStartDateTime());
			outMessage.setXML(messageObject.toString());
			endpointTerminal = incomingMessage.getEndPointTerminal();
			outMessage.setEndPointTerminal(endpointTerminal);

			LifeCycle lifeCycle = new LifeCycle();
			lifeCycle.setIsComplete(true);
			GeneralDao.Instance.saveOrUpdate(lifeCycle);
			transaction.setLifeCycle(lifeCycle);

			transaction.addOutputMessage(outMessage);
			GeneralDao.Instance.saveOrUpdate(outMessage);
			GeneralDao.Instance.saveOrUpdate(outMessage.getMsgXml());
			GeneralDao.Instance.saveOrUpdate(outIfx);

		} catch (Exception e) {
			logger.error("Exception in producing UIMessage: "+e , e);
			throw e;
		}
	}

	public Message ProcessWalletTopupReversal(ProcessContext processContext) throws Exception {
		try {
			logger.debug("Try to reverse Wallet Topup");
			ScheduleMessage scheduleMessage;
			if (processContext.getInputMessage().isScheduleMessage()) {
				scheduleMessage = (ScheduleMessage) processContext.getInputMessage();
				if (scheduleMessage.getMessageType().equals(SchedulerConsts.WALLET_TOPUP_REVERSAL_MSG_TYPE)) {

					Transaction refTranaction = scheduleMessage.getTransaction().getReferenceTransaction();
					logger.debug("Try to reverse msg " + refTranaction.getDebugTag() + "(" + refTranaction.getId() + ")");

					Transaction trans = processContext.getTransaction();

					Message message = refTranaction.getOutputMessage();
					if (message != null) {
						Message outMsg = new Message(MessageType.OUTGOING);

						outMsg.setEndPointTerminal(scheduleMessage.getEndPointTerminal());
						outMsg.setTransaction(trans);

						outMsg.setRequest(false);
						outMsg.setNeedToBeSent(false);
						outMsg.setNeedToBeInstantlyReversed(false);
						outMsg.setNeedResponse(false);

						Ifx ifx = message.getIfx().copy();

						ifx.setIfxType(IfxType.WALLET_TOPUP_REV_REPEAT_RQ);
						ifx.setIfxDirection(IfxDirection.SELF_GENERATED);
						ifx.setRsCode(scheduleMessage.getResponseCode());
						ifx.setMti(ISOMessageTypes.REVERSAL_ADVICE_87);
						ifx.setTrnType(TrnType.WALLET_TOPUP);
						ifx.setRequest(false);

						Ifx refIfx = refTranaction.getIncomingIfx();
						ifx.setReceivedDt(outMsg.getStartDateTime());
						ifx.setFirstTrxId(trans.getId());
						ifx.setTransaction(refTranaction);

						//ifx = incomingMessage.getIfx();
						Integer iretval;
						String addData = refIfx.getAddDataPrivate();
						if (Util.hasText(addData) && addData.substring(0, 1).equals("W")) {
							iretval = WalletDBOperations.Instance.ValidateWalletByOtherInfo(ifx);
						} else {
							iretval = WalletDBOperations.Instance.ValidateWalletByPan(ifx);
						}

						if (iretval > 0) {
							logger.debug("Wallet validated successfully");

							WalletDBOperations.Instance.TopupWalletAmount(ifx, Boolean.TRUE);
							ifx.setRsCode(ISOResponseCodes.APPROVED);

						} else if (iretval == 0) {
							//Off-Us transaction flow
							logger.info("OFFUS Wallet Card not allowed. Error!!!");
							ifx.setRsCode(ISOResponseCodes.TRANSACTION_TIMEOUT); //Raza verify this return code
							throw new WalletCardValidationException();

						} else {
							logger.error("Card Validation Failed");
							ifx.setRsCode(ISOResponseCodes.LIMIT_EXCEEDED); //Raza verify this return code
							throw new WalletCardValidationException();
						}

						outMsg.setIfx(ifx);

						//outMsg.setChannel(processContext.getChannel(
						//		message.getChannel().getOriginatorChannelId()));
						outMsg.setChannel(message.getChannel());

						trans.addOutputMessage(outMsg);

						TransactionService.updateLifeCycleStatusNormally(trans, ifx);
						TransactionService.updateMessageForNotSuccessful(ifx, refIfx, trans);

						if (ifx.getRsCode().equals(ISOResponseCodes.APPROVED))
							SchedulerService.removeReversalJobInfo(refTranaction.getId());
						//else
						//	SchedulerService.updateJobInfo(refTranaction.getId(), ifx.getRsCode());

						GeneralDao.Instance.saveOrUpdate(ifx);
						GeneralDao.Instance.saveOrUpdate(outMsg);
						GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
						GeneralDao.Instance.saveOrUpdate(trans);

					} else {
						logger.debug("RefTransaction doesn't have outputmessage, so reversal message cannot be created!");
					}
				} else
					logger.debug("input message is not of applicable type (Reversal_ScheduleMessage): " + scheduleMessage.getMessageType());
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	public void ProcessCVVGenerationRequest(ProcessContext processContext) throws Exception {
		try {
			Message outMessage;
			Channel outChannel;
			Terminal endpointTerminal;
			Ifx ifx, outIfx;
			List<CMSProductKeys> cmsProductKeys;
			String productId, rsCode;
			Transaction transaction;
			MessageObject messageObject;
			Message incomingMessage;

			incomingMessage = processContext.getInputMessage();
			transaction = incomingMessage.getTransaction();
			messageObject = (MessageObject) incomingMessage.getProtocolMessage();
			ifx = incomingMessage.getIfx();

			productId = ifx.getCmsCardRelation().getProductId();
			cmsProductKeys = processContext.getCMSProductKeys(productId);

			if (cmsProductKeys == null)
				throw new Exception("Product Keys not found");

			HardwareSecurityModule.getInstance().CVVGeneration(ifx, cmsProductKeys, Boolean.TRUE);
			rsCode = ifx.getRsCode();
			if (rsCode != null && rsCode.equals(ISOResponseCodes.APPROVED)) {
				logger.debug("CVV Generated Successfully!!!");

			} else {
				ifx.setRsCode(ISOResponseCodes.INVALID_IMD);
			}

			outIfx = MsgProcessor.processor(ifx);
			outIfx.setIfxType(IfxType.getResponseIfxType(ifx.getIfxType()));
			outIfx.setReceivedDt(incomingMessage.getStartDateTime());
			outIfx.setIfxDirection(IfxDirection.OUTGOING);
			outIfx.setTerminalType(TerminalType.SWITCH);
			outIfx.setRequest(false);
			outIfx.setTransaction(transaction);
			outIfx.setRsCode(ifx.getRsCode());
			if (ifx.getCardAcctId().getCVV() != null)
				outIfx.getSafeCardAcctId().setCVV(ifx.getCardAcctId().getCVV());

			//outChannel = processContext.getChannel(incomingMessage.getChannel().getOriginatorChannelId());
			outChannel = incomingMessage.getChannel();

			outMessage = new Message(MessageType.OUTGOING);
			outMessage.setChannel(outChannel);
			outMessage.setTransaction(transaction);
			outMessage.setRequest(false);
			outMessage.setNeedResponse(false);
			outMessage.setNeedToBeInstantlyReversed(false);
			outMessage.setNeedToBeSent(true);
			outMessage.setIfx(outIfx);
			outMessage.setStartDateTime(incomingMessage.getStartDateTime());
			outMessage.setXML(messageObject.toString());
			endpointTerminal = incomingMessage.getEndPointTerminal();
			outMessage.setEndPointTerminal(endpointTerminal);

			LifeCycle lifeCycle = new LifeCycle();
			lifeCycle.setIsComplete(true);
			GeneralDao.Instance.saveOrUpdate(lifeCycle);
			transaction.setLifeCycle(lifeCycle);

			transaction.addOutputMessage(outMessage);
			GeneralDao.Instance.saveOrUpdate(outMessage);
			GeneralDao.Instance.saveOrUpdate(outMessage.getMsgXml());
			GeneralDao.Instance.saveOrUpdate(outIfx);

		} catch (Exception e) {
			logger.error("Exception in producing UIMessage: "+e , e);
			throw e;
		}
	}

	public void CreateWalletTopupReversal(ProcessContext processContext) throws Exception {
		try {
			Message incomingMessage, outMessage;
			Channel outChannel;
			Terminal endpointTerminal;
			Ifx ifx, outIfx;
			Transaction transaction, originatorTransaction;
			List<Transaction> transactionList;
			MessageObject messageObject;
			ScheduleMessage reversalScheduleMsg;
			Set<Message> pendingRequests;

			incomingMessage = processContext.getInputMessage();
			transaction = incomingMessage.getTransaction();
			messageObject = (MessageObject)incomingMessage.getProtocolMessage();

			ifx = incomingMessage.getIfx();
			ifx.getSafeOriginalDataElements().setAppPAN(ifx.getAppPAN());
			ifx.getSafeOriginalDataElements().setBankId(ifx.getBankId());
			ifx.getSafeOriginalDataElements().setMessageType(ISOMessageTypes.FINANCIAL_REQUEST_87);
			ifx.getSafeOriginalDataElements().setTrnSeqCounter(ifx.getSrc_TrnSeqCntr());
			ifx.getSafeOriginalDataElements().setOrigDt(ifx.getOrigDt());

			//outChannel = processContext.getChannel(incomingMessage.getChannel().getOriginatorChannelId());
			outChannel = incomingMessage.getChannel();

			outIfx = MsgProcessor.processor(ifx);
			outIfx.setIfxType(IfxType.getResponseIfxType(ifx.getIfxType()));
			outIfx.setMti(ISOMessageTypes.getResponseMTI(ifx.getMti()));
			outIfx.setReceivedDt(incomingMessage.getStartDateTime());
			outIfx.setIfxDirection(IfxDirection.OUTGOING);
			outIfx.setTerminalType(TerminalType.SWITCH);
			outIfx.setRequest(false);
			outIfx.setTransaction(transaction);
			outIfx.setRsCode(ISOResponseCodes.APPROVED);

			outMessage = new Message(MessageType.OUTGOING);
			outMessage.setChannel(outChannel);
			outMessage.setTransaction(transaction);
			outMessage.setRequest(false);
			outMessage.setNeedResponse(false);
			outMessage.setNeedToBeInstantlyReversed(false);
			outMessage.setNeedToBeSent(true);
			outMessage.setIfx(outIfx);
			outMessage.setStartDateTime(incomingMessage.getStartDateTime());
			outMessage.setXML(messageObject.toString());
			endpointTerminal = incomingMessage.getEndPointTerminal();
			outMessage.setEndPointTerminal(endpointTerminal);

			transactionList = TransactionService.getReversalOriginatorTransaction(ifx);
			originatorTransaction = transactionList.get(0);
			originatorTransaction.getAndLockLifeCycle(LockMode.UPGRADE);
			reversalScheduleMsg = SchedulerService.createReversalScheduleMsg(originatorTransaction,
					ISOResponseCodes.INVALID_TO_ACCOUNT, ifx.getAuth_Amt(),
					SchedulerConsts.WALLET_TOPUP_REVERSAL_MSG_TYPE);
			SchedulerService.createReversalJobInfo(originatorTransaction, ifx.getAuth_Amt());
			originatorTransaction.getLifeCycle().setIsComplete(Boolean.FALSE);

			if (reversalScheduleMsg.getId() != null) {
				GeneralDao.Instance.saveOrUpdate(reversalScheduleMsg);
				GeneralDao.Instance.saveOrUpdate(reversalScheduleMsg.getMsgXml());
			}

			pendingRequests = new HashSet<Message>();
			pendingRequests.add(reversalScheduleMsg);

			outMessage.setPendingRequests(pendingRequests);

			LifeCycle lifeCycle = new LifeCycle();
			lifeCycle.setIsComplete(true);
			GeneralDao.Instance.saveOrUpdate(lifeCycle);
			transaction.setLifeCycle(lifeCycle);

			transaction.addOutputMessage(outMessage);
			GeneralDao.Instance.saveOrUpdate(outMessage);
			GeneralDao.Instance.saveOrUpdate(outMessage.getMsgXml());
			GeneralDao.Instance.saveOrUpdate(outIfx);
			GeneralDao.Instance.saveOrUpdate(originatorTransaction);

		} catch (Exception e) {
			logger.error("Exception in producing UIMessage: "+e , e);
			throw e;
		}
	}

	public static void ProcessInitRequest(String command) throws Exception { //Raza INITIALIZE
			try
			{
				if(command.equals("Init_CHANNELS"))
				{
					logger.info("Initializing Channels.... please wait...");
					GlobalContext.getInstance().setAllChannels();

					logger.info("Initializing HSM Channels.... please wait...");
					GlobalContext.getInstance().setAllHSMChannels();
				}
				else if(command.equals("Init_CLEARINGPROFILES"))
				{
					logger.info("Initializing ClearingProfiles.... please wait...");
					GlobalContext.getInstance().setAllClearingProfile();
					logger.info("Initializing ClearingStuff.... please wait...");
					GlobalContext.getInstance().setAllClearingStuff();
				}
				else if(command.equals("Init_LOTTERYASSIGNMENTPOLICY"))
				{
					logger.info("Initializing LotteryAssignmentPolicy.... please wait...");
					GlobalContext.getInstance().setAllLotteryAssignmentPolicy();
				}
				else if(command.equals("Init_CONVERTERS"))
				{
					logger.info("Initializing Converters.... please wait...");
					GlobalContext.getInstance().setAllConvertors();
				}
				else if(command.equals("Init_CURRENCIES"))
				{
					logger.info("Initializing Currencies.... please wait...");
					GlobalContext.getInstance().setAllCurrencies();
				}
				else if(command.equals("Init_SECURITYFUNC"))
				{
					logger.info("Initializing SecurityFunctions.... please wait...");
					GlobalContext.getInstance().setAllSecurityFunctions();
				}
				else if(command.equals("Init_FEEPROFILES"))
				{
					logger.info("Initializing FeeProfiles.... please wait...");
					GlobalContext.getInstance().setAllFeeProfiles();
				}
				else if(command.equals("Init_DISCOUNTPROFILES"))
				{
					logger.info("Initializing DiscountProfiles.... please wait...");
					GlobalContext.getInstance().setAllDiscountProfiles();
				}
				else if(command.equals("Init_AUTHPROFILES"))
				{
					logger.info("Initializing AuthorizationProfiles.... please wait...");
					GlobalContext.getInstance().setAllAuthorizationProfiles();
				}
				else if(command.equals("Init_INSTITUTIONS"))
				{
					logger.info("Initializing Institutions.... please wait...");
					GlobalContext.getInstance().setAllInstitutions();
				}
				else if(command.equals("Init_BANKS"))
				{
					logger.info("Initializing Banks.... please wait...");
					GlobalContext.getInstance().setAllBanks();
				}
				else if(command.equals("Init_ATM"))
				{
					logger.info("Initializing ATMKeys.... please wait...");
					GlobalContext.getInstance().setATMKey();
					logger.info("Initializing ATMConfigurations.... please wait...");
					GlobalContext.getInstance().setAllATMConfigurations();
				}
				else if(command.equals("Init_ROUTING"))
				{
					logger.info("Initializing MessageRouting.... please wait...");
					GlobalContext.getInstance().setAllMessageRouting();
				}
				else if(command.equals("Init_CMS")) //General CMS initialize all relaterd stuff
				{
					logger.info("Initializing CMSStatusCodes.... please wait...");
					CMSStatusCodes.LoadCodes();
					logger.info("Initializing CMSProducts.... please wait...");
					GlobalContext.getInstance().setAllCMSProducts();
				}
				else if(command.equals("Init_CELLCHARGE"))
				{
					try {
						logger.info("Initializing CellPhoneChargeSpecification.... please wait...");
						GlobalContext.getInstance().setAllCellPhoneChargeSpecification();
						logger.info("Initializing GeneralChargePolicy.... please wait...");
						GlobalContext.getInstance().setGeneralChargePolicy();
					} catch (Exception e) {
						logger.error("MTN Charge couldn't loaded in GlobalContext");
					}
				}
				else if(command.equals("Init_PROTOCOLS"))
				{
					logger.info("Initializing Protocols.... please wait...");
					GlobalContext.getInstance().setAllProtocolConfig();
				}
				else if(command.equals("Init_LORO"))
				{
					logger.info("Initializing LoroEntries.... please wait...");
					GlobalContext.getInstance().setAllLoroEntries();
				}
				else if(command.equals("Init_SYS")){
					logger.info("Initializing System.... please wait...");

					GlobalContext.getInstance().setAllChannels();
					logger.info("Puting all ClearingProfile in GlobalContext");
					GlobalContext.getInstance().setAllClearingProfile();
					logger.info("Puting all LotteryAssignmentPolicy in GlobalContext");
					GlobalContext.getInstance().setAllLotteryAssignmentPolicy();
					logger.info("Puting all convertors in GlobalContext");
					GlobalContext.getInstance().setAllConvertors();
					logger.info("Puting all clearing stuff in GlobalContext");
					GlobalContext.getInstance().setAllClearingStuff();
					logger.info("Puting currencies in GlobalContext");
					GlobalContext.getInstance().setAllCurrencies();

					logger.info("Puting all SecurityFunctions in GlobalContext");
					GlobalContext.getInstance().setAllSecurityFunctions();

					logger.info("Puting all FeeProfiles in GlobalContext");
					GlobalContext.getInstance().setAllFeeProfiles();

					logger.info("Puting all DiscountProfiles in GlobalContext");
					GlobalContext.getInstance().setAllDiscountProfiles();

					logger.info("Puting all AuthorizationProfiles in GlobalContext");
					GlobalContext.getInstance().setAllAuthorizationProfiles();

					logger.info("Puting all institutions in GlobalContext");
					GlobalContext.getInstance().setAllInstitutions();

					logger.info("Puting all banks in GlobalContext");
					GlobalContext.getInstance().setAllBanks();

					logger.info("Puting ATM Key in GlobalContext");
					GlobalContext.getInstance().setATMKey();

					logger.info("Puting ATM Configurations in GlobalContext");
					GlobalContext.getInstance().setAllATMConfigurations();

					logger.info("Putting Message Routing in GlobalContext");
					GlobalContext.getInstance().setAllMessageRouting();

					logger.info("Putting HSM Channels in GlobalContext");
					GlobalContext.getInstance().setAllHSMChannels();

					logger.info("Putting Card & Account Status Codes");
					CMSStatusCodes.LoadCodes();

					try {
						logger.info("Puting all Cell Phone Charge Specification in GlobalContext");
						GlobalContext.getInstance().setAllCellPhoneChargeSpecification();
						logger.info("Puting charge policy in GlobalContext");
						GlobalContext.getInstance().setGeneralChargePolicy();
					} catch (Exception e) {
						logger.error("MTN Charge couldn't loaded in GlobalContext");
					}

					logger.info("Loading protocol configs");
					GlobalContext.getInstance().setAllProtocolConfig();

					logger.info("Loading Loro entries");
					GlobalContext.getInstance().setAllLoroEntries();

					logger.info("Loading Product entries");
					GlobalContext.getInstance().setAllCMSProducts();

					logger.info("System Initialize Done OK");
				}
				else
				{
					logger.error("Initialize Command ["+  command +"] Not Reconized");
				}
				logger.info("Initialize Command ["+  command +"] Done OK");
			}
			catch(Exception e)
			{
				logger.error("Unable to process Initialize command [" + command + "]");
				logger.error("System Initialize Failed" + e.getMessage());

				e.printStackTrace();
			}
	}
}
