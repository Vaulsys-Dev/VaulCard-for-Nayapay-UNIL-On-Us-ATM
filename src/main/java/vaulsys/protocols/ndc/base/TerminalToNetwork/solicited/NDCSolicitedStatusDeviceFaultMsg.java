package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCSolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCDeviceStatusInfo;
import vaulsys.protocols.ndc.constants.NDCStatusDescriptor;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyInteger;

public class NDCSolicitedStatusDeviceFaultMsg extends NDCSolicitedStatusMsg {

    public NDCSolicitedStatusDeviceFaultMsg(MyInteger offset, byte[] rawdata) throws NotParsedBinaryToProtocolException {
        super(offset, rawdata);
        NDCParserUtils.readFS(rawdata, offset);
        solicitedStatus = NDCDeviceStatusInfo.fromBinary(offset, rawdata);
//        statusInformation = rawdata[offset.value++];
//        NDCParserUtils.readFS(rawdata, offset);
//        MAC = new String(rawdata, offset.value, rawdata.length - offset.value + 1);
        statusDescriptor = NDCStatusDescriptor.DEVICE_FAULT_OR_CONFIG_INFO;
    }

    public NDCSolicitedStatusDeviceFaultMsg() {
        statusDescriptor = NDCStatusDescriptor.DEVICE_FAULT_OR_CONFIG_INFO;
    }


}
