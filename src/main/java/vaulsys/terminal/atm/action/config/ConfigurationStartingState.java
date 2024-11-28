package vaulsys.terminal.atm.action.config;

import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedPowerFailureStatusMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.ATMProducer;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.action.supervisor.SupervisorExitState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationStartingState extends ConfigurationState {
	public static final ConfigurationStartingState Instance = new ConfigurationStartingState();

	private ConfigurationStartingState(){}

	@Override
	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		if (ndcMsg instanceof NDCUnsolicitedPowerFailureStatusMsg) {
			inputMessage.getIfx().setIfxType(IfxType.POWER_FAILURE);
			NDCUnsolicitedPowerFailureStatusMsg msg = (NDCUnsolicitedPowerFailureStatusMsg) ndcMsg;

			Integer configId = Integer.parseInt(msg.statusInformation.configId);
			atm.setConfigId(configId);
			GeneralDao.Instance.saveOrUpdate(atm);
			
			if (atm.getProducer() != null && atm.getProducer().equals(ATMProducer.NCR)) {
				return ConfigurationSendVaulsysNCRFontState.Instance;
			} else {
				if (ATMTerminalService.isNeedToSendConfigData(atm)) {
					ATMTerminalService.prepareProcessForSentConfig(atm);
					return ConfigurationOutOfServiceState.Instance;
				} else {
					ATMTerminalService.prepareProcess(atm);
	//				return ConfigurationInServiceState.getInstance();
					return SupervisorExitState.Instance;
				}
			}
			
		} else
//			return ConfigurationInServiceState.getInstance();
			return SupervisorExitState.Instance;
	}

	@Override
	protected Message process(Message inputMessage, ATMTerminal atm) {
		setDebugTag(inputMessage.getTransaction());
		return null;
	}
}
