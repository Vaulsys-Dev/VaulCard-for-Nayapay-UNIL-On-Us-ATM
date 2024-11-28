package vaulsys.clearing.jobs;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.reconcile.ISOReconcilement;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.ChannelManager;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.ProtocolToXmlUtils;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.text.ParseException;
import java.util.Date;

public abstract class ISOReconcilementResponseJob extends AbstractISOClearingJob {
        protected ISOReconcilementResponseJob(){
                setReconcilement(ISOReconcilement.Instance);
        }

    public void execute(Message incomingMessage, Transaction refTransaction, ProcessContext processContext) throws Exception {
    	ISOMsg isoMsg = (ISOMsg) incomingMessage.getProtocolMessage();
    	Terminal endPointTerminal = TerminalService.findEndpointTerminalForMessageWithoutIFX(incomingMessage, Util.longValueOf(isoMsg.getString(41)));
    	incomingMessage.setEndPointTerminal(endPointTerminal);
        Ifx incommingIfx = createIncommingIfx(incomingMessage);
        incomingMessage.setIfx(incommingIfx);
		
        refTransaction.setDebugTag(incommingIfx.getIfxType().toString());
        
        GeneralDao.Instance.saveOrUpdate(incommingIfx);
        GeneralDao.Instance.saveOrUpdate(incomingMessage);
        GeneralDao.Instance.saveOrUpdate(incomingMessage.getMsgXml());
     
    	Message outMessage;
    	
    	Channel destInstchannel = ChannelManager.getInstance().getChannel(incomingMessage.getIfx().getDestBankId(), "out");
    	
    	
        if (!destInstchannel.getMasterDependant()) {

			Institution institution = FinancialEntityService.findEntity(Institution.class, incomingMessage.getChannel().getInstitutionId());

			Terminal terminal = findAppropriateTerminal(institution);

			ISOMsg outIsoMsg1 = (ISOMsg) getReconcilement().buildResponse(isoMsg, incomingMessage.getIfx(), terminal, processContext);

			Integer responseCode = Integer.parseInt(outIsoMsg1.getString(66));

			// MyDateFormat MMdd = new MyDateFormat("MMdd");
			Date stlDate = MyDateFormatNew.parse("MMdd", isoMsg.getString(15));
			MonthDayDate dayDate = new MonthDayDate(stlDate);
			DateTime dateTime = new DateTime(stlDate);

			// getTransactionService().applyResponseCode(terminal, dayDate,
			// responseCode);
			// getAccountingService().moveDailyLogsToArchive(terminal,
			// dateTime);

			// List<Transaction> trxList =
			// getTransactionService().getTransactions(terminal.getCode(),
			// dayDate);

			// generateReportFile(responseCode, trxList,
			// institution, stlDate, getClearingMode());

			outMessage = createOutputMessage(outIsoMsg1, incomingMessage, refTransaction, endPointTerminal);
			Ifx ifx = createOutgoingIfx(outMessage, incommingIfx);
			outMessage.setIfx(ifx);

			outMessage.setRequest(false);
			outMessage.setNeedResponse(false);
			outMessage.setNeedToBeInstantlyReversed(false);
			outMessage.setNeedToBeSent(true);
		} else {
			ISOMsg outIsoMsg = (ISOMsg) isoMsg.clone(new int[]{0,7,11,15,32,33, 50, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 97, 99});
			outIsoMsg.set(128, "0000000000000000");
			outMessage = createFwdOutputMessage(outIsoMsg, incomingMessage, refTransaction);
			outMessage.setRequest(true);
			outMessage.setIfx(MsgProcessor.processor(incomingMessage.getIfx()));
		}
        
        GeneralDao.Instance.saveOrUpdate(outMessage.getIfx());
        GeneralDao.Instance.saveOrUpdate(outMessage);
        GeneralDao.Instance.saveOrUpdate(outMessage.getMsgXml());
		
        refTransaction.addOutputMessage(outMessage);
    }

    protected Ifx createIncommingIfx(Message message) throws ParseException {
        ISOMsg protocolMessage = (ISOMsg) message.getProtocolMessage();
        Ifx ifx = new Ifx();

        if (getClearingMode().equals(TerminalClearingMode.ACQUIER))
            ifx.setIfxType(IfxType.ACQUIRER_REC_RQ);
        else
            ifx.setIfxType(IfxType.CARD_ISSUER_REC_RQ);

        ifx.setIfxDirection(IfxDirection.INCOMING);
        
        ifx.setReceivedDt(message.getStartDateTime());
        ifx.setSrc_TrnSeqCntr( ISOUtil.zeroUnPad(protocolMessage.getString(11)));
        ifx.setMy_TrnSeqCntr( ifx.getSrc_TrnSeqCntr());
        
        Transaction referenceTransaction = message.getTransaction().getReferenceTransaction();
		if (referenceTransaction!= null && referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/!=null
			&& referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getNetworkRefId()!= null	)
			ifx.setNetworkRefId(referenceTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getNetworkRefId());
		else
			ifx.setNetworkRefId(ifx.getSrc_TrnSeqCntr());
        
        
//        MyDateFormat MMdd = new MyDateFormat("MMdd");
        ifx.setSettleDt( new MonthDayDate(MyDateFormatNew.parse("MMdd", (protocolMessage).getString(15))));
        ifx.setBankId(protocolMessage.getString(99));
        ifx.setFwdBankId(protocolMessage.getString(33));
        ifx.setDestBankId(protocolMessage.getString(33));
        ifx.setTerminalType(TerminalType.SWITCH);
	 ifx.setOrigDt(DateTime.now());
//        ifx.setTerminalId(ISOUtil.zeroUnPad(protocolMessage.getString(41)));
        ifx.setTerminalId(message.getEndPointTerminal().getCode()+"");
        
        try {
			ifx.getSafeReconciliationData().setDebitNumber(Util.integerValueOf(protocolMessage.getString(74)));
			ifx.getSafeReconciliationData().setDebitReversalNumber(Util.integerValueOf(protocolMessage.getString(75)));
			ifx.getSafeReconciliationData().setCreditNumber(Util.integerValueOf(protocolMessage.getString(76)));
			ifx.getSafeReconciliationData().setCreditReversalNumber(Util.integerValueOf(protocolMessage.getString(77)));
			ifx.getSafeReconciliationData().setTransferNumber(Util.integerValueOf(protocolMessage.getString(78)));
			ifx.getSafeReconciliationData().setTransferReversalNumber(Util.integerValueOf(protocolMessage.getString(79)));
			ifx.getSafeReconciliationData().setBallInqNumber(Util.integerValueOf(protocolMessage.getString(80)));
			ifx.getSafeReconciliationData().setAuthorizationNumber(Util.integerValueOf(protocolMessage.getString(81)));
			ifx.getSafeReconciliationData().setDebitFee(Util.longValueOf(protocolMessage.getString(82)));
			ifx.getSafeReconciliationData().setCreditFee(Util.longValueOf(protocolMessage.getString(84)));
			ifx.getSafeReconciliationData().setDebitAmount(Util.longValueOf(protocolMessage.getString(86)));
			ifx.getSafeReconciliationData().setDebitReversalAmount(Util.longValueOf(protocolMessage.getString(87)));
			ifx.getSafeReconciliationData().setCreditAmount(Util.longValueOf(protocolMessage.getString(88)));
			ifx.getSafeReconciliationData().setCreditReversalAmount(Util.longValueOf(protocolMessage.getString(89)));
		} catch (Exception e) {
			// TODO: handle exception
		}
        
        return ifx;
    }

    protected Ifx createOutgoingIfx(Message message, Ifx incomingIfx) throws ParseException, CloneNotSupportedException {
        ISOMsg protocolMessage = (ISOMsg) message.getProtocolMessage();
        Ifx ifx = MsgProcessor.processor(incomingIfx);
        if (getClearingMode().equals(TerminalClearingMode.ACQUIER))
            ifx.setIfxType(IfxType.ACQUIRER_REC_RS);
        else
            ifx.setIfxType( IfxType.CARD_ISSUER_REC_RS);

        ifx.setIfxDirection(IfxDirection.OUTGOING);
        ifx.setRsCode( protocolMessage.getString(66));
        return ifx;
    }

    
    private Message createFwdOutputMessage(ISOMsg isoMsg, Message incomingMessage, Transaction refTransaction) {
		Message outgoingMessage = new Message(MessageType.OUTGOING);
        outgoingMessage.setTransaction(refTransaction);
        String destBankId = incomingMessage.getIfx().getDestBankId();
        Channel channel = ChannelManager.getInstance().getChannel(destBankId, "out");
        if (channel == null)
            channel = ChannelManager.getInstance().getChannel(destBankId, "");
        
        outgoingMessage.setChannel(channel);
        outgoingMessage.setProtocolMessage(isoMsg);
        Institution institution = FinancialEntityService.getInstitutionByCode(destBankId);
        if (institution == null){
        	logger.error("Fwd Institution couldn't be found! "+ destBankId);
        }
//		SwitchTerminal switchTerminal = FinancialEntityService.getIssuerSwitchTerminal(institution);
        SwitchTerminal switchTerminal = ProcessContext.get().getIssuerSwitchTerminal(institution);
		if (switchTerminal == null)
//			switchTerminal = FinancialEntityService.getAcquireSwitchTerminal(institution);
			switchTerminal = ProcessContext.get().getAcquireSwitchTerminal(institution);
		outgoingMessage.setEndPointTerminal(switchTerminal);
        
        ProtocolToXmlUtils.setXMLdata(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
        return outgoingMessage;
	}
    
    @Override
    public ClearingJob preJob() throws Exception {
    	return BindISOReconcilementRequest.Instance;
    }
    
//    @Override
//    public ClearingJob postJob() throws Exception {
//    	return BindISOReconcilementRequest.class.newInstance();
//    }
}
