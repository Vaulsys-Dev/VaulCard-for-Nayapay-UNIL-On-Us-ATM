package vaulsys.terminal.atm;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class ATMDisplayFlag implements IEnum {

    private static final byte DONT_DISPLAY_VALUE = 1;
    private static final byte DISPLAY_VALUE = 2;
    private static final byte DISPLAY_STAR_VALUE = 3;

    public static final ATMDisplayFlag DONT_DISPLAY = new ATMDisplayFlag(DONT_DISPLAY_VALUE);
    public static final ATMDisplayFlag DISPLAY = new ATMDisplayFlag(DISPLAY_VALUE);
    public static final ATMDisplayFlag DISPLAY_STAR = new ATMDisplayFlag(DISPLAY_STAR_VALUE);

    private byte flag;

    public ATMDisplayFlag() {
    }

    public ATMDisplayFlag(byte flag) {
        this.flag = flag;
    }

    public byte getValue() {
        switch (flag) {
            case DONT_DISPLAY_VALUE:
                return '0';
            case DISPLAY_VALUE:
                return '1';
            case DISPLAY_STAR_VALUE:
                return '2';
        }
        return 0;
    }
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ATMDisplayFlag that = (ATMDisplayFlag) o;

        if (flag != that.flag) return false;

        return true;
    }

    public int hashCode() {
        return (int) flag;
    }
}
