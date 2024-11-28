package vaulsys.repeatreversal;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class CreateSettlementHandler extends BaseHandler {
	private static final Logger logger = Logger.getLogger(CreateSettlementHandler.class);

	public static final CreateSettlementHandler Instance = new CreateSettlementHandler();

	private CreateSettlementHandler(){
	}

	@Override
	public void execute(ProcessContext processContext) throws Exception {
		logger.debug("Try to Process settlement msg");
		ScheduleMessage scheduleMessage;
		if (processContext.getInputMessage().isScheduleMessage()) {
			scheduleMessage = (ScheduleMessage) processContext.getInputMessage();
			if (SchedulerConsts.SETTLEMENT_MSG_TYPE.equals(scheduleMessage.getMessageType())) {
				Transaction trans = processContext.getTransaction();
				Institution neginInstitution = FinancialEntityService.getInstitutionByCode("639347");
//				Terminal terminal = FinancialEntityService.getIssuerSwitchTerminal(neginInstitution);
				Terminal terminal = ProcessContext.get().getIssuerSwitchTerminal(neginInstitution);
//				Channel channel = GlobalContext.getInstance().getChannel("channelNegin87OutA");
				Channel channel = ProcessContext.get().getChannel("channelNegin87OutA");
//				Channel channel = GlobalContext.getInstance().getChannel("channelSHETABOut");

				Message outMsg = new Message(MessageType.OUTGOING);
				outMsg.setTransaction(trans);
				outMsg.setEndPointTerminal(terminal);
				outMsg.setChannel(channel);
				outMsg.setRequest(true);
				outMsg.setNeedToBeSent(true);
				outMsg.setNeedToBeInstantlyReversed(false);
				outMsg.setNeedResponse(true);

				Ifx	ifx = scheduleMessage.getIfx().copy();
				ifx.setIfxDirection(IfxDirection.OUTGOING);
				outMsg.setIfx(ifx);
				trans.addOutputMessage(outMsg);

				TransactionService.updateLifeCycleStatusNormally(trans, ifx);

				GeneralDao.Instance.saveOrUpdate(ifx);
				GeneralDao.Instance.saveOrUpdate(outMsg);
		        GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
				GeneralDao.Instance.saveOrUpdate(trans);
			} else
				logger.debug("input message is not of applicable type (Settlement_ScheduleMessage): "+ scheduleMessage.getMessageType());
		}
	}

/*	private Message generateTransaferToMessage(String cardNumber, Long amount) {
		Message message = new Message();
        message.setType(MessageType.INCOMING);
        message.setChannel(GlobalContext.getInstance().getChannel("InternalChannel"));

        Transaction transaction = new Transaction(TransactionType.SELF_GENERATED);
        transaction.setInputMessage(message);
        transaction.setStatus(TransactionStatus.RECEIVED);
        transaction.setFirstTransaction(transaction);
        message.setTransaction(transaction);

        message.setIfx(ifx);

        XMLIFXMsg protocolMessage = new XMLIFXMsg(ifx);
        message.setProtocolMessage(protocolMessage);

		XStream xStream = new XStream();
		xStream.alias("ifx", XMLIFXMsg.class);
		String xml = xStream.toXML(protocolMessage);
		byte[] binaryData = xml.getBytes();
		message.setBinaryData(binaryData);

        getGeneralDao().saveOrUpdate(transaction);
        getGeneralDao().saveOrUpdate(ifx);
        getGeneralDao().saveOrUpdate(message);

        return message;

	}
*/}
