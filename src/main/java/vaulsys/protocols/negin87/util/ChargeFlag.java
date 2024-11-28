package vaulsys.protocols.negin87.util;

import vaulsys.persistence.IEnum;

public class ChargeFlag implements IEnum{
	private static final Byte NORMAL_VALUE = 0;
	private static final Byte CHARGED_VALUE = 1;
	

	public static final ChargeFlag NORMAL = new ChargeFlag(NORMAL_VALUE);
	public static final ChargeFlag CHARGED = new ChargeFlag(CHARGED_VALUE);
	
	private Byte type;

	public ChargeFlag(Byte type){
		super();
		this.type = type;
	}
	
	public Byte getType() {
		return type;
	}

	public void setType(Byte type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		ChargeFlag that = (ChargeFlag) obj;
		return type.equals(that.type);
	}

	@Override
	public int hashCode() {
		return type;
	}

	public static ChargeFlag getCFlag(long t){
		if (t == 1)
			return NORMAL;
		if (t==0)
			return CHARGED;
		return null;
	}
	
	@Override
	public String toString() {
		if (type.byteValue() == 0x00)
			return "C";
		if (type.byteValue() == 0x01)
			return "D";
		return super.toString();
	}

	public ChargeFlag reverse(){
		return (type.byteValue() == 0x00? CHARGED:NORMAL);
	}
	
}
