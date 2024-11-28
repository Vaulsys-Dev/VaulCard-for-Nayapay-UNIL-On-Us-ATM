package vaulsys.protocols.ndc.base;

import vaulsys.persistence.IEnum;

public class NDCMessageIdentifierTypes implements IEnum{

    private static final char UNKNOWN_VALUE = '0';
    private static final char CONFIGURATION_INFO_VALUE = '1';
    private static final char SUPPLY_COUNTERS_VALUE = '2';
    private static final char TALLY_INFO_VALUE = '3';
    private static final char ERROR_LOG_VALUE = '4';
    private static final char DATE_AND_TIME_VALUE = '5';
    private static final char SEND_CONFIG_ID_VALUE = '6';
    private static final char HADWARE_CONFIG_VALUE = 'H';
    private static final char SUPPLIES_VALUE = 'I';
    private static final char FITNESS_VALUE = 'J';
    private static final char TAMPER_AND_SENSOR_VALUE = 'K';
    private static final char SOFTWARE_ID_VALUE = 'L';
    private static final char LOCAL_CONFIGURATION_VALUE = 'M';
    
    public static NDCMessageIdentifierTypes UNKNOWN = new NDCMessageIdentifierTypes(UNKNOWN_VALUE);
    public static NDCMessageIdentifierTypes CONFIGURATION_INFO = new NDCMessageIdentifierTypes(CONFIGURATION_INFO_VALUE);
    public static NDCMessageIdentifierTypes SUPPLY_COUNTERS = new NDCMessageIdentifierTypes(SUPPLY_COUNTERS_VALUE);
    public static NDCMessageIdentifierTypes SEND_CONFIG_ID = new NDCMessageIdentifierTypes(SEND_CONFIG_ID_VALUE);
    public static NDCMessageIdentifierTypes TALLY_INFO = new NDCMessageIdentifierTypes(TALLY_INFO_VALUE);
    public static NDCMessageIdentifierTypes ERROR_LOG = new NDCMessageIdentifierTypes(ERROR_LOG_VALUE);
    public static NDCMessageIdentifierTypes DATE_AND_TIME = new NDCMessageIdentifierTypes(DATE_AND_TIME_VALUE);
    public static NDCMessageIdentifierTypes HADWARE_CONFIG = new NDCMessageIdentifierTypes(HADWARE_CONFIG_VALUE);
    public static NDCMessageIdentifierTypes SUPPLIES = new NDCMessageIdentifierTypes(SUPPLIES_VALUE);
    public static NDCMessageIdentifierTypes FITNESS = new NDCMessageIdentifierTypes(FITNESS_VALUE);
    public static NDCMessageIdentifierTypes TAMPER_AND_SENSOR = new NDCMessageIdentifierTypes(TAMPER_AND_SENSOR_VALUE);
    public static NDCMessageIdentifierTypes SOFTWARE_ID = new NDCMessageIdentifierTypes(SOFTWARE_ID_VALUE);
    public static NDCMessageIdentifierTypes LOCAL_CONFIGURATION = new NDCMessageIdentifierTypes(LOCAL_CONFIGURATION_VALUE);
    
    private char code;

    public NDCMessageIdentifierTypes() {
    }

    public NDCMessageIdentifierTypes(char code) {
        this.code = code;
    }

    public static NDCMessageIdentifierTypes getByCode(char type) {
    	switch(type){
    	case CONFIGURATION_INFO_VALUE:
    		return CONFIGURATION_INFO;
    		
    	case SUPPLY_COUNTERS_VALUE:
    		return SUPPLY_COUNTERS;
    		
    	case TALLY_INFO_VALUE:
    		return TALLY_INFO;
    		
    	case ERROR_LOG_VALUE:
    		return ERROR_LOG;
    		
    	case DATE_AND_TIME_VALUE:
    		return DATE_AND_TIME;
    		
    	case SEND_CONFIG_ID_VALUE:
    		return SEND_CONFIG_ID;
    		
    	case HADWARE_CONFIG_VALUE:
    		return HADWARE_CONFIG;
    		
    	case SUPPLIES_VALUE:
    		return SUPPLIES;
    		
    	case FITNESS_VALUE:
    		return FITNESS;
    		
    	case TAMPER_AND_SENSOR_VALUE:
    		return TAMPER_AND_SENSOR;
    		
    	case SOFTWARE_ID_VALUE:
    		return SOFTWARE_ID;
    		
    	case LOCAL_CONFIGURATION_VALUE:
    		return LOCAL_CONFIGURATION;
    	}
    	
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
		NDCMessageIdentifierTypes other = (NDCMessageIdentifierTypes) obj;
		if (code != other.code)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return code + "";
	}
    
}