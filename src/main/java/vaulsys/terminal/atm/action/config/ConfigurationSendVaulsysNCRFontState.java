package vaulsys.terminal.atm.action.config;

import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCNetworkToTerminalMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCOperationalMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.action.supervisor.SensorState;
import vaulsys.terminal.atm.action.supervisor.SupervisorExitState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationSendVaulsysNCRFontState extends SensorState {
	public static final ConfigurationSendVaulsysNCRFontState Instance = new ConfigurationSendVaulsysNCRFontState();

	private ConfigurationSendVaulsysNCRFontState(){}

	@Override
	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
		if (ATMTerminalService.isNeedToSendConfigData(atm)) {
			ATMTerminalService.prepareProcessForSentConfig(atm);
			return ConfigurationOutOfServiceState.Instance;
		
		} else {
			ATMTerminalService.prepareProcess(atm);
//			return ConfigurationInServiceState.getInstance();
			return SupervisorExitState.Instance;
		}
	}

	
	@Override
	protected Message process(Message inputMessage, ATMTerminal atm) {
		setDebugTag(inputMessage.getTransaction());
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		NDCNetworkToTerminalMsg protocolMessage = ATMTerminalService.generateSupplyCountersMessage(ndcMsg.getLogicalUnitNumber());
		((NDCOperationalMsg) protocolMessage).doPrintImmediate = true;

		Message outMsg = new Message(MessageType.OUTGOING);
		outMsg.setProtocolMessage(protocolMessage);
		outMsg.setTransaction(inputMessage.getTransaction());
		outMsg.setIfx(createOutgoingIfx(outMsg, atm));
		
		return outMsg;
	}
	
	@Override
    protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
		NDCOperationalMsg protocolMessage = (NDCOperationalMsg) outputMsg.getProtocolMessage();
		Ifx ifx = new Ifx();
		ifx.setIfxType(IfxType.ATM_ACKNOWLEDGE);
		ifx.setTerminalType(TerminalType.ATM);
		ifx.setTerminalId(((NDCMsg) outputMsg.getProtocolMessage()).getLogicalUnitNumber().toString());
		ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.messageSequenceNumber));
		ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.messageSequenceNumber));
		ifx.setIfxDirection(IfxDirection.OUTGOING);
		ifx.setReceivedDt(outputMsg.getStartDateTime());
		return ifx;	
    }
}
