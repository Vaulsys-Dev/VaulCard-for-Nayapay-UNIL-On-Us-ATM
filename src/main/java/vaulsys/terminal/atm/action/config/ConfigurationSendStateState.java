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
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandStateTableLoadMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusReadyMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedPowerFailureStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedSensorsStatusMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.constants.CustomizationDataLength;
import vaulsys.terminal.atm.customizationdata.StateData;
import vaulsys.terminal.impl.ATMTerminal;

import java.util.List;

public class ConfigurationSendStateState extends ConfigurationState {
	public static final ConfigurationSendStateState Instance = new ConfigurationSendStateState();

	private ConfigurationSendStateState(){}

	@Override
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
			
			if (isAllStatesSent(msg, atm)){
				if(!ConfigurationSendScreenState.Instance.isAllScreensSent(msg, atm)){
					return ConfigurationSendScreenState.Instance;
				}else{
					return ConfigurationIDLoadState.Instance;
				}
			}
			
			return this.Instance;
		}
		return ConfigurationOutOfServiceState.Instance;
	}

	protected boolean isAllStatesSent(NDCMsg inputMessage, ATMTerminal atm) {
		int lastIndex = atm.getLastSentStateIndex();
		List<StateData> states = ATMTerminalService.getCustomizationDataAfter(atm, StateData.class, atm.getConfigId());

		if (states.size() == lastIndex) {
			atm.setLastSentStateIndex(0);
			return true;
		} else
			return false;
	}

	@Override
	protected Message process(Message inputMessage, ATMTerminal atm){
		setDebugTag(inputMessage.getTransaction());
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		int lastIndex = atm.getLastSentStateIndex();
		List<StateData> states = ATMTerminalService.getCustomizationDataAfter(atm, StateData.class, atm.getConfigId());
		
		if (states == null || states.size() == 0) {
//			atm.setCurrentAbstractStateClass(ConfigurationSendScreenState.Instance);
//			return ConfigurationSendScreenState.Instance.process(inputMessage, atm);
			return null;
		}
		
		int length = Math.min(CustomizationDataLength.MAX_STATES_IN_MSG, states.size() - lastIndex);
		NDCMsg msg = ATMTerminalService.generateStateTableLoadMessage(ndcMsg.getLogicalUnitNumber(), states, lastIndex, length);
		
		if (msg != null) {
			lastIndex += length;
			atm.setLastSentStateIndex(lastIndex);

			Message outMsg = new Message(MessageType.OUTGOING);
			outMsg.setProtocolMessage(msg);
			outMsg.setTransaction(inputMessage.getTransaction());
			outMsg.setIfx(createOutgoingIfx(outMsg, atm));
			return outMsg;
		}
		
		return null;
	}

	@Override
	protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
		NDCWriteCommandStateTableLoadMsg protocolMessage = (NDCWriteCommandStateTableLoadMsg) outputMsg.getProtocolMessage();
		Ifx ifx = new Ifx();
		ifx.setIfxType(IfxType.ATM_STATE_TABLE_LOAD);
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
