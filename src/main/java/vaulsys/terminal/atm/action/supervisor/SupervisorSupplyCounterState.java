package vaulsys.terminal.atm.action.supervisor;

import vaulsys.calendar.DateTime;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.message.exception.ReversalOriginatorNotFoundException;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCOperationalMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCSolicitedStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusReadyMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusSupplyCounterMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.action.config.ConfigurationHardwareConfigState;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.transaction.TransactionService;
import vaulsys.util.StringFormat;

public class SupervisorSupplyCounterState extends SensorState {
	public static final SupervisorSupplyCounterState Instance = new SupervisorSupplyCounterState();
	
	private SupervisorSupplyCounterState(){}

	@Override
	protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		
		if(ndcMsg instanceof NDCSolicitedStatusSupplyCounterMsg)
			return SupervisorSupplyCounterState.Instance;

		return ConfigurationHardwareConfigState.Instance;	
//		}  else
//			return ConfigurationInServiceState.getInstance();
	}
	
	@Override
	protected Message process(Message inputMessage, ATMTerminal atm) {
		setDebugTag(inputMessage.getTransaction());
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		
		inputMessage.getIfx().setIfxType(IfxType.SUPPLY_COUNTER_RESPONSE);
		
		
		/*** handle response of supply counter ***/
		Long luno = ndcMsg.getLogicalUnitNumber();
		if(ndcMsg instanceof NDCSolicitedStatusReadyMsg)
			return null;
		
		((NDCSolicitedStatusMsg) ndcMsg).updateStatus(atm);
		/***************/
		/*** set CLEAR flag on last transaction in supply counter response ***/
		NDCSolicitedStatusSupplyCounterMsg msg = (NDCSolicitedStatusSupplyCounterMsg) ndcMsg;
		inputMessage.getIfx().setLast_TrnSeqCntr(msg.transactionSerialNo);
		
		String lastNote = StringFormat.formatNew(5, StringFormat.JUST_RIGHT, msg.lastTrxNotesDispensed[0], '0');
		lastNote += StringFormat.formatNew(5, StringFormat.JUST_RIGHT, msg.lastTrxNotesDispensed[1], '0');
		lastNote += StringFormat.formatNew(5, StringFormat.JUST_RIGHT, msg.lastTrxNotesDispensed[2], '0');
		lastNote += StringFormat.formatNew(5, StringFormat.JUST_RIGHT, msg.lastTrxNotesDispensed[3], '0');
		
		inputMessage.getIfx().setLastTrxNotesDispensed(lastNote);
		try {
			TransactionService.checkValidityOfLastTransactionStatus(atm, inputMessage.getIfx());
		} catch (ReversalOriginatorNotFoundException e) {
		}
		/***************/
		
		GeneralDao.Instance.saveOrUpdate(atm);

		Message outMsg = new Message(MessageType.OUTGOING);
		outMsg.setProtocolMessage(ATMTerminalService.generateSendConfigInfoMessage(luno, null));
		outMsg.setTransaction(inputMessage.getTransaction());
		outMsg.setIfx(createOutgoingIfx(outMsg, atm));
		return outMsg;
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
