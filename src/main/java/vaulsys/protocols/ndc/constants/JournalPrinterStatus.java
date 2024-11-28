package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;
import vaulsys.terminal.atm.device.DeviceStatus;

public class JournalPrinterStatus extends DeviceStatus implements IEnum {
	
	private static final char UNKNOWN_VALUE = '?';
	private static final char SUCCESSFUL_PRINT_VALUE = '0';
	private static final char PRINT_OPERATION_NOT_SUCCESS_COMPLETED_VALUE = '1';
	private static final char DEVICE_NOT_CONFIGURED_VALUE = '2';
	private static final char DEVICE_BACK_UP_ACTIVATED_VALUE = '6';
	private static final char DEVICE_BACK_UP_AND_REPRINT_TERMINATED_VALUE = '7';
	private static final char DEVICE_BACK_UP_REPRINT_STARTED_VALUE = '8';
	private static final char DEVICE_BACK_UP_HALTED_VALUE = '9';
	private static final char DEVICE_BACK_UP_LOG_SECURITY_ERROR_VALUE = ':';
	private static final char DEVICE_BACK_UP_REPRINT_HALTED_VALUE = ';';
	private static final char DEVICE_BACK_UP_TAMPER_STATE_ENTERED_VALUE = '<';
	private static final char JOURNAL_IN_DUAL_MODE_PRINT_OPERATION_SUCCESSFUL_VALUE = '=';
	private static final char JOURNAL_IN_DUAL_MODE_PRINT_OPERATION_NOT_SUCCESSFUL_VALUE = '>';
	
	public static JournalPrinterStatus UNKNOWN = new JournalPrinterStatus(UNKNOWN_VALUE);
	public static JournalPrinterStatus SUCCESSFUL_PRINT = new JournalPrinterStatus(SUCCESSFUL_PRINT_VALUE);
	public static JournalPrinterStatus PRINT_OPERATION_NOT_SUCCESS_COMPLETED = new JournalPrinterStatus(PRINT_OPERATION_NOT_SUCCESS_COMPLETED_VALUE);
	public static JournalPrinterStatus DEVICE_NOT_CONFIGURED = new JournalPrinterStatus(DEVICE_NOT_CONFIGURED_VALUE);
	public static JournalPrinterStatus DEVICE_BACK_UP_ACTIVATED = new JournalPrinterStatus(DEVICE_BACK_UP_ACTIVATED_VALUE);
	public static JournalPrinterStatus DEVICE_BACK_UP_AND_REPRINT_TERMINATED = new JournalPrinterStatus(DEVICE_BACK_UP_AND_REPRINT_TERMINATED_VALUE);
	public static JournalPrinterStatus DEVICE_BACK_UP_REPRINT_STARTED = new JournalPrinterStatus(DEVICE_BACK_UP_REPRINT_STARTED_VALUE);
	public static JournalPrinterStatus DEVICE_BACK_UP_HALTED = new JournalPrinterStatus(DEVICE_BACK_UP_HALTED_VALUE);
	public static JournalPrinterStatus DEVICE_BACK_UP_LOG_SECURITY_ERROR = new JournalPrinterStatus(DEVICE_BACK_UP_LOG_SECURITY_ERROR_VALUE);
	public static JournalPrinterStatus DEVICE_BACK_UP_REPRINT_HALTED = new JournalPrinterStatus(DEVICE_BACK_UP_REPRINT_HALTED_VALUE);
	public static JournalPrinterStatus DEVICE_BACK_UP_TAMPER_STATE_ENTERED = new JournalPrinterStatus(DEVICE_BACK_UP_TAMPER_STATE_ENTERED_VALUE);
	public static JournalPrinterStatus JOURNAL_IN_DUAL_MODE_PRINT_OPERATION_SUCCESSFUL = new JournalPrinterStatus(JOURNAL_IN_DUAL_MODE_PRINT_OPERATION_SUCCESSFUL_VALUE);
	public static JournalPrinterStatus JOURNAL_IN_DUAL_MODE_PRINT_OPERATION_NOT_SUCCESSFUL = new JournalPrinterStatus(JOURNAL_IN_DUAL_MODE_PRINT_OPERATION_NOT_SUCCESSFUL_VALUE);
	
    public JournalPrinterStatus() {
		super();
	}

	public JournalPrinterStatus(char status) {
		super(status);
	}

	public static JournalPrinterStatus getByCode(char code) {
    	if (code == '0')
    		return SUCCESSFUL_PRINT;
    	
    	if (code == '1')
    		return PRINT_OPERATION_NOT_SUCCESS_COMPLETED;
    	
    	if (code == '2')
    		return DEVICE_NOT_CONFIGURED;
    	
    	if (code == '6')
    		return DEVICE_BACK_UP_ACTIVATED;
    	
    	if (code == '7')
    		return DEVICE_BACK_UP_AND_REPRINT_TERMINATED;
    	
    	if (code == '8')
    		return DEVICE_BACK_UP_REPRINT_STARTED;
    	
    	if (code == '9')
    		return DEVICE_BACK_UP_HALTED;
    	
    	if (code == ':')
    		return DEVICE_BACK_UP_LOG_SECURITY_ERROR;
    	
    	if (code == ';')
    		return DEVICE_BACK_UP_REPRINT_HALTED;
    	
    	if (code == '<')
    		return DEVICE_BACK_UP_TAMPER_STATE_ENTERED;
    	
    	if (code == '=')
    		return JOURNAL_IN_DUAL_MODE_PRINT_OPERATION_SUCCESSFUL;
    	
    	if (code == '>')
    		return JOURNAL_IN_DUAL_MODE_PRINT_OPERATION_NOT_SUCCESSFUL;
    	
    	
    	return UNKNOWN;
    	
    	
    }
    
    @Override
	public String toString() {
    	if (getStatus() == '0')
    		return "SUCCESSFUL_PRINT";
    	
    	if (getStatus() == '1')
    		return "PRINT_OPERATION_NOT_SUCCESS_COMPLETED";
    	
    	if (getStatus() == '2')
    		return "DEVICE_NOT_CONFIGURED";
    	
    	if (getStatus() == '6')
    		return "DEVICE_BACK_UP_ACTIVATED";
    	
    	if (getStatus() == '7')
    		return "DEVICE_BACK_UP_AND_REPRINT_TERMINATED";
    	
    	if (getStatus() == '8')
    		return "DEVICE_BACK_UP_REPRINT_STARTED";
    	
    	if (getStatus() == '9')
    		return "DEVICE_BACK_UP_HALTED";
    	
    	if (getStatus() == ':')
    		return "DEVICE_BACK_UP_LOG_SECURITY_ERROR";
    	
    	if (getStatus() == ';')
    		return "DEVICE_BACK_UP_REPRINT_HALTED";
    	
    	if (getStatus() == '<')
    		return "DEVICE_BACK_UP_TAMPER_STATE_ENTERED";
    	
    	if (getStatus() == '=')
    		return "JOURNAL_IN_DUAL_MODE_PRINT_OPERATION_SUCCESSFUL";
    	
    	if (getStatus() == '>')
    		return "JOURNAL_IN_DUAL_MODE_PRINT_OPERATION_NOT_SUCCESSFUL";
    	
    	return "UNKNOWN";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getStatus();
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
		JournalPrinterStatus other = (JournalPrinterStatus) obj;
		if (getStatus() != other.getStatus())
			return false;
		return true;
	}
	
}
