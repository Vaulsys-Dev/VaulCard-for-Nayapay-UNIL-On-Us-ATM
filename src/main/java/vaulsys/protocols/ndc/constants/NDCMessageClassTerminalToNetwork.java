package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCMessageClassTerminalToNetwork implements NDCMessageClass, IEnum {
   
	private static final char UNKNOWN_VALUE = '?';
	private static final char CONSUMER_REQUEST_OPERATIONAL_MESSAGE_VALUE = '1';
	private static final char STATUS_MESSAGE_VALUE = '2';
	private static final char WRITE_COMMAND_VALUE = '3';
	private static final char FUNCTION_COMMAND_VALUE = '4';
	
	private static final char ENCRYPTOR_INITIALISATION_DATA_VALUE = '3';
    
    public static NDCMessageClassTerminalToNetwork UNKNOWN = new NDCMessageClassTerminalToNetwork(UNKNOWN_VALUE);
    public static NDCMessageClassTerminalToNetwork CONSUMER_REQUEST_OPERATIONAL_MESSAGE = new NDCMessageClassTerminalToNetwork(CONSUMER_REQUEST_OPERATIONAL_MESSAGE_VALUE);
    public static NDCMessageClassTerminalToNetwork STATUS_MESSAGE = new NDCMessageClassTerminalToNetwork(STATUS_MESSAGE_VALUE);
    public static NDCMessageClassTerminalToNetwork WRITE_COMMAND = new NDCMessageClassTerminalToNetwork(WRITE_COMMAND_VALUE);
    public static NDCMessageClassTerminalToNetwork FUNCTION_COMMAND = new NDCMessageClassTerminalToNetwork(FUNCTION_COMMAND_VALUE);
    
    public static NDCMessageClassTerminalToNetwork ENCRYPTOR_INITIALISATION_DATA = new NDCMessageClassTerminalToNetwork(ENCRYPTOR_INITIALISATION_DATA_VALUE);

    private char code;

    public NDCMessageClassTerminalToNetwork() {
    }

    public NDCMessageClassTerminalToNetwork(char code) {
        this.code = code;
    }

    public static NDCMessageClassTerminalToNetwork getByCode(char type) {
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
		NDCMessageClassTerminalToNetwork other = (NDCMessageClassTerminalToNetwork) obj;
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
