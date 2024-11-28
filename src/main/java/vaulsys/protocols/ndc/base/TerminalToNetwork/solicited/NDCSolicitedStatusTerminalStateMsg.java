package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.NDCMessageIdentifierTypes;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCSolicitedStatusMsg;
import vaulsys.protocols.ndc.constants.NDCStatusDescriptor;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyInteger;
import vaulsys.util.NotYetImplementedException;

public class NDCSolicitedStatusTerminalStateMsg extends NDCSolicitedStatusMsg {

    public byte messageIdentifier;

    public static NDCSolicitedStatusTerminalStateMsg fromBinary(MyInteger offset, byte[] rawdata) throws NotParsedBinaryToProtocolException {
        NDCMessageIdentifierTypes messageIdentifier = NDCMessageIdentifierTypes.getByCode(findMessageIdentifier(offset, rawdata));
            if (NDCMessageIdentifierTypes.CONFIGURATION_INFO.equals(messageIdentifier)){
                return new NDCSolicitedStatusConfigTerminalStateMsg(offset, rawdata);
            }
           
            if (NDCMessageIdentifierTypes.SUPPLY_COUNTERS.equals(messageIdentifier))
                return new NDCSolicitedStatusSupplyCounterMsg(offset, rawdata);
            
            if (NDCMessageIdentifierTypes.SEND_CONFIG_ID.equals(messageIdentifier))
            	return new NDCSolicitedStatusSendConfigIDResponseMsg(offset, rawdata);
            
            if (NDCMessageIdentifierTypes.SUPPLIES.equals(messageIdentifier))
            	return new NDCSolicitedStatusSuppliesDataResponseMsg(offset, rawdata);

            if (NDCMessageIdentifierTypes.FITNESS.equals(messageIdentifier))
            	return new NDCSolicitedStatusFitnessDataResponseMsg(offset, rawdata);

            throw new NotYetImplementedException("Unknown messageIdentifier in NDCSolicitedStatusTerminalStateMsg");
    }

    public NDCSolicitedStatusTerminalStateMsg(MyInteger offset, byte[] rawdata) throws NotParsedBinaryToProtocolException {
        super(offset, rawdata);
        NDCParserUtils.readFS(rawdata, offset);
        messageIdentifier = rawdata[offset.value++];
    }

    private static char findMessageIdentifier(MyInteger offset, byte[] rawdata) throws NotParsedBinaryToProtocolException {
        MyInteger newOffset = new MyInteger(offset);
        NDCParserUtils.readUntilFS(rawdata, newOffset);
        NDCParserUtils.readFS(rawdata, newOffset);
        NDCParserUtils.readFS(rawdata, newOffset);
        newOffset.value++;
        NDCParserUtils.readFS(rawdata, newOffset);
        return (char)rawdata[newOffset.value];
    }

    public NDCSolicitedStatusTerminalStateMsg() {
        statusDescriptor = NDCStatusDescriptor.TERMINAL_STATE;
    }
    
    public boolean isConfigurationInfo() {
    	return NDCMessageIdentifierTypes.CONFIGURATION_INFO.equals(messageIdentifier);
    }

}