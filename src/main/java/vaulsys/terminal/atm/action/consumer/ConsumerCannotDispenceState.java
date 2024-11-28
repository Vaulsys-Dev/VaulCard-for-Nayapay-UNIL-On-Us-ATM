package vaulsys.terminal.atm.action.consumer;

import vaulsys.calendar.DateTime;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCFunctionCommandMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCConsumerRequestMsg;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;
import vaulsys.protocols.ndc.encoding.NDCConvertor;
import vaulsys.protocols.ndc.parsers.NDCFunctionCommandMapper;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

public class ConsumerCannotDispenceState extends ConsumerState {
	public static final ConsumerCannotDispenceState Instance = new ConsumerCannotDispenceState();

	private ConsumerCannotDispenceState(){}

    @Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
        return this.Instance;
    }

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
		// setDebugTag(inputMessage.getTransaction());
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		Message outMsg = new Message(MessageType.OUTGOING);
//		NDCConvertor convertor = (NDCConvertor) GlobalContext.getInstance().getConvertor(inputMessage.getChannel().getEncodingConvertor());
		NDCConvertor convertor = (NDCConvertor) ProcessContext.get().getConvertor(inputMessage.getChannel().getEncodingConverter());
		outMsg.setProtocolMessage(NDCFunctionCommandMapper.fromProtocol((NDCConsumerRequestMsg) ndcMsg, ATMErrorCodes.ATM_NOT_ROUND_AMOUNT,
				convertor));
		outMsg.setTransaction(inputMessage.getTransaction());
		outMsg.setIfx(createOutgoingIfx(outMsg, atm));
		return outMsg;
	}
    
    @Override
    protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
		NDCFunctionCommandMsg protocolMessage = (NDCFunctionCommandMsg) outputMsg.getProtocolMessage();
		Ifx ifx = new Ifx();
		ifx.setIfxType(IfxType.ATM_FUNCTION_COMMAND);
		ifx.setTerminalType(TerminalType.ATM);
		ifx.setOrigDt(DateTime.now());
		ifx.setTerminalId(((NDCMsg) outputMsg.getProtocolMessage()).getLogicalUnitNumber().toString());
		ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.transactionSerialNumber));
		ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(protocolMessage.transactionSerialNumber));
		ifx.setNetworkRefId(ATMTerminalService.timeVariantToNetworkRefId(protocolMessage.messageSequenceNumber).toString());
		ifx.setIfxDirection(IfxDirection.OUTGOING);
		ifx.setReceivedDt(outputMsg.getStartDateTime());
		
		if (outputMsg.getTransaction().getInputMessage() != null) {
			Ifx inIfx = outputMsg.getTransaction().getIncomingIfx()/*getInputMessage().getIfx()*/;
			ifx.setName(inIfx.getName());
			ifx.setOrgIdNum(inIfx.getOrgIdNum());
			ifx.setOrgIdType(inIfx.getOrgIdType());
			ifx.setBankId(inIfx.getBankId());
			ifx.setDestBankId(inIfx.getDestBankId());
			ifx.setFwdBankId(inIfx.getFwdBankId());
			inIfx.setSrc_TrnSeqCntr(ifx.getSrc_TrnSeqCntr());
			inIfx.setMy_TrnSeqCntr(ifx.getMy_TrnSeqCntr());
			inIfx.setNetworkRefId(ifx.getNetworkRefId());
			GeneralDao.Instance.saveOrUpdate(inIfx);
		}
		
		return ifx;	
    }
}
