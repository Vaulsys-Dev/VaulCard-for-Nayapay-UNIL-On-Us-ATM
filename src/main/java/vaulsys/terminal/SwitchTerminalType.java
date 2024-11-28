package vaulsys.terminal;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class SwitchTerminalType implements IEnum{

    public static final byte ISSUER_VALUE = 1;
    public static final byte ACQUIER_VALUE = 2;

    public static final SwitchTerminalType ISSUER = new SwitchTerminalType(ISSUER_VALUE);
    public static final SwitchTerminalType ACQUIER = new SwitchTerminalType(ACQUIER_VALUE);

    private byte type;

    public SwitchTerminalType() {
    }

    public SwitchTerminalType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SwitchTerminalType that = (SwitchTerminalType) o;

        if (type != that.type) return false;

        return true;
    }

    public int hashCode() {
        return (int) type;
    }
}
