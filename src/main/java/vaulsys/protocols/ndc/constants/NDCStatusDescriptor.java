package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCStatusDescriptor implements IEnum{
	
	private static final char UNKNOWN_VALUE = '?';
    private static final char DEVICE_FAULT_OR_CONFIG_INFO_VALUE = '8';
    private static final char READY_VALUE = '9';
    private static final char COMMAND_REJECT_VALUE = 'A';
    private static final char READY_SEPERATE_VALUE = 'B';
    private static final char SPECIFIC_COMMAND_REJECT_VALUE = 'C';
    private static final char TERMINAL_STATE_VALUE = 'F';
    
    public static final NDCStatusDescriptor UNKNOWN = new NDCStatusDescriptor(UNKNOWN_VALUE);
    public static final NDCStatusDescriptor DEVICE_FAULT_OR_CONFIG_INFO = new NDCStatusDescriptor(DEVICE_FAULT_OR_CONFIG_INFO_VALUE);
    public static final NDCStatusDescriptor READY = new NDCStatusDescriptor(READY_VALUE);
    public static final NDCStatusDescriptor COMMAND_REJECT = new NDCStatusDescriptor(COMMAND_REJECT_VALUE);
    public static final NDCStatusDescriptor READY_SEPERATE = new NDCStatusDescriptor(READY_SEPERATE_VALUE);
    public static final NDCStatusDescriptor SPECIFIC_COMMAND_REJECT = new NDCStatusDescriptor(SPECIFIC_COMMAND_REJECT_VALUE);
    public static final NDCStatusDescriptor TERMINAL_STATE = new NDCStatusDescriptor(TERMINAL_STATE_VALUE);
    
    private char type;
	
    public NDCStatusDescriptor() {
    }

    public static NDCStatusDescriptor getByType(char type) {
    	if (type == '8')
    		return DEVICE_FAULT_OR_CONFIG_INFO;

    	if (type == '9')
    		return READY;
    	
    	if (type == 'A')
    		return COMMAND_REJECT;
    	
    	if (type == 'B')
    		return READY_SEPERATE;
    	
    	if (type == 'C')
    		return SPECIFIC_COMMAND_REJECT;
    	
    	if (type == 'F')
    		return TERMINAL_STATE;
    	
    	return UNKNOWN;
    }
    
    public NDCStatusDescriptor(char type) {
        this.type = type;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NDCStatusDescriptor that = (NDCStatusDescriptor) o;
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
		if (type == '8')
    		return "DEVICE_FAULT_OR_CONFIG_INFO";

    	if (type == '9')
    		return "READY";
    	
    	if (type == 'A')
    		return "COMMAND_REJECT";
    	
    	if (type == 'B')
    		return "READY_SEPERATE";
    	
    	if (type == 'C')
    		return "SPECIFIC_COMMAND_REJECT";
    	
    	if (type == 'F')
    		return "TERMINAL_STATE";
    	
    	return "UNKNOWN";
	}
}
