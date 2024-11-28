package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCFunctionIdentifierConstants implements IEnum{
	
	private static final char UNKNOWN_VALUE = '?';
	private static final char DEPOSIT_AND_PRINT_VALUE = '1';
	private static final char DISPENSE_AND_PRINT_VALUE = '2';
	private static final char DISPLAY_AND_PRINT_VALUE = '3';
	private static final char PRINT_IMMEDIATE_VALUE = '4';
	private static final char SET_NEXT_STATE_AND_PRINT_VALUE = '5';
	private static final char NIGHT_SAFE_DEPOSIT_AND_PRINT_VALUE = '6';
	private static final char CARD_BEFORE_CACH_VALUE = 'A';
	private static final char PARALLEL_DISPENSE_AND_PRINT_AND_CARD_EJECT_VALUE = 'B';
	private static final char CARD_BEFORE_PARALLEL_DISPENSE_PRINT_VALUE = 'F';
	private static final char PRINT_STATEMENT_AND_WAIT_VALUE = 'P';
	private static final char PRINT_STATEMENT_AND_SET_NEXT_STATE_VALUE = 'Q';
	private static final char PROCESS_DOCUMENT_WITH_CASH_VALUE = 'S';
	private static final char DP_ATM_DEPOSIT_ENVELOPE_VALUE = 'T';
	
	 public static NDCFunctionIdentifierConstants UNKNOWN = new NDCFunctionIdentifierConstants(UNKNOWN_VALUE);
	 public static NDCFunctionIdentifierConstants DEPOSIT_AND_PRINT = new NDCFunctionIdentifierConstants(DEPOSIT_AND_PRINT_VALUE);
	 public static NDCFunctionIdentifierConstants DISPENSE_AND_PRINT = new NDCFunctionIdentifierConstants(DISPENSE_AND_PRINT_VALUE);
	 public static NDCFunctionIdentifierConstants DISPLAY_AND_PRINT = new NDCFunctionIdentifierConstants(DISPLAY_AND_PRINT_VALUE);
	 public static NDCFunctionIdentifierConstants PRINT_IMMEDIATE = new NDCFunctionIdentifierConstants(PRINT_IMMEDIATE_VALUE);
	 public static NDCFunctionIdentifierConstants SET_NEXT_STATE_AND_PRINT = new NDCFunctionIdentifierConstants(SET_NEXT_STATE_AND_PRINT_VALUE);
	 public static NDCFunctionIdentifierConstants NIGHT_SAFE_DEPOSIT_AND_PRINT = new NDCFunctionIdentifierConstants(NIGHT_SAFE_DEPOSIT_AND_PRINT_VALUE);
	 public static NDCFunctionIdentifierConstants CARD_BEFORE_CACH = new NDCFunctionIdentifierConstants(CARD_BEFORE_CACH_VALUE);
	 public static NDCFunctionIdentifierConstants PARALLEL_DISPENSE_AND_PRINT_AND_CARD_EJECT = new NDCFunctionIdentifierConstants(PARALLEL_DISPENSE_AND_PRINT_AND_CARD_EJECT_VALUE);
	 public static NDCFunctionIdentifierConstants CARD_BEFORE_PARALLEL_DISPENSE_PRINT = new NDCFunctionIdentifierConstants(CARD_BEFORE_PARALLEL_DISPENSE_PRINT_VALUE);
	 public static NDCFunctionIdentifierConstants PRINT_STATEMENT_AND_WAIT = new NDCFunctionIdentifierConstants(PRINT_STATEMENT_AND_WAIT_VALUE);
	 public static NDCFunctionIdentifierConstants PRINT_STATEMENT_AND_SET_NEXT_STATE = new NDCFunctionIdentifierConstants(PRINT_STATEMENT_AND_SET_NEXT_STATE_VALUE);
	 public static NDCFunctionIdentifierConstants PROCESS_DOCUMENT_WITH_CASH = new NDCFunctionIdentifierConstants(PROCESS_DOCUMENT_WITH_CASH_VALUE);
	 public static NDCFunctionIdentifierConstants DP_ATM_DEPOSIT_ENVELOPE = new NDCFunctionIdentifierConstants(DP_ATM_DEPOSIT_ENVELOPE_VALUE);
	 
	 private char code;

	public NDCFunctionIdentifierConstants() {
	}

	public NDCFunctionIdentifierConstants(char code) {
		this.code = code;
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
		NDCFunctionIdentifierConstants other = (NDCFunctionIdentifierConstants) obj;
		if (code != other.code)
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (code == '1')
			return "DEPOSIT_AND_PRINT";

		if (code == '2')
			return "DISPENSE_AND_PRINT";

		if (code == '3')
			return "DISPLAY_AND_PRINT";

		if (code == '4')
			return "PRINT_IMMEDIATE";

		if (code == '5')
			return "SET_NEXT_STATE_AND_PRINT";

		if (code == '6')
			return "NIGHT_SAFE_DEPOSIT_AND_PRINT";

		if (code == 'A')
			return "CARD_BEFORE_CACH";

		if (code == 'B')
			return "PARALLEL_DISPENSE_AND_PRINT_AND_CARD_EJECT";

		if (code == 'F')
			return "CARD_BEFORE_PARALLEL_DISPENSE_PRINT";

		if (code == 'P')
			return "PRINT_STATEMENT_AND_WAIT";

		if (code == 'Q')
			return "PRINT_STATEMENT_AND_SET_NEXT_STATE";

		if (code == 'S')
			return "PROCESS_DOCUMENT_WITH_CASH";
		
		if (code == 'T')
			return "DP_ATM_DEPOSIT_ENVELOPE";

		return "UNKNOWN";
	}
	 
}
