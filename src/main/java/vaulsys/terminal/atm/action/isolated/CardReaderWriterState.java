package vaulsys.terminal.atm.action.isolated;

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
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedCardReaderWriterStatusMsg;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCardReaderWriterDidntTakeCard;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCardReaderWriterFailedEjectCard;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.device.CardBin;
import vaulsys.terminal.impl.ATMTerminal;

public class CardReaderWriterState extends IsolatedState {
	public static final CardReaderWriterState Instance = new CardReaderWriterState();

	private CardReaderWriterState(){}

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
    	inputMessage.getIfx().setIfxType(IfxType.CARD_READER_WRITER);
    	setDebugTag(inputMessage.getTransaction());
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
        NDCUnsolicitedCardReaderWriterStatusMsg msg = (NDCUnsolicitedCardReaderWriterStatusMsg) ndcMsg;
        if (msg.statusInformation instanceof NDCCardReaderWriterDidntTakeCard
                || msg.statusInformation instanceof NDCCardReaderWriterFailedEjectCard) {       	
            CardBin cardBin = atm.getDevice(CardBin.class);
            cardBin.add();
        }
        if (ErrorSeverity.FATAL.equals(msg.statusInformation.errorSeverity)){
		atm.setATMState(ATMState.OUT_OF_SERVICE);
        	 Message outMsg = new Message(MessageType.OUTGOING);
        	 outMsg.setProtocolMessage(ATMTerminalService.generateGoOutOfServiceMessage(ndcMsg.logicalUnitNumber));
             outMsg.setTransaction(inputMessage.getTransaction());
             outMsg.setIfx(createOutgoingIfx(outMsg, atm));
            return outMsg;
        }
        return null;
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
