package vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCoinDispencer;
import vaulsys.util.MyInteger;

public class NDCUnsolicitedCoinDispenserStatusMsg extends NDCUnsolicitedStatusMsg<NDCCoinDispencer> {

    public NDCUnsolicitedCoinDispenserStatusMsg() {
    }

    public NDCUnsolicitedCoinDispenserStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
        statusInformation = new NDCCoinDispencer(rawdata, offset);
    }
}