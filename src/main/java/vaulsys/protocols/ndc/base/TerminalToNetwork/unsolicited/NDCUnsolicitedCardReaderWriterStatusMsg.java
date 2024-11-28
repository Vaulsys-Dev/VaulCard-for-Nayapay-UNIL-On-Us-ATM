package vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCCardReaderWriter;
import vaulsys.util.MyInteger;

public class NDCUnsolicitedCardReaderWriterStatusMsg extends NDCUnsolicitedStatusMsg<NDCCardReaderWriter> {

    public NDCUnsolicitedCardReaderWriterStatusMsg() {
    }

    public NDCUnsolicitedCardReaderWriterStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
        statusInformation = NDCCardReaderWriter.fromBinary(rawdata, offset);
    }
}