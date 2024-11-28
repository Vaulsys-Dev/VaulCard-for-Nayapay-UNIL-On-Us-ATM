package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCSupervisorKeyMenuTypeConstants implements IEnum{
	
	private static final char UNKNOWN_VALUE = '?';
	private static final char SELECT_MENU_VALUE = '0';
	private static final char REPLENISHMENT_MENU_VALUE = '1';
	private static final char CONFIGURE_MENU_VALUE = '2';
	private static final char ACCESS_MENU_VALUE = '3';
	private static final char IN_SERVICE_SUPERVISOR_MENU_VALUE = '4';
	private static final char EXIT_MENU_1_VALUE = '7';
	private static final char EXIT_MENU_2_VALUE = '8';
	
	 public static NDCSupervisorKeyMenuTypeConstants UNKNOWN = new NDCSupervisorKeyMenuTypeConstants(UNKNOWN_VALUE);
	 public static NDCSupervisorKeyMenuTypeConstants SELECT_MENU = new NDCSupervisorKeyMenuTypeConstants(SELECT_MENU_VALUE);
	 public static NDCSupervisorKeyMenuTypeConstants REPLENISHMENT_MENU = new NDCSupervisorKeyMenuTypeConstants(REPLENISHMENT_MENU_VALUE);
	 public static NDCSupervisorKeyMenuTypeConstants CONFIGURE_MENU = new NDCSupervisorKeyMenuTypeConstants(CONFIGURE_MENU_VALUE);
	 public static NDCSupervisorKeyMenuTypeConstants ACCESS_MENU = new NDCSupervisorKeyMenuTypeConstants(ACCESS_MENU_VALUE);
	 public static NDCSupervisorKeyMenuTypeConstants IN_SERVICE_SUPERVISOR_MENU = new NDCSupervisorKeyMenuTypeConstants(IN_SERVICE_SUPERVISOR_MENU_VALUE);
	 public static NDCSupervisorKeyMenuTypeConstants EXIT_MENU_1 = new NDCSupervisorKeyMenuTypeConstants(EXIT_MENU_1_VALUE);
	 public static NDCSupervisorKeyMenuTypeConstants EXIT_MENU_2 = new NDCSupervisorKeyMenuTypeConstants(EXIT_MENU_2_VALUE);
	 
	 private char code;

	public NDCSupervisorKeyMenuTypeConstants() {
	}

	public NDCSupervisorKeyMenuTypeConstants(char code) {
		this.code = code;
	}

	public static NDCSupervisorKeyMenuTypeConstants getByCode(char code) {
		if (code == '0')
			return SELECT_MENU;
		
		if (code == '1')
			return REPLENISHMENT_MENU;
		
		if (code == '2')
			return CONFIGURE_MENU;
		
		if (code == '3')
			return ACCESS_MENU;
		
		if (code == '4')
			return IN_SERVICE_SUPERVISOR_MENU;
		
		if (code == '7')
			return EXIT_MENU_1;
		
		if (code == '8')
			return EXIT_MENU_2;
		
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
		NDCSupervisorKeyMenuTypeConstants other = (NDCSupervisorKeyMenuTypeConstants) obj;
		if (code != other.code)
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (code == '0')
			return "SELECT_MENU";

		if (code == '1')
			return "REPLENISHMENT_MENU";
		
		if (code == '2')
			return "CONFIGURE_MENU";

		if (code == '3')
			return "ACCESS_MENU";
		
		if (code == '4')
			return "IN_SERVICE_SUPERVISOR_MENU";
		
		if (code == '7')
			return "EXIT_MENU_1";
		
		if (code == '8')
			return "EXIT_MENU_2";
		
		return "UNKNOWN";
	}
	 
}
