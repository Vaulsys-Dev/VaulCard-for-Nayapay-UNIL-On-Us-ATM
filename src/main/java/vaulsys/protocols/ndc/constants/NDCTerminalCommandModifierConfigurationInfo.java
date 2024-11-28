package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCTerminalCommandModifierConfigurationInfo implements NDCTerminalCommandModifier,IEnum{
	private static final char UNKNOWN_VALUE = '?';
    private static final char SEND_HARDWARE_CONFIG_DATA_ONLY_VALUE = '1';
    private static final char SEND_SUPPLIES_DATA_ONLY_VALUE = '2';
    private static final char SEND_FITNESS_DATA_ONLY_VALUE = '3';
    private static final char SEND_TAMPER_AND_SENSOR_STATUS_DATA_ONLY_VALUE = '4';
    private static final char SEND_SOFTWARE_RELEASE_DATA_ONLY_VALUE = '5';
    private static final char SEND_ENHANCED_CONFIGURATION_DATA_VALUE = '6';
    private static final char SEND_LOCAL_CONFIGURATION_OPTION_VALUE = '7';
    
    public static NDCTerminalCommandModifierConfigurationInfo UNKNOWN = new NDCTerminalCommandModifierConfigurationInfo(UNKNOWN_VALUE);
    public static NDCTerminalCommandModifierConfigurationInfo SEND_HARDWARE_CONFIG_DATA_ONLY = new NDCTerminalCommandModifierConfigurationInfo(SEND_HARDWARE_CONFIG_DATA_ONLY_VALUE);
    public static NDCTerminalCommandModifierConfigurationInfo SEND_SUPPLIES_DATA_ONLY = new NDCTerminalCommandModifierConfigurationInfo(SEND_SUPPLIES_DATA_ONLY_VALUE);
    public static NDCTerminalCommandModifierConfigurationInfo SEND_FITNESS_DATA_ONLY = new NDCTerminalCommandModifierConfigurationInfo(SEND_FITNESS_DATA_ONLY_VALUE);
    public static NDCTerminalCommandModifierConfigurationInfo SEND_TAMPER_AND_SENSOR_STATUS_DATA_ONLY = new NDCTerminalCommandModifierConfigurationInfo(SEND_TAMPER_AND_SENSOR_STATUS_DATA_ONLY_VALUE);
    public static NDCTerminalCommandModifierConfigurationInfo SEND_SOFTWARE_RELEASE_DATA_ONLY = new NDCTerminalCommandModifierConfigurationInfo(SEND_SOFTWARE_RELEASE_DATA_ONLY_VALUE);
    public static NDCTerminalCommandModifierConfigurationInfo SEND_ENHANCED_CONFIGURATION_DATA = new NDCTerminalCommandModifierConfigurationInfo(SEND_ENHANCED_CONFIGURATION_DATA_VALUE);
    public static NDCTerminalCommandModifierConfigurationInfo SEND_LOCAL_CONFIGURATION_OPTION = new NDCTerminalCommandModifierConfigurationInfo(SEND_LOCAL_CONFIGURATION_OPTION_VALUE);
    
    private char code;

    public NDCTerminalCommandModifierConfigurationInfo() {
    }

    public NDCTerminalCommandModifierConfigurationInfo(char code) {
        this.code = code;
    }

    public static NDCTerminalCommandModifierConfigurationInfo getByCode(char type) {
    	if (type == '1')
    		return SEND_HARDWARE_CONFIG_DATA_ONLY;
    	
    	if (type == '2')
    		return SEND_SUPPLIES_DATA_ONLY;
    	
    	if (type == '3')
    		return SEND_FITNESS_DATA_ONLY;

    	if (type == '4')
    		return SEND_TAMPER_AND_SENSOR_STATUS_DATA_ONLY;
    	
    	if (type == '5')
    		return SEND_SOFTWARE_RELEASE_DATA_ONLY;
    	
    	if (type == '6')
    		return SEND_ENHANCED_CONFIGURATION_DATA;
    	
    	if (type == '7')
    		return SEND_LOCAL_CONFIGURATION_OPTION;

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
		NDCTerminalCommandModifierConfigurationInfo other = (NDCTerminalCommandModifierConfigurationInfo) obj;
		if (code != other.code)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		if (code == '1')
    		return "SEND_HARDWARE_CONFIG_DATA_ONLY";
    	
    	if (code == '2')
    		return "SEND_SUPPLIES_DATA_ONLY";
    	
    	if (code == '3')
    		return "SEND_FITNESS_DATA_ONLY";

    	if (code == '4')
    		return "SEND_TAMPER_AND_SENSOR_STATUS_DATA_ONLY";
    	
    	if (code == '5')
    		return "SEND_SOFTWARE_RELEASE_DATA_ONLY";
    	
    	if (code == '6')
    		return "SEND_ENHANCED_CONFIGURATION_DATA";
    	
    	if (code == '7')
    		return "SEND_LOCAL_CONFIGURATION_OPTION";

    	return "UNKNOWN";
	}
}
