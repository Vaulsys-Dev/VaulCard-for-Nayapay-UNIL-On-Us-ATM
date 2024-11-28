package vaulsys.terminal.atm.action;

import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCConsumerRequestMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusConfigTerminalStateMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusEncryptorInitialisationDataMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusFitnessDataResponseMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusReadyMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusSuppliesDataResponseMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusSupplyCounterMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedCardReaderWriterStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedEncryptorStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedJournalPrinterStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedPowerFailureStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedReceiptPrinterStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedSensorsStatusMsg;
import vaulsys.terminal.atm.action.config.ConfigurationFitnessState;
import vaulsys.terminal.atm.action.config.ConfigurationStartingState;
import vaulsys.terminal.atm.action.config.ConfigurationSupplyDataState;
import vaulsys.terminal.atm.action.consumer.ConsumerCannotDispenceState;
import vaulsys.terminal.atm.action.isolated.CardReaderWriterState;
import vaulsys.terminal.atm.action.isolated.EncryptorState;
import vaulsys.terminal.atm.action.isolated.JournalPrinterState;
import vaulsys.terminal.atm.action.isolated.ReceiptPrinterState;
import vaulsys.terminal.atm.action.supervisor.SensorStartingState;
import vaulsys.terminal.atm.action.supervisor.SupervisorSupplyCounterState;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.NotUsed;

public class ActionInitializer {

    private static AbstractState guessAction(NDCMsg ndcMsg, ATMTerminal terminal) {
    	//TODO Only for test!!
        if (isPowerupMessage(ndcMsg)) {
            return ConfigurationStartingState.Instance;
        }
        if (isSensorMsg(ndcMsg)) {
//        	return getConfigurationAction();
            return SensorStartingState.Instance;
        }
        if (isCardReaderWriterMsg(ndcMsg)) {
            return CardReaderWriterState.Instance;
        }
        if (isReceiptPrinterMsg(ndcMsg)) {
            return ReceiptPrinterState.Instance;
        }
        if (isJournalPrinterMsg(ndcMsg)) {
            return JournalPrinterState.Instance;
        }
        if (isEncryptorMsg(ndcMsg)) {
            return EncryptorState.Instance;
        }
        if (isFitnessDataResponseMsg(ndcMsg)) {
        	return ConfigurationFitnessState.Instance;
        }
        if (isSuppliesDataResponseMsg(ndcMsg)) {
        	return ConfigurationSupplyDataState.Instance;
        }
        
        if (isSupplyCounterResponse(ndcMsg)) {
        	return SupervisorSupplyCounterState.Instance;
        }
//        if (isEncryptorInitioalizationMsg(ndcMsg)) {
//        	return ConfigurationUpdatingPINKey.Instance;
//        }

        return null;
    }

    private static boolean isCardReaderWriterMsg(NDCMsg ndcMsg) {
        return ndcMsg instanceof NDCUnsolicitedCardReaderWriterStatusMsg;
    }

    private static boolean isFitnessDataResponseMsg(NDCMsg ndcMsg) {
    	return ndcMsg instanceof NDCSolicitedStatusFitnessDataResponseMsg;
    }
    
    private static boolean isSuppliesDataResponseMsg(NDCMsg ndcMsg) {
    	return ndcMsg instanceof NDCSolicitedStatusSuppliesDataResponseMsg;
    }
    
    private static boolean isReceiptPrinterMsg(NDCMsg ndcMsg) {
        return ndcMsg instanceof NDCUnsolicitedReceiptPrinterStatusMsg;
    }

    private static boolean isJournalPrinterMsg(NDCMsg ndcMsg) {
        return ndcMsg instanceof NDCUnsolicitedJournalPrinterStatusMsg;
    }

    private static boolean isEncryptorMsg(NDCMsg ndcMsg) {
        return ndcMsg instanceof NDCUnsolicitedEncryptorStatusMsg;
    }
    
    private static boolean isEncryptorInitioalizationMsg(NDCMsg ndcMsg) {
        return ndcMsg instanceof NDCSolicitedStatusEncryptorInitialisationDataMsg;
    }

    private static boolean isConsumerMsg(NDCMsg ndcMsg) {
        return ndcMsg instanceof NDCConsumerRequestMsg;
    }

    public static AbstractState findAction(NDCMsg ndcMsg, ATMTerminal terminal) {
        if (isConsumerMsg(ndcMsg)) {
            return ConsumerCannotDispenceState.Instance;
        }
        
        AbstractState initialAction = guessAction(ndcMsg, terminal);
               
        AbstractState currentAction = null;
        if (terminal.getCurrentStateClass() != null) {
        	AbstractState action = terminal.getCurrentAbstractStateClass();
            if (!(action instanceof EndState)) {
                currentAction = action;
            }
        }
        if (initialAction != null) {
            if (currentAction == null || !initialAction.equals(currentAction)){
                return initialAction;
            }
        }

        
//        IATMAction initialAction = guessAction(ndcMsg, terminal);
//
//        IATMAction currentAction = null;
//        if (terminal.getCurrentAction() != null) {
//        	IATMAction action = terminal.getCurrentAction();
//            if (action.getState() != null && !(action.getState() instanceof IEndState)) {
//                currentAction = action;
//            }
//        }
//        if (initialAction != null) {
//            if (currentAction == null || !initialAction.getState().getType().equals(currentAction.getState().getType()))
//                return initialAction;
//        }
        return currentAction;
    }

    private static boolean isPowerupMessage(NDCMsg ndcMsg) {
        return ndcMsg instanceof NDCUnsolicitedPowerFailureStatusMsg;
    }

    @NotUsed
    public boolean isDeviceMsg(NDCMsg ndcMsg) {
        return isSensorMsg(ndcMsg) && !isSupervisorEntry(ndcMsg) && !isSupervisorExit(ndcMsg) && !isAlarmStateChange(ndcMsg);
    }

    public static boolean isSupervisorEntry(NDCMsg ndcMsg) {
        if (isSensorMsg(ndcMsg)) {
            NDCUnsolicitedSensorsStatusMsg statusMsg = (NDCUnsolicitedSensorsStatusMsg) ndcMsg;
            return statusMsg.isSupervisorEntry();
        }
        return false;
    }

    public static boolean isSupervisorExit(NDCMsg ndcMsg) {
        if (isSensorMsg(ndcMsg)) {
            NDCUnsolicitedSensorsStatusMsg statusMsg = (NDCUnsolicitedSensorsStatusMsg) ndcMsg;
            return statusMsg.isSupervisorExit();
        }
        return false;
    }

    public static boolean isSensorChange(NDCMsg ndcMsg) {
        if (isSensorMsg(ndcMsg)) {
            NDCUnsolicitedSensorsStatusMsg statusMsg = (NDCUnsolicitedSensorsStatusMsg) ndcMsg;
            return statusMsg.isSensorChange();
        }
        return false;
    }

    public static boolean isReadyMsg(NDCMsg ndcMsg) {
    	return (ndcMsg instanceof NDCSolicitedStatusReadyMsg);
    }

    @NotUsed
    //only used in a NotUsed method 
    public boolean isAlarmStateChange(NDCMsg ndcMsg) {
        if (isSensorMsg(ndcMsg)) {
            NDCUnsolicitedSensorsStatusMsg statusMsg = (NDCUnsolicitedSensorsStatusMsg) ndcMsg;
            return statusMsg.isAlarmStatusChange();
        }
        return false;
    }

    public static boolean isConfigurationInfo(NDCMsg ndcMsg) {
    	return (ndcMsg instanceof NDCSolicitedStatusConfigTerminalStateMsg);
    }
    
    public static boolean isSupplyCounterResponse(NDCMsg ndcMsg) {
        return ndcMsg instanceof NDCSolicitedStatusSupplyCounterMsg;
    }
    
//    public static boolean isCashinStatusInformation(NDCMsg ndcMsg){
//    	return ndcMsg instanceof NDCSolicitedStatusCashInStatusInformation;
//    }

    public static boolean isSensorMsg(NDCMsg ndcMsg) {
        return ndcMsg instanceof NDCUnsolicitedSensorsStatusMsg;
    }


//    public static AbstractState getCosumerAction() {
//        return ConsumerStartingState.getInstance;
//    }
//
//    private static AbstractState getSensorAction() {
//        return SensorStartingState.getInstance;
//    }
}
