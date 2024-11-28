package vaulsys.terminal.atm.action.config;

import vaulsys.calendar.DateTime;
import vaulsys.message.Message;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCOperationalMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCSolicitedStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusSuppliesDataResponseMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedSecurityCameraStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedSensorsStatusMsg;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationSupplyDataState extends ConfigurationState {
	public static final ConfigurationSupplyDataState Instance = new ConfigurationSupplyDataState();

	private ConfigurationSupplyDataState(){}

	@Override
	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		
		if (ndcMsg instanceof NDCSolicitedStatusSuppliesDataResponseMsg) {
			return this;
		}
		
		return this;
	}
	
	@Override
	protected Message process(Message inputMessage, ATMTerminal atm) {
		setDebugTag(inputMessage.getTransaction());
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		if(ndcMsg instanceof NDCUnsolicitedSensorsStatusMsg)
			return null;
		///**************************************************
		if(ndcMsg instanceof NDCUnsolicitedSecurityCameraStatusMsg)
			return null;
        ///****************************************************
		inputMessage.getIfx().setIfxType(IfxType.CONFIG_INFO_RESPONSE);
		
//		atm.setATMState(ATMState.IN_SERIVCE);
		
		((NDCSolicitedStatusMsg) ndcMsg).updateStatus(atm);

		return null;
	}
	
	@Override
	protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
		NDCOperationalMsg protocolMessage = (NDCOperationalMsg) outputMsg.getProtocolMessage();
		Ifx ifx = new Ifx();
		ifx.setIfxType(IfxType.CONFIG_INFO_REQUEST);
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
