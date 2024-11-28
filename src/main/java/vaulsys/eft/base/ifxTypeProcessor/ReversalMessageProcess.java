package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.eft.util.MsgProcessor;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.protocols.PaymentSchemes.UnionPay.UnionPayResponseCodes;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.scheduler.SchedulerService;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Pair;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class ReversalMessageProcess extends MessageProcessor {
	private Logger logger = Logger.getLogger(this.getClass());
	public static final ReversalMessageProcess Instance = new ReversalMessageProcess();
	private ReversalMessageProcess(){};

	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel/*=null*/, ProcessContext processContext) throws Exception {

		Message outgoingMessage = new Message(MessageType.OUTGOING);
		if (incomingMessage.getChannel().getCommunicationMethod() == CommunicationMethod.SAME_SOCKET)
			outgoingMessage.setChannel(incomingMessage.getChannel());
		else
			outgoingMessage.setChannel(((InputChannel) incomingMessage.getChannel()).getOriginatorChannel());
		Ifx incomingIfx = incomingMessage.getIfx();

		//m.rehman: backup of original response code
		String cause = incomingIfx.getRsCode();

		if (!Util.hasText(cause) && incomingMessage.getChannel().getChannelId().equals(ChannelCodes.UNION_PAY)) {
			cause = UnionPayResponseCodes.mapRespCode(Integer.parseInt(incomingIfx.getSelfDefineData().substring(0,4)));
		}
		Ifx outIfx = MsgProcessor.processor(incomingIfx);

		outIfx.setIfxType(IfxType.getResponseIfxType(outIfx.getIfxType()));
		//m.rehman: changing msg type
		//outIfx.setMti(ISOMessageTypes.getResponseMTI(incomingIfx.getMti()));
		//outIfx.setRsCode("00");
		outIfx.setRsCode(ISOResponseCodes.APPROVED);
		logger.info("outIfx Type [" + outIfx.getIfxType() + "]"); //Raza TEMP
		outgoingMessage.setTransaction(transaction);
		outgoingMessage.setIfx(outIfx);
		outgoingMessage.setEndPointTerminal(incomingMessage.getEndPointTerminal());
		addNecessaryDataToIfx(outIfx, incomingMessage.getChannel(), incomingMessage.getEndPointTerminal());
		if (!IfxType.PARTIAL_DISPENSE_REV_REPEAT_RQ.equals(incomingIfx.getIfxType()) || transaction.getFirstTransaction() == null)
			transaction.setFirstTransaction(transaction);

		setMessageFlag(outgoingMessage, false, false, true, false);

		transaction.addOutputMessage(outgoingMessage);

		//Mirkamali: Side effect of Task139 and Task140
		//m.rehman: defining cause at the start as we need original response code for reversal
		//String cause = ErrorCodes.APPROVED;
		Pair<Boolean, ClearingState> reverseTransaction = TransactionService.isReverseTransaction(incomingIfx);
		if (ISOFinalMessageType.isNeedReverseTrigger(incomingIfx.getIfxType())
			&& reverseTransaction.first != null && reverseTransaction.first) {
			
			
			/*if (ClearingState.PARTIALLY_CLEARED.equals(reverseTransaction.second)){
				transaction.setReferenceTransaction(transaction.getReferenceTransaction().getReferenceTransaction());
				if (!Util.hasText(incomingIfx.getNew_AmtIssCur())){
					incomingIfx.setNew_AmtIssCur(transaction.getReferenceTransaction().getOutputMessage().getIfx().getReal_Amt()+"");
					incomingIfx.setNew_AmtAcqCur(incomingIfx.getNew_AmtIssCur()+"");
				}
			}*/
			
			if (reverseTransaction.second != null){
				if (ClearingState.NO_CARD_REJECTED.equals(reverseTransaction.second))
					cause = ATMErrorCodes.ATM_NO_CARD_REJECTED+"";
			}
			
			Long newamount = Util.longValueOf(incomingIfx.getNew_AmtAcqCur());
			newamount = (newamount==null || newamount.equals(0L))?Util.longValueOf(incomingIfx.getNew_AmtIssCur()):newamount;
			
			if (incomingIfx.getAuth_Amt().equals(0L) ||
					!incomingIfx.getAuth_Amt().equals(Util.longValueOf(incomingIfx.getNew_AmtAcqCur()))) {
				
				Transaction referenceTransaction = incomingMessage.getTransaction().getReferenceTransaction();
				if (IfxType.TRANSFER_REV_REPEAT_RQ.equals( incomingIfx.getIfxType()) ||
						IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ.equals(incomingIfx.getIfxType())){
					cause = ISOResponseCodes.CUSTOMER_RELATION_NOT_FOUND;
					Transaction t = TransactionService.getTransferToTrx(referenceTransaction);
					referenceTransaction = (t!=null)? t: referenceTransaction;
				}
				
				ScheduleMessage reverseMessage = SchedulerService.addInstantReversalAndRepeatTriggerAndRemoveOldTriggers(
						referenceTransaction, cause, newamount);
	
				if (reverseMessage.getId() != null) {
					GeneralDao.Instance.saveOrUpdate(reverseMessage);
			        GeneralDao.Instance.saveOrUpdate(reverseMessage.getMsgXml());
				}
	
				Set<Message> pendingRequests = new HashSet<Message>();
				pendingRequests.add(reverseMessage);
				//outgoingMessage.setPendingRequests(pendingRequests);

				//m.rehman: if loro required, add a schedule message <start>
				String key, origChannel, destChannel;
				Channel loroHost, backupChannel;
				key = outIfx.getMti() + outIfx.getTrnType().getType() + outIfx.getRsCode();

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
					//loroHost = GlobalContext.getInstance().getLoroHost(key);
					//backupChannel = referenceTransaction.getOutputMessage().getChannel();
					//referenceTransaction.getOutputMessage().setChannel(loroHost);
					ScheduleMessage loroReversalScheduleMsg = SchedulerService.createReversalScheduleMsg(
							referenceTransaction, cause, newamount, SchedulerConsts.LORO_REVERSAL_MSG_TYPE);
					SchedulerService.createReversalJobInfo(referenceTransaction, newamount);
					//referenceTransaction.getOutputMessage().setChannel(backupChannel);

					if (loroReversalScheduleMsg.getId() != null) {
						GeneralDao.Instance.saveOrUpdate(loroReversalScheduleMsg);
						GeneralDao.Instance.saveOrUpdate(loroReversalScheduleMsg.getMsgXml());
					}

					//pendingRequests = new HashSet<Message>();
					pendingRequests.add(loroReversalScheduleMsg);
				}
				//<end>

				outgoingMessage.setPendingRequests(pendingRequests);
			}
			
			/*** 1389/11/03: change from after loop to INJA (to support unlockOnlineBill)***/
			/*** vaghti niaz be reverse nist, in method ham nabayad seda zade beshe! ***/
			TransactionService.updateMessageForNotSuccessful(outIfx, transaction);
		}


		GeneralDao.Instance.saveOrUpdate(outIfx);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
		GeneralDao.Instance.saveOrUpdate(transaction);

		return outgoingMessage;
	}
	
	@Override
	public Message postProcess(Transaction transaction, Message incomingMessage, Message outgoingMessage, Channel channel)
			throws Exception {
		return outgoingMessage;
	}

	@Override
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
