package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCPrinterFlag implements IEnum {
	
	private static final char UNKNOWN_VALUE = '?';
	private static final char DONT_PRINT_VALUE = '0';
	private static final char PRINT_ON_JOURNAL_PRINTER_ONLY_VALUE = '1';
	private static final char PRINT_ON_CUSTOMER_PRINTER_ONLY_VALUE = '2';
	private static final char PRINT_ON_BOTH_PRINTERS_VALUE = '3';
	private static final char PRINT_ON_DEPOSIT_VALUE = '4';
	private static final char PRINT_SIDEWAY_ON_RECEIPT_VALUE = '=';
	
	public static NDCPrinterFlag UNKNOWN = new NDCPrinterFlag(UNKNOWN_VALUE);
	public static NDCPrinterFlag DONT_PRINT = new NDCPrinterFlag(DONT_PRINT_VALUE);
	public static NDCPrinterFlag PRINT_ON_JOURNAL_PRINTER_ONLY = new NDCPrinterFlag(PRINT_ON_JOURNAL_PRINTER_ONLY_VALUE);
	public static NDCPrinterFlag PRINT_ON_CUSTOMER_PRINTER_ONLY = new NDCPrinterFlag(PRINT_ON_CUSTOMER_PRINTER_ONLY_VALUE);
	public static NDCPrinterFlag PRINT_ON_BOTH_PRINTERS = new NDCPrinterFlag(PRINT_ON_BOTH_PRINTERS_VALUE);
	public static NDCPrinterFlag PRINT_ON_DEPOSIT = new NDCPrinterFlag(PRINT_ON_DEPOSIT_VALUE);
	public static NDCPrinterFlag PRINT_SIDEWAY_ON_RECEIPT = new NDCPrinterFlag(PRINT_SIDEWAY_ON_RECEIPT_VALUE);

	private char code;

	public char getCode() {
		return code;
	}
	
    public NDCPrinterFlag() {
    }

    public NDCPrinterFlag(char code) {
        this.code = code;
    }
    
    @Override
	public String toString() {
		return code + "";
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
		NDCPrinterFlag other = (NDCPrinterFlag) obj;
		if (code != other.code)
			return false;
		return true;
	}
	
}
