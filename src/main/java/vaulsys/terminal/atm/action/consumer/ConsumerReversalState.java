package vaulsys.terminal.atm.action.consumer;

import vaulsys.calendar.DateTime;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCFunctionCommandMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusDeviceFaultMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusMacRejectMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedCashHandlerStatusMsg;
import vaulsys.protocols.ndc.base.config.TransactionStatusType;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCardReaderWriterDidntTakeCard;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCardReaderWriterFailedEjectCard;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCashHandler;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;
import vaulsys.protocols.ndc.encoding.NDCConvertor;
import vaulsys.protocols.ndc.parsers.NDCFunctionCommandMapper;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.ATMRequest;
import vaulsys.terminal.atm.ATMResponse;
import vaulsys.terminal.atm.FunctionCommandResponse;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.device.Cassette;
import vaulsys.terminal.atm.device.CassetteA;
import vaulsys.terminal.atm.device.CassetteB;
import vaulsys.terminal.atm.device.CassetteC;
import vaulsys.terminal.atm.device.CassetteD;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.SourceDestination;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class ConsumerReversalState extends ConsumerState {
	public static final ConsumerReversalState Instance = new ConsumerReversalState();

	private ConsumerReversalState(){}

    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
        return ConsumerEndState.Instance;
    }

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm){
    	setDebugTag(inputMessage.getTransaction());
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
//        int[] noteDispensed = new int[4];
        long amountDispensed = 0;
//        String noteDispensedS = "";
//        NDCFunctionCommandMsg functionCommandMsg = null;
        boolean isNeededReturnResponseToAtm = true;
        boolean isNeededReversetrx = true;

        Cassette cassetteA = atm.getDevice(CassetteA.class);
        Cassette cassetteB = atm.getDevice(CassetteB.class);
        Cassette cassetteC = atm.getDevice(CassetteC.class);
        Cassette cassetteD = atm.getDevice(CassetteD.class);
        String cause = ISOResponseCodes.APPROVED;
        
        Transaction lastTransaction = atm.getLastTransaction();
        Transaction trxForRev = lastTransaction.getFirstTransaction();
        Message outMessage = lastTransaction.getOutputMessage();
        Message outMsgRef = null;
        outMsgRef = trxForRev.getOutputMessage();
        if (lastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/ != null && IfxType.PARTIAL_DISPENSE_RQ.equals(lastTransaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType())) {
        	outMsgRef = lastTransaction.getReferenceTransaction().getOutputMessage();
        	trxForRev = lastTransaction.getReferenceTransaction();
        }
        Ifx ifx = outMessage.getIfx();
        
        if (ndcMsg instanceof NDCSolicitedStatusMacRejectMsg) {
            NDCSolicitedStatusMacRejectMsg msg = (NDCSolicitedStatusMacRejectMsg) ndcMsg;
//            noteDispensed = new int[]{0, 0, 0, 0};
//            functionCommandMsg = null;
            isNeededReturnResponseToAtm = false;
//            isNeededReversetrx = false;
            
            //TODO reverse mac reject or not??!!
            if (!ISOFinalMessageType.isWithdrawalOrPartialMessage(ifx.getIfxType()))
            	return null;
            else{
            	Transaction realTransaction = atm.getLastRealTransaction();
            	if (realTransaction != null && 
            			realTransaction.getBeginDateTime().getDateTimeLong() > lastTransaction.getBeginDateTime().getDateTimeLong()) {
            		logger.info("last transaction is special: " + realTransaction.getId() + ", no action by receiving mac reject!");
            		return null;
            		
            	}
            	
            	logger.info("mac reject is received, put SUSPECTED_DISAGREEMENT flag to last transaction: " + lastTransaction.getId());
            	TransactionService.putDesiredFlagForNormalTransaction(trxForRev, inputMessage.getIfx(), new SourceDestination[] { SourceDestination.SOURCE }, 
            			ClearingState.SUSPECTED_DISAGREEMENT);
//            	if (trxForRev.getSourceClearingInfo()!= null) {
//					trxForRev.getSourceClearingInfo().setClearingState(ClearingState.SUSPECTED_DISAGREEMENT);
//					trxForRev.getSourceClearingInfo().setClearingDate(DateTime.now());
//				}
				return null;
            }
        } else if (ndcMsg instanceof NDCSolicitedStatusDeviceFaultMsg) {
			NDCSolicitedStatusDeviceFaultMsg faultMsg = (NDCSolicitedStatusDeviceFaultMsg) ndcMsg;
			
			if (faultMsg.solicitedStatus instanceof NDCCashHandler) {
				NDCCashHandler cashHandler = (NDCCashHandler) faultMsg.solicitedStatus;
//				noteDispensed = cashHandler.notesDispensed;
//				noteDispensedS = cashHandler.notesDispensedS;

				amountDispensed += cashHandler.notesDispensed[0] * cassetteA.getDenomination();
				amountDispensed += cashHandler.notesDispensed[1] * cassetteB.getDenomination();
				amountDispensed += cashHandler.notesDispensed[2] * cassetteC.getDenomination();
				amountDispensed += cashHandler.notesDispensed[3] * cassetteD.getDenomination();

				if (TransactionStatusType.NOTES_DISPENSED_UNKNOWN.equals(cashHandler.transactionStatus)) {
					int[] notes = NDCParserUtils.parseDispensedNote(ifx.getCurrentDispense(), 2);
					cassetteA.setNotesRetracted(cassetteA.getNotesRetracted() + notes[0]);
					cassetteB.setNotesRetracted(cassetteB.getNotesRetracted()+ notes[1]);
					cassetteC.setNotesRetracted(cassetteC.getNotesRetracted()+ notes[2]);
					cassetteD.setNotesRetracted(cassetteD.getNotesRetracted()+ notes[3]);
					
					cause = ATMErrorCodes.ATM_CACH_HANDLER + "";
					trxForRev.getSourceClearingInfo().setClearingState(ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED);
					trxForRev.getSourceClearingInfo().setClearingDate(DateTime.now());
					
//					ATMTerminalService.updateTerminalStatus(atm, cashHandler);
					
					isNeededReversetrx = false;
//					return null;
					
				} 
				/*else if (TransactionStatusType.SOME_NOTES_RETRACTED.equals(cashHandler.transactionStatus)) {
					int[] notes = NDCParserUtils.parseDispensedNote(ifx.getCurrentDispense(), 2);
					cassetteA.setNotesRetracted(cassetteA.getNotesRetracted() + notes[0]);
					cassetteB.setNotesRetracted(cassetteB.getNotesRetracted()+ notes[1]);
					cassetteC.setNotesRetracted(cassetteC.getNotesRetracted()+ notes[2]);
					cassetteD.setNotesRetracted(cassetteD.getNotesRetracted()+ notes[3]);
					
					cause = ATMErrorCodes.ATM_CACH_HANDLER + "";
					trxForRev.getSourceClearingInfo().setClearingState(ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED);
					trxForRev.getSourceClearingInfo().setClearingDate(DateTime.now());
					
				}*/
//				else
					ATMTerminalService.updateTerminalStatus(atm, cashHandler);
				
				
			} else if (faultMsg.solicitedStatus instanceof NDCCardReaderWriterDidntTakeCard ||
					faultMsg.solicitedStatus instanceof NDCCardReaderWriterFailedEjectCard) {
//				noteDispensed = new int[]{0, 0, 0, 0};
				
				int[] notes = NDCParserUtils.parseDispensedNote(ifx.getCurrentDispense(), 2);
				
				cassetteA.increaseRejectedNotes(/*cassetteA.getNotesRejected()+ */notes[0]);
				cassetteB.increaseRejectedNotes(/*cassetteB.getNotesRejected()+ */notes[1]);
				cassetteC.increaseRejectedNotes(/*cassetteC.getNotesRejected()+ */notes[2]);
				cassetteD.increaseRejectedNotes(/*cassetteD.getNotesRejected()+ */notes[3]); 

				cause = ATMErrorCodes.ATM_NO_CARD_REJECTED+"";
			}
		} else if (ndcMsg instanceof NDCUnsolicitedCashHandlerStatusMsg) {
			NDCUnsolicitedCashHandlerStatusMsg faultMsg = (NDCUnsolicitedCashHandlerStatusMsg) ndcMsg;
			NDCCashHandler cashHandler = faultMsg.statusInformation;
			
			amountDispensed += cashHandler.notesDispensed[0] * cassetteA.getDenomination();
			amountDispensed += cashHandler.notesDispensed[1] * cassetteB.getDenomination();
			amountDispensed += cashHandler.notesDispensed[2] * cassetteC.getDenomination();
			amountDispensed += cashHandler.notesDispensed[3] * cassetteD.getDenomination();
			
			if (TransactionStatusType.SOME_NOTES_RETRACTED.equals(cashHandler.transactionStatus)) {
				int[] notes = NDCParserUtils.parseDispensedNote(ifx.getCurrentDispense(), 2);
				cassetteA.setNotesRetracted(cassetteA.getNotesRetracted() + notes[0]);
				cassetteB.setNotesRetracted(cassetteB.getNotesRetracted() + notes[1]);
				cassetteC.setNotesRetracted(cassetteC.getNotesRetracted() + notes[2]);
				cassetteD.setNotesRetracted(cassetteD.getNotesRetracted() + notes[3]);
				
				cause = ATMErrorCodes.ATM_CACH_HANDLER + "";
				trxForRev.getSourceClearingInfo().setClearingState(ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED);
				trxForRev.getSourceClearingInfo().setClearingDate(DateTime.now());
			}
			
//			ATMTerminalService.updateTerminalStatus(atm, cashHandler);
		}

       if (isNeededReversetrx) { 
        
        
        if (!ISOFinalMessageType.isWithdrawalOrPartialMessage(ifx.getIfxType()))
        	amountDispensed = 0L;
        else 
        	amountDispensed += ATMTerminalService.getDispenseAmount(atm, ifx);
        
        if ((ISOFinalMessageType.isWithdrawalOrPartialMessage(ifx.getIfxType()) && amountDispensed <  ifx.getAuth_Amt().longValue()) ||
        		(!ISOFinalMessageType.isWithdrawalOrPartialMessage(ifx.getIfxType()))
        		&& !ISOFinalMessageType.isMessageNotToBeReverse(outMsgRef.getIfx().getIfxType())
        		&& !ISOFinalMessageType.isMessageNotToBeReverse(ifx.getIfxType())) {
        	
        	if ( !ISOFinalMessageType.isPartialDispenseMessage(ifx.getIfxType()) &&
        			trxForRev !=null && 
        			trxForRev.getSourceClearingInfo() != null &&
        			trxForRev.getSourceClearingInfo().getClearingState() != null &&
        			ClearingState.CLEARED.equals(trxForRev.getSourceClearingInfo().getClearingState())) {
        		logger.error("OHOH transacion is cleared before but we want to reverse it!!!!");
        		// do nothing
        	}else{
        		SchedulerService.processReversalJob(trxForRev, lastTransaction, cause, amountDispensed, false);
//	        	LifeCycle lifeCycle = (LifeCycle) GeneralDao.Instance.synchObject(trxForRev.getLifeCycle());
//				lifeCycle.setIsComplete(false);
//				lifeCycle.setIsFullyReveresed(LifeCycleStatus.REQUEST);
//				GeneralDao.Instance.saveOrUpdate(lifeCycle);
//	        	
//				SchedulerService.createReversalJobInfo(trxForRev, cause, amountDispensed);
        	}
        }
        
       }
        
        if (!isNeededReturnResponseToAtm)
        	return null;
        
//        ATMRequest atmRequest = ATMTerminalService.findATMRequest(atm, /*ifx.getOpkey()*/ifx.getProperOpkey());
        ATMRequest atmRequest = ProcessContext.get().getATMRequest(atm.getOwnOrParentConfigurationId(), /*ifx.getOpkey()*/ifx.getProperOpkey());
        ATMResponse response = atmRequest.getAtmResponse(ATMErrorCodes.ATM_CACH_HANDLER);
        if (response == null)
//        	response = atm.getOwnOrParentConfiguration().getResponse(1, ATMErrorCodes.ATM_CACH_HANDLER);
        	response = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId()).getResponse(1, ATMErrorCodes.ATM_CACH_HANDLER);
       
        NDCConvertor convertor = (NDCConvertor) ProcessContext.get().getConvertor(inputMessage.getChannel().getEncodingConverter());
		NDCFunctionCommandMsg msg = NDCFunctionCommandMapper.fromATMResponse(atm, (FunctionCommandResponse) response, convertor);
        
		Message outMsg = new Message(MessageType.OUTGOING);
        outMsg.setProtocolMessage(msg);
        outMsg.setTransaction(inputMessage.getTransaction());
        outMsg.setIfx(createOutgoingIfx(outMsg, atm));
    	return outMsg;
//        return reverseMessage;
    }
    
    @Override
    protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
    	NDCFunctionCommandMsg protocolMessage = (NDCFunctionCommandMsg) outputMsg.getProtocolMessage();
		Ifx ifx = new Ifx();
		ifx.setIfxType(IfxType.CASH_HANDLER_RESPONSE);
		ifx.setTerminalType(TerminalType.ATM);
		ifx.setOrigDt(DateTime.now());
		ifx.setTerminalId(((NDCMsg) outputMsg.getProtocolMessage()).getLogicalUnitNumber()+"");
		ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.transactionSerialNumber));
		ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.transactionSerialNumber));
		if (Util.hasText(protocolMessage.messageSequenceNumber)) {
			ifx.setNetworkRefId(ATMTerminalService.timeVariantToNetworkRefId(protocolMessage.messageSequenceNumber).toString());
		}
		ifx.setIfxDirection(IfxDirection.OUTGOING);
		ifx.setReceivedDt(outputMsg.getStartDateTime());
		
		if (outputMsg.getTransaction().getInputMessage() != null) {
			Ifx inIfx = outputMsg.getTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/;
			ifx.setName(inIfx.getName());
			ifx.setOrgIdNum(inIfx.getOrgIdNum());
			ifx.setOrgIdType(inIfx.getOrgIdType());
			ifx.setBankId(inIfx.getBankId());
			ifx.setDestBankId(inIfx.getDestBankId());
			ifx.setFwdBankId(inIfx.getFwdBankId());
			inIfx.setSrc_TrnSeqCntr(ifx.getSrc_TrnSeqCntr());
			inIfx.setMy_TrnSeqCntr(ifx.getMy_TrnSeqCntr());
			inIfx.setNetworkRefId(ifx.getNetworkRefId());
			GeneralDao.Instance.saveOrUpdate(inIfx);
		}
		
		return ifx;		
    }
}
