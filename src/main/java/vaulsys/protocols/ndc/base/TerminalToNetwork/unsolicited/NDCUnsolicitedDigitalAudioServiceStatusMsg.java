package vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCDigitalAudioService;
import vaulsys.util.MyInteger;

public class NDCUnsolicitedDigitalAudioServiceStatusMsg extends NDCUnsolicitedStatusMsg<NDCDigitalAudioService> {

    public NDCUnsolicitedDigitalAudioServiceStatusMsg() {
    }

    public NDCUnsolicitedDigitalAudioServiceStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
        statusInformation = new NDCDigitalAudioService(rawdata, offset);
    }
}