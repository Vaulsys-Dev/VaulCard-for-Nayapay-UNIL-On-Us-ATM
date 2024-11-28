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
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationInServiceState extends ConfigurationState {
	public static final ConfigurationInServiceState Instance = new ConfigurationInServiceState();

	private ConfigurationInServiceState(){}

	@Override
	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
        return ConfigurationEndingState.Instance;
    }

    protected NDCWriteCommandMsg prepareMessage() throws Exception {
        return null;
    }
    
    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
    	setDebugTag(inputMessage.getTransaction());
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
//		ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, ndcMsg.getLogicalUnitNumber());
//	    atm.setATMState(ATMState.IN_SERIVCE);
    	Message outMessage = new Message(MessageType.OUTGOING);
    	outMessage.setProtocolMessage(ATMTerminalService.generateGoInServiceMessage(ndcMsg.logicalUnitNumber));
    	outMessage.setTransaction(inputMessage.getTransaction());
    	outMessage.setIfx(createOutgoingIfx(outMessage, atm));
        return outMessage;
    }

    @Override
    protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
		NDCOperationalMsg protocolMessage = (NDCOperationalMsg) outputMsg.getProtocolMessage();
		Ifx ifx = new Ifx();
		ifx.setIfxType(IfxType.ATM_GO_IN_SERVICE);
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