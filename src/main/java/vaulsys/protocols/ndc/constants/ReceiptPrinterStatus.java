package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;
import vaulsys.terminal.atm.device.DeviceStatus;

public class ReceiptPrinterStatus extends DeviceStatus implements IEnum {
	
	private static final char UNKNOWN_VALUE = '?';
	private static final char SUCCESSFUL_PRINT_VALUE = '0';
	private static final char PRINT_OPERATION_NOT_SUCCESS_COMPLETED_VALUE = '1';
	private static final char DEVICE_NOT_CONFIGURED_VALUE = '2';
	private static final char CANCEL_KEY_PRESSED_DURING_SIDEWAYS_RECEIPT_PRINT_VALUE = '4';
	
	public static ReceiptPrinterStatus UNKNOWN = new ReceiptPrinterStatus(UNKNOWN_VALUE);
	public static ReceiptPrinterStatus SUCCESSFUL_PRINT = new ReceiptPrinterStatus(SUCCESSFUL_PRINT_VALUE);
	public static ReceiptPrinterStatus PRINT_OPERATION_NOT_SUCCESS_COMPLETED = new ReceiptPrinterStatus(PRINT_OPERATION_NOT_SUCCESS_COMPLETED_VALUE);
	public static ReceiptPrinterStatus DEVICE_NOT_CONFIGURED = new ReceiptPrinterStatus(DEVICE_NOT_CONFIGURED_VALUE);
	public static ReceiptPrinterStatus CANCEL_KEY_PRESSED_DURING_SIDEWAYS_RECEIPT_PRINT = new ReceiptPrinterStatus(CANCEL_KEY_PRESSED_DURING_SIDEWAYS_RECEIPT_PRINT_VALUE);
	
    public ReceiptPrinterStatus() {
		super();
	}

	public ReceiptPrinterStatus(char status) {
		super(status);
	}

	public static ReceiptPrinterStatus getByCode(char code) {
    	if (code == '0')
    		return SUCCESSFUL_PRINT;
    	
    	if (code == '1')
    		return PRINT_OPERATION_NOT_SUCCESS_COMPLETED;
    	
    	if (code == '2')
    		return DEVICE_NOT_CONFIGURED;
    	
    	if (code == '4')
    		return CANCEL_KEY_PRESSED_DURING_SIDEWAYS_RECEIPT_PRINT;
    	
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
    	
    	if (getStatus() == '4')
    		return "CANCEL_KEY_PRESSED_DURING_SIDEWAYS_RECEIPT_PRINT";
    	
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
		ReceiptPrinterStatus other = (ReceiptPrinterStatus) obj;
		if (getStatus() != other.getStatus())
			return false;
		return true;
	}
	
}
