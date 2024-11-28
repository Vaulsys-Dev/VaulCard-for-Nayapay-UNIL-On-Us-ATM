package vaulsys.transaction;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class SettledState implements IEnum {

    private static final byte NOT_SETTLED_VALUE = 1;
    private static final byte SENT_FOR_SETTLEMENT_VALUE = 2;
    private static final byte SETTLED_VALUE = 3;
    private static final byte RETURNED_VALUE = 4;

    public static final SettledState NOT_SETTLED = new SettledState(NOT_SETTLED_VALUE);
    public static final SettledState SENT_FOR_SETTLEMENT = new SettledState(SENT_FOR_SETTLEMENT_VALUE);
    public static final SettledState SETTLED = new SettledState(SETTLED_VALUE);
    public static final SettledState RETURNED = new SettledState(RETURNED_VALUE);

    private byte state;

    public SettledState() {
    }
    
    public byte getState() {
        return state;
    }

    public SettledState(byte state) {
        this.state = state;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SettledState that = (SettledState) o;

        if (state != that.state) return false;

        return true;
    }

    public int hashCode() {
        return (int) state;
    }
}
