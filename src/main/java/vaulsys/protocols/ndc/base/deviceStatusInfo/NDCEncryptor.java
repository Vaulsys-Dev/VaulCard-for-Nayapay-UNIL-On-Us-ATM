package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.constants.EncryptorStatus;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyInteger;

public class NDCEncryptor extends NDCDeviceStatusInfo {
    public EncryptorStatus deviceStatus;
    public ErrorSeverity errorSeverity;
    public String Mstatus;
    public String Mdata;
    public byte suppliesStatus; // the state of the card capture bin

    public static NDCEncryptor fromBinary(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
    	return new NDCEncryptor(rawdata, offset);
        /*byte status = rawdata[offset.value + 1];
        switch (status) {
            case '1':
                return new NDCEncryptorError(rawdata, offset);
            case '2':
                return new NDCEncryptorNotConfigured(rawdata, offset);
        }
        return null;*/
    }

    public NDCEncryptor(byte[] rawdata, MyInteger offset)
            throws NotParsedBinaryToProtocolException {
        deviceIdentifier = NDCDeviceIdentifier.getByCode((char)rawdata[offset.value++]);
        deviceStatus = EncryptorStatus.getByCode((char) rawdata[offset.value++]);
        if (offset.value < rawdata.length) {
            NDCParserUtils.readFS(rawdata, offset);
            errorSeverity = ErrorSeverity.getByCode((char) rawdata[offset.value++]);
            NDCParserUtils.readFS(rawdata, offset);
            Mstatus = new String(rawdata, offset.value, 2);
            offset.value += 2;
            Mdata = NDCParserUtils.readUntilFS(rawdata, offset);
//            NDCParserUtils.readFS(rawdata, offset);
//            suppliesStatus = rawdata[offset.value++];
        }
    }


    public String toString() {
        return super.toString() + "deviceStatus:\t\t" + deviceStatus + "\r\n" + "Severity device:\t\t" + errorSeverity
                + "\r\n" + "MStatusDiagnostic:\t\t" + Mstatus + "\r\n" + "MDataDiagnostic:\t\t"
                + Mdata + "\r\n";
    }

}
