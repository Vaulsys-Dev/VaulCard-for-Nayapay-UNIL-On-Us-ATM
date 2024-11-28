package vaulsys.terminal.atm.action.consumer;

import vaulsys.calendar.DateTime;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusCommandRejectMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusDeviceFaultMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusMacRejectMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusReadyMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedCashHandlerStatusMsg;
import vaulsys.protocols.ndc.base.config.TransactionStatusType;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCardReaderWriterDidntTakeCard;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCashHandler;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.device.CassetteA;
import vaulsys.terminal.atm.device.CassetteB;
import vaulsys.terminal.atm.device.CassetteC;
import vaulsys.terminal.atm.device.CassetteD;
import vaulsys.terminal.atm.device.Retract;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.transaction.ClearingState;

public class ConsumerCashHandlerState extends ConsumerState {
	public static final ConsumerCashHandlerState Instance = new ConsumerCashHandlerState();

	private ConsumerCashHandlerState(){}

    @Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
        if (ndcMsg instanceof NDCSolicitedStatusDeviceFaultMsg) {
        	
            NDCSolicitedStatusDeviceFaultMsg faultMsg = (NDCSolicitedStatusDeviceFaultMsg) ndcMsg;
            if (faultMsg.solicitedStatus instanceof NDCCashHandler) {
            	NDCCashHandler cashHandler = (NDCCashHandler) faultMsg.solicitedStatus;
            	inputMessage.getIfx().setTransactionStatus(cashHandler.transactionStatus);
            	inputMessage.getIfx().setIfxType(IfxType.CASH_HANDLER);
                return ConsumerReversalState.Instance;
            }
            
            if (faultMsg.solicitedStatus instanceof NDCCardReaderWriterDidntTakeCard) {
            	inputMessage.getIfx().setIfxType(IfxType.CARD_READER_WRITER);
            	return ConsumerReversalState.Instance;
            }
        }

        if (ndcMsg instanceof NDCUnsolicitedCashHandlerStatusMsg) {
        	inputMessage.getIfx().setIfxType(IfxType.CASH_HANDLER);
            return ConsumerCashHandlerState.Instance;
        }

        if (ndcMsg instanceof NDCSolicitedStatusMacRejectMsg) {
        	inputMessage.getIfx().setIfxType(IfxType.MAC_REJECT);
            return ConsumerReversalState.Instance;
        }

        if (ndcMsg instanceof NDCSolicitedStatusCommandRejectMsg) {
        	inputMessage.getIfx().setIfxType(IfxType.COMMAND_REJECT);
            return ConsumerReversalState.Instance;
        }
        
        if (ndcMsg instanceof NDCSolicitedStatusReadyMsg)
        	return ConsumerHoldingState.Instance;
        
        return ConsumerEndState.Instance;
    }

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm){
    	inputMessage.getIfx().setIfxType(IfxType.CASH_HANDLER);
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
        NDCUnsolicitedCashHandlerStatusMsg faultMsg = (NDCUnsolicitedCashHandlerStatusMsg) ndcMsg;
        NDCCashHandler cashHandler = faultMsg.statusInformation;
        inputMessage.getIfx().setTransactionStatus(cashHandler.transactionStatus);
        
        Ifx ifx = atm.getLastTransaction().getOutgoingIfx()/*getOutputMessage().getIfx()*/;
        int[] currentDispenseNotes = NDCParserUtils.parseDispensedNote(ifx.getCurrentDispense(), 2);
        
        if (TransactionStatusType.SUCCESSFUL_OPERATION.equals(cashHandler.transactionStatus)) {
        	//Note: we call ConsumerHoldingState upon receiving atm_confirmation message! so cassettes will be updated at that time
        	//return ConsumerHoldingState.getInstance().process(inputMessage);
        	return null;
        } else if (TransactionStatusType.SOME_NOTES_RETRACTED.equals(cashHandler.transactionStatus)){
        	/*
        	 * NOTE: 1389/2/15
        	 * I guess the atm confirmation message must have been received before! this means that cassettes have been updated before.
        	 * So we only change the clearingFlag of withdrawal message!
        	*/
        	if (atm.getLastTransaction().getFirstTransaction().getSourceClearingInfo()!= null){
        		atm.getLastTransaction().getFirstTransaction().getSourceClearingInfo().setClearingState(ClearingState.NOT_NOTE_SUCCESSFULLY_DISPENSED);
        		atm.getLastTransaction().getFirstTransaction().getSourceClearingInfo().setClearingDate(DateTime.now());
        		GeneralDao.Instance.saveOrUpdate(atm.getLastTransaction().getFirstTransaction().getSourceClearingInfo());
        	}
        }

        CassetteA casseteA = atm.getDevice(CassetteA.class);
        casseteA.increaseDispensedNotes(currentDispenseNotes[0] - cashHandler.notesDispensed[0]);
        casseteA.setNotesRejected(casseteA.getNotesRejected() + cashHandler.notesDispensed[0]);
        
        CassetteB casseteB = atm.getDevice(CassetteB.class);
        casseteB.increaseDispensedNotes(currentDispenseNotes[1] - cashHandler.notesDispensed[1]);
        casseteB.setNotesRejected(casseteB.getNotesRejected() + cashHandler.notesDispensed[1]);
        
        CassetteC casseteC = atm.getDevice(CassetteC.class);
        casseteC.increaseDispensedNotes(currentDispenseNotes[2] - cashHandler.notesDispensed[2]);
        casseteC.setNotesRejected(casseteC.getNotesRejected() + cashHandler.notesDispensed[2]);
        
        CassetteD casseteD = atm.getDevice(CassetteD.class);
        casseteD.increaseDispensedNotes(currentDispenseNotes[3] - cashHandler.notesDispensed[3]);
        casseteD.setNotesRejected(casseteD.getNotesRejected() + cashHandler.notesDispensed[3]);
        
        Retract retract = atm.getDevice(Retract.class);
        int retractNotes = cashHandler.notesDispensed[0]
                + cashHandler.notesDispensed[1]
                + cashHandler.notesDispensed[2]
                + cashHandler.notesDispensed[3];
        retract.increseNotes(retractNotes);

        inputMessage.getIfx().setActualDispenseCaset1(cashHandler.notesDispensed[0]);
        inputMessage.getIfx().setActualDispenseCaset2(cashHandler.notesDispensed[1]);
        inputMessage.getIfx().setActualDispenseCaset3(cashHandler.notesDispensed[2]);
        inputMessage.getIfx().setActualDispenseCaset4(cashHandler.notesDispensed[3]);
        
		return null;
    }
}