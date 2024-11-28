package vaulsys.clearing.report;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

//Relative TASK Task060 : Resalat sanad repball
//TASK Task061 : Add repextra indicator to shetab_repball
//TASK Task084 : Add repextra indicator to shetab_repball (Pasargad)
@Embeddable
public class RepExteraState implements IEnum {

    private static final byte UNKNOWN_VALUE = 0;
    private static final byte REPBAL_TODAY_VALUE = 1;
    private static final byte REPEXTRA_TODAY_VALUE = 2;
    private static final byte REPEXTRA_YESTERDAY_VALUE = 3;

    public static final RepExteraState UNKNOWN = new RepExteraState(UNKNOWN_VALUE);
    public static final RepExteraState REPBAL_TODAY = new RepExteraState(REPBAL_TODAY_VALUE);
    public static final RepExteraState REPEXTRA_TODAY = new RepExteraState(REPEXTRA_TODAY_VALUE);
    public static final RepExteraState REPEXTRA_YESTERDAY = new RepExteraState(REPEXTRA_YESTERDAY_VALUE);

    private byte state;

    public RepExteraState() {
    }

    public RepExteraState(byte state) {
        this.state = state;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RepExteraState that = (RepExteraState) o;

        if (state != that.state) return false;

        return true;
    }

    public int hashCode() {
        return (int) state;
    }
}
