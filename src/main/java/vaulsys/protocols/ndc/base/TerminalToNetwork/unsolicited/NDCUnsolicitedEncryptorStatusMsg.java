package vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCEncryptor;
import vaulsys.util.MyInteger;

public class NDCUnsolicitedEncryptorStatusMsg extends NDCUnsolicitedStatusMsg<NDCEncryptor> {

    public NDCUnsolicitedEncryptorStatusMsg() {
    }

    public NDCUnsolicitedEncryptorStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
        statusInformation = NDCEncryptor.fromBinary(rawdata, offset);
    }
}