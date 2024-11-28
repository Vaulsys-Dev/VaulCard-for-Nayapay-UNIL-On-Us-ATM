package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCMessageClassSolicitedUnSokicited implements IEnum {
   
	private static final char UNKNOWN_VALUE = '?';
	private static final char UNSOLICITED_MESSAGE_VALUE = '1';
	private static final char SOLICITED_MESSAGE_VALUE = '2';
    
    public static NDCMessageClassSolicitedUnSokicited UNKNOWN = new NDCMessageClassSolicitedUnSokicited(UNKNOWN_VALUE);
    public static NDCMessageClassSolicitedUnSokicited UNSOLICITED_MESSAGE = new NDCMessageClassSolicitedUnSokicited(UNSOLICITED_MESSAGE_VALUE);
    public static NDCMessageClassSolicitedUnSokicited SOLICITED_MESSAGE = new NDCMessageClassSolicitedUnSokicited(SOLICITED_MESSAGE_VALUE);
    
    private char code;

    public NDCMessageClassSolicitedUnSokicited() {
    }

    public NDCMessageClassSolicitedUnSokicited(char code) {
        this.code = code;
    }

    public static NDCMessageClassSolicitedUnSokicited getByCode(char type) {
    	if (type == '1')
    		return UNSOLICITED_MESSAGE;
    	
    	if (type == '2')
    		return SOLICITED_MESSAGE;
    	
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
		NDCMessageClassSolicitedUnSokicited other = (NDCMessageClassSolicitedUnSokicited) obj;
		if (code != other.code)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		if (code == '1')
    		return "UNSOLICITED_MESSAGE";
    	
    	if (code == '2')
    		return "SOLICITED_MESSAGE";
    	
    	
    	return "UNKNOWN";
	}
}
