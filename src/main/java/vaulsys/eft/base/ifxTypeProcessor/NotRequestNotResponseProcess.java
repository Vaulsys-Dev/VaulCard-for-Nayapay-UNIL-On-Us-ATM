package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.billpayment.MCIBillPaymentUtil;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.routing.exception.ScheduleMessageFlowBreakDown;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.wfe.ProcessContext;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

public class NotRequestNotResponseProcess extends MessageProcessor {

	transient Logger logger = Logger.getLogger(NotRequestNotResponseProcess.class); 

	public static final NotRequestNotResponseProcess Instance = new NotRequestNotResponseProcess();
	private NotRequestNotResponseProcess(){};

	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws Exception {

		Transaction refTrx = transaction.getFirstTransaction();

		if (refTrx != null) {

			Message refMessage = refTrx.getInputMessage();

			if (refMessage.isScheduleMessage() && refMessage.getIfx()==null)
				// TODO:Schedule should create IFX!
			{
				logger.info("Breaking down normal flow into own-schedule-answer handler: " +
						"RSCode:" + transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getRsCode());

				//                 finalizeSelfEndedTransaction(transaction);
				TransactionService.putFlagOnOurReversalTransaction(transaction, false, null);
				//                 transaction.setEndDateTime(DateTime.now());
				logger.error("ScheduleMessageFlowBreakDown");
				throw new ScheduleMessageFlowBreakDown();
			}
		}

		return createOutgoingMessageForNormalTransactions(transaction, incomingMessage, channel, processContext);
	}

	private Message createOutgoingMessageForNormalTransactions(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext) throws CloneNotSupportedException, RemoteException, Exception {
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setTransaction(transaction);
		transaction.addOutputMessage(outgoingMessage);
		outgoingMessage.setChannel(channel);

		logger.debug("Process incoming message ");
		Ifx incomingIfx = incomingMessage.getIfx();
		Ifx outgoingIfx = MsgProcessor.processor(incomingIfx);

		outgoingIfx.setFwdBankId ( (channel.getInstitutionId() == null ? null :(FinancialEntityService.findEntity(Institution.class, channel.getInstitutionId())).getBin()).toString());
		outgoingMessage.setIfx(outgoingIfx);


		setMessageFlag(outgoingMessage, false, incomingMessage.getNeedResponse(), incomingMessage.getNeedToBeSent(), incomingMessage.getNeedToBeInstantlyReversed());

		Terminal endpointTerminal = getEndpointTerminal(outgoingMessage, incomingMessage.getChannel().getEndPointType(), true, processContext); 
		outgoingMessage.setEndPointTerminal(endpointTerminal);
		addNecessaryDataToIfx(outgoingIfx, channel, endpointTerminal);

		if(ISOFinalMessageType.isPrepareBillPaymentRqMessage(incomingMessage.getIfx().getIfxType()) && MCIBillPaymentUtil.isBillPaymentWithMobileNumber(incomingIfx)){
			new MCIBillPaymentUtil().retreiveSetBillInfo(outgoingIfx);
			BillPaymentProcess.Instance.messageValidation(outgoingIfx, incomingMessage);
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
	}

}
