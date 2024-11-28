package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCSupervisorKeyOptionDigitConstants implements IEnum{
	
	private static final char UNKNOWN_VALUE = '?';
	private static final char KEY_SELECTION_VALUE = '0';
	private static final char MAIN_MENU_VALUE = '1';
	private static final char ADDITIONAL_SUB_MENU_1_VALUE = '2';
	private static final char ADDITIONAL_SUB_MENU_2_VALUE = '3';
	
	 public static NDCSupervisorKeyOptionDigitConstants UNKNOWN = new NDCSupervisorKeyOptionDigitConstants(UNKNOWN_VALUE);
	 public static NDCSupervisorKeyOptionDigitConstants KEY_SELECTION = new NDCSupervisorKeyOptionDigitConstants(KEY_SELECTION_VALUE);
	 public static NDCSupervisorKeyOptionDigitConstants MAIN_MENU = new NDCSupervisorKeyOptionDigitConstants(MAIN_MENU_VALUE);
	 public static NDCSupervisorKeyOptionDigitConstants ADDITIONAL_SUB_MENU_1 = new NDCSupervisorKeyOptionDigitConstants(ADDITIONAL_SUB_MENU_1_VALUE);
	 public static NDCSupervisorKeyOptionDigitConstants ADDITIONAL_SUB_MENU_2 = new NDCSupervisorKeyOptionDigitConstants(ADDITIONAL_SUB_MENU_2_VALUE);
	 
	 private char code;

	public NDCSupervisorKeyOptionDigitConstants() {
	}

	public NDCSupervisorKeyOptionDigitConstants(char code) {
		this.code = code;
	}

	public static NDCSupervisorKeyOptionDigitConstants getByCode(char code) {
		if (code == '0')
			return KEY_SELECTION;
		
		if (code == '1')
			return MAIN_MENU;
		
		if (code == '2')
			return ADDITIONAL_SUB_MENU_1;
		
		if (code == '3')
			return ADDITIONAL_SUB_MENU_2;
		
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
		NDCSupervisorKeyOptionDigitConstants other = (NDCSupervisorKeyOptionDigitConstants) obj;
		if (code != other.code)
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (code == '0')
			return "KEY_SELECTION";

		if (code == '1')
			return "MAIN_MENU";
		
		if (code == '2')
			return "ADDITIONAL_SUB_MENU_1";

		if (code == '3')
			return "ADDITIONAL_SUB_MENU_2";
		
		return "UNKNOWN";
	}
	 
}
