package vaulsys.terminal.atm.action.config;

import vaulsys.message.Message;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.atm.action.EndState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationEndingState extends EndState {
	public static final ConfigurationEndingState Instance = new ConfigurationEndingState();

	private ConfigurationEndingState(){}
	
    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
    	setDebugTag(inputMessage.getTransaction());
    	atm.setATMState(ATMState.IN_SERIVCE);
        return null;
    }
}
