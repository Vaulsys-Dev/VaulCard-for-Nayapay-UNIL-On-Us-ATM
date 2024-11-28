package vaulsys.terminal.atm.action.isolated;

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
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCOperationalMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedEncryptorStatusMsg;
import vaulsys.protocols.ndc.constants.EncryptorStatus;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.ATMLog;
import vaulsys.terminal.atm.ATMState;
import vaulsys.terminal.atm.ActionType;
import vaulsys.terminal.atm.device.DeviceStatus;
import vaulsys.terminal.atm.device.Encryptor;
import vaulsys.terminal.impl.ATMTerminal;

public class EncryptorState extends IsolatedState {
	public static final EncryptorState Instance = new EncryptorState();

	private EncryptorState(){}

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
    	inputMessage.getIfx().setIfxType(IfxType.ENCRYPTOR_STATE);
    	setDebugTag(inputMessage.getTransaction());
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
        NDCUnsolicitedEncryptorStatusMsg msg = (NDCUnsolicitedEncryptorStatusMsg) ndcMsg;
        Encryptor encryptor = atm.getDevice(Encryptor.class);
//        DeviceStatus prevState = encryptor.getStatus();
        encryptor.setStatus(msg.statusInformation.deviceStatus);
        encryptor.setErrorSeverity(msg.statusInformation.errorSeverity);
        encryptor.setErrorSeverityDate(DateTime.now());
        
//        ATMLog log;
        
//        if(prevState != null){
//	        log = new ATMLog(atm.getCode(), "ENCRYPTOR:" + EncryptorStatus.getByCode(prevState.getStatus()).toString(), "LAST_STATE", ActionType.DEVICE_UPDATE);
//	        GeneralDao.Instance.saveOrUpdate(log);
//        }

//        log = new ATMLog(atm.getCode(), "ENCRYPTOR:" + encryptor.getStatus().toString(), "NEXT_STATE", ActionType.DEVICE_UPDATE);
//        GeneralDao.Instance.saveOrUpdate(log);
        
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
