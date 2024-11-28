package vaulsys.protocols.ifx.enums;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class IfxDirection implements Cloneable, Serializable {

	private static final int INCOMING_VALUE = 0;
	private static final int OUTGOING_VALUE = 1;
	private static final int SELF_GENERATED_VALUE = 2;

	public static final IfxDirection INCOMING = new IfxDirection(INCOMING_VALUE);
	public static final IfxDirection OUTGOING = new IfxDirection(OUTGOING_VALUE);
	public static final IfxDirection SELF_GENERATED = new IfxDirection(SELF_GENERATED_VALUE);

	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	public IfxDirection() {
		super();
	}
	
	public IfxDirection(int type){
		super();
		this.type = type;
	}

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IfxDirection that = (IfxDirection) o;

        if (type != that.type) return false;

        return true;
    }

    public int hashCode() {
        return type;
    }

    @Override
	protected Object clone() {
		return new IfxDirection(this.type); 
	}
	
	public IfxDirection copy() {
		return (IfxDirection) clone();
	}
	
	@Override
	public String toString() {
		switch (type) {
			case INCOMING_VALUE:
				return "in";

			case OUTGOING_VALUE:
				return "out";

			case SELF_GENERATED_VALUE:
				return "self";

			default:
				return "-";
		}
	}
}
