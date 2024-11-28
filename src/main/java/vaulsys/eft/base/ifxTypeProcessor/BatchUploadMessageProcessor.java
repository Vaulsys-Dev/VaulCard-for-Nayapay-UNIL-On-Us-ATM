package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.eft.util.MsgProcessor;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.ChannelManager;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.base.POSBatchTransactionLog;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

public class BatchUploadMessageProcessor extends MessageProcessor {
	private Logger logger = Logger.getLogger(this.getClass());
	public static final BatchUploadMessageProcessor Instance = new BatchUploadMessageProcessor();
	private BatchUploadMessageProcessor(){};

	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext) throws Exception {

		Message outgoingMessage;
		Ifx incomingIfx, outIfx;

		logger.info("In POS Batch Transaction Log");

		outgoingMessage = new Message(MessageType.OUTGOING);

		//TODO: check if following if/else is replaced by message routing entry
		if (incomingMessage.getChannel().getCommunicationMethod().equals(CommunicationMethod.SAME_SOCKET))
			outgoingMessage.setChannel(incomingMessage.getChannel());
		else
			outgoingMessage.setChannel(
					ChannelManager.getInstance().getChannel(incomingMessage.getChannel().getInstitutionId(), "in")
			);

		incomingIfx = incomingMessage.getIfx();

		//m.rehman: saving POS Batch data in db
		savePOSBatchData(processContext);
		logger.info("Saving data in DB");

		outIfx = MsgProcessor.processor(incomingIfx);

		outIfx.setIfxType(IfxType.getResponseIfxType(outIfx.getIfxType()));
		outIfx.setMti(ISOMessageTypes.getResponseMTI(incomingIfx.getMti()));
		outIfx.setRequest(Boolean.FALSE);
		if (!Util.hasText(outIfx.getRsCode()))
			outIfx.setRsCode(ISOResponseCodes.APPROVED);

		outgoingMessage.setTransaction(transaction);
		outgoingMessage.setIfx(outIfx);
		outgoingMessage.setEndPointTerminal(incomingMessage.getEndPointTerminal());
		addNecessaryDataToIfx(outIfx, incomingMessage.getChannel(), incomingMessage.getEndPointTerminal());

		if (transaction.getFirstTransaction() == null)
			transaction.setFirstTransaction(transaction);

		setMessageFlag(outgoingMessage, false, false, true, false);

		transaction.addOutputMessage(outgoingMessage);

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

	//m.rehman: saving pos batch data in db
	private void savePOSBatchData(ProcessContext processContext) throws Exception {

		try {
			POSBatchTransactionLog transactionLog = new POSBatchTransactionLog();
			transactionLog = transactionLog.copyFields(processContext);
			GeneralDao.Instance.saveOrUpdate(transactionLog);

		} catch (Exception e) {
			logger.error("Error in Saving POS Batch Transaction Log data");
			throw e;
		}
	}
}
