package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.util.MyInteger;

public class NDCCardReaderWriterDidntTakeCard extends NDCCardReaderWriter {
    public NDCCardReaderWriterDidntTakeCard(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
    }
}
