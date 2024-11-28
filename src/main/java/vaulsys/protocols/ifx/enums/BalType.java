package vaulsys.protocols.ifx.enums;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class BalType implements Cloneable, Serializable{

	private static final int UNKNOWN_VALUE = -1;
	private static final int LEDGER_VALUE = 0;
	private static final int OPENINGLEDGER_VALUE = 1;
	private static final int CLOSINGLEDGER_VALUE = 2;
	private static final int MINLEDGER_VALUE = 3;
	private static final int AVGLEDGER_VALUE = 4;
	private static final int AVAIL_VALUE = 5;
	private static final int CURRENT_VALUE = 6;
	private static final int OUTSTANDING_VALUE = 7;
	private static final int OPENINGOUTSTANDING_VALUE = 8;
	private static final int CLOSINGOUTSTANDING_VALUE = 9;
	private static final int AVAILCREDIT_VALUE = 10;
	private static final int CREDITLIMIT_VALUE = 11;
	private static final int PAYOFFAMT_VALUE = 12;
	private static final int PRINCIPAL_VALUE = 13;
	private static final int ESCROW_VALUE = 14;
	private static final int CREDITHELD_VALUE = 15;
	private static final int DEBITHELD_VALUE = 16;
	private static final int TOTALHELD_VALUE = 17;

	public static final BalType LEDGER = new BalType(LEDGER_VALUE);
	public static final BalType OPENINGLEDGER = new BalType(OPENINGLEDGER_VALUE);
	public static final BalType CLOSINGLEDGER = new BalType(CLOSINGLEDGER_VALUE);
	public static final BalType MINLEDGER = new BalType(MINLEDGER_VALUE);
	public static final BalType AVGLEDGER = new BalType(AVGLEDGER_VALUE);
	public static final BalType AVAIL = new BalType(AVAIL_VALUE);
	public static final BalType CURRENT = new BalType(CURRENT_VALUE);
	public static final BalType OUTSTANDING = new BalType(OUTSTANDING_VALUE);
	public static final BalType OPENINGOUTSTANDING = new BalType(OPENINGOUTSTANDING_VALUE);
	public static final BalType CLOSINGOUTSTANDING = new BalType(CLOSINGOUTSTANDING_VALUE);
	public static final BalType AVAILCREDIT = new BalType(AVAILCREDIT_VALUE);
	public static final BalType CREDITLIMIT = new BalType(CREDITLIMIT_VALUE);
	public static final BalType PAYOFFAMT = new BalType(PAYOFFAMT_VALUE);
	public static final BalType PRINCIPAL = new BalType(PRINCIPAL_VALUE);
	public static final BalType ESCROW = new BalType(ESCROW_VALUE);
	public static final BalType CREDITHELD = new BalType(CREDITHELD_VALUE);
	public static final BalType DEBITHELD = new BalType(DEBITHELD_VALUE);
	public static final BalType TOTALHELD = new BalType(TOTALHELD_VALUE);
	public static final BalType UNKNOWN = new BalType(UNKNOWN_VALUE);

	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public BalType() {
	}

	public BalType(int type) {
		super();
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		BalType that = (BalType) obj;
		return type == that.type;
	}

	@Override
	public int hashCode() {
		return type;
	}

	@Override
	protected Object clone() {
		return new BalType(this.type); 
	}
	
	public BalType copy() {
		return (BalType) clone();
	}

	@Override
	public String toString() {
		return type + "";
	}
}
