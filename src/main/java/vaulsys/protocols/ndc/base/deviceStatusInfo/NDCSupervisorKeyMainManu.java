package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.constants.NDCSupervisorKeyMenuTypeConstants;
import vaulsys.protocols.ndc.constants.NDCSupervisorKeyOptionDigitConstants;
import vaulsys.util.MyInteger;

public class NDCSupervisorKeyMainManu extends NDCSupervisorKey {
    
	public NDCSupervisorKeyMenuTypeConstants menuType;
	public String menuItem;
	
	public NDCSupervisorKeyMainManu(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
		optionDigit = NDCSupervisorKeyOptionDigitConstants.MAIN_MENU;
		offset.value += 2;
		menuType = NDCSupervisorKeyMenuTypeConstants.getByCode((char)rawdata[offset.value]);
        offset.value ++;
        menuItem = new String(rawdata, offset.value, 1);
        offset.value += 1;
        
        if (rawdata.length > offset.value) {
        	menuItem += new String(rawdata, offset.value, 1);
        	offset.value += 1;
        }
    }
	
	@Override
	public String toString() {
		return super.toString() +
			"menuType:\t\t" + menuType.toString() + "\r\n" +
			"menuItem:\t\t" + menuItem + "\r\n"
			;
	}
}
