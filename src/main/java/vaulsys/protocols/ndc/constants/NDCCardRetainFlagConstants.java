package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCCardRetainFlagConstants implements IEnum{
	
	private static final char UNKNOWN_VALUE = '?';
	private static final char EJECT_CARD_VALUE = '0';
	private static final char CRAD_CAPTURED_VALUE = '1';
	
	 public static NDCCardRetainFlagConstants UNKNOWN = new NDCCardRetainFlagConstants(UNKNOWN_VALUE);
	 public static NDCCardRetainFlagConstants EJECT_CARD = new NDCCardRetainFlagConstants(EJECT_CARD_VALUE);
	 public static NDCCardRetainFlagConstants CRAD_CAPTURED = new NDCCardRetainFlagConstants(CRAD_CAPTURED_VALUE);
	 
	 private char code;

	public NDCCardRetainFlagConstants() {
	}

	public NDCCardRetainFlagConstants(char code) {
		this.code = code;
	}

	public static NDCCardRetainFlagConstants getByBoolean(Boolean code) {
		if (code)
			return CRAD_CAPTURED;
		else 
			return EJECT_CARD;
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
		NDCCardRetainFlagConstants other = (NDCCardRetainFlagConstants) obj;
		if (code != other.code)
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (code == '0')
			return "EJECT_CARD";

		if (code == '1')
			return "CRAD_CAPTURED";

		return "UNKNOWN";
	}
	 
}
