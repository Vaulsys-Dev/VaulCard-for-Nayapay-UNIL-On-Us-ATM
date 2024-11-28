package vaulsys.terminal.atm.action.config.encryptionKey;

import vaulsys.message.Message;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCNetworkToTerminalMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationUpdatingMasterKey extends BaseConfigurationEncryptionKey {
	public static final ConfigurationUpdatingMasterKey Instance = new ConfigurationUpdatingMasterKey();
	
	private ConfigurationUpdatingMasterKey(){}

	@Override
	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
		return ConfigurationUpdatingPINKey.Instance;
//		return ConfigurationUpdatingMACKey.Instance;
	}
	
	@Override
	public NDCNetworkToTerminalMsg genNetToTermMsg(Long luno) {
		return ATMTerminalService.generateExtEncKeyChngMsg_newMasterByCurMaster(luno);
	}
	
	@Override
	public IfxType getIfxType() {
		return IfxType.MASTER_KEY_CHANGE_RQ;
	}
}
