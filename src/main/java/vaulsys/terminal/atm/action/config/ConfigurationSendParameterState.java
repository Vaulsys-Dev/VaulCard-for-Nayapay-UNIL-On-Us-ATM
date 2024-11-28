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
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandEnhancedParameterTableLoadMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusReadyMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedPowerFailureStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited.NDCUnsolicitedSensorsStatusMsg;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.customizationdata.EnhancedParameterData;
import vaulsys.terminal.atm.customizationdata.FITData;
import vaulsys.terminal.atm.customizationdata.TimerData;
import vaulsys.terminal.impl.ATMTerminal;

import java.util.List;

import org.apache.log4j.Logger;

public class ConfigurationSendParameterState extends ConfigurationState {
	public static final ConfigurationSendParameterState Instance = new ConfigurationSendParameterState();

	private ConfigurationSendParameterState(){}
	
	transient Logger logger = Logger.getLogger(this.getClass());

    @Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
    	//TODO only for test!
//        return ConfigurationEndingState.getInstance();

    	/******************/
    	NDCMsg msg = (NDCMsg) inputMessage.getProtocolMessage();
    	
    	if (msg instanceof NDCSolicitedStatusReadyMsg ||
    			msg instanceof NDCUnsolicitedPowerFailureStatusMsg ||
    			msg instanceof NDCUnsolicitedSensorsStatusMsg) {
    		
    		if (msg instanceof NDCUnsolicitedPowerFailureStatusMsg)
				inputMessage.getIfx().setIfxType(IfxType.POWER_FAILURE);
			
			if (msg instanceof NDCUnsolicitedSensorsStatusMsg)
				inputMessage.getIfx().setIfxType(IfxType.SENSOR);
			
			if (msg instanceof NDCSolicitedStatusReadyMsg)
				inputMessage.getIfx().setIfxType(IfxType.ATM_ACKNOWLEDGE);
			
//    		if(isAllParamsSent(msg, atm)){
    			if(!ConfigurationSendFitState.Instance.isAllFitsSent(msg, atm)){
    				return ConfigurationSendFitState.Instance;
    			}else if(!ConfigurationSendStateState.Instance.isAllStatesSent(msg, atm)){
    				return ConfigurationSendStateState.Instance;
    			}else if(!ConfigurationSendScreenState.Instance.isAllScreensSent(msg, atm)){
    				return ConfigurationSendScreenState.Instance;
    			}else{
    				return ConfigurationIDLoadState.Instance;
    			}
//    		}
				
//	    	return this.Instance;
    	}
    	return ConfigurationOutOfServiceState.Instance;
    	/******************/
    }

    
    protected boolean isAllParamsSent(NDCMsg inputMessage, ATMTerminal atm){
		int lastIndex = atm.getLastSentFitIndex();

		List<EnhancedParameterData> params = ATMTerminalService.getCustomizationDataAfter(atm, EnhancedParameterData.class, atm.getConfigId());
		if(params != null && params.size() > 0)
			return false;
		
		List<TimerData> timers = ATMTerminalService.getCustomizationDataAfter(atm, TimerData.class, atm.getConfigId());

		if(timers != null && timers.size() > 0)
			return false;
		
		return true;
//		ATMTerminal atm = getTerminalService().findTerminal(ATMTerminal.class, inputMessage.getLogicalUnitNumber());
//		int lastIndex = atm.getLastSentParamIndex();
//		List<EnhancedParameterData> params = atm.getOwnOrParentConfiguration().getParams();
//
//		if(params.size() == lastIndex) {
//			atm.setLastSentParamIndex(0);
//			return true;
//		}
//		else
//			return false;
    	
//    	ATMTerminal atm = getTerminalService().findTerminal(ATMTerminal.class, inputMessage.getLogicalUnitNumber());
//    	atm.setLastSentParamIndex(0);
//    	return true;
    }

	@Override
	protected Message process(Message inputMessage, ATMTerminal atm)
	{
		setDebugTag(inputMessage.getTransaction());
		NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
		
//		int lastIndex = atm.getLastSentParamIndex();
//		int lastIndexTimer = atm.getAtmStatus().getLastSentTimerIndex();
		
		List<EnhancedParameterData> params = ATMTerminalService.getCustomizationDataAfter(atm, EnhancedParameterData.class, atm.getConfigId());
		List<TimerData> timers = ATMTerminalService.getCustomizationDataAfter(atm, TimerData.class, atm.getConfigId());
		
//		int length = Math.min(CustomizationDataLength.MAX_PARAM_IN_LENGTH, params.size() + timers.size() - lastIndex);
//		int lengthTimer = Math.min(ConfigDataLength.MAX_PARAM_IN_LENGTH, timers.size()-lastIndexTimer);
    	
		NDCMsg msg = ATMTerminalService.generateParameterAndTimerTableLoadMessage(ndcMsg, params, timers);
//    	if(msg != null)
//    		lastIndex += length;
//    	atm.setLastSentParamIndex(lastIndex);
		if (msg != null) {
			Message outMsg = new Message(MessageType.OUTGOING);
			outMsg.setProtocolMessage(msg);
			outMsg.setTransaction(inputMessage.getTransaction());
			outMsg.setIfx(createOutgoingIfx(outMsg, atm));
			return outMsg;
		}
		
		return null;
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
}
