package vaulsys.terminal;

import vaulsys.persistence.IEnum;

public class POSConnectionType implements IEnum {

    private static final byte UNKNOWN_VALUE = 0;
    private static final byte MODEM_VALUE = 1;
    private static final byte LAN_VALUE = 2;
    private static final byte GSM_VALUE = 3;
    private static final byte GPRS_VALUE = 4;
    private static final byte BLUETOOTH_VALUE =5;

    public static final POSConnectionType UNKNOWN = new POSConnectionType(UNKNOWN_VALUE);
    public static final POSConnectionType MODEM = new POSConnectionType(MODEM_VALUE);
    public static final POSConnectionType LAN = new POSConnectionType(LAN_VALUE);
    public static final POSConnectionType GSM = new POSConnectionType(GSM_VALUE);
    public static final POSConnectionType GPRS = new POSConnectionType(GPRS_VALUE);
    public static final POSConnectionType BLUETOOTH = new POSConnectionType(BLUETOOTH_VALUE);

    private byte type = UNKNOWN_VALUE;
    
    public POSConnectionType(){
    }

    public POSConnectionType(byte type) {
        this.type = type;
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        POSConnectionType posCType = (POSConnectionType) o;

        if (type != posCType.type) return false;

        return true;
    }

    public byte getType(){
        return type;
    }

    public int hashCode() {
        return (int) type;
    }
    
    public String toString(){
    	switch (type) {
			case UNKNOWN_VALUE:
				return "Unknown";
			case MODEM_VALUE:
				return "Modem";
			case LAN_VALUE:
				return "LAN";
			case GSM_VALUE:
				return "GSM";
			case GPRS_VALUE:
				return "GPRS";
			case BLUETOOTH_VALUE:
				return "BlueThooth";
			default:
				return "";
		}
    }
}
