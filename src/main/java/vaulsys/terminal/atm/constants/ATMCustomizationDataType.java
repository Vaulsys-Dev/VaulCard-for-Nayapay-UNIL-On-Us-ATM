package vaulsys.terminal.atm.constants;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class ATMCustomizationDataType implements IEnum{

    public static final byte FIT_VALUE = 1;
    public static final byte SCREEN_VALUE = 2;
    public static final byte STATE_VALUE = 3;
    public static final byte PARAMETER_VALUE = 4;
    public static final byte TIMER_VALUE = 5;

    public static final ATMCustomizationDataType FIT = new ATMCustomizationDataType(FIT_VALUE);
    public static final ATMCustomizationDataType SCREEN = new ATMCustomizationDataType(SCREEN_VALUE);
    public static final ATMCustomizationDataType STATE = new ATMCustomizationDataType(STATE_VALUE);
    public static final ATMCustomizationDataType PARAMETER = new ATMCustomizationDataType(PARAMETER_VALUE);
    public static final ATMCustomizationDataType TIMER = new ATMCustomizationDataType(TIMER_VALUE);

    private byte type;

    public ATMCustomizationDataType() {
    }

    public ATMCustomizationDataType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ATMCustomizationDataType that = (ATMCustomizationDataType) o;

        if (type != that.type) return false;

        return true;
    }

    public int hashCode() {
        return (int) type;
    }
}
