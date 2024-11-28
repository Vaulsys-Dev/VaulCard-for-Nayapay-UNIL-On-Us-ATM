package vaulsys.terminal.atm.device;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class DeviceStatus implements IEnum {

//    private static final int UNKOWN_VALUE = -1;
//    private static final int NORMAL_VALUE = 0;
//    private static final int WARNIN_VALUE = 1;
//    private static final int FATAL_VALUE = 2;
//
//    public static DeviceStatus UNKOWN = new DeviceStatus(UNKOWN_VALUE);
//    public static DeviceStatus NORMAL = new DeviceStatus(NORMAL_VALUE);
//    public static DeviceStatus WARNING = new DeviceStatus(WARNIN_VALUE);
//    public static DeviceStatus FATAL = new DeviceStatus(FATAL_VALUE);

    private char status;

    public DeviceStatus() {
    }

    public DeviceStatus(char status) {
        this.status = status;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceStatus that = (DeviceStatus) o;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        return status;
    }
}
