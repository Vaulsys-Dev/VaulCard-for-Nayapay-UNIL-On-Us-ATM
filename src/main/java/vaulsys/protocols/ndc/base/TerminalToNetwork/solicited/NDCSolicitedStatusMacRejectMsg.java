package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCSolicitedStatusMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.enums.FieldValueErrorStatusQualifierType;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.enums.HardwareFailureErrorStatusQualifierType;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.enums.IllegalMessageTypeErrorStatusQualifierType;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.enums.MessageFormatErrorStatusQualifierType;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.enums.NotSupportedErrorStatusQualifierType;
import vaulsys.protocols.ndc.base.config.CommandRejectType;
import vaulsys.protocols.ndc.constants.NDCStatusDescriptor;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyInteger;

public class NDCSolicitedStatusMacRejectMsg extends NDCSolicitedStatusMsg {

    public NDCSolicitedStatusMacRejectMsg(MyInteger offset, byte[] rawdata) throws NotParsedBinaryToProtocolException {
        super(offset, rawdata);
        NDCParserUtils.readFS(rawdata, offset);
        statusDescriptor = NDCStatusDescriptor.SPECIFIC_COMMAND_REJECT;
        statusInformation = CommandRejectType.getByType((char) rawdata[offset.value++]);
        
        if (rawdata.length >= offset.value+2 ){
        	if (CommandRejectType.MESSAGE_FORMAT_ERROR.equals(statusInformation))
        		statusQualifier = MessageFormatErrorStatusQualifierType.getByType(new String (rawdata, offset.value, 2));
        	
        	else if (CommandRejectType.FIELD_VALUE_ERROR.equals(statusInformation))
        		statusQualifier = FieldValueErrorStatusQualifierType.getByType(new String (rawdata, offset.value, 2));
        	
        	else if (CommandRejectType.ILLEGAL_MESSAGE_TYPE_FOR_CURRENT_MODE.equals(statusInformation))
        		statusQualifier = IllegalMessageTypeErrorStatusQualifierType.getByType(new String (rawdata, offset.value, 2));
        	
        	else if (CommandRejectType.HARDWARE_FAILURE.equals(statusInformation))
        		statusQualifier = HardwareFailureErrorStatusQualifierType.getByType(new String (rawdata, offset.value, 2));
        	
        	else if (CommandRejectType.NOT_SUPPORTED.equals(statusInformation))
        		statusQualifier = NotSupportedErrorStatusQualifierType.getByType(new String (rawdata, offset.value, 2));
        }
    }

    public NDCSolicitedStatusMacRejectMsg() {
        statusDescriptor = NDCStatusDescriptor.SPECIFIC_COMMAND_REJECT;
    }
}