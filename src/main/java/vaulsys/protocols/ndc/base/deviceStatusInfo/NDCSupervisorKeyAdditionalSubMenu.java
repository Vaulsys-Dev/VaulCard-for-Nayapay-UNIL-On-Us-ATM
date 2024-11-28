package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.constants.NDCSupervisorKeyMenuTypeConstants;
import vaulsys.protocols.ndc.constants.NDCSupervisorKeyOptionDigitConstants;
import vaulsys.util.MyInteger;

public class NDCSupervisorKeyAdditionalSubMenu extends NDCSupervisorKey {
    
	public NDCSupervisorKeyMenuTypeConstants menuType;
	public String menuItem;
	public String subMenu;
	public String subMenuOfSubMenu;
	
	public NDCSupervisorKeyAdditionalSubMenu(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
		offset.value ++;
		optionDigit = NDCSupervisorKeyOptionDigitConstants.getByCode((char)rawdata[offset.value]);
		offset.value ++;
		menuType = NDCSupervisorKeyMenuTypeConstants.getByCode((char)rawdata[offset.value]);
        offset.value ++;
        menuItem = new String(rawdata, offset.value, 1);
        offset.value += 1;
        
        if (rawdata.length > offset.value) {
        	menuItem += new String(rawdata, offset.value, 1);
        	offset.value += 1;
        }
        
        if (rawdata.length > offset.value) {
	        subMenu = new String(rawdata, offset.value, 2);
	        offset.value += 2;
        }
        
        if (rawdata.length > offset.value) {
	        subMenuOfSubMenu = new String(rawdata, offset.value, 2);
	        offset.value += 2;
        }
    }
	
	@Override
	public String toString() {
		return super.toString() +
			"menuType:\t\t" + menuType.toString() + "\r\n" +
			"menuItem:\t\t" + menuItem + "\r\n" +
			"subMenu:\t\t" + subMenu + "\r\n" +
			"subMenuOfSubMenu:\t\t" + subMenuOfSubMenu + "\r\n"
			;
	}
}
