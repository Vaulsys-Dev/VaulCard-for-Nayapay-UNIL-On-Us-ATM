package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class CassetteSupplyStatus implements IEnum{
	
	private static final char UNKNOWN_VALUE = '?';
	private static final char NO_NEW_STATE_VALUE = '0';
	private static final char SUFFICIENT_NOTES_VALUE = '1';
	private static final char NOTES_LOW_VALUE = '2';
	private static final char OUT_OF_NOTES_VALUE = '3';
	
	 public static CassetteSupplyStatus UNKNOWN = new CassetteSupplyStatus(UNKNOWN_VALUE);
	 public static CassetteSupplyStatus NO_NEW_STATE = new CassetteSupplyStatus(NO_NEW_STATE_VALUE);
	 public static CassetteSupplyStatus SUFFICIENT_NOTES = new CassetteSupplyStatus(SUFFICIENT_NOTES_VALUE);
	 public static CassetteSupplyStatus NOTES_LOW = new CassetteSupplyStatus(NOTES_LOW_VALUE);
	 public static CassetteSupplyStatus OUT_OF_NOTES = new CassetteSupplyStatus(OUT_OF_NOTES_VALUE);

	 private char code;

	public CassetteSupplyStatus() {
	}

	public CassetteSupplyStatus(char code) {
		this.code = code;
	}

	public char getCode() {
		return code;
	}
	
	public static CassetteSupplyStatus getByCode(char code) {
		if (code == '0')
			return NO_NEW_STATE;

		if (code == '1')
			return SUFFICIENT_NOTES;
		
		if (code == '2')
			return NOTES_LOW;

		if (code == '3')
			return OUT_OF_NOTES;

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
		CassetteSupplyStatus other = (CassetteSupplyStatus) obj;
		if (code != other.code)
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (code == '0')
			return "NO_NEW_STATE";

		if (code == '1')
			return "SUFFICIENT_NOTES";
		
		if (code == '2')
			return "NOTES_LOW";

		if (code == '3')
			return "OUT_OF_NOTES";

		return "UNKNOWN";
	}
	 
}
