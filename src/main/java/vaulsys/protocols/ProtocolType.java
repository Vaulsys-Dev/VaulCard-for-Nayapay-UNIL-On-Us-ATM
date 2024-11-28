package vaulsys.protocols;

import javax.persistence.Embeddable;

import vaulsys.persistence.IEnum;

@Embeddable
public class ProtocolType implements IEnum {
	private static final int APACS70_VALUE = 1;
	private static final int CMS_VALUE = 2;
	private static final int EPAY_VALUE = 3;
	private static final int INFOTECH_VALUE = 4;
	private static final int NEGIN87_VALUE = 5;
	private static final int POS87_VALUE = 6;
	private static final int SHETAB87_VALUE = 7;
	private static final int UI_VALUE = 8;

	public static final ProtocolType APACS70 = new ProtocolType(APACS70_VALUE);
	public static final ProtocolType CMS = new ProtocolType(CMS_VALUE);
	public static final ProtocolType EPAY = new ProtocolType(EPAY_VALUE);
	public static final ProtocolType INFOTECH = new ProtocolType(INFOTECH_VALUE);
	public static final ProtocolType NEGIN87 = new ProtocolType(NEGIN87_VALUE);
	public static final ProtocolType POS87 = new ProtocolType(POS87_VALUE);
	public static final ProtocolType SHETAB87 = new ProtocolType(SHETAB87_VALUE);
	public static final ProtocolType UI = new ProtocolType(UI_VALUE);

	private int type;
	
	public ProtocolType() {
	}

	public ProtocolType(Integer type) {
		this.type = type;
	}

	public Integer getType() {
		return type;
	}
	
	@Override
	public int hashCode() {
		return type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof ProtocolType))
			return false;
		ProtocolType that = (ProtocolType) obj;
		return this.type==that.type;
	}
	
	@Override
	public String toString() {
		switch (type) {
		case APACS70_VALUE:
			return "APACS";
		}
		return "";
	}
}
