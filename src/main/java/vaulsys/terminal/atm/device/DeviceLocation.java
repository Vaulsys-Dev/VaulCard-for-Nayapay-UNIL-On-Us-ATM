package vaulsys.terminal.atm.device;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class DeviceLocation implements IEnum {

	
    private static final char UNKOWN_VALUE = '?';
    private static final char OUT_VALUE = '0';
    private static final char IN_VALUE = '1';

    public static DeviceLocation UNKOWN = new DeviceLocation(UNKOWN_VALUE);
    public static DeviceLocation IN = new DeviceLocation(IN_VALUE);
    public static DeviceLocation OUT = new DeviceLocation(OUT_VALUE);

    private char status;

    public DeviceLocation() {
    }

    public DeviceLocation(char status) {
        this.status = status;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }
    
    public static DeviceLocation getByChar(char status) {
    	if (status == '0')
    		return OUT;
    	
    	if (status == '1')
    		return IN;
    	
    	return UNKOWN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceLocation that = (DeviceLocation) o;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        return status;
    }
    
    @Override
    public String toString() {
    	if (status == '0')
    		return "LOCATION_OUT";
    	
    	if (status == '1')
    		return "LOCATION_IN";
    	
    	return "UNKOWN";
    }
}
