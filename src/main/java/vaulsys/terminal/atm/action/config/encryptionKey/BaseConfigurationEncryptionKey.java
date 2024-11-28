package vaulsys.terminal.atm.action.config.encryptionKey;

import vaulsys.calendar.DateTime;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCNetworkToTerminalMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.terminal.atm.action.config.ConfigurationState;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.transaction.Transaction;

public abstract class BaseConfigurationEncryptionKey extends ConfigurationState {
	public abstract NDCNetworkToTerminalMsg genNetToTermMsg(Long luno);
	public abstract IfxType getIfxType();

	@Override
	protected Message process(Message inputMessage, ATMTerminal atm) {
    	setDebugTag(inputMessage.getTransaction());
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
    	NDCNetworkToTerminalMsg msg = genNetToTermMsg(ndcMsg.getLogicalUnitNumber());

    	Transaction transaction = inputMessage.getTransaction();
		Message outMessage = new Message(MessageType.OUTGOING);
		outMessage.setProtocolMessage(msg);
		outMessage.setTransaction(transaction);
		transaction.addOutputMessage(outMessage);

    	outMessage.setIfx(createOutgoingIfx(outMessage, atm));
		outMessage.setRequest(true);
		outMessage.setNeedResponse(false);
		outMessage.setNeedToBeInstantlyReversed(false);
		outMessage.setNeedToBeSent(true);

		return outMessage;
	}
	
	@Override
	protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
		NDCWriteCommandMsg protocolMessage = (NDCWriteCommandMsg) outputMsg.getProtocolMessage();
		Ifx ifx = new Ifx();
		ifx.setIfxType(getIfxType());
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
