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
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandEnhancedParameterTableLoadMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConfigurationStartupState extends ConfigurationState {
	public static final ConfigurationStartupState Instance = new ConfigurationStartupState();

	private ConfigurationStartupState(){}

    @Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
        return ConfigurationInServiceState.Instance;
    }

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
    	setDebugTag(inputMessage.getTransaction());
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
        atm.setATMState(ATMState.PARAMETER_DATA_LOADING);
        Message outMsg = new Message(MessageType.OUTGOING);
        outMsg.setProtocolMessage(ATMTerminalService.generateEnhancedParameterTableLoadMessage(ndcMsg.logicalUnitNumber));
        outMsg.setTransaction(inputMessage.getTransaction());
        outMsg.setIfx(createOutgoingIfx(outMsg, atm));
        return outMsg;
    }

    @Override
    protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
		NDCWriteCommandEnhancedParameterTableLoadMsg protocolMessage = (NDCWriteCommandEnhancedParameterTableLoadMsg) outputMsg.getProtocolMessage();
		Ifx ifx = new Ifx();
		ifx.setIfxType(IfxType.ATM_ENHANCED_PARAMETER_TABLE_LOAD);
		ifx.setTerminalType(TerminalType.ATM);
		ifx.setOrigDt(DateTime.now());
		ifx.setTerminalId(((NDCMsg) outputMsg.getProtocolMessage()).getLogicalUnitNumber().toString());
		ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.messageSequenceNumber));
		ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.messageSequenceNumber));
		ifx.setIfxDirection(IfxDirection.OUTGOING);
		ifx.setReceivedDt(outputMsg.getStartDateTime());
		return ifx;	
    }
    
    protected NDCWriteCommandMsg prepareMessage() throws Exception {
        return null;
    }
}
