package vaulsys.terminal.atm.action.config.encryptionKey;

import vaulsys.message.Message;
import vaulsys.terminal.atm.action.EndState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationInvalidMasterKey extends EndState {
	public static final ConfigurationInvalidMasterKey Instance = new ConfigurationInvalidMasterKey();
	
	private ConfigurationInvalidMasterKey(){}
	
	@Override
	protected Message process(Message inputMessage, ATMTerminal atm) {
		setDebugTag(inputMessage.getTransaction());
		return null;
	}
}
