package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCOperationalTypes implements IEnum{

	private static final char UNKNOWN_VALUE = '?';
    private static final char GO_IN_SERVICE_VALUE = '1';
    private static final char GO_OUT_OF_SERVICE_VALUE = '2';
    private static final char SEND_CONFIG_ID_VALUE = '3';
    private static final char SEND_SUPPLY_COUNTERS_VALUE = '4';
    private static final char SEND_TALLY_INFORMATION_VALUE = '5';
    private static final char SEND_ERROR_LOG_INFO_VALUE = '6';
    private static final char SEND_CONFIG_INFO_VALUE = '7';
    private static final char SEND_DATE_AND_TIME_VALUE = '8';
    
    public static NDCOperationalTypes UNKNOWN = new NDCOperationalTypes(UNKNOWN_VALUE);
    public static NDCOperationalTypes GO_IN_SERVICE = new NDCOperationalTypes(GO_IN_SERVICE_VALUE);
    public static NDCOperationalTypes GO_OUT_OF_SERVICE = new NDCOperationalTypes(GO_OUT_OF_SERVICE_VALUE);
    public static NDCOperationalTypes SEND_CONFIG_ID = new NDCOperationalTypes(SEND_CONFIG_ID_VALUE);
    public static NDCOperationalTypes SEND_SUPPLY_COUNTERS = new NDCOperationalTypes(SEND_SUPPLY_COUNTERS_VALUE);
    public static NDCOperationalTypes SEND_TALLY_INFORMATION = new NDCOperationalTypes(SEND_TALLY_INFORMATION_VALUE);
    public static NDCOperationalTypes SEND_ERROR_LOG_INFO = new NDCOperationalTypes(SEND_ERROR_LOG_INFO_VALUE);
    public static NDCOperationalTypes SEND_CONFIG_INFO = new NDCOperationalTypes(SEND_CONFIG_INFO_VALUE);
    public static NDCOperationalTypes SEND_DATE_AND_TIME = new NDCOperationalTypes(SEND_DATE_AND_TIME_VALUE);
    
    private char code;

    public NDCOperationalTypes() {
    }

    public NDCOperationalTypes(char code) {
        this.code = code;
    }

    public static NDCOperationalTypes getByCode(char type) {
    	if (type == '1')
    		return GO_IN_SERVICE;
    	
    	if (type == '2')
    		return GO_OUT_OF_SERVICE;
    	
    	if (type == '3')
    		return SEND_CONFIG_ID;

    	if (type == '4')
    		return SEND_SUPPLY_COUNTERS;
    	
    	if (type == '5')
    		return SEND_TALLY_INFORMATION;
    	
    	if (type == '6')
    		return SEND_ERROR_LOG_INFO;
    	
    	if (type == '7')
    		return SEND_CONFIG_INFO;

    	if (type == '8')
    		return SEND_DATE_AND_TIME;
    	
    	return UNKNOWN;
    }
    
	public char getCode() {
		return code;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NDCOperationalTypes other = (NDCOperationalTypes) obj;
		if (code != other.code)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		if (code == '1')
    		return "GO_IN_SERVICE";
    	
    	if (code == '2')
    		return "GO_OUT_OF_SERVICE";
    	
    	if (code == '3')
    		return "SEND_CONFIG_ID";

    	if (code == '4')
    		return "SEND_SUPPLY_COUNTERS";
    	
    	if (code == '5')
    		return "SEND_TALLY_INFORMATION";
    	
    	if (code == '6')
    		return "SEND_ERROR_LOG_INFO";
    	
    	if (code == '7')
    		return "SEND_CONFIG_INFO";

    	if (code == '8')
    		return "SEND_DATE_AND_TIME";
    	
    	return "UNKNOWN";
	}

}
