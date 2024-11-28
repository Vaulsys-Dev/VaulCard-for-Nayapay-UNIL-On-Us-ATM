package vaulsys.protocols.ndc.base.config;

import vaulsys.persistence.IEnum;

public class ErrorSeverity implements IEnum {

	private static final char UNKNOWN_VALUE = '?';
	private static final char NO_ERROR_VALUE = '0';
	private static final char ROUTINE_ERROR_VALUE = '1';
	private static final char WARNING_VALUE = '2';
	private static final char SUSPEND_VALUE = '3';
	private static final char FATAL_VALUE = '4';
	
	public static final ErrorSeverity UNKNOWN = new ErrorSeverity(UNKNOWN_VALUE);
	public static final ErrorSeverity NO_ERROR = new ErrorSeverity(NO_ERROR_VALUE);
	public static final ErrorSeverity ROUTINE_ERROR = new ErrorSeverity(ROUTINE_ERROR_VALUE);
	public static final ErrorSeverity WARNING = new ErrorSeverity(WARNING_VALUE);
	public static final ErrorSeverity SUSPEND = new ErrorSeverity(SUSPEND_VALUE);
	public static final ErrorSeverity FATAL = new ErrorSeverity(FATAL_VALUE);
	
	private char code;
	
    public ErrorSeverity() {
    }

    public static ErrorSeverity getByCode(char type) {
    	if (type == '0')
    		return NO_ERROR;

    	if (type == '1')
    		return ROUTINE_ERROR;
    	
    	if (type == '2')
    		return WARNING;
    	
    	if (type == '3')
    		return SUSPEND;
    	
    	if (type == '4')
    		return FATAL;
    	
    	return UNKNOWN;
    }
    
    public ErrorSeverity(char type) {
        this.code = type;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorSeverity that = (ErrorSeverity) o;
        return code == that.code;
    }

    @Override
    public int hashCode() {
        return code;
    }

	public char getCode() {
		return code;
	}

	@Override
	public String toString() {
		if (code == '0')
    		return "NO_ERROR";

    	if (code == '1')
    		return "ROUTINE_ERROR";
    	
    	if (code == '2')
    		return "WARNING";
    	
    	if (code == '3')
    		return "SUSPEND";
    	
    	if (code == '4')
    		return "FATAL";
    	
    	return "UNKNOWN";
	}
}
