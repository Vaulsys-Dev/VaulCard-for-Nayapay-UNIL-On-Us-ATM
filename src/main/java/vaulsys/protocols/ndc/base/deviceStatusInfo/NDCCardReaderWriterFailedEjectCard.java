package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.util.MyInteger;

public class NDCCardReaderWriterFailedEjectCard extends NDCCardReaderWriter {
    public NDCCardReaderWriterFailedEjectCard(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
    }
}
