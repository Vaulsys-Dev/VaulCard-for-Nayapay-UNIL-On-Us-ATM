package vaulsys.security.hsm.base;

import vaulsys.persistence.IEnum;

public class CommandType implements IEnum {

    private static final int THALES_VALUE = 1;
    private static final int SAFE_NET_VALUE = 2;
    private static final int ATALLA_VALUE = 3;


    public static final CommandType THALES = new CommandType(THALES_VALUE);
    public static final CommandType SAFE_NET = new CommandType(SAFE_NET_VALUE);
    public static final CommandType ATALLA = new CommandType(ATALLA_VALUE);

    private int type;


    public CommandType() {
    }

    public CommandType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandType)) return false;

        CommandType that = (CommandType) o;

        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return type;
    }

    //m.rehman
    @Override
    public String toString() {
        String value;
        if (this == THALES)
            value = "Thales";
        else if (this == SAFE_NET)
            value = "Safenet";
        else if (this == ATALLA)
            value = "Atalla";
        else
            value = null;

        return value;
    }

}
