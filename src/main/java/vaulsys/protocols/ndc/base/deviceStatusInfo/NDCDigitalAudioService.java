package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyInteger;

public class NDCDigitalAudioService extends NDCDeviceStatusInfo {
    public String deviceStatus;
    public String errorSeverity;
    public String diagnosticStatus;
    public String suppliesStatus;

    public NDCDigitalAudioService(byte[] rawdata, MyInteger offset)
            throws NotParsedBinaryToProtocolException {
        deviceIdentifier = NDCDeviceIdentifier.getByCode((char)rawdata[offset.value++]);
        deviceStatus = NDCParserUtils.readUntilFS(rawdata, offset);
        NDCParserUtils.readFS(rawdata, offset);
        errorSeverity = NDCParserUtils.readUntilFS(rawdata, offset);
        NDCParserUtils.readFS(rawdata, offset);
        diagnosticStatus = NDCParserUtils.readUntilFS(rawdata, offset);
        NDCParserUtils.readFS(rawdata, offset);
        suppliesStatus = NDCParserUtils.readUntilFS(rawdata, offset);
        NDCParserUtils.readFS(rawdata, offset);
    }

}
