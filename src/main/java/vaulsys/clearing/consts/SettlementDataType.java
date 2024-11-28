package vaulsys.clearing.consts;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class SettlementDataType implements IEnum {
//second
	private static final int UNKNOWN_VALUE = -1;
	private static final int RECONCILE_VALUE = 0;
    private static final int MAIN_VALUE = 1;
    private static final int SECOND_VALUE = 2;
    private static final int RETURNED_VALUE = 3;

    public static final SettlementDataType UNKNOWN = new SettlementDataType(UNKNOWN_VALUE);
    public static final SettlementDataType RECONCILE = new SettlementDataType(RECONCILE_VALUE);
    public static final SettlementDataType MAIN = new SettlementDataType(MAIN_VALUE);
    public static final SettlementDataType SECOND = new SettlementDataType(SECOND_VALUE);
    public static final SettlementDataType RETURNED = new SettlementDataType(RETURNED_VALUE);

    private int type;

    public SettlementDataType() {
    }

    public SettlementDataType(int type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SettlementDataType that = (SettlementDataType) o;

        if (type != that.type) return false;

        return true;
    }

    public int hashCode() {
        return (int) type;
    }

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
