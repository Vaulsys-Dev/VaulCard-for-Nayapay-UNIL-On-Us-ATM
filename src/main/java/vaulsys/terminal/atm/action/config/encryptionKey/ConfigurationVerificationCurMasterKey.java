package vaulsys.terminal.atm.action.config.encryptionKey;

import vaulsys.message.Message;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCNetworkToTerminalMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusEncryptorInitialisationDataMsg;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationVerificationCurMasterKey extends BaseConfigurationEncryptionKey {
	public static final ConfigurationVerificationCurMasterKey Instance = new ConfigurationVerificationCurMasterKey();
	
	private ConfigurationVerificationCurMasterKey(){}

	@Override
	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		if(ndcMsg instanceof NDCSolicitedStatusEncryptorInitialisationDataMsg) {
			NDCSolicitedStatusEncryptorInitialisationDataMsg ndcEncInitData = (NDCSolicitedStatusEncryptorInitialisationDataMsg) ndcMsg;
			SecureDESKey masterKey = (SecureDESKey) atm.getKeyByType(KeyType.TYPE_TMK);
			if(ndcEncInitData.getMasterKeyKVV().equals(masterKey.getKeyCheckValue()))
				return ConfigurationUpdatingMasterKey.Instance;
			
			atm.setATMState(ATMState.INVALID_MASTER_KEY);
		}

		return ConfigurationInvalidMasterKey.Instance;
	}

	@Override
	public NDCNetworkToTerminalMsg genNetToTermMsg(Long luno) {
		return ATMTerminalService.generateExtEncKeyChngMsg_acquireAllKVV(luno);
	}

	@Override
	public IfxType getIfxType() {
		return IfxType.ATM_GET_ALL_KVV;
	}
}
