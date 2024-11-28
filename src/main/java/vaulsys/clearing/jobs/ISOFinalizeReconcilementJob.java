package vaulsys.clearing.jobs;

import vaulsys.calendar.MonthDayDate;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.ProtocolToXmlUtils;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.text.ParseException;

public abstract class ISOFinalizeReconcilementJob extends AbstractISOClearingJob {

    public void execute(Message incomingMessage, Transaction refTransaction, ProcessContext processContext) throws Exception {

        refTransaction.setDebugTag(getIfxType()+""); /*"ReconcilmentRs's"*/;
//        refTransaction.setAuthorized(true);
        String entityCode = incomingMessage.getChannel().getInstitutionId();
//		Institution institution = FinancialEntityService.findEntity(Institution.class, entityCode);

        
//        SwitchTerminal issuerSwitchTerminal = GlobalContext.getInstance().getIssuerSwitchTerminal(entityCode);
        SwitchTerminal issuerSwitchTerminal = ProcessContext.get().getIssuerSwitchTerminal(entityCode);
        incomingMessage.setEndPointTerminal(issuerSwitchTerminal);
        incomingMessage.setIfx(createIncommingIfx(incomingMessage));
        GeneralDao.Instance.saveOrUpdate(incomingMessage.getIfx());
        GeneralDao.Instance.saveOrUpdate(incomingMessage);
        GeneralDao.Instance.saveOrUpdate(incomingMessage.getMsgXml());
        
        if (incomingMessage.getChannel().getMasterDependant()){
        	Message refInputMsg = refTransaction.getFirstTransaction().getInputMessage();
			ISOMsg outIsoMsg = (ISOMsg) ((ISOMsg)incomingMessage.getProtocolMessage()).clone(new int[]{0,7,11,15,32, 33, 50, 66, 99}) ;
			outIsoMsg.set(128, "0000000000000000");
			Message outputMessage = new Message(MessageType.OUTGOING);
	        outputMessage.setTransaction(refTransaction);
	        outputMessage.setChannel(((InputChannel)refInputMsg.getChannel()).getOriginatorChannel());
	        outputMessage.setProtocolMessage(outIsoMsg);
	        outputMessage.setEndPointTerminal(refInputMsg.getEndPointTerminal());
	        outputMessage.setIfx(MsgProcessor.processor(incomingMessage.getIfx()));
	        outputMessage.setRequest(false);
	        outputMessage.setNeedResponse(false);
	        outputMessage.setNeedToBeInstantlyReversed(false);
	        outputMessage.setNeedToBeSent(true);
	        
	        ProtocolToXmlUtils.setXMLdata(outputMessage);
	        GeneralDao.Instance.saveOrUpdate(outputMessage.getIfx());
			GeneralDao.Instance.saveOrUpdate(outputMessage);
			GeneralDao.Instance.saveOrUpdate(outputMessage.getMsgXml());
			refTransaction.addOutputMessage(outputMessage);
        }
        
        
       /* Terminal terminal = findAppropriateTerminal(institution);
        //TODO: change refTransaction
        ISOMsg isoMsg = (ISOMsg) incomingMessage.getProtocolMessage();
        MonthDayDate date = null;
        Integer responseCode = 0;
        if (isoMsg.getString(66).isEmpty()) {
        	return;
        }
        responseCode = Integer.parseInt(isoMsg.getString(66));
        
        try {
			date = refTransaction.getFirstTransaction().getOutputMessage().getIfx().getSettleDt();//new DayDate(stlDate);
		} catch (Exception e) {
			date = refTransaction.getReferenceTransaction().getOutputMessage().getIfx().getSettleDt();//new DayDate(stlDate);
		}

		TransactionService.applyResponseCode(terminal, date, responseCode);

        DateTime dateTime = new DateTime(date, DayTime.UNKNOWN);*/

    }

    @Override
    public ClearingJob preJob() throws Exception {
        return BindISORecocilementResponse.class.newInstance();
    }
    
    protected Ifx createIncommingIfx(Message message) throws ParseException {
        ISOMsg protocolMessage = (ISOMsg) message.getProtocolMessage();
        Ifx ifx = new Ifx();
        ifx.setIfxType(getIfxType()); 
        ifx.setIfxDirection( IfxDirection.INCOMING);
        ifx.setReceivedDt(message.getStartDateTime());
        ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.getString(11)));
        ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.getString(11)));
//        MyDateFormat MMdd = new MyDateFormat("MMdd");
        ifx.setSettleDt( new MonthDayDate(MyDateFormatNew.parse("MMdd", (protocolMessage).getString(15))));
        ifx.setBankId(protocolMessage.getString(99));
        ifx.setFwdBankId(protocolMessage.getString(33));
        ifx.setDestBankId(protocolMessage.getString(33));
        return ifx;
    }
 
    private IfxType getIfxType(){
    	if (getClearingMode().equals(TerminalClearingMode.ACQUIER))
            return (IfxType.ACQUIRER_REC_RS);
        else
            return (IfxType.CARD_ISSUER_REC_RS);
    }
    
}