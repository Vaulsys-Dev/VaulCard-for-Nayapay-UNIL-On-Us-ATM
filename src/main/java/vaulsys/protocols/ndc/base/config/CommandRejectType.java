package vaulsys.protocols.ndc.base.config;

import vaulsys.persistence.IEnum;

public class CommandRejectType implements IEnum {

	private static final char UNKNOWN_VALUE = 0;
	private static final char MAC_FAILURE_VALUE = '1';
	private static final char TIME_VARIANT_NUMBER_FAILURE_VALUE = '2';
	private static final char SECURITY_TERMINAL_NUMBER_MISMATCH_VALUE = '3';
	private static final char MESSAGE_FORMAT_ERROR_VALUE = 'A';
	private static final char FIELD_VALUE_ERROR_VALUE = 'B';
	private static final char ILLEGAL_MESSAGE_TYPE_FOR_CURRENT_MODE_VALUE = 'C';
	private static final char HARDWARE_FAILURE_VALUE = 'D';
	private static final char NOT_SUPPORTED_VALUE = 'E';
	
	public static final CommandRejectType UNKNOWN = new CommandRejectType(UNKNOWN_VALUE);
	public static final CommandRejectType MAC_FAILURE = new CommandRejectType(MAC_FAILURE_VALUE);
	public static final CommandRejectType TIME_VARIANT_NUMBER_FAILURE = new CommandRejectType(TIME_VARIANT_NUMBER_FAILURE_VALUE);
	public static final CommandRejectType SECURITY_TERMINAL_NUMBER_MISMATCH = new CommandRejectType(SECURITY_TERMINAL_NUMBER_MISMATCH_VALUE);
	public static final CommandRejectType MESSAGE_FORMAT_ERROR = new CommandRejectType(MESSAGE_FORMAT_ERROR_VALUE);
	public static final CommandRejectType FIELD_VALUE_ERROR = new CommandRejectType(FIELD_VALUE_ERROR_VALUE);
	public static final CommandRejectType ILLEGAL_MESSAGE_TYPE_FOR_CURRENT_MODE = new CommandRejectType(ILLEGAL_MESSAGE_TYPE_FOR_CURRENT_MODE_VALUE);
	public static final CommandRejectType HARDWARE_FAILURE = new CommandRejectType(HARDWARE_FAILURE_VALUE);
	public static final CommandRejectType NOT_SUPPORTED = new CommandRejectType(NOT_SUPPORTED_VALUE);
	
	private char type;
	
    public CommandRejectType() {
    }

    public static CommandRejectType getByType(char type) {
    	
    	if(type == '1')
    		return MAC_FAILURE;
    	
    	if (type == '2')
    		return TIME_VARIANT_NUMBER_FAILURE;
    	
    	if (type == '3')
    		return SECURITY_TERMINAL_NUMBER_MISMATCH;
    	
    	if (type == 'A')
    		return MESSAGE_FORMAT_ERROR;

    	if (type == 'B')
    		return FIELD_VALUE_ERROR;
    	
    	if (type == 'C')
    		return ILLEGAL_MESSAGE_TYPE_FOR_CURRENT_MODE;
    	
    	if (type == 'D')
    		return HARDWARE_FAILURE;
    	
    	if (type == 'E')
    		return NOT_SUPPORTED;
    	
    	
    	return UNKNOWN;
    }
    
    
    public boolean hasStatusQualifier(){
    	return (type == 'A')||(type == 'B')|| (type == 'C')|| (type == 'D')||(type == 'E');
    }
    
    public CommandRejectType(char type) {
        this.type = type;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandRejectType that = (CommandRejectType) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type;
    }

	public char getType() {
		return type;
	}

	@Override
	public String toString() {
		if(type == '1')
    		return "MAC_FAILURE("+type+")";
    	
    	if (type == '2')
    		return "TIME_VARIANT_NUMBER_FAILURE("+type+")";
    	
    	if (type == '3')
    		return "SECURITY_TERMINAL_NUMBER_MISMATCH("+type+")";
		
		if (type == 'A')
    		return "MESSAGE_FORMAT_ERROR("+type+")";

    	if (type == 'B')
    		return "FIELD_VALUE_ERROR("+type+")";
    	
    	if (type == 'C')
    		return "ILLEGAL_MESSAGE_TYPE_FOR_CURRENT_MODE("+type+")";
    	
    	if (type == 'D')
    		return "HARDWARE_FAILURE("+type+")";
    	
    	if (type == 'E')
    		return "NOT_SUPPORTED("+type+")";
    	
    	return "UNKNOWN("+type+")";
	}
}
