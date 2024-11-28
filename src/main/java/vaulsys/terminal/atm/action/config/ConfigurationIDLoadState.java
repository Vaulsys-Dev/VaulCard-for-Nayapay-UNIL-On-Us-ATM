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
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandConfigurationIDLoadMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusReadyMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedPowerFailureStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedSensorsStatusMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationIDLoadState extends ConfigurationState{
	public static final ConfigurationIDLoadState Instance = new ConfigurationIDLoadState();

	private ConfigurationIDLoadState(){}

	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
		NDCMsg msg = (NDCMsg) inputMessage.getProtocolMessage();
		if (msg instanceof NDCSolicitedStatusReadyMsg
				|| msg instanceof NDCUnsolicitedPowerFailureStatusMsg
				|| msg instanceof NDCUnsolicitedSensorsStatusMsg) {
			
			if (msg instanceof NDCUnsolicitedPowerFailureStatusMsg)
				inputMessage.getIfx().setIfxType(IfxType.POWER_FAILURE);
			
			if (msg instanceof NDCUnsolicitedSensorsStatusMsg)
				inputMessage.getIfx().setIfxType(IfxType.SENSOR);
			
			if (msg instanceof NDCSolicitedStatusReadyMsg)
				inputMessage.getIfx().setIfxType(IfxType.ATM_ACKNOWLEDGE);

//TODO: double check this			
//			if (TerminalService.findTerminal(ATMTerminal.class, msg.getLogicalUnitNumber()).getCurrentStateClass().equals(this.Instance))
//			if (TerminalService.findTerminal(ATMTerminal.class, msg.getLogicalUnitNumber()).getCurrentAction().getState().getType().equals(getInstance().getType()))
				return ConfigurationInServiceState.Instance;
//			else
//				return ConfigurationStartupState.Instance;
		}
		return ConfigurationOutOfServiceState.Instance;
	}

	@Override
	protected Message process(Message inputMessage, ATMTerminal atm) {
		setDebugTag(inputMessage.getTransaction());
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
//		ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, ndcMsg.getLogicalUnitNumber());
		NDCMsg msg = ATMTerminalService.generateConfigIdLoadMessage(ndcMsg, ATMTerminalService.getMaxCustomizationDataConfigId(atm));

		Message outMsg = new Message(MessageType.OUTGOING);
	    outMsg.setProtocolMessage(msg);
	    outMsg.setTransaction(inputMessage.getTransaction());
		outMsg.setIfx(createOutgoingIfx(outMsg, atm));
         
		return outMsg;
	}
	
	@Override
	protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
		Ifx ifx = new Ifx();
		ifx.setIfxType(IfxType.ATM_CONFIG_ID_LOAD);
		NDCWriteCommandConfigurationIDLoadMsg protocolMessage = (NDCWriteCommandConfigurationIDLoadMsg)outputMsg.getProtocolMessage();
		ifx.setTerminalId(protocolMessage.getLogicalUnitNumber().toString());
		ifx.setTerminalType(TerminalType.ATM);
		ifx.setOrigDt(DateTime.now());
		ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.messageSequenceNumber));
		ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.messageSequenceNumber));
		ifx.setIfxDirection(IfxDirection.OUTGOING);
		ifx.setReceivedDt(outputMsg.getStartDateTime());
		return ifx;
	}
}
