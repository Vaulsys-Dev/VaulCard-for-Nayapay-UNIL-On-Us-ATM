package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyInteger;

public class NDCDoorAccess extends NDCDeviceStatusInfo {

    public byte deviceStatus;
    public byte errorSeverity;
    public String Mstatus;
    public String Mdata;
    public byte suppliesStatus; // the state of the card capture bin

    public NDCDoorAccess(byte[] rawdata, MyInteger offset)
            throws NotParsedBinaryToProtocolException {
        deviceIdentifier = NDCDeviceIdentifier.getByCode((char)rawdata[offset.value++]);
        deviceStatus = rawdata[offset.value++];
        NDCParserUtils.readFS(rawdata, offset);
        errorSeverity = rawdata[offset.value++];
        if (offset.value < rawdata.length) {
            NDCParserUtils.readFS(rawdata, offset);
            Mstatus = new String(rawdata, offset.value, 2);
            offset.value += 2;
            Mdata = NDCParserUtils.readUntilFS(rawdata, offset);
        }
    }

}
