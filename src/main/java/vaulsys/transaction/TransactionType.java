package vaulsys.transaction;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class TransactionType implements IEnum {

    private static final byte EXTERNAL_VALUE = 1;
    private static final byte SELF_GENERATED_VALUE = 2;

    public static TransactionType EXTERNAL = new TransactionType(EXTERNAL_VALUE);
    public static TransactionType SELF_GENERATED = new TransactionType(SELF_GENERATED_VALUE);

    private byte type;

    public TransactionType() {
    }

    public TransactionType(byte type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionType that = (TransactionType) o;

        if (type != that.type) return false;

        return true;
    }

    public int hashCode() {
        return (int) type;
    }
}
