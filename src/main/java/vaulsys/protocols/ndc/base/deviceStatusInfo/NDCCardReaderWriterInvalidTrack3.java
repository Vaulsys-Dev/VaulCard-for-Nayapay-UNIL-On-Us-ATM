package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.util.MyInteger;

public class NDCCardReaderWriterInvalidTrack3 extends NDCCardReaderWriter {
    public NDCCardReaderWriterInvalidTrack3(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
    }
}
