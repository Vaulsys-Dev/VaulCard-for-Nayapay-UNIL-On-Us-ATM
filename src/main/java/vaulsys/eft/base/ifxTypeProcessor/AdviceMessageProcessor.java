package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.eft.util.MsgProcessor;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.channel.base.ChannelManager;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.scheduler.SchedulerService;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class AdviceMessageProcessor extends MessageProcessor {
	private Logger logger = Logger.getLogger(this.getClass());
	public static final AdviceMessageProcessor Instance = new AdviceMessageProcessor();
	private AdviceMessageProcessor(){}

	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext) throws Exception {

		Message outgoingMessage;
		Ifx incomingIfx, outIfx;

		outgoingMessage = new Message(MessageType.OUTGOING);

		if (incomingMessage.getChannel().getCommunicationMethod().equals(CommunicationMethod.SAME_SOCKET))
			outgoingMessage.setChannel(incomingMessage.getChannel());
		else
			outgoingMessage.setChannel(
					ChannelManager.getInstance().getChannel(incomingMessage.getChannel().getInstitutionId(), "in")
			);

		incomingIfx = incomingMessage.getIfx();

		outIfx = MsgProcessor.processor(incomingIfx);

		outIfx.setIfxType(IfxType.getResponseIfxType(outIfx.getIfxType()));
		outIfx.setMti(ISOMessageTypes.getResponseMTI(incomingIfx.getMti()));
		outIfx.setRsCode(ISOResponseCodes.APPROVED);
		outIfx.setRequest(Boolean.FALSE);

		outgoingMessage.setTransaction(transaction);
		outgoingMessage.setIfx(outIfx);
		outgoingMessage.setEndPointTerminal(incomingMessage.getEndPointTerminal());
		addNecessaryDataToIfx(outIfx, incomingMessage.getChannel(), incomingMessage.getEndPointTerminal());

		if (transaction.getFirstTransaction() == null)
			transaction.setFirstTransaction(transaction);

		setMessageFlag(outgoingMessage, false, false, true, false);

		transaction.addOutputMessage(outgoingMessage);

		ScheduleMessage confirmationMessage = SchedulerService.createConfirmationScheduleMsg(
				transaction, SchedulerConsts.ADVICE_MSG_TYPE);
		SchedulerService.createConfirmationJobInfo(transaction);

		if (confirmationMessage.getId() != null) {
			GeneralDao.Instance.saveOrUpdate(confirmationMessage);
			GeneralDao.Instance.saveOrUpdate(confirmationMessage.getMsgXml());
		}

		Set<Message> pendingRequests = new HashSet<Message>();
		pendingRequests.add(confirmationMessage);
		logger.info("Advice message added in queue successfully.");
		outgoingMessage.setPendingRequests(pendingRequests);


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
