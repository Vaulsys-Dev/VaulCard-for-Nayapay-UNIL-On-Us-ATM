package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCMessageClassNetworkToTerminal implements NDCMessageClass, IEnum {
   
	private static final char UNKNOWN_VALUE = '?';
	private static final char TERMINAL_COMMAND_VALUE = '1';
	private static final char CUSTOMIZATION_DATA_COMMAND_VALUE = '3';
	private static final char TRANSACTION_REPLY_COMMAND_VALUE = '2';
	private static final char EJ_COMMAND_VALUE = '2';
	private static final char MESSAGE_FROM_HOST_TO_EXIT_VALUE = '2';
    
    public static NDCMessageClassNetworkToTerminal UNKNOWN = new NDCMessageClassNetworkToTerminal(UNKNOWN_VALUE);
    public static NDCMessageClassNetworkToTerminal TERMINAL_COMMAND = new NDCMessageClassNetworkToTerminal(TERMINAL_COMMAND_VALUE);
    public static NDCMessageClassNetworkToTerminal CUSTOMIZATION_DATA_COMMAND = new NDCMessageClassNetworkToTerminal(CUSTOMIZATION_DATA_COMMAND_VALUE);
    public static NDCMessageClassNetworkToTerminal TRANSACTION_REPLY_COMMAND = new NDCMessageClassNetworkToTerminal(TRANSACTION_REPLY_COMMAND_VALUE);
    public static NDCMessageClassNetworkToTerminal EJ_COMMAND = new NDCMessageClassNetworkToTerminal(EJ_COMMAND_VALUE);
    public static NDCMessageClassNetworkToTerminal MESSAGE_FROM_HOST_TO_EXIT = new NDCMessageClassNetworkToTerminal(MESSAGE_FROM_HOST_TO_EXIT_VALUE);
    
    private char code;

    public NDCMessageClassNetworkToTerminal() {
    }

    public NDCMessageClassNetworkToTerminal(char code) {
        this.code = code;
    }

    public static NDCMessageClassNetworkToTerminal getByCode(char type) {
    	if (type == '1')
    		return TERMINAL_COMMAND;
    	
    	if (type == '3')
    		return CUSTOMIZATION_DATA_COMMAND;
    	
    	if (type == '2')
    		return TRANSACTION_REPLY_COMMAND;
    	
    	if (type == '2')
    		return EJ_COMMAND;
    	
    	if (type == '2')
    		return MESSAGE_FROM_HOST_TO_EXIT;
    	
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
		NDCMessageClassNetworkToTerminal other = (NDCMessageClassNetworkToTerminal) obj;
		if (code != other.code)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		if (code == '1')
    		return "TERMINAL_COMMAND";
    	
    	if (code == '3')
    		return "CUSTOMIZATION_DATA_COMMAND";
    	
    	if (code == '2')
    		return "TRANSACTION_REPLY_COMMAND";
    	
    	if (code == '2')
    		return "EJ_COMMAND";
    	
    	if (code == '2')
    		return "MESSAGE_FROM_HOST_TO_EXIT";
    	
    	
    	return "UNKNOWN";
	}
}
