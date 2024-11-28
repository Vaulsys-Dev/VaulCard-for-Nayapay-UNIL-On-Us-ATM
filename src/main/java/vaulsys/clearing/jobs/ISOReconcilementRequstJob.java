package vaulsys.clearing.jobs;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.base.ClearingDate;
import vaulsys.clearing.base.ClearingDateManager;
import vaulsys.clearing.reconcile.ISOReconcilement;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.terminal.TerminalClearingMode;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.ProtocolToXmlUtils;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.text.ParseException;

public class ISOReconcilementRequstJob extends AbstractISOClearingJob implements ClearingJob {

	public static final ISOReconcilementRequstJob Instance = new ISOReconcilementRequstJob();
	public ISOReconcilementRequstJob(){
		setReconcilement(ISOReconcilement.Instance);
		setPreJob(BindISOCutOverResponse.Instance);
	}
	
    public void execute(Message incomingMessage, Transaction refTransaction, ProcessContext processContext) throws Exception {
        ISOMsg isoMsg = (ISOMsg) incomingMessage.getProtocolMessage();
//        if (NetworkManagementInfo.CUTOVER.getType() != (Integer.parseInt(isoMsg.getString(70).trim()))){
////        if (Integer.parseInt(isoMsg.getString(70).trim()) != ShetabTransactionTypes.CUTOVER){
//        	logger.error("NotApplicableTypeMessageException: f_70 = "+ isoMsg.getString(70).trim());
//            throw new NotApplicableTypeMessageException();
//        }
        
        String entityCode = incomingMessage.getChannel().getInstitutionId();
//        SwitchTerminal issuerSwitchTerminal = GlobalContext.getInstance().getIssuerSwitchTerminal(entityCode);
        SwitchTerminal issuerSwitchTerminal = ProcessContext.get().getIssuerSwitchTerminal(entityCode);
        incomingMessage.setEndPointTerminal(issuerSwitchTerminal);
        incomingMessage.setIfx(createIncommingIfx(incomingMessage));
        GeneralDao.Instance.saveOrUpdate(incomingMessage.getIfx());
        GeneralDao.Instance.saveOrUpdate(incomingMessage);
        GeneralDao.Instance.saveOrUpdate(incomingMessage.getMsgXml());
        

		Message outputMessage = null; 
		
		if (!(ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87 == isoMsg.getMTI()) ||
				ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_87 == isoMsg.getMTI()) {

			logger.debug("it is cutoff response...");
			Institution institution = FinancialEntityService.findEntity(Institution.class, entityCode);
			ClearingDate lastworkingDay = FinancialEntityService.getLastWorkingDay(institution);
			MonthDayDate cutOverDate = incomingMessage.getIfx().getSettleDt();
			if (lastworkingDay != null && lastworkingDay.getDate().equals(cutOverDate)) {
				if (!lastworkingDay.isValid()) {
					lastworkingDay.setValid(true);
					institution.setLastWorkingDay(institution.getCurrentWorkingDay());
					institution.setCurrentWorkingDay(lastworkingDay);
					ClearingDateManager.getInstance().deleteOtherWorkingDay(institution);
				}
			} else if (lastworkingDay == null || lastworkingDay.getDate().before(cutOverDate)
					|| lastworkingDay.getDate().after(cutOverDate)) {
				ClearingDateManager.getInstance().push(cutOverDate, DateTime.now(), true, institution);
			}

			if (incomingMessage.getChannel().getMasterDependant()) {

				Message refInputMsg = refTransaction.getFirstTransaction().getInputMessage();
				institution = (Institution) refInputMsg.getEndPointTerminal().getOwner();
				lastworkingDay = FinancialEntityService.getLastWorkingDay(institution);
				cutOverDate = incomingMessage.getIfx().getSettleDt();
				if (lastworkingDay != null && lastworkingDay.getDate().equals(cutOverDate)) {
					if (!lastworkingDay.isValid()) {
						lastworkingDay.setValid(true);
						institution.setLastWorkingDay(institution.getCurrentWorkingDay());
						institution.setCurrentWorkingDay(lastworkingDay);
						ClearingDateManager.getInstance().deleteOtherWorkingDay(institution);
					}
				} else if (lastworkingDay == null || lastworkingDay.getDate().before(cutOverDate)
						|| lastworkingDay.getDate().after(cutOverDate)) {
					ClearingDateManager.getInstance().push(cutOverDate, DateTime.now(), true, institution);
				}

				ISOMsg outIsoMsg = (ISOMsg) isoMsg.clone(new int[] { 0, 7, 11, 15, 32, 33, 39, 70 });
				outIsoMsg.set(128, "0000000000000000");
				outputMessage = new Message(MessageType.OUTGOING);
				outputMessage.setTransaction(refTransaction);
				outputMessage.setChannel(((InputChannel) refInputMsg.getChannel()).getOriginatorChannel());
				outputMessage.setProtocolMessage(outIsoMsg);
				outputMessage.setEndPointTerminal(refInputMsg.getEndPointTerminal());
				outputMessage.setIfx(MsgProcessor.processor(incomingMessage.getIfx()));
				outputMessage.setRequest(false);
				outputMessage.setNeedResponse(false);
				outputMessage.setNeedToBeInstantlyReversed(false);
				outputMessage.setNeedToBeSent(true);

				ProtocolToXmlUtils.setXMLdata(outputMessage);
				GeneralDao.Instance.saveOrUpdate(outputMessage.getMsgXml());
				refTransaction.setDebugTag("CUTOVER_RS");
//				refTransaction.setAuthorized(true);
			}
			
		} else {
			Thread.sleep(12000);
			// MASTER
			
//        List<Terminal> terminals = TerminalService.findTerminals(institution);
			
			Terminal terminal = null;
			if (ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_87 == isoMsg.getMTI()
					||	ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_RESPONSE_87 == isoMsg.getMTI() ) {
				terminal = issuerSwitchTerminal;
			} else if (ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87 == isoMsg.getMTI()
					|| ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_87 == isoMsg.getMTI())
//				terminal = GlobalContext.getInstance().getAcquierSwitchTerminal(entityCode);
				terminal = ProcessContext.get().getAcquierSwitchTerminal(entityCode);
			
//        for (Terminal terminal : terminals) {
			ISOMsg outIsoMsg = (ISOMsg) getReconcilement().buildRequest(terminal);
			outputMessage = createOutputMessage(outIsoMsg, incomingMessage, refTransaction, issuerSwitchTerminal);
			outputMessage.setProtocolMessage(outIsoMsg);
			outputMessage.setIfx(createOutgoingIfx(outputMessage, terminal, incomingMessage.getIfx().getSettleDt()));
			outputMessage.setRequest(true);
			outputMessage.setNeedResponse(true);
			outputMessage.setNeedToBeInstantlyReversed(false);
			outputMessage.setNeedToBeSent(true);
//        }
			refTransaction.setDebugTag("RECONCILMENT_RQ's");
//			refTransaction.setAuthorized(true);
		}
		
		if (outputMessage != null) {
			GeneralDao.Instance.saveOrUpdate(outputMessage.getIfx());
			GeneralDao.Instance.saveOrUpdate(outputMessage);
			GeneralDao.Instance.saveOrUpdate(outputMessage.getMsgXml());
			refTransaction.addOutputMessage(outputMessage);
		}
    }

    @Override
    public ClearingJob preJob() throws Exception {
        return BindISOCutOverResponse.class.newInstance();
    }


    private Ifx createOutgoingIfx(Message message, Terminal terminal, MonthDayDate stlDate) throws ParseException {
        ISOMsg protocolMessage = (ISOMsg) message.getProtocolMessage();
        Ifx ifx = new Ifx();
        ifx.setIfxType(getIfxType(terminal));
        ifx.setIfxDirection(IfxDirection.OUTGOING);
        ifx.setTerminalType(TerminalType.SWITCH);
        ifx.setTerminalId(terminal.getId().toString());
        ifx.setReceivedDt(message.getStartDateTime());
        ifx.setSettleDt(stlDate);
        ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.getString(11)));
        ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.getString(11)));
//        ifx.setSettleDt(new MonthDayDate(MMdd.parse((protocolMessage).getString(15))));
        ifx.setBankId(protocolMessage.getString(99));
        if (terminal.getClearingMode().equals(TerminalClearingMode.ACQUIER)){
            ifx.setFwdBankId(protocolMessage.getString(32));
            ifx.setDestBankId(protocolMessage.getString(32));
        }else if (terminal.getClearingMode().equals(TerminalClearingMode.ISSUER)){
            ifx.setFwdBankId(protocolMessage.getString(33));
            ifx.setDestBankId(protocolMessage.getString(33));
        }
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
        
        ifx.setOrigDt(DateTime.now());
        return ifx;
    }
    
    protected Ifx createIncommingIfx(Message message) throws Exception {
    	ISOMsg isoMsg = (ISOMsg) message.getProtocolMessage();
    	Ifx ifx = new Ifx();
    	Ifx refIfx = message.getTransaction().getFirstTransaction().getOutgoingIfx()/*getOutputMessage().getIfx()*/;

    	if (IfxType.CUTOVER_RQ.equals(refIfx.getIfxType()))
    		ifx.setIfxType( IfxType.CUTOVER_RS);
    	else if (IfxType.CUTOVER_REPEAT_RQ.equals(refIfx.getIfxType()))
    		ifx.setIfxType( IfxType.CUTOVER_REPEAT_RS);
    	
		MonthDayDate settleDt = refIfx.getSettleDt();
        ifx.setSettleDt(settleDt); 
        ifx.setReceivedDt(message.getStartDateTime());
        ifx.setIfxDirection(IfxDirection.INCOMING);
        ifx.setSrc_TrnSeqCntr( ISOUtil.zeroUnPad(isoMsg.getString(11)));
        ifx.setMy_TrnSeqCntr( ISOUtil.zeroUnPad(isoMsg.getString(11)));
        ifx.setFwdBankId(isoMsg.getString(33));
        ifx.setDestBankId(isoMsg.getString(33));
        ifx.setBankId(isoMsg.getString(32));
        ifx.setRsCode(isoMsg.getString(39));
        ifx.setTerminalType(TerminalType.SWITCH);
	 ifx.setOrigDt(DateTime.now());
        return ifx;
    }
    
    
    private IfxType getIfxType(Terminal terminal) {
		if (terminal.getClearingMode().equals(TerminalClearingMode.ISSUER))
            return IfxType.CARD_ISSUER_REC_RQ;
        else
            return IfxType.ACQUIRER_REC_RQ;
	}

	@Override
	protected TerminalClearingMode getClearingMode() {
		return TerminalClearingMode.ISSUER;
	}
}
