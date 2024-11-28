package vaulsys.transaction;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;


@Embeddable
public class TransactionStatus implements IEnum {

    private static final byte RECEIVED_VALUE = 1;
    private static final byte IN_PROGRESS_VALUE = 2;
    private static final byte WAITING_VALUE = 3;
    private static final byte PROCESSED_VALUE = 4;
    private static final byte SENDING_VALUE = 5;
    private static final byte DONE_VALUE = 6;
    private static final byte INCOMPLETE_VALUE = 7;

    public static final TransactionStatus RECEIVED = new TransactionStatus(RECEIVED_VALUE);
    public static final TransactionStatus IN_PROGRESS = new TransactionStatus(IN_PROGRESS_VALUE);
    public static final TransactionStatus WAITING = new TransactionStatus(WAITING_VALUE);
    public static final TransactionStatus PROCESSED = new TransactionStatus(PROCESSED_VALUE);
    public static final TransactionStatus SENDING = new TransactionStatus(SENDING_VALUE);
    public static final TransactionStatus DONE = new TransactionStatus(DONE_VALUE);
    public static final TransactionStatus INCOMPLETE = new TransactionStatus(INCOMPLETE_VALUE);

    private byte status;

    TransactionStatus() {
    }

    TransactionStatus(byte status) {
        this.status = status;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionStatus that = (TransactionStatus) o;

        if (status != that.status) return false;

        return true;
    }

    public int hashCode() {
        return (int) status;
    }
}
