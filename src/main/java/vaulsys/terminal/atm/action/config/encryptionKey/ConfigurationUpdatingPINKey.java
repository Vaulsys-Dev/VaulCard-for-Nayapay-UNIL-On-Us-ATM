package vaulsys.terminal.atm.action.config.encryptionKey;

import vaulsys.message.Message;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCNetworkToTerminalMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.action.config.ConfigurationEndingState;
import vaulsys.terminal.atm.action.config.ConfigurationInServiceState;
import vaulsys.terminal.atm.action.config.ConfigurationUpdatingTimeState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationUpdatingPINKey extends BaseConfigurationEncryptionKey {
	public static final ConfigurationUpdatingPINKey Instance = new ConfigurationUpdatingPINKey();
	
	private ConfigurationUpdatingPINKey(){}

	@Override
	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
		return ConfigurationUpdatingMACKey.Instance;
//		return ConfigurationInServiceState.Instance;
	}
	
	@Override
	public NDCNetworkToTerminalMsg genNetToTermMsg(Long luno) {
		return ATMTerminalService.generateExtEncKeyChngMsg_PINByMaster(luno);
	}
	
	@Override
	public IfxType getIfxType() {
		return IfxType.PIN_KEY_CHANGE_RQ;
	}
}
