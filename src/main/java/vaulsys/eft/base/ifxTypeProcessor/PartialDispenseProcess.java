package vaulsys.eft.base.ifxTypeProcessor;

import vaulsys.eft.util.MsgProcessor;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.ATMSpecificData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class PartialDispenseProcess extends MessageProcessor {

	private Logger logger = Logger.getLogger(this.getClass());
	
	public static final PartialDispenseProcess Instance = new PartialDispenseProcess();
	private PartialDispenseProcess(){};
	
	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel, ProcessContext processContext)
			throws Exception {

		Transaction referenceTransaction = transaction.getReferenceTransaction();
		Transaction refTrx = referenceTransaction.getReferenceTransaction();
		if (refTrx == null)
			refTrx = referenceTransaction.getFirstTransaction();
		transaction.setReferenceTransaction(refTrx);
		transaction.setFirstTransaction(referenceTransaction);
		
		Ifx ifx = null;
		try {
			ifx = transaction.getFirstTransaction().getOutgoingIfx()/*getOutputMessage().getIfx()*/;
		} catch (Exception e) {
			logger.error("Exception in getting transaction.getFirstTransaction().getOutputMessage().getIfx() for trx: " + transaction.getId());
			logger.error(e);
			return null;
		}
		Ifx outgoingIfx = createOutgoingIfx(incomingMessage.getIfx(), ifx);
		ATMTerminal atm = (ATMTerminal) incomingMessage.getEndPointTerminal();

		Message outMsg = new Message(MessageType.OUTGOING);
		outMsg.setTransaction(transaction);
		transaction.addOutputMessage(outMsg);
		outMsg.setIfx(outgoingIfx);
		outMsg.setChannel(incomingMessage.getChannel());
		outMsg.setEndPointTerminal(atm);
		setMessageFlag(outMsg, false, false, true, true);
//		updateRealAmount(atm, transaction.getReferenceTransaction());
		
		if (isFinishedTransaction(ifx.getAtmSpecificData())) {
			return null;
		}

		GeneralDao.Instance.saveOrUpdate(outgoingIfx);
		GeneralDao.Instance.saveOrUpdate(outMsg);
        GeneralDao.Instance.saveOrUpdate(outMsg.getMsgXml());

        transaction.setOutgoingIfx(outgoingIfx);
		GeneralDao.Instance.saveOrUpdate(transaction);
		
		outMsg.getTransaction().setDebugTag(outgoingIfx.getIfxType() + "_" + (outgoingIfx.getCurrentStep() + 1));

		return outMsg;
	}

	private Ifx createOutgoingIfx(Ifx incomingIfx, Ifx refIfx) throws CloneNotSupportedException {
		Ifx outgoingIfx = MsgProcessor.processor(refIfx);
		outgoingIfx.setIfxType(IfxType.PARTIAL_DISPENSE_RS);
		outgoingIfx.setAtmSpecificData(refIfx.getAtmSpecificData() != null ? refIfx.getAtmSpecificData().copy() : incomingIfx.getSafeAtmSpecificData());
		outgoingIfx.setNetworkTrnInfo(incomingIfx.getNetworkTrnInfo());
		outgoingIfx.setOpkey(incomingIfx.getOpkey());
		/**************/
		outgoingIfx.getAtmSpecificData().setNextOpkey(null);
		outgoingIfx.getAtmSpecificData().setSecIfxType(null);
		outgoingIfx.getAtmSpecificData().setSecTrnType(null);
		/**************/
		outgoingIfx.setCoordinationNumber(incomingIfx.getCoordinationNumber());
		outgoingIfx.setNetworkRefId(incomingIfx.getNetworkRefId());
		outgoingIfx.setTimeVariantNumber(incomingIfx.getTimeVariantNumber());
		outgoingIfx.setPINBlock(incomingIfx.getPINBlock());
		outgoingIfx.setMy_TrnSeqCntr(incomingIfx.getMy_TrnSeqCntr());
		outgoingIfx.setSrc_TrnSeqCntr(outgoingIfx.getMy_TrnSeqCntr());
		outgoingIfx.setRsCode(ISOResponseCodes.APPROVED);
		return outgoingIfx;
	}

	private boolean isFinishedTransaction(ATMSpecificData atmSpecificData) {
		if (atmSpecificData == null)
			return true;

		if (!Util.hasText(atmSpecificData.getCurrentDispense()))
			return true;

		Integer cassette1 = Integer.parseInt(atmSpecificData.getCurrentDispense().substring(0, 2));
		Integer cassette2 = Integer.parseInt(atmSpecificData.getCurrentDispense().substring(2, 4));
		Integer cassette3 = Integer.parseInt(atmSpecificData.getCurrentDispense().substring(4, 6));
		Integer cassette4 = Integer.parseInt(atmSpecificData.getCurrentDispense().substring(6, 8));

		if (atmSpecificData.getDesiredDispenseCaset1().equals(atmSpecificData.getActualDispenseCaset1() + cassette1)
				&& atmSpecificData.getDesiredDispenseCaset2().equals(
						atmSpecificData.getActualDispenseCaset2() + cassette2)
				&& atmSpecificData.getDesiredDispenseCaset3().equals(
						atmSpecificData.getActualDispenseCaset3() + cassette3)
				&& atmSpecificData.getDesiredDispenseCaset4().equals(
						atmSpecificData.getActualDispenseCaset4() + cassette4))
			return true;

		return false;
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
