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
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusFitnessDataResponseMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedSensorsStatusMsg;
import vaulsys.protocols.ndc.constants.NDCTerminalCommandModifierConfigurationInfo;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationFitnessState extends ConfigurationState {
	public static final ConfigurationFitnessState Instance = new ConfigurationFitnessState();

	private ConfigurationFitnessState(){}

	@Override
	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		
		if (ndcMsg instanceof NDCSolicitedStatusFitnessDataResponseMsg) {
			return this;
		}
		
		return this;
	}
	
	@Override
	protected Message process(Message inputMessage, ATMTerminal atm) {
		//AldTODO Task074 for test only
		System.out.println("*************"+inputMessage.getTransaction().getIncomingIfx().getIfxType().toString()); 
		setDebugTag(inputMessage.getTransaction());
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		if (ndcMsg instanceof NDCUnsolicitedSensorsStatusMsg)
			return null;

		//AldTODO Task074 : 
		System.out.println(atm.getLastRealTransaction().getOutgoingIfx().getIfxType());//test only
			
		inputMessage.getIfx().setIfxType(IfxType.CONFIG_INFO_RESPONSE);

//		atm.setATMState(ATMState.IN_SERIVCE);

		((NDCSolicitedStatusMsg) ndcMsg).updateStatus(atm);

		//AldTODO Task074 Original Comment Only for test
//		Message outMsg = new Message(MessageType.OUTGOING);
//		outMsg.setProtocolMessage(ATMTerminalService.generateSendConfigInfoMessage(ndcMsg.logicalUnitNumber,
//				NDCTerminalCommandModifierConfigurationInfo.SEND_SUPPLIES_DATA_ONLY));
//		outMsg.setTransaction(inputMessage.getTransaction());
//		outMsg.setIfx(createOutgoingIfx(outMsg, atm));
//		return outMsg;
		
		//TASK Task074 : 
		if (!IfxType.ATM_STATUS_MONITOR_REQUEST.equals(atm.getLastRealTransaction().getOutgoingIfx().getIfxType())){
			inputMessage.getIfx().setIfxType(IfxType.CONFIG_INFO_RESPONSE);
			Message outMsg = new Message(MessageType.OUTGOING);
			outMsg.setProtocolMessage(ATMTerminalService.generateSendConfigInfoMessage(ndcMsg.logicalUnitNumber,
					NDCTerminalCommandModifierConfigurationInfo.SEND_SUPPLIES_DATA_ONLY));
			outMsg.setTransaction(inputMessage.getTransaction());
			outMsg.setIfx(createOutgoingIfx(outMsg, atm));
			return outMsg;
		}
		else {
			inputMessage.getIfx().setIfxType(IfxType.ATM_STATUS_MONITOR_RESPONSE);
			inputMessage.getIfx().setTerminalType(TerminalType.ATM);
			return null;
		}
		//End Task074
		
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
