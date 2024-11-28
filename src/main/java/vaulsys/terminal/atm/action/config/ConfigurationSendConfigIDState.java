package vaulsys.terminal.atm.action.config;

import vaulsys.message.Message;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationSendConfigIDState extends ConfigurationState {
	public static final ConfigurationSendConfigIDState Instance = new ConfigurationSendConfigIDState();

	private ConfigurationSendConfigIDState(){}

    @Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
    	return ConfigurationInServiceState.Instance;
	}

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
    	setDebugTag(inputMessage.getTransaction());
		return null;
    }
}
