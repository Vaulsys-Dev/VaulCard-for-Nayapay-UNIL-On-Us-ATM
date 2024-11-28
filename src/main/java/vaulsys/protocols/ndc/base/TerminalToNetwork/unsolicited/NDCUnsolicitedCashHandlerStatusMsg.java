package vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCashHandler;
import vaulsys.util.MyInteger;

public class NDCUnsolicitedCashHandlerStatusMsg extends NDCUnsolicitedStatusMsg<NDCCashHandler> {

    public NDCUnsolicitedCashHandlerStatusMsg() {
    }

    public NDCUnsolicitedCashHandlerStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
        statusInformation = new NDCCashHandler(rawdata, offset);
    }
}