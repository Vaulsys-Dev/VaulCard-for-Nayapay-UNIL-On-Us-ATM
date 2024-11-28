package vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCDoorAccess;
import vaulsys.util.MyInteger;

public class NDCUnsolicitedDoorAccessStatusMsg extends NDCUnsolicitedStatusMsg<NDCDoorAccess> {

    public NDCUnsolicitedDoorAccessStatusMsg() {
    }

    public NDCUnsolicitedDoorAccessStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
        statusInformation = new NDCDoorAccess(rawdata, offset);
    }
}