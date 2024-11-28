package vaulsys.entity.impl;

import javax.persistence.Embeddable;

import vaulsys.persistence.IEnum;

@Embeddable
public class VisitorType implements IEnum{
	
	public static final VisitorType SUPPORTER = new VisitorType((byte)1);
	public static final VisitorType VISITOR_SUPPORTER = new VisitorType((byte)2);
	
	private byte type;
	
	public VisitorType() {
	}

	public VisitorType(byte type) {
		this.type = type;
	}
	
	public byte getType(){
		return type;
	}

	@Override
	public int hashCode() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VisitorType))
			return false;
		VisitorType other = (VisitorType) obj;
		if (type != other.type)
			return false;
		return true;
	}

	
}
