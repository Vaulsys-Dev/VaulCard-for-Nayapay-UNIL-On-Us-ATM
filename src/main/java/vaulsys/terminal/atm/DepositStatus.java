package vaulsys.terminal.atm;

import javax.persistence.Embeddable;


import vaulsys.persistence.IEnum;

@Embeddable
public class DepositStatus implements IEnum {

	private static final int UNDEFINED_VALUE = 0;
	private static final int HAVING_DEPOSIT_VALUE = 1;
	private static final int NOT_HAVING_DEPOSIT_VALUE = 2;
	
	public static DepositStatus UNDEFINED = new DepositStatus(UNDEFINED_VALUE);
	public static DepositStatus HAVING_DEPOSIT = new DepositStatus(HAVING_DEPOSIT_VALUE);
	public static DepositStatus NOT_HAVING_DEPOSIT = new DepositStatus(NOT_HAVING_DEPOSIT_VALUE);
	
	private int code;

	public DepositStatus() {
		// TODO Auto-generated constructor stub
	}
	
	public DepositStatus(int code) {
		this.code = code;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || !(o instanceof DepositStatus))
			return false;
		DepositStatus that = (DepositStatus) o;
		return code == that.code;
	}


	public int getCode() {
		return code;
	}
	
	@Override
	public String toString() {
		if (code == UNDEFINED_VALUE)
			return "ØªØ¹Ø±ÛŒÙ� Ù†Ø´Ø¯Ù‡";
		if (code == HAVING_DEPOSIT_VALUE)
			return "Ø®ÙˆØ¯ Ø¯Ø±ÛŒØ§Ù�Øª Ø¯Ø§Ø±Ø¯";
		if (code == NOT_HAVING_DEPOSIT_VALUE)
			return "Ø®ÙˆØ¯ Ø¯Ø±ÛŒØ§Ù�Øª Ù†Ø¯Ø§Ø±Ø¯";
	
		return "Ù†Ø§Ø´Ù†Ø§Ø®ØªÙ‡";
	}
	public String toStringEnglish() {
		if(code == UNDEFINED_VALUE)
			return "UNDEFINED";
		if(code == HAVING_DEPOSIT_VALUE)
			return "HAVING DEPOSIT";
		if(code == NOT_HAVING_DEPOSIT_VALUE)
			return "NOT HAVING DEPOSIT";
		
		return "UNKNOWN";
	}
	@Override
	public int hashCode() {
		return code;
	}
	
}
