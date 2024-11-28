package vaulsys.terminal.atm.action.supervisor;

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
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.impl.ATMTerminal;

public class SupervisorEntryState extends SensorState {
	public static final SupervisorEntryState Instance = new SupervisorEntryState();

	private SupervisorEntryState(){}

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
    	setDebugTag(inputMessage.getTransaction());
        atm.setATMState(ATMState.OUT_OF_SERVICE);
        Message outMsg = new Message(MessageType.OUTGOING);
        outMsg.setProtocolMessage(ATMTerminalService.generateGoOutOfServiceMessage(ndcMsg.logicalUnitNumber));
        outMsg.setTransaction(inputMessage.getTransaction());
        outMsg.setIfx(createOutgoingIfx(outMsg, atm));
        return outMsg;
    }
    
    @Override
    protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
		NDCOperationalMsg protocolMessage = (NDCOperationalMsg) outputMsg.getProtocolMessage();
		Ifx ifx = new Ifx();
		ifx.setIfxType(IfxType.ATM_GO_OUT_OF_SERVICE);
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