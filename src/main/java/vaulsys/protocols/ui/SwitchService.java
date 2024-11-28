package vaulsys.protocols.ui;

import vaulsys.persistence.IEnum;
import vaulsys.protocols.ifx.enums.AccType;

public class SwitchService implements IEnum {

	private static final int UNKNOWN_VALUE = -1;
	private static final int ISSUE_SHETAB_DOCUMENT_VALUE = 0;
	private static final int ATM_SERVICE_VALUE = 1;
	private static final int SETTLE_VALUE = 2;
	
	
	public static final SwitchService UNKNOWN = new SwitchService(UNKNOWN_VALUE);
	public static final SwitchService ISSUE_SHETAB_DOCUMENT = new SwitchService(ISSUE_SHETAB_DOCUMENT_VALUE);
	public static final SwitchService ATM_SERVICE = new SwitchService(ATM_SERVICE_VALUE);
	public static final SwitchService SETTLE = new SwitchService(SETTLE_VALUE);
	
	private int type;

	public SwitchService() {
	}

	public SwitchService(int type) {
		super();
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		SwitchService that = (SwitchService) obj;
		return type == that.type;
	}

	@Override
	public int hashCode() {
		return type;
	}
	
	@Override
	protected Object clone() {
		return new AccType(this.type); 
	}
	
	public AccType copy() {
		return (AccType) clone();
	}
	
	@Override
	public String toString() {
		switch (type) {
		case UNKNOWN_VALUE:
			return "UNKNOWN";
		case ISSUE_SHETAB_DOCUMENT_VALUE:
			return "ISSUE_SHETAB_DOCUMENT";
		case ATM_SERVICE_VALUE:
			return "ATM_SERVICE";
		
		default:
			return type + "";
		}
	}

}
