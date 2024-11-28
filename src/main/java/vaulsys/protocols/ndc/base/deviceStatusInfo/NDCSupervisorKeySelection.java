package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.constants.NDCSupervisorKeyOptionDigitConstants;
import vaulsys.util.MyInteger;

public class NDCSupervisorKeySelection extends NDCSupervisorKey {
    
	public String keySelection;
	
	public NDCSupervisorKeySelection(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
		optionDigit = NDCSupervisorKeyOptionDigitConstants.KEY_SELECTION;
		offset.value += 2;
    	keySelection = new String(rawdata, offset.value, rawdata.length-offset.value);
        offset.value += 2;
    }
	
	@Override
	public String toString() {
		return super.toString() +
			"keySelection:\t\t" + keySelection + "\r\n";
	}
}
