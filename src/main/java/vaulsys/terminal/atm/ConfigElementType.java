package vaulsys.terminal.atm;

import javax.persistence.Embeddable;

@Embeddable
public class ConfigElementType {

    public static ConfigElementType STATE = new ConfigElementType(1);
    public static ConfigElementType SCREEN = new ConfigElementType(2);
    public static ConfigElementType FIT = new ConfigElementType(3);
    public static ConfigElementType TIMER = new ConfigElementType(4);
    public static ConfigElementType PARAMETER = new ConfigElementType(5);

    private int type;

    public ConfigElementType(int type) {
        this.type = type;
    }

    public ConfigElementType() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigElementType that = (ConfigElementType) o;

        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type;
    }

}
