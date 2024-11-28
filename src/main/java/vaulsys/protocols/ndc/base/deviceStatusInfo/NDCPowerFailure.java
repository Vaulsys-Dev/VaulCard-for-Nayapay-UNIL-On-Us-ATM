package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.util.MyInteger;

public class NDCPowerFailure extends NDCDeviceStatusInfo {

    public String configId;

    public NDCPowerFailure(byte[] rawdata, MyInteger offset)
            throws NotParsedBinaryToProtocolException {
        deviceIdentifier = NDCDeviceIdentifier.getByCode((char)rawdata[offset.value++]);
        configId = new String(rawdata, offset.value, 4);
        offset.value += 4;
    }

}
