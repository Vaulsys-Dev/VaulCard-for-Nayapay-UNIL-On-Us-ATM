package vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCPowerFailure;
import vaulsys.util.MyInteger;

public class NDCUnsolicitedPowerFailureStatusMsg extends NDCUnsolicitedStatusMsg<NDCPowerFailure> {

    public NDCUnsolicitedPowerFailureStatusMsg() {
    }

    public NDCUnsolicitedPowerFailureStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
        statusInformation = new NDCPowerFailure(rawdata, offset);
    }
}