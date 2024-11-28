package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCSupplyStatusConstants implements IEnum{
	
	private static final char UNKNOWN_VALUE = '?';
	private static final char NOT_CONFIGURED_VALUE = '0';
	private static final char GOOD_STATE_VALUE = '1';
	private static final char MEDIA_LOW_VALUE = '2';
	private static final char MEDIA_OUT_VALUE = '3';
	private static final char OVERFILL_VALUE = '4';
	
	 public static NDCSupplyStatusConstants UNKNOWN = new NDCSupplyStatusConstants(UNKNOWN_VALUE);
	 public static NDCSupplyStatusConstants NOT_CONFIGURED = new NDCSupplyStatusConstants(NOT_CONFIGURED_VALUE);
	 public static NDCSupplyStatusConstants GOOD_STATE = new NDCSupplyStatusConstants(GOOD_STATE_VALUE);
	 public static NDCSupplyStatusConstants MEDIA_LOW = new NDCSupplyStatusConstants(MEDIA_LOW_VALUE);
	 public static NDCSupplyStatusConstants MEDIA_OUT = new NDCSupplyStatusConstants(MEDIA_OUT_VALUE);
	 public static NDCSupplyStatusConstants OVERFILL = new NDCSupplyStatusConstants(OVERFILL_VALUE);

	 private char code;

	public NDCSupplyStatusConstants() {
	}

	public NDCSupplyStatusConstants(char code) {
		this.code = code;
	}

	public char getCode() {
		return code;
	}
	
	public static NDCSupplyStatusConstants getByCode(char code) {
		if (code == '0')
			return NOT_CONFIGURED;

		if (code == '1')
			return GOOD_STATE;
		
		if (code == '2')
			return MEDIA_LOW;

		if (code == '3')
			return MEDIA_OUT;

		if (code == '4')
			return OVERFILL;

		return UNKNOWN;
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
		NDCSupplyStatusConstants other = (NDCSupplyStatusConstants) obj;
		if (code != other.code)
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (code == '0')
			return "NOT_CONFIGURED";

		if (code == '1')
			return "GOOD_STATE";
		
		if (code == '2')
			return "MEDIA_LOW";

		if (code == '3')
			return "MEDIA_OUT";

		if (code == '4')
			return "OVERFILL";

		return "UNKNOWN";
	}
	 
}
