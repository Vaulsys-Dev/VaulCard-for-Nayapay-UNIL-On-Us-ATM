package vaulsys.clearing.consts;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class LockObject implements IEnum {
//second
	private static final int UNKNOWN_VALUE = -1;
	private static final int CLEARING_PROFILE_VALUE = 0;
    private static final int TERMINAL_VALUE = 1;

    public static final LockObject UNKNOWN = new LockObject(UNKNOWN_VALUE);
    public static final LockObject CLEARING_PROFILE = new LockObject(CLEARING_PROFILE_VALUE);
    public static final LockObject TERMINAL = new LockObject(TERMINAL_VALUE);

    private int type;

    public LockObject() {
    }

    public LockObject(int type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LockObject that = (LockObject) o;

        if (type != that.type) return false;

        return true;
    }

    public int hashCode() {
        return (int) type;
    }
    
}
