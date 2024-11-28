package vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCSupervisorKey;
import vaulsys.util.MyInteger;

public class NDCUnsolicitedSupervisorKeyStatusMsg extends NDCUnsolicitedStatusMsg {

    public NDCUnsolicitedSupervisorKeyStatusMsg() {
    }

    public NDCUnsolicitedSupervisorKeyStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
        statusInformation = NDCSupervisorKey.fromBinary(rawdata, offset);
    }
}