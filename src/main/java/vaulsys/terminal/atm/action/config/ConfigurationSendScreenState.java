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
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandScreenTableLoadMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusReadyMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedPowerFailureStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedSensorsStatusMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.customizationdata.ScreenData;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.MyInteger;

import java.util.List;

public class ConfigurationSendScreenState extends ConfigurationState {
	public static final ConfigurationSendScreenState Instance = new ConfigurationSendScreenState();

	private ConfigurationSendScreenState(){}

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
			
			if (isAllScreensSent(msg, atm))
				return ConfigurationIDLoadState.Instance;

			return this.Instance;
		}
		return ConfigurationOutOfServiceState.Instance;
	}

	protected boolean isAllScreensSent(NDCMsg inputMessage, ATMTerminal atm) {
//		ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, inputMessage.getLogicalUnitNumber());
		int lastIndex = atm.getLastSentScreenIndex();
		List<ScreenData> screens = ATMTerminalService.getCustomizationDataAfter(atm, ScreenData.class, atm.getConfigId());

		if (screens.size() <= lastIndex) {
			atm.setLastSentScreenIndex(0);
			return true;
		} else
			return false;
	}

	@Override
	protected Message process(Message inputMessage, ATMTerminal atm) {
		setDebugTag(inputMessage.getTransaction());
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		int lastIndex = atm.getLastSentScreenIndex();
		List<ScreenData> screens = ATMTerminalService.getCustomizationDataAfter(atm, ScreenData.class, atm.getConfigId());
		
		MyInteger length = new MyInteger(0);

		if (screens == null || screens.size() == 0) {
//			atm.setCurrentAbstractStateClass(ConfigurationIDLoadState.Instance);
//			return ConfigurationIDLoadState.Instance.process(inputMessage, atm);
			return null;
		}

		NDCMsg msg = ATMTerminalService.generateScreenTableLoadMessage(ndcMsg.getLogicalUnitNumber(), screens, lastIndex, length);
		if (msg != null) {
			lastIndex += length.value;

			atm.setLastSentScreenIndex(lastIndex);

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
		NDCWriteCommandScreenTableLoadMsg protocolMessage = (NDCWriteCommandScreenTableLoadMsg) outputMsg.getProtocolMessage();
		Ifx ifx = new Ifx();
		ifx.setIfxType(IfxType.ATM_SCREEN_TABLE_LOAD);
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
