package vaulsys.terminal.atm.action.supervisor;

import vaulsys.message.Message;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.action.ActionInitializer;
import vaulsys.terminal.atm.action.config.ConfigurationHardwareConfigState;
import vaulsys.terminal.atm.action.isolated.DeviceLocationState;
import vaulsys.terminal.impl.ATMTerminal;

public abstract class SensorState extends AbstractState {

    @Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
        
    	if (ActionInitializer.isSensorChange(ndcMsg)) {
    		inputMessage.getIfx().setIfxType(IfxType.DEVICE_LOCATION);
            return DeviceLocationState.Instance;
        }
        if (ActionInitializer.isSupervisorEntry(ndcMsg)) {
        	inputMessage.getIfx().setIfxType(IfxType.SUPERVISOR_ENTRY);
            return SupervisorEntryState.Instance;
        }
        if (ActionInitializer.isSupervisorExit(ndcMsg)) {
        	inputMessage.getIfx().setIfxType(IfxType.SUPERVISOR_EXIT);
            return SupervisorExitState.Instance;
        }
        if (ActionInitializer.isSupplyCounterResponse(ndcMsg)) {
        	inputMessage.getIfx().setIfxType(IfxType.ATM_SUPPLY_COUNTER_REQUEST);
            return SupervisorSupplyCounterState.Instance;
        }
        if (ActionInitializer.isConfigurationInfo(ndcMsg)) {
        	inputMessage.getIfx().setIfxType(IfxType.CONFIG_INFO_RESPONSE);
            return ConfigurationHardwareConfigState.Instance;
        }
    	if (ActionInitializer.isReadyMsg(ndcMsg)) {
    		if(this instanceof SensorStartingState) {
	    		inputMessage.getIfx().setIfxType(IfxType.ATM_ACKNOWLEDGE);
	            return SensorStartingState.Instance;
    		}
        }
        return SensorStartingState.Instance;
    }
    
    @Override
    protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
    	return null;
    }
}