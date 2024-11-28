package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.eft.util.MsgProcessor;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.transaction.Transaction;
import vaulsys.wfe.ProcessContext;

public class LastPurchaseChargeProcess extends MessageProcessor {

	public static final LastPurchaseChargeProcess Instance = new LastPurchaseChargeProcess();
	private LastPurchaseChargeProcess(){};

	
	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws Exception {
		Ifx ifx = transaction.getReferenceTransaction().getOutgoingIfx()/*getOutputMessage().getIfx()*/;
		Ifx outgoingIfx = createOutgoingIfx(incomingMessage.getIfx(), ifx);
		
		Message outMsg = new Message(MessageType.OUTGOING);
		outMsg.setTransaction(transaction);
		transaction.addOutputMessage(outMsg);
		outMsg.setIfx(outgoingIfx);
		outMsg.setChannel(incomingMessage.getChannel());
		outMsg.setEndPointTerminal(incomingMessage.getEndPointTerminal());
		setMessageFlag(outMsg, false, false, true, true);

		GeneralDao.Instance.saveOrUpdate(outgoingIfx);
		GeneralDao.Instance.saveOrUpdate(outMsg);
		GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());
		
		transaction.setOutgoingIfx(outgoingIfx);
		GeneralDao.Instance.saveOrUpdate(transaction);
		return outMsg;
	}

	private Ifx createOutgoingIfx(Ifx incomingIfx, Ifx refIfx) throws CloneNotSupportedException {
		Ifx outgoingIfx = MsgProcessor.processor(incomingIfx);
//		outgoingIfx.setNetworkTrnInfo(refIfx.getNetworkTrnInfo().copy());
		outgoingIfx.setIfxType(IfxType.LAST_PURCHASE_CHARGE_RS);
		outgoingIfx.setTrnType(TrnType.LASTPURCHASECHARGE);
//		outgoingIfx.setMy_TrnSeqCntr(incomingIfx.getMy_TrnSeqCntr());
//		outgoingIfx.setSrc_TrnSeqCntr(outgoingIfx.getMy_TrnSeqCntr());
		outgoingIfx.setRsCode(ISOResponseCodes.APPROVED);
		return outgoingIfx;
	}

	@Override
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
		// TODO Auto-generated method stub
	}
}
