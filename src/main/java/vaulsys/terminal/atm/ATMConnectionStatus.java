package vaulsys.terminal.atm;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;


@Embeddable
public class ATMConnectionStatus implements IEnum {

    public static final byte NOT_CONNECTED_VALUE = 0;
    public static final byte CONNECTED_VALUE = 1;

    public static final ATMConnectionStatus NOT_CONNECTED = new ATMConnectionStatus(NOT_CONNECTED_VALUE);
    public static final ATMConnectionStatus CONNECTED = new ATMConnectionStatus(CONNECTED_VALUE);

    private byte status;

    public ATMConnectionStatus() {
    }

    public ATMConnectionStatus(byte status) {
        this.status = status;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ATMConnectionStatus atmState = (ATMConnectionStatus) o;

        if (status != atmState.status) return false;

        return true;
    }

    public int hashCode() {
        return (int) status;
    }
}
