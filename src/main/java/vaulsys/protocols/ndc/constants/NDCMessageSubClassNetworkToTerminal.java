package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCMessageSubClassNetworkToTerminal implements NDCMessageSubClass, IEnum {
   
	private static final char UNKNOWN_VALUE = '?';
	private static final char CONSUMER_REQUEST_OPERATIONAL_MESSAGE_VALUE = '1';
	private static final char STATUS_MESSAGE_VALUE = '2';
	private static final char WRITE_COMMAND_VALUE = '3';
	private static final char FUNCTION_COMMAND_VALUE = '4';
    
    public static NDCMessageSubClassNetworkToTerminal UNKNOWN = new NDCMessageSubClassNetworkToTerminal(UNKNOWN_VALUE);
    public static NDCMessageSubClassNetworkToTerminal CONSUMER_REQUEST_OPERATIONAL_MESSAGE = new NDCMessageSubClassNetworkToTerminal(CONSUMER_REQUEST_OPERATIONAL_MESSAGE_VALUE);
    public static NDCMessageSubClassNetworkToTerminal STATUS_MESSAGE = new NDCMessageSubClassNetworkToTerminal(STATUS_MESSAGE_VALUE);
    public static NDCMessageSubClassNetworkToTerminal WRITE_COMMAND = new NDCMessageSubClassNetworkToTerminal(WRITE_COMMAND_VALUE);
    public static NDCMessageSubClassNetworkToTerminal FUNCTION_COMMAND = new NDCMessageSubClassNetworkToTerminal(FUNCTION_COMMAND_VALUE);
    
    private char code;

    public NDCMessageSubClassNetworkToTerminal() {
    }

    public NDCMessageSubClassNetworkToTerminal(char code) {
        this.code = code;
    }

    public static NDCMessageSubClassNetworkToTerminal getByCode(char type) {
    	if (type == '1')
    		return CONSUMER_REQUEST_OPERATIONAL_MESSAGE;
    	
    	if (type == '2')
    		return STATUS_MESSAGE;
    	
    	if (type == '3')
    		return WRITE_COMMAND;
    	
    	if (type == '4')
    		return FUNCTION_COMMAND;
    	
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
		NDCMessageSubClassNetworkToTerminal other = (NDCMessageSubClassNetworkToTerminal) obj;
		if (code != other.code)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		if (code == '1')
    		return "CONSUMER_REQUEST_OPERATIONAL_MESSAGE";
    	
    	if (code == '2')
    		return "STATUS_MESSAGE";
    	
    	if (code == '3')
    		return "WRITE_COMMAND";
    	
    	if (code == '4')
    		return "FUNCTION_COMMAND";
    	
    	return "UNKNOWN";
	}
}
