package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.protocols.ndc.constants.NDCSupplyStatusConstants;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyInteger;

public class NDCCardReaderWriter extends NDCDeviceStatusInfo {
    public byte deviceStatus;
//    public byte errorSeverity;
    public ErrorSeverity errorSeverity;
    public String Mstatus;
    public String Mdata;
    public NDCSupplyStatusConstants suppliesStatus; // the state of the card capture bin

	public NDCCardReaderWriter() {
		super();
	}
    public static NDCCardReaderWriter fromBinary(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        byte status = rawdata[offset.value + 1];
        switch (status) {
            case '0':
                return new NDCCardReaderWriterNoException(rawdata, offset);
            case '1':
                return new NDCCardReaderWriterDidntTakeCard(rawdata, offset);
            case '2':
                return new NDCCardReaderWriterFailedEjectCard(rawdata, offset);
            case '3':
                return new NDCCardReaderWriterFailedUpdateTrack3(rawdata, offset);
            case '4':
                return new NDCCardReaderWriterInvalidTrack3(rawdata, offset);
        }
        return null;
    }

    public NDCCardReaderWriter(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
		
    	deviceIdentifier = NDCDeviceIdentifier.getByCode((char) rawdata[offset.value++]);
		deviceStatus = rawdata[offset.value++];
		
		if (offset.value < rawdata.length) {
			NDCParserUtils.readFS(rawdata, offset);
			// errorSeverity = rawdata[offset.value++];
			errorSeverity = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
			NDCParserUtils.readFS(rawdata, offset);
			Mstatus = new String(rawdata, offset.value, 2);
			offset.value += 2;
			Mdata = NDCParserUtils.readUntilFS(rawdata, offset);
			NDCParserUtils.readFS(rawdata, offset);
			suppliesStatus = NDCSupplyStatusConstants.getByCode((char)rawdata[offset.value++]);
		}
	}

    public static NDCCardReaderWriter getSuppliesStatus(byte[] rawdata, MyInteger offset){
    	NDCCardReaderWriter cardReaderWriter = new NDCCardReaderWriter();
    	cardReaderWriter.suppliesStatus = NDCSupplyStatusConstants.getByCode((char)rawdata[offset.value++]);
    	return cardReaderWriter;
    }
    
    public static NDCCardReaderWriter getFitnessStatus(byte[] rawdata, MyInteger offset){
    	NDCCardReaderWriter cardReaderWriter = new NDCCardReaderWriter();
    	cardReaderWriter.errorSeverity = ErrorSeverity.getByCode((char)rawdata[offset.value++]);
    	return cardReaderWriter;
    }
}
