package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.constants.NDCDeviceIdentifier;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyInteger;

public class NDCEnvelopeDepository extends NDCDeviceStatusInfo {
	public String deviceStatus;
	public String errorSeverity;
	public String Mstatus;
	public String Mdata;
	public String suppliesStatus;

	public NDCEnvelopeDepository(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
		deviceIdentifier = NDCDeviceIdentifier.getByCode((char) rawdata[offset.value++]);
		deviceStatus = NDCParserUtils.readUntilFS(rawdata, offset);
		NDCParserUtils.readFS(rawdata, offset);
		errorSeverity = NDCParserUtils.readUntilFS(rawdata, offset);
		NDCParserUtils.readFS(rawdata, offset);
		Mstatus = new String(rawdata, offset.value, 2);
		offset.value += 2;
		Mdata = NDCParserUtils.readUntilFS(rawdata, offset);
		NDCParserUtils.readFS(rawdata, offset);
		suppliesStatus = NDCParserUtils.readUntilFS(rawdata, offset);
		// NDCParserUtils.readFS(rawdata, offset);
	}

}
