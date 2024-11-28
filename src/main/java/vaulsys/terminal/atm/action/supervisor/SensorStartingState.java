package vaulsys.terminal.atm.action.supervisor;

import vaulsys.message.Message;
import vaulsys.terminal.impl.ATMTerminal;

public class SensorStartingState extends SensorState {
	public static final SensorStartingState Instance = new SensorStartingState();

	private SensorStartingState(){}

//	@Override
//	public AbstractState nextState(Message inputMessage) {
//		return this.Instance;
//	}
    
//    public IActionState nextState(NDCMsg inputMessage) {
//    	return ConfigurationInServiceState.getInstance();
//    	 return this;
//		if (inputMessage instanceof NDCUnsolicitedSensorsStatusMsg 
//				|| inputMessage instanceof NDCUnsolicitedPowerFailureStatusMsg
//				) {
			
			/*** implement this! ***/
//			updateStatus(atm);
			
//			NDCUnsolicitedPowerFailureStatusMsg msg = (NDCUnsolicitedPowerFailureStatusMsg) inputMessage;
//			ATMTerminal atm = getTerminalService().findTerminal(ATMTerminal.class, inputMessage.getLogicalUnitNumber());

//			if (/*true || */atm.getOwnOrParentConfiguration().getConfigID().equals(0L/*Long.parseLong(msg.statusInformation.configId)*/)) {
//				prepareProcess(atm);
//				return ConfigurationInServiceState.getInstance();
//			} 
//			else
				//TODO Only for test!!!
//				return ConfigurationSendFitState.getInstance();
//			return ConfigurationSendStateState.getInstance();

//			return ConfigurationInServiceState.getInstance();
//		} 
//			else
//			return ConfigurationInServiceState.getInstance();
//	}

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
		/*** implement this! ***/
//		updateStatus(atm);
		
		setDebugTag(inputMessage.getTransaction());
		return null;
	}
}
