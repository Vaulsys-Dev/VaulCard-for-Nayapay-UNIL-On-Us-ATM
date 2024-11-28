package vaulsys.terminal.atm.action.config;

import vaulsys.calendar.DateTime;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCOperationalMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCSolicitedStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusSendConfigIDResponseMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedSensorsStatusMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.action.supervisor.SensorState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationHardwareConfigState extends SensorState {
	public static final ConfigurationHardwareConfigState Instance = new ConfigurationHardwareConfigState();

	private ConfigurationHardwareConfigState(){}

	@Override
	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		
		if (ndcMsg instanceof NDCSolicitedStatusSendConfigIDResponseMsg) {
			inputMessage.getIfx().setIfxType(IfxType.CONFIG_ID_RESPONSE);
			NDCSolicitedStatusSendConfigIDResponseMsg msg = (NDCSolicitedStatusSendConfigIDResponseMsg) ndcMsg;

			atm.setConfigId(Integer.parseInt(msg.configId));

			if (ATMTerminalService.isNeedToSendConfigData(atm)) {
				ATMTerminalService.prepareProcessForSentConfig(atm);
				return ConfigurationOutOfServiceState.Instance;

			} else {
				ATMTerminalService.prepareProcess(atm);
//				if(TerminalStatus.NOT_INSTALL.equals(atm.getStatus()))
//					return ConfigurationVerificationCurMasterKey.Instance;
//				else
					return ConfigurationUpdatingTimeState.Instance;
//				return ConfigurationInServiceState.getInstance();
			}
		}
//		} else if (ndcMsg instanceof NDCSolicitedStatusMsg) {
//			 return ConfigurationHardwareConfigState.Instance;
//		} else if (ndcMsg instanceof NDCSolicitedStatusConfigTerminalStateMsg) {
//			 return ConfigurationHardwareConfigState.Instance;
//		}else{
////			return ConfigurationInServiceState.getInstance();
//			return ConfigurationUpdatingTimeState.Instance;
//		}
		 return ConfigurationHardwareConfigState.Instance;
	}
	
	@Override
	protected Message process(Message inputMessage, ATMTerminal atm) {
		setDebugTag(inputMessage.getTransaction());
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		if(ndcMsg instanceof NDCUnsolicitedSensorsStatusMsg)
			return null;
		
		inputMessage.getIfx().setIfxType(IfxType.CONFIG_INFO_RESPONSE);
		
		// handle response of supplycounter
		Long luno = ndcMsg.getLogicalUnitNumber();
		((NDCSolicitedStatusMsg) ndcMsg).updateStatus(atm);
//		atm.setATMState(ATMState.IN_SERIVCE);
		
		Message outMsg = new Message(MessageType.OUTGOING);
		outMsg.setProtocolMessage(ATMTerminalService.generateSendConfigIDMessage(luno));
		outMsg.setTransaction(inputMessage.getTransaction());
		outMsg.setIfx(createOutgoingIfx(outMsg, atm));
		return outMsg;
	}
	
	@Override
    protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
		NDCOperationalMsg protocolMessage = (NDCOperationalMsg) outputMsg.getProtocolMessage();
		Ifx ifx = new Ifx();
		ifx.setIfxType(IfxType.ATM_SEND_CONFIG_ID);
		ifx.setTerminalType(TerminalType.ATM);
		ifx.setOrigDt(DateTime.now());
		ifx.setTerminalId(((NDCMsg) outputMsg.getProtocolMessage()).getLogicalUnitNumber().toString());
		ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.messageSequenceNumber));
		ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.messageSequenceNumber));
		ifx.setIfxDirection(IfxDirection.OUTGOING);
		ifx.setReceivedDt(outputMsg.getStartDateTime());
		return ifx;	
    }
}
