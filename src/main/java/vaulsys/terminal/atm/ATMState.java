package vaulsys.terminal.atm;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class ATMState implements IEnum {
    private static final byte UNKNOWN_VALUE = 0;
    private static final byte POWER_OFF_VALUE = 1;
    private static final byte PARAMETER_DATA_LOADING_VALUE = 2;
    private static final byte PARAMETER_DATA_LOAD_FINISHED_VALUE = 3;
    private static final byte IN_SERIVCE_VALUE = 4;
    private static final byte OUT_OF_SERVICE_VALUE = 5;
    private static final byte INVALID_MASTER_KEY_VALUE = 6;
    
    private static final byte CONFIG_INFO_REQUEST_VALUE = 7;

    public static final ATMState UNKNOWN = new ATMState(UNKNOWN_VALUE);
    public static final ATMState POWER_OFF = new ATMState(POWER_OFF_VALUE);
    public static final ATMState PARAMETER_DATA_LOADING = new ATMState(PARAMETER_DATA_LOADING_VALUE);
    public static final ATMState PARAMETER_DATA_LOAD_FINISHED = new ATMState(PARAMETER_DATA_LOAD_FINISHED_VALUE);
    public static final ATMState IN_SERIVCE = new ATMState(IN_SERIVCE_VALUE);
    public static final ATMState OUT_OF_SERVICE = new ATMState(OUT_OF_SERVICE_VALUE);
    public static final ATMState INVALID_MASTER_KEY = new ATMState(INVALID_MASTER_KEY_VALUE);
    
    public static final ATMState CONFIG_INFO_REQUEST = new ATMState(CONFIG_INFO_REQUEST_VALUE);

    private byte status;

    public ATMState() {
    }

    public ATMState(byte status) {
        this.status = status;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ATMState atmState = (ATMState) o;

        if (status != atmState.status) return false;

        return true;
    }

    public int hashCode() {
        return (int) status;
    }
    
    //TASK Task074 
	@Override
	public String toString() {
		switch (status) {
			case POWER_OFF_VALUE:
				return "POWER_OFF";
			case PARAMETER_DATA_LOADING_VALUE:
				return "PARAMETER_DATA_LOADING";
			case PARAMETER_DATA_LOAD_FINISHED_VALUE:
				return "PARAMETER_DATA_LOAD_FINISHED";
			case IN_SERIVCE_VALUE:
				return "IN_SERIVCE";
			case OUT_OF_SERVICE_VALUE:
				return "OUT_OF_SERVICE";
			case INVALID_MASTER_KEY_VALUE:
				return "INVALID_MASTER_KEY";
			case CONFIG_INFO_REQUEST_VALUE:
				return "CONFIG_INFO_REQUEST";
			default:
				return "UNKNOWN";	
		}
	}     
}
