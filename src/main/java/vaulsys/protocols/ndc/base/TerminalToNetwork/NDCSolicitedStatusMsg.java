package vaulsys.protocols.ndc.base.TerminalToNetwork;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusCommandRejectMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusDeviceFaultMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusMacRejectMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusReadyMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusTerminalStateMsg;
import vaulsys.protocols.ndc.base.config.CommandRejectType;
import vaulsys.protocols.ndc.base.config.StatusQualifierType;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCDeviceStatusInfo;
import vaulsys.protocols.ndc.constants.NDCMessageClassSolicitedUnSokicited;
import vaulsys.protocols.ndc.constants.NDCMessageClassTerminalToNetwork;
import vaulsys.protocols.ndc.constants.NDCStatusDescriptor;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.MyInteger;
import vaulsys.util.NotYetImplementedException;

public class NDCSolicitedStatusMsg extends NDCTerminalToNetworkMsg {

    public NDCStatusDescriptor statusDescriptor;
    public CommandRejectType statusInformation;
    public StatusQualifierType statusQualifier;
    public NDCDeviceStatusInfo solicitedStatus;
    public String MAC;

    public NDCSolicitedStatusMsg() {
        messageType = NDCMessageClassTerminalToNetwork.STATUS_MESSAGE;
        solicited = NDCMessageClassSolicitedUnSokicited.SOLICITED_MESSAGE;
    }

    public Ifx toIfx() throws Exception {
        throw new UnsupportedOperationException("Cannot convert to Ifx message");
    }

    public NDCSolicitedStatusMsg(MyInteger offset, byte[] rawdata) throws NotParsedBinaryToProtocolException {
        messageType = NDCMessageClassTerminalToNetwork.STATUS_MESSAGE;
        solicited = NDCMessageClassSolicitedUnSokicited.SOLICITED_MESSAGE;
        logicalUnitNumber = Long.valueOf(NDCParserUtils.readUntilFS(rawdata, offset));
        NDCParserUtils.readFS(rawdata, offset);
        NDCParserUtils.readFS(rawdata, offset);
        statusDescriptor = NDCStatusDescriptor.getByType((char)rawdata[offset.value++]);
    }

    public static NDCSolicitedStatusMsg fromBinary(byte[] rawdata, int index)
            throws NotParsedBinaryToProtocolException {
        MyInteger offset = new MyInteger(index);
        char statusDescriptor = findStatusDescriptor(rawdata, index);
        NDCStatusDescriptor status = NDCStatusDescriptor.getByType(statusDescriptor);

        if (NDCStatusDescriptor.READY.equals(status) || NDCStatusDescriptor.READY_SEPERATE.equals(status))
			return new NDCSolicitedStatusReadyMsg(offset, rawdata);
		
        if (NDCStatusDescriptor.DEVICE_FAULT_OR_CONFIG_INFO.equals(status))
			return new NDCSolicitedStatusDeviceFaultMsg(offset, rawdata);
		
        if (NDCStatusDescriptor.TERMINAL_STATE.equals(status))
			return NDCSolicitedStatusTerminalStateMsg.fromBinary(offset, rawdata);
		
        if (NDCStatusDescriptor.SPECIFIC_COMMAND_REJECT.equals(status))
			return new NDCSolicitedStatusMacRejectMsg(offset, rawdata);

		// TODO to be decision for reject command
		if (NDCStatusDescriptor.COMMAND_REJECT.equals(status))
			return new NDCSolicitedStatusCommandRejectMsg(offset, rawdata);
    
        throw new NotYetImplementedException("Unsopported statusDescriptor for SolicitedMessage: "
                + statusDescriptor);
    }

    private static char findStatusDescriptor(byte[] rawdata, int index) throws NotParsedBinaryToProtocolException {
        MyInteger newOffset = new MyInteger(index);
        NDCParserUtils.readUntilFS(rawdata, newOffset);
        NDCParserUtils.readFS(rawdata, newOffset);
        NDCParserUtils.readFS(rawdata, newOffset);
        return (char)rawdata[newOffset.value];
    }

    public String toString() {
    	return super.toString() +
        	"statusDescrib:\t\t" + statusDescriptor + "\r\n" + 
        	(statusInformation != null ? ("statusInfo:\t\t" + statusInformation.toString()) : statusInformation) + "\r\n" + 
        	(statusQualifier != null ? ("statusQualifier:\t\t"+ statusQualifier.toString()) : statusQualifier) + "\r\n" + 
        	"deviceInfo:\t\t" + solicitedStatus + "\r\n" 
//        	"MAC\t\t" + MAC + "\r\n"
        	;
    }

    public void updateStatus(ATMTerminal status) {
    }
}
