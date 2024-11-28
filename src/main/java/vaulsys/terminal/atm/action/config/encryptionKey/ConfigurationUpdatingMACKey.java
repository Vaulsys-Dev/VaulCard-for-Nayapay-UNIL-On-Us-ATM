package vaulsys.terminal.atm.action.config.encryptionKey;

import vaulsys.message.Message;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCNetworkToTerminalMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.action.config.ConfigurationInServiceState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationUpdatingMACKey extends BaseConfigurationEncryptionKey {
	public static final ConfigurationUpdatingMACKey Instance = new ConfigurationUpdatingMACKey();
	
	private ConfigurationUpdatingMACKey(){}

	@Override
	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
		return ConfigurationInServiceState.Instance;
//		return ConfigurationUpdatingPINKey.Instance;
	}
	
	@Override
	public NDCNetworkToTerminalMsg genNetToTermMsg(Long luno) {
		return ATMTerminalService.generateExtEncKeyChngMsg_MACByMaster(luno);
	}
	
	@Override
	public IfxType getIfxType() {
		return IfxType.MAC_KEY_CHANGE_RQ;
	}
}
