package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class RejectBinSupplyStatus implements IEnum {
	
	private static final char UNKNOWN_VALUE = '?';
	private static final char NO_NEW_STATE_VALUE = '0';
	private static final char NO_OVERFILL_CONDITION_VALUE = '1';
	private static final char OVERFILL_CONDITION_VALUE = '4';
	
	 public static RejectBinSupplyStatus UNKNOWN = new RejectBinSupplyStatus(UNKNOWN_VALUE);
	 public static RejectBinSupplyStatus NO_NEW_STATE = new RejectBinSupplyStatus(NO_NEW_STATE_VALUE);
	 public static RejectBinSupplyStatus NO_OVERFILL_CONDITION = new RejectBinSupplyStatus(NO_OVERFILL_CONDITION_VALUE);
	 public static RejectBinSupplyStatus OVERFILL_CONDITION = new RejectBinSupplyStatus(OVERFILL_CONDITION_VALUE);

	 private char code;

	public RejectBinSupplyStatus() {
	}

	public RejectBinSupplyStatus(char code) {
		this.code = code;
	}

	public char getCode() {
		return code;
	}
	
	public static RejectBinSupplyStatus getByCode(char code) {
		if (code == '0')
			return NO_NEW_STATE;

		if (code == '1')
			return NO_OVERFILL_CONDITION;
		
		if (code == '4')
			return OVERFILL_CONDITION;

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
		RejectBinSupplyStatus other = (RejectBinSupplyStatus) obj;
		if (code != other.code)
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (code == '0')
			return "NO_NEW_STATE";

		if (code == '1')
			return "NO_OVERFILL_CONDITION";
		
		if (code == '4')
			return "OVERFILL_CONDITION";

		return "UNKNOWN";
	}
	 
}
