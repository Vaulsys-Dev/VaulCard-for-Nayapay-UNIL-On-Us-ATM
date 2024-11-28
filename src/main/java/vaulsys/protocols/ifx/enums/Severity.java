package vaulsys.protocols.ifx.enums;

import vaulsys.persistence.IEnum;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class Severity implements IEnum{
	
	private static final int INFO_VALUE = 0;
	private static final int WARN_VALUE = 1;
	private static final int ERROR_VALUE = 2;

	
	public static final Severity INFO = new Severity(INFO_VALUE);
	public static final Severity WARN = new Severity(WARN_VALUE);
	public static final Severity ERROR= new Severity(ERROR_VALUE);
	
	int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Severity(int type) {
		super();
		this.type = type;
	}
	
	public Severity() {
		super();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Severity that = (Severity) obj;
		return type == that.type;
	}
	
	@Override
	public int hashCode() {
		return type;
	}

	@Override
	protected Object clone() {
		return new Severity(this.type); 
	}
	
	public Severity copy() {
		return (Severity) clone();
	}

}
