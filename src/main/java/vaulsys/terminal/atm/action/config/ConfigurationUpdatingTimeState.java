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
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCNetworkToTerminalMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.action.config.encryptionKey.ConfigurationUpdatingMasterKey;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Util;

import org.apache.log4j.Logger;

public class ConfigurationUpdatingTimeState extends ConfigurationState {
	public static final ConfigurationUpdatingTimeState Instance = new ConfigurationUpdatingTimeState();

	private static final Logger logger = Logger.getLogger(ConfigurationUpdatingTimeState.class);
	
	private ConfigurationUpdatingTimeState(){}

    @Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
    	
    	boolean isNeedUpdate = false;
    	String changeKeyInterval = "";
    	try {
    		changeKeyInterval = ConfigUtil.getProperty(ConfigUtil.ATM_CHANGE_KEY_INTERVAL_DAY);
    	} catch (Exception e) {
    		isNeedUpdate = false;
    		logger.error("Exception in getting ConfigUtil.ATM_CHANGE_KEY_INTERVAL_DAY" + e, e);
    	}
    	DateTime now = DateTime.now();
		if (Util.hasText(changeKeyInterval)) {
			DateTime intervalDate = DateTime.toDateTime(now.getTime() - Util.longValueOf(changeKeyInterval) * DateTime.ONE_DAY_MILLIS);
			DateTime lastKeyChangeDate = atm.getLastKeyChangeDate();
			
			if (Boolean.TRUE.equals(atm.getChangeKey()) && (lastKeyChangeDate == null || lastKeyChangeDate.getTime() < intervalDate.getTime())) {
				isNeedUpdate = true;
			} else {
				isNeedUpdate = false;
			}
			
		} else {
			isNeedUpdate = false;
			
		}

		if (isNeedUpdate) {
			atm.setLastKeyChangeDate(DateTime.now());
//			return ConfigurationUpdatingPINKey.Instance;
			return ConfigurationUpdatingMasterKey.Instance;
		}
		
		else
    		return ConfigurationInServiceState.Instance;
    }

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
    	setDebugTag(inputMessage.getTransaction()); 
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
    	NDCNetworkToTerminalMsg msg = ATMTerminalService.generateDateTimeLoadMessage(ndcMsg.getLogicalUnitNumber());

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
		ifx.setIfxType(IfxType.ATM_DATE_TIME_LOAD);
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
