package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.authorization.exception.MandatoryFieldException;
import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.billpayment.MCIBillPaymentUtil;
import vaulsys.caching.CheckAccountParamsForCache;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.lottery.LotteryService;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.netmgmt.extended.NetworkInfoStatus;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.routing.exception.ScheduleMessageFlowBreakDown;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
//import vaulsys.webservices.mci.billpayment.BillInfo;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class GeneralMessageProcessor extends MessageProcessor {

	transient Logger logger = Logger.getLogger(GeneralMessageProcessor.class);

	public static final GeneralMessageProcessor Instance = new GeneralMessageProcessor();
	private GeneralMessageProcessor(){};


	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws Exception {

		Message message;
		Transaction refTrx = transaction.getFirstTransaction();

		if (refTrx != null) {

			Message refMessage = refTrx.getInputMessage();

			boolean isPartiallyReversed = false;

			if (ISOFinalMessageType.isReversalRsMessage(incomingMessage.getIfx().getIfxType()) &&
					(ISOResponseCodes.APPROVED.equals(incomingMessage.getIfx().getRsCode())
							||ISOResponseCodes.INVALID_ACCOUNT.equals(incomingMessage.getIfx().getRsCode())
							) ) {
				Ifx outIfx = refTrx.getOutgoingIfx()/*getOutputMessage().getIfx()*/;
				Long amt_acq = Util.longValueOf(outIfx.getNew_AmtAcqCur());
				Long amt_iss = Util.longValueOf(outIfx.getNew_AmtIssCur());
				if ((amt_acq != null && !amt_acq.equals(0L)) || (amt_iss != null && !amt_iss.equals(0L))) {
					isPartiallyReversed = true;
				}

				Transaction referenceTransaction = refTrx.getReferenceTransaction();
				if (referenceTransaction != null) {
					Long real_Amt = (amt_acq != null && !amt_acq.equals(0L)) ? amt_acq : amt_iss;
					incomingMessage.getIfx().setReal_Amt(real_Amt);
					referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.setReal_Amt(real_Amt);
					GeneralDao.Instance.saveOrUpdate(incomingMessage.getIfx());
					GeneralDao.Instance.saveOrUpdate(referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/);
					Transaction responseTrx = TransactionService.findResponseTrx(referenceTransaction.getLifeCycleId(), referenceTransaction);
					if (responseTrx != null) {
						try {
							responseTrx.getIncomingIfx()/*getInputMessage().getIfx()*/.setReal_Amt(real_Amt);
							GeneralDao.Instance.saveOrUpdate(responseTrx.getIncomingIfx()/*getInputMessage().getIfx()*/);
						} catch (Exception e) {
						}
					}
				}
			} else if (!refMessage.isScheduleMessage() && ISOFinalMessageType.isReversalRsMessage(incomingMessage.getIfx().getIfxType())
					&& ISOResponseCodes.FIELD_ERROR.equals(incomingMessage.getIfx().getRsCode())
					&& refTrx.getReferenceTransaction()!=null){

				ScheduleMessage reverseMessage = SchedulerService.addInstantReversalAndRepeatTriggerAndRemoveOldTriggers(
						refTrx.getReferenceTransaction(), refMessage.getIfx().getRsCode(), Util.longValueOf(refMessage.getIfx().getNew_AmtIssCur()));

				if (reverseMessage.getId() != null) {
					GeneralDao.Instance.saveOrUpdate(reverseMessage);
					GeneralDao.Instance.saveOrUpdate(reverseMessage.getMsgXml());
				}

				processContext.addPendingRequests(reverseMessage);
			}

			//m.rehman: TODO: improve this check, need to implement to handle advice
			if ((refMessage.isScheduleMessage() && refMessage.getIfx()==null)
					|| (refMessage.isScheduleMessage() && refMessage.getIfx()!=null
							&& transaction.getInputMessage().getIfx() != null
							&& ISOMessageTypes.isFinancialAdviceResponseMessage(
								transaction.getInputMessage().getIfx().getMti())))
				// TODO:Schedule should create IFX!
			{
				logger.info("Breaking down normal flow into own-schedule-answer handler: " +
						"RSCode:" + transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getRsCode());

				//                 finalizeSelfEndedTransaction(transaction);
				String cause = ((ScheduleMessage)refMessage).getResponseCode();

				TransactionService.putFlagOnOurReversalTransaction(transaction, isPartiallyReversed, cause);
				//                 transaction.setEndDateTime(DateTime.now());
				logger.info("ScheduleMessageFlowBreakDown");
				throw new ScheduleMessageFlowBreakDown();
			}
		}


		/************** add to checkAccount map for transfer *******************/
		try{
			Ifx incomingIfx = transaction.getIncomingIfx();

			if(!(incomingIfx.getBankId().equals(ProcessContext.get().getMyInstitution().getBin()) && TerminalType.ATM.equals(incomingIfx.getTerminalType()))){

				if(IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(incomingIfx.getIfxType()) || IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(incomingIfx.getIfxType())) {

					CheckAccountParamsForCache checkAccount = TransactionService.createCheckAccountObjForAddOrGet(incomingIfx);

					GlobalContext.getInstance().addCheckAccountForTransfer(checkAccount, transaction.getId());

					logger.debug("checkAccountCache: Size of cache AFTER adding a new checkAccount is: " + GlobalContext.getInstance().getCheckAccountForTransafer().size());
				}

			}

		}catch (Exception e) {
			logger.error("checkAccountCache: An Exception occures in adding obj to checkAccount to map but continue! "+e.getMessage());
		}

		/***********************************************************************/
		//For Invalid Txn
		if(incomingMessage.getProtocolMessage() instanceof ISOMsg) { //Raza adding when getting response from CMS have to verify
			if(ISOMessageTypes.isRequestMessage(incomingMessage.getIfx().getIfxType())){
				if (((ISOMsg) incomingMessage.getProtocolMessage()).getMessageStatus() == (ISOMsg.INVALID)) {
					logger.error("Message Format Issue");
					message = createOutgoingMessageForInvalidTransactions(transaction, incomingMessage, channel, ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
			} else if ((!incomingMessage.getChannel().getProcessingStatus().equals(NetworkInfoStatus.PROCESSING_ENABLED) && incomingMessage.getChannel().getSignonreq())) { //Raza adding for SIGNON when not supported
					logger.error("Channel " + incomingMessage.getChannel().getName() + " is not ready!!! Send Sign On Message first");
					message = createOutgoingMessageForInvalidTransactions(transaction, incomingMessage, channel, ISOResponseCodes.ORIGINAL_ALREADY_REJECTED);
				} else if (!channel.getConnectionStatus().equals(NetworkInfoStatus.SOCKET_CONNECTED)
						|| ((!channel.getProcessingStatus().equals(NetworkInfoStatus.PROCESSING_ENABLED)) && channel.getSignonreq())) { //Raza adding for SIGNON when not supported
					logger.error("Channel " + channel.getName() + " is not Connected "
							+ "or is not ready to process. Check Connectivity and Signing In!!!");
					message = createOutgoingMessageForInvalidTransactions(transaction, incomingMessage, channel, ISOResponseCodes.ISSUER_REVERSAL);
				} else {
					return createOutgoingMessageForNormalTransactions(transaction, incomingMessage, channel, processContext);
				}
			}
			else
			{
				//Raza Add here for Response ??? below commented Code
				message = createOutgoingMessageForNormalTransactions(transaction, incomingMessage, channel, processContext);
			}

		}
		else //Raza For Response Only Check Invalid Message, Channel Status & Reversal will be managed by NetworkManager
		{
//			if (((ISOMsg) incomingMessage.getProtocolMessage()).getMessageStatus() == (ISOMsg.INVALID)) { //Raza commenting
//				logger.error("Message Format Issue");
//				message = createOutgoingMessageForInvalidTransactions(transaction, incomingMessage, channel, ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
//			}
//			else
//			{
				message = createOutgoingMessageForNormalTransactions(transaction, incomingMessage, channel, processContext);
			//}
		}
		return message;
	}

	private Message createOutgoingMessageForNormalTransactions(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext) throws CloneNotSupportedException {
		String origChannel, destChannel, key;
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setTransaction(transaction);
		transaction.addOutputMessage(outgoingMessage);
		outgoingMessage.setChannel(channel);

		//        logger.debug("Process incoming message ");
		Ifx incomingIfx = incomingMessage.getIfx();

		if (IfxType.RETURN_RS.equals(incomingIfx.getIfxType()) &&
				ISOResponseCodes.APPROVED.equals(incomingIfx.getRsCode()))
			LotteryService.unlockLottery(incomingIfx, transaction);

		Ifx outgoingIfx = MsgProcessor.processor(incomingIfx);

		//TODO only for reversal messages(shetab duplicate)
		outgoingIfx.setMy_TrnSeqCntr(outgoingIfx.getSrc_TrnSeqCntr());

        //m.rehman: forward bank id should be same as received if available, adding condition
        /*if (outgoingIfx.getFwdBankId() == null)
		    outgoingIfx.setFwdBankId(channel.getInstitution());*/

		if(incomingMessage.getIfx().getIfxType().equals(IfxType.BILL_PMT_RQ) && MCIBillPaymentUtil.isBillPaymentWithMobileNumber(incomingIfx.getBillPaymentID())){
			findSetBillInfo(outgoingIfx, transaction);
		}

		outgoingMessage.setIfx(outgoingIfx);
		setMessageFlag(outgoingMessage, incomingMessage.getRequest(), incomingMessage.getNeedResponse(), incomingMessage.getNeedToBeSent(), incomingMessage.getNeedToBeInstantlyReversed());

		Terminal endpointTerminal = getEndpointTerminal(outgoingMessage, incomingMessage.getChannel().getEndPointType(), true, processContext);
		outgoingMessage.setEndPointTerminal(endpointTerminal);
		addNecessaryDataToIfx(outgoingIfx, channel, endpointTerminal);

		//m.rehman: if loro required, add a schedule message
		if (ISOFinalMessageType.isResponseMessage(outgoingIfx.getIfxType())) {
			key = outgoingIfx.getMti() + outgoingIfx.getTrnType().getType() + outgoingIfx.getRsCode();

			//origChannel = ProcessContext.get().getChannel(outgoingMessage.getChannelId()).getChannelType();
			origChannel = outgoingMessage.getChannel().getName();
			//origChannel = (origChannel.contains("Out")) ? (origChannel.substring(0, origChannel.indexOf("Out")))
			//		: (origChannel.substring(0, origChannel.indexOf("In")));
			if (origChannel.contains("Out")){
				origChannel = origChannel.substring(0, origChannel.indexOf("Out"));
			} else if (origChannel.contains("In")) {
				origChannel = origChannel.substring(0, origChannel.indexOf("In"));
			}

			//destChannel = ProcessContext.get().getChannel(outgoingMessage.getChannelId()).getChannelType(); //incomingMessage.getChannelName();
			destChannel = incomingMessage.getChannel().getName();
			//destChannel = (destChannel.contains("Out")) ? (destChannel.substring(0, destChannel.indexOf("Out")))
			//		: (destChannel.substring(0, destChannel.indexOf("In")));
			if (destChannel.contains("Out")){
				destChannel = destChannel.substring(0, destChannel.indexOf("Out"));
			} else if (destChannel.contains("In")) {
				destChannel = destChannel.substring(0, destChannel.indexOf("In"));
			}

			key += origChannel + destChannel;

			if (GlobalContext.getInstance().isLoroExist(key)) {
				ScheduleMessage confirmationMessage = SchedulerService.createConfirmationScheduleMsg(
						transaction.getFirstTransaction(), SchedulerConsts.LORO_MSG_TYPE);
				SchedulerService.createConfirmationJobInfo(transaction.getFirstTransaction());

				if (confirmationMessage.getId() != null) {
					GeneralDao.Instance.saveOrUpdate(confirmationMessage);
					GeneralDao.Instance.saveOrUpdate(confirmationMessage.getMsgXml());
				}

				Set<Message> pendingRequests = new HashSet<Message>();
				pendingRequests.add(confirmationMessage);
				outgoingMessage.setPendingRequests(pendingRequests);

				logger.info("Loro message added in queue successfully.");
			}

			//m.rehman: set ifx fields according to the request
			setIfxAttributes(transaction.getFirstTransaction().getIncomingIfx(), outgoingIfx);
		}

		GeneralDao.Instance.saveOrUpdate(outgoingIfx);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());

		transaction.setOutgoingIfx(outgoingIfx);
		GeneralDao.Instance.saveOrUpdate(transaction);

		return outgoingMessage;
	}

	@Override
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
		IfxType ifxType = ifx.getIfxType();
		if (ISOFinalMessageType.isRequestMessage(ifxType) &&
				!ISOFinalMessageType.isReversalMessage(ifxType)) {

			/** ghasedak **/
			if (ifx.getTrnType().equals(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT) || ifx.getTrnType().equals(TrnType.NONFINANCIAL_INFO))
				return;

			if (ifx.getAppPAN() == null || (ifx.getAppPAN().length() != 16 && ifx.getAppPAN().length() != 19))
				throw new MandatoryFieldException("Failed: " + ifx.getIfxType() + " has wrong AppPan: " + ifx.getAppPAN());
		}
	}
	private void findSetBillInfo(Ifx ifx, Transaction transaction) {
		Ifx referenceIfx = transaction.getReferenceTransaction().getOutgoingIfx();
		ifx.setBillID(referenceIfx.getBillID());
		ifx.setBillPaymentID(referenceIfx.getBillPaymentID());
		try {
			BillPaymentUtil.setBillData(ifx, referenceIfx.getBillID(), referenceIfx.getBillPaymentID());
		} catch (Exception e) {
			logger.warn("Exception in setting Bill Data...");
		}
	}

	//m.rehman: for invalid transactions having message format issue
	private Message createOutgoingMessageForInvalidTransactions(Transaction transaction, Message incomingMessage, Channel channel, String respCode)
			throws CloneNotSupportedException {

		Message outgoingMessage = new Message(MessageType.OUTGOING);

		Ifx incomingIfx = incomingMessage.getIfx();

		Ifx outIfx = MsgProcessor.processor(incomingIfx);

		//m.rehman: if error occurred on response message, no need to get response MTI
		if (!ISOFinalMessageType.isResponseMessage(incomingIfx.getIfxType()))
			outIfx.setMti(ISOMessageTypes.getResponseMTI(incomingIfx.getMti()));
		outIfx.setRsCode(respCode);

		if (transaction.getFirstTransaction() == null)
			transaction.setFirstTransaction(transaction);

		if (ISOFinalMessageType.isRequestMessage(incomingIfx.getIfxType())) {

			outIfx.setIfxType(IfxType.getResponseIfxType(outIfx.getIfxType()));

			if (incomingMessage.getChannel().getCommunicationMethod() == CommunicationMethod.SAME_SOCKET)
				outgoingMessage.setChannel(incomingMessage.getChannel());
			else
				outgoingMessage.setChannel(((InputChannel) incomingMessage.getChannel()).getOriginatorChannel());

		} else if (ISOFinalMessageType.isResponseMessage(incomingIfx.getIfxType())) {

			outgoingMessage.setChannel(channel);

			ScheduleMessage reverseMessage = SchedulerService.addInstantReversalAndRepeatTriggerAndRemoveOldTriggers(
					transaction.getFirstTransaction(), ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND, incomingIfx.getAuth_Amt());

			if (reverseMessage.getId() != null) {
				GeneralDao.Instance.saveOrUpdate(reverseMessage);
				GeneralDao.Instance.saveOrUpdate(reverseMessage.getMsgXml());
			}

			Set<Message> pendingRequests = new HashSet<Message>();
			pendingRequests.add(reverseMessage);
			outgoingMessage.setPendingRequests(pendingRequests);

			TransactionService.updateMessageForNotSuccessful(outIfx, transaction);
		}

		outgoingMessage.setTransaction(transaction);
		outgoingMessage.setIfx(outIfx);
		outgoingMessage.setEndPointTerminal(incomingMessage.getEndPointTerminal());
		addNecessaryDataToIfx(outIfx, incomingMessage.getChannel(), incomingMessage.getEndPointTerminal());

		setMessageFlag(outgoingMessage, false, false, true, false);

		transaction.addOutputMessage(outgoingMessage);

		//m.rehman: separating BI from financial incase of limit
		//reverse limit
		if (ISOFinalMessageType.isFinancialMessage(incomingIfx.getIfxType(), true)) {
			if (incomingIfx.getCardLimit() != null)
				GeneralDao.Instance.evict(incomingIfx.getCardLimit());
		}

		GeneralDao.Instance.saveOrUpdate(outIfx);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
		GeneralDao.Instance.saveOrUpdate(transaction);

		return outgoingMessage;
	}

	//m.rehman: set outgoingifx according to the request ifx
	private void setIfxAttributes(Ifx requestIfx, Ifx outIfx) {

		//for nac and onelink transactions, if pre-auth convert to financial request, need to change in response also
		//outIfx.setMti(ISOMessageTypes.setMessageClass(requestIfx.getMti(),outIfx.getMti()));

		//set trntype and ifxtype
		outIfx.setTrnType(requestIfx.getTrnType());
		outIfx.setIfxType(IfxType.getResponseIfxType(requestIfx.getIfxType()));
	}
}
