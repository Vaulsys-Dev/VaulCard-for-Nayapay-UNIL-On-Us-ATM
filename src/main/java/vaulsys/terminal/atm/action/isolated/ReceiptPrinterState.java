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
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusFitnessDataResponseMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusSendConfigIDResponseMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedReceiptPrinterStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCReceiptPrinter;
import vaulsys.protocols.ndc.constants.NDCSupplyStatusConstants;
import vaulsys.protocols.ndc.constants.NDCTerminalCommandModifier;
import vaulsys.protocols.ndc.constants.NDCTerminalCommandModifierConfigurationInfo;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.action.config.ConfigurationFitnessState;
import vaulsys.terminal.impl.ATMTerminal;

public class ReceiptPrinterState extends IsolatedState {
	public static final ReceiptPrinterState Instance = new ReceiptPrinterState();
   
	private ReceiptPrinterState(){}

	@Override
	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		
		if (ndcMsg instanceof NDCSolicitedStatusFitnessDataResponseMsg) {
			inputMessage.getIfx().setIfxType(IfxType.CONFIG_INFO_RESPONSE);
			return ConfigurationFitnessState.Instance;
		}
		
		return this;
	}
	
    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
    	inputMessage.getIfx().setIfxType(IfxType.RECEIPT_PRINTER_STATE);
    	setDebugTag(inputMessage.getTransaction());
    	ATMTerminalService.updateReceiptPrinter(atm, inputMessage);
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
    	 NDCUnsolicitedReceiptPrinterStatusMsg msg = (NDCUnsolicitedReceiptPrinterStatusMsg) ndcMsg;
    	 NDCReceiptPrinter printerInfo = msg.statusInformation;
    	
    	 if (NDCSupplyStatusConstants.MEDIA_OUT.equals(printerInfo.paperStatus))
    		return null;
    	
    	 Message outMsg = new Message(MessageType.OUTGOING);
         outMsg.setProtocolMessage(ATMTerminalService.generateSendConfigInfoMessage(ndcMsg.logicalUnitNumber, NDCTerminalCommandModifierConfigurationInfo.SEND_FITNESS_DATA_ONLY));
         outMsg.setTransaction(inputMessage.getTransaction());
         outMsg.setIfx(createOutgoingIfx(outMsg, atm));
         return outMsg;
    	
//        return null;
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