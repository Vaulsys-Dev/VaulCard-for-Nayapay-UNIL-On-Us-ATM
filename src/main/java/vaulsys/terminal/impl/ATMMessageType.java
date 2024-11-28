package vaulsys.terminal.impl;


import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class ATMMessageType implements IEnum {
    int types;
    public static ATMMessageType ATM_GO_IN_SERVICE = new ATMMessageType(1);
    public static ATMMessageType ATM_GO_OUT_OF_SERVICE = new ATMMessageType(2);
    public static ATMMessageType ATM_DATE_TIME_LOAD = new ATMMessageType(3);
    public static ATMMessageType MASTER_KEY_CHANGE_RQ = new ATMMessageType(4);
    public static ATMMessageType MAC_KEY_CHANGE_RQ = new ATMMessageType(5);
    public static ATMMessageType PIN_KEY_CHANGE_RQ = new ATMMessageType(6);
    public static ATMMessageType ATM_STATE_TABLE_LOAD = new ATMMessageType(7);
//    public static ATMMessageType ATM_STATE_TABLE_LOAD = new ATMMessageType(8);
    public static ATMMessageType ATM_SCREEN_TABLE_LOAD = new ATMMessageType(9);
//    public static ATMMessageType ATM_SCREEN_TABLE_LOAD = new ATMMessageType(10);
    public static ATMMessageType ATM_CONFIG_ID_LOAD = new ATMMessageType(11);
    public static ATMMessageType ATM_SUPPLY_COUNTER_REQUEST = new ATMMessageType(12);
    public static ATMMessageType ATM_ENHANCED_PARAMETER_TABLE_LOAD = new ATMMessageType(13);
    public static ATMMessageType ATM_FIT_TABLE_LOAD = new ATMMessageType(14);
    public static ATMMessageType CONFIG_INFO_REQUEST = new ATMMessageType(15);
    public static ATMMessageType ATM_SEND_CONFIG_ID = new ATMMessageType(16);


        public ATMMessageType(){
    }

    public int getTypes() {
        return types;
    }

    public void setTypes(int types) {
        this.types = types;
    }

    public ATMMessageType(int type) {
        this.types = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ATMMessageType that = (ATMMessageType) o;

        if (types != that.types) return false;

        return true;
}

    @Override
    public int hashCode() {
        return types;
    }

    @Override
    public String toString() {
        return "ATMMessageType{" +
                "types=" + types +
                '}';
    }


}
