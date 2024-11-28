package vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCSupervisorKey;
import vaulsys.util.MyInteger;

public class NDCUnsolicitedSecurityCameraStatusMsg extends NDCUnsolicitedStatusMsg {

    public NDCUnsolicitedSecurityCameraStatusMsg() {
    }

    public NDCUnsolicitedSecurityCameraStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
//        statusInformation = NDCSupervisorKey.fromBinary(rawdata, offset);
    }
}