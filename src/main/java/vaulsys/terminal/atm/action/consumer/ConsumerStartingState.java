package vaulsys.terminal.atm.action.consumer;

import vaulsys.message.Message;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusCommandRejectMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusDeviceFaultMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusMacRejectMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedCashHandlerStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCardReaderWriterDidntTakeCard;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCardReaderWriterFailedEjectCard;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCashHandler;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConsumerStartingState extends ConsumerState {
	public static final ConsumerStartingState Instance = new ConsumerStartingState();

	private ConsumerStartingState(){}

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
            
            /*if (faultMsg.solicitedStatus instanceof NDCReceiptPrinter) {
            	return ConsumerReversalState.getInstance();
            }*/
            
            if (faultMsg.solicitedStatus instanceof NDCCardReaderWriterDidntTakeCard ||
            		faultMsg.solicitedStatus instanceof NDCCardReaderWriterFailedEjectCard) {
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
        
        return ConsumerHoldingState.Instance;
    }

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
//    	setDebugTag(inputMessage.getTransaction());
        return null;
    }
}
