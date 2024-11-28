package vaulsys.terminal.atm.device;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class CashType implements IEnum {

    private static final byte TEN_THOUSAND_VALUE = 1;
    private static final byte TWENTY_THOUSAND_VALUE = 2;
    private static final byte FIFTY_THOUSAND_VALUE = 3;
    private static final byte FIFTEEN_THOUSAND_VALUE = 4;

    public static final CashType TEN_THOUSAND = new CashType(TEN_THOUSAND_VALUE);
    public static final CashType TWENTY_THOUSAND = new CashType(TWENTY_THOUSAND_VALUE);
    public static final CashType FIFTY_THOUSAND = new CashType(FIFTY_THOUSAND_VALUE);
    public static final CashType FIFTEEN_THOUSAND = new CashType(FIFTEEN_THOUSAND_VALUE);


    private byte type;

    public CashType() {
    }

    public CashType(byte type) {
        this.type = type;
    }

    public long getValue() {
        switch (type) {
            case TEN_THOUSAND_VALUE:
                return 10000;
            case TWENTY_THOUSAND_VALUE:
                return 20000;
            case FIFTY_THOUSAND_VALUE:
                return 50000;
            case FIFTEEN_THOUSAND_VALUE:
            	return 500000;
        }
        return 0;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CashType cashType = (CashType) o;

        if (type != cashType.type) return false;

        return true;
    }

    public int hashCode() {
        return (int) type;
    }
}
