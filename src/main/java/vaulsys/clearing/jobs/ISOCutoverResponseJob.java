package vaulsys.clearing.jobs;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
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
import vaulsys.protocols.ifx.enums.TrnType;
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

public class ISOCutoverResponseJob extends AbstractISOClearingJob implements ClearingJob {
	
	public static final ISOCutoverResponseJob Instance = new ISOCutoverResponseJob();
	private ISOCutoverResponseJob(){}

    public void execute(Message incomingMessage, Transaction refTransaction, ProcessContext processContext) throws Exception {

    	Terminal endPointTerminal = TerminalService.findEndpointTerminalForMessageWithoutIFX(incomingMessage, Util.longValueOf(((ISOMsg)incomingMessage.getProtocolMessage()).getString(41)));
    	incomingMessage.setEndPointTerminal(endPointTerminal);
    	ISOMsg protocolMessage = (ISOMsg) incomingMessage.getProtocolMessage();
    	MonthDayDate stlDate = new MonthDayDate(MyDateFormatNew.parse("MMdd", (protocolMessage).getString(15)));
//    	refTransaction.setAuthorized(true);
    	refTransaction.setDebugTag("CutOver");
    	incomingMessage.setIfx(createIncomingIfx(incomingMessage, protocolMessage, stlDate));
    	GeneralDao.Instance.saveOrUpdate(incomingMessage.getIfx());
    	GeneralDao.Instance.saveOrUpdate(incomingMessage);
    	GeneralDao.Instance.saveOrUpdate(incomingMessage.getMsgXml());

    	Message outMessage;
    	ISOMsg outIsoMsg; 
    	
    	Channel destInstchannel = ChannelManager.getInstance().getChannel(incomingMessage.getIfx().getBankId(), "out");
    	
    	
        if (!destInstchannel.getMasterDependant()) {
			outIsoMsg = (ISOMsg) getCutover().buildResponse(incomingMessage);
			outMessage = createOutputMessage(outIsoMsg, incomingMessage, refTransaction, endPointTerminal);
			outMessage.setRequest(false);
			outMessage.setIfx(creatOutgoingIfx(outMessage, incomingMessage.getIfx()));
		} else {
			outIsoMsg = (ISOMsg) protocolMessage.clone(new int[]{0,7,11,15,32,33,53,48,70,96});
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

	private Ifx creatOutgoingIfx(Message outMessage, Ifx ifx) throws CloneNotSupportedException {
		Ifx outIfx = ifx.clone();
		outIfx.setIfxType(IfxType.CUTOVER_RS);
		outIfx.setIfxDirection(IfxDirection.OUTGOING);
		outIfx.setReceivedDt(outMessage.getStartDateTime());
		outIfx.setRsCode(((ISOMsg)outMessage.getProtocolMessage()).getString(39));
		return outIfx;
	}

	private Ifx createIncomingIfx(Message incomingMessage, ISOMsg protocolMessage, MonthDayDate stlDate) {
		Ifx ifx = new Ifx();
		ifx.setIfxType( IfxType.CUTOVER_RQ);
		
		if (stlDate != null && stlDate.getMonth() == 1 && DateTime.now().getDayDate().getMonth() == 12) {
			stlDate.setYear(DateTime.now().getDayDate().getYear() + 1);
		}
		
		ifx.setSettleDt(stlDate);
		 try {
			ifx.setTrnDt ( new DateTime( MyDateFormatNew.parse("MMddHHmmss", 
					protocolMessage.getString(7).trim())));
		} catch (ParseException e) {
			logger.error("Creating Cut_over Ifx. ("+ e.getClass().getSimpleName()+" :"+ e.getMessage()+")", e);
//			new Exception("Creating Cut_over Ifx: ", e).printStackTrace();
		}
		ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad((protocolMessage).getString(11)));
		ifx.setMy_TrnSeqCntr(ifx.getSrc_TrnSeqCntr());
		ifx.setNetworkRefId(ifx.getSrc_TrnSeqCntr());
		ifx.setFwdBankId(protocolMessage.getString(33));
		ifx.setDestBankId(protocolMessage.getString(33));
		ifx.setBankId(protocolMessage.getString(32));
		ifx.setIfxDirection(IfxDirection.INCOMING);
		ifx.setTrnType(TrnType.NETWORKMANAGEMENT);
		ifx.setTerminalType(TerminalType.SWITCH);
		ifx.setOrigDt(DateTime.now());
		ifx.setReceivedDt( incomingMessage.getStartDateTime());
		ifx.setTerminalId(incomingMessage.getEndPointTerminal().getCode().toString());
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
	protected TerminalClearingMode getClearingMode() {
		return TerminalClearingMode.ISSUER;
	}

	
	
}
