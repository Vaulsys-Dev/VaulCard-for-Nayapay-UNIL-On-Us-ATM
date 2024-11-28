package vaulsys.transaction;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class AccountingState implements IEnum {

    private static final byte NOT_COUNTED_VALUE = 1;
    private static final byte COUNTED_VALUE = 2;
    private static final byte RETURNED_VALUE = 3;
    private static final byte NO_NEED_TO_BE_COUNTED_VALUE = 4;
    private static final byte NEED_TO_BE_RETURNED_VALUE = 6;

    public static final AccountingState NOT_COUNTED = new AccountingState(NOT_COUNTED_VALUE);
    public static final AccountingState COUNTED = new AccountingState(COUNTED_VALUE);
    public static final AccountingState RETURNED = new AccountingState(RETURNED_VALUE);
    public static final AccountingState NO_NEED_TO_BE_COUNTED = new AccountingState(NO_NEED_TO_BE_COUNTED_VALUE);
    public static final AccountingState NEED_TO_BE_RETURNED = new AccountingState(NEED_TO_BE_RETURNED_VALUE);

    private byte state;

    public AccountingState() {
    }

    public AccountingState(byte state) {
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

        AccountingState that = (AccountingState) o;

        if (state != that.state) return false;

        return true;
    }

    public int hashCode() {
        return (int) state;
    }
}
