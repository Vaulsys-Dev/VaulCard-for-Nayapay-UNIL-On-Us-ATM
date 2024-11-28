package vaulsys.terminal.atm;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class ActionType implements IEnum {

    private static final Integer CONSUMER_VALUE = 1;
    private static final Integer CASH_HANDLER_VALUE = 2;
    private static final Integer REVERSE_VALUE = 3;
    private static final Integer CARD_CAPTURE_VALUE = 4;

    private static final Integer ENCRYPTOR_ERROR_VALUE = 5;
    private static final Integer SUPERVISOR_ENTRY_VALUE = 6;
    private static final Integer SUPERVISOR_EXIT_VALUE = 7;
    
    private static final Integer DEVICE_UPDATE_VALUE = 8;
    private static final Integer SUPPLY_STATUS_VALUE = 9;


    public static final ActionType CONSUMER = new ActionType(CONSUMER_VALUE);
    public static final ActionType CASH_HANDLER = new ActionType(CASH_HANDLER_VALUE);
    public static final ActionType REVERSE = new ActionType(REVERSE_VALUE);
    public static final ActionType CARD_CAPTURE = new ActionType(CARD_CAPTURE_VALUE);
    public static final ActionType ENCRYPTOR_ERROR = new ActionType(ENCRYPTOR_ERROR_VALUE);
    public static final ActionType SUPERVISOR_ENTRY = new ActionType(SUPERVISOR_ENTRY_VALUE);
    public static final ActionType SUPERVISOR_EXIT = new ActionType(SUPERVISOR_EXIT_VALUE);
    public static final ActionType DEVICE_UPDATE = new ActionType(DEVICE_UPDATE_VALUE);
    public static final ActionType SUPPLY_STATUS = new ActionType(SUPPLY_STATUS_VALUE);

    private Integer type;

    public ActionType() {
    }

    public ActionType(Integer type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
    	if (type == 1)
    		return "CONSUMER";
    	
    	if (type == 2)
    		return "CASH_HANDLER";
    	
    	if (type == 3)
    		return "REVERSE";
    	
    	if (type == 4)
    		return "CARD_CAPTURE";
    	
    	if (type == 5)
    		return "ENCRYPTOR_ERROR";
    	
    	if (type == 6)
    		return "SUPERVISOR_ENTRY";
    	
    	if (type == 7)
    		return "SUPERVISOR_EXIT";
    	
    	if (type == 8)
    		return "DEVICE_UPDATE";
    	
    	if (type == 9)
    		return "SUPPLY_STATUS";
    	
    	return "UNKNOWN";
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ActionType)) return false;

        ActionType that = (ActionType) o;

        return type.equals(that.type);
    }

    public int hashCode() {
        return type;
    }
}
