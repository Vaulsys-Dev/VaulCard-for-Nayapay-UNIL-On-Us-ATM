package vaulsys.clearing.consts;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class ClearingProcessType implements IEnum {
//second
	private static final int UNKNOWN_VALUE = -1;
	private static final int ONLINE_VALUE = 0;
    private static final int BATCH_VALUE = 1;

    public static final ClearingProcessType UNKNOWN = new ClearingProcessType(UNKNOWN_VALUE);
    public static final ClearingProcessType ONLINE = new ClearingProcessType(ONLINE_VALUE);
    public static final ClearingProcessType BATCH = new ClearingProcessType(BATCH_VALUE);

    private int type;

    public ClearingProcessType() {
    }

    public ClearingProcessType(int type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClearingProcessType that = (ClearingProcessType) o;

        if (type != that.type) return false;

        return true;
    }

    public int hashCode() {
        return (int) type;
    }
    
}
