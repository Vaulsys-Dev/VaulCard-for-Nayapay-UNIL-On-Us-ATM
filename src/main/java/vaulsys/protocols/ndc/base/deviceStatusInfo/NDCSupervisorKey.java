package vaulsys.protocols.ndc.base.deviceStatusInfo;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.constants.NDCSupervisorKeyOptionDigitConstants;
import vaulsys.util.MyInteger;

public class NDCSupervisorKey extends NDCDeviceStatusInfo {
    public NDCSupervisorKeyOptionDigitConstants optionDigit;

    public static NDCSupervisorKey fromBinary(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
    	NDCSupervisorKeyOptionDigitConstants od = NDCSupervisorKeyOptionDigitConstants.getByCode((char)rawdata[offset.value + 1]);
         
    	if (NDCSupervisorKeyOptionDigitConstants.KEY_SELECTION.equals(od))
                return new NDCSupervisorKeySelection(rawdata, offset);
    	
    	if (NDCSupervisorKeyOptionDigitConstants.MAIN_MENU.equals(od))
                return new NDCSupervisorKeyMainManu(rawdata, offset);
    	
        if (NDCSupervisorKeyOptionDigitConstants.ADDITIONAL_SUB_MENU_1.equals(od))
                return new NDCSupervisorKeyAdditionalSubMenu(rawdata, offset);
        
        if (NDCSupervisorKeyOptionDigitConstants.ADDITIONAL_SUB_MENU_2.equals(od))
        	return new NDCSupervisorKeyAdditionalSubMenu(rawdata, offset);
       
        return null;
    }
    
    @Override
    public String toString() {
    	return super.toString() +
    		"optionDigit:\t\t" + optionDigit.toString() + "\r\n"
    	;
    }

}
