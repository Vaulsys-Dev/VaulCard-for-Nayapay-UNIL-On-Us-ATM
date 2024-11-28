package vaulsys.transaction;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class ClearingState implements IEnum {

    private static int NOT_CLEARED_VALUE = 1;
    private static int CLEARED_VALUE = 2;
    private static int DISAGREEMENT_VALUE = 3;
    private static int DISPUTE_VALUE = 4;
    private static int SUSPECTED_DISPUTE_VALUE = 5;
    private static int RECONCILED_VALUE = 6;
    private static int PARTIALY_CLEARED_VALUE = 7;
    private static int PARTIALY_REVERSED_VALUE = 8;
    private static int NO_CARD_REJECTED_VALUE = 9;
    private static int NOT_NOTE_SUCCESSFULLY_DISPENSED_VALUE = 10;
    private static int SUSPECTED_DISAGREEMENT_VALUE = 11;
    

    public static ClearingState NOT_CLEARED =  new ClearingState(NOT_CLEARED_VALUE);
    public static ClearingState CLEARED =  new ClearingState(CLEARED_VALUE);
    public static ClearingState PARTIALLY_CLEARED =  new ClearingState(PARTIALY_CLEARED_VALUE);
    public static ClearingState PARTIALLY_REVERSED =  new ClearingState(PARTIALY_REVERSED_VALUE);
    public static ClearingState NO_CARD_REJECTED =  new ClearingState(NO_CARD_REJECTED_VALUE);
    public static ClearingState NOT_NOTE_SUCCESSFULLY_DISPENSED =  new ClearingState(NOT_NOTE_SUCCESSFULLY_DISPENSED_VALUE);
    public static ClearingState DISAGREEMENT =  new ClearingState(DISAGREEMENT_VALUE);
    public static ClearingState DISPUTE =  new ClearingState(DISPUTE_VALUE);
    public static ClearingState SUSPECTED_DISPUTE =  new ClearingState(SUSPECTED_DISPUTE_VALUE);
    public static ClearingState RECONCILED =  new ClearingState(RECONCILED_VALUE);
    public static ClearingState SUSPECTED_DISAGREEMENT =  new ClearingState(SUSPECTED_DISAGREEMENT_VALUE);
    
    private int state;

    public ClearingState() {
    }

    public ClearingState(int state) {
        this.state = state;
    }

    public String getName() {
    	if (state == NOT_CLEARED_VALUE)
    		return "NOT_CLEARED";
    	if (state == CLEARED_VALUE)
    		return "CLEARED";
    	if (state == DISAGREEMENT_VALUE)
    		return "DISAGREEMENT";
    	if (state == DISPUTE_VALUE)
    		return "DISPUTE";
    	if (state == SUSPECTED_DISPUTE_VALUE)
    		return "SUSPECTED_DISPUTE";
    	if (state == RECONCILED_VALUE)
    		return "RECONCILED";
    	if (state == PARTIALY_CLEARED_VALUE)
    		return "PARTIALY_CLEARED";
    	if (state == PARTIALY_REVERSED_VALUE)
    		return "PARTIALY_REVERSED";
    	if (state == NO_CARD_REJECTED_VALUE)
    		return "NO_CARD_REJECTED";
    	if (state == NOT_NOTE_SUCCESSFULLY_DISPENSED_VALUE)
    		return "NOT_NOTE_SUCCESSFULLY_DISPENSED";
    	if (state == SUSPECTED_DISAGREEMENT_VALUE)
    		return "SUSPECTED_DISAGREEMENT";
    	
    	return "UNKNOWN";
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClearingState that = (ClearingState) o;

        if (state != that.state) return false;

        return true;
    }


	public int hashCode() {
        return (int) state;
    }
	
	public int getState() {
		return state;
	}
	
	public ClearingState getClrearingSate(int value){
		if (value == NOT_CLEARED_VALUE)
    		return NOT_CLEARED;
    	if (value == CLEARED_VALUE)
    		return CLEARED;
    	if (value == DISAGREEMENT_VALUE)
    		return DISAGREEMENT;
    	if (value == DISPUTE_VALUE)
    		return DISPUTE;
    	if (value == SUSPECTED_DISPUTE_VALUE)
    		return SUSPECTED_DISPUTE;
    	if (value == RECONCILED_VALUE)
    		return RECONCILED;
    	if (value == PARTIALY_CLEARED_VALUE)
    		return PARTIALLY_CLEARED;
    	if (value == PARTIALY_REVERSED_VALUE)
    		return PARTIALLY_REVERSED;
    	if (value == NO_CARD_REJECTED_VALUE)
    		return NO_CARD_REJECTED;
    	if (value == NOT_NOTE_SUCCESSFULLY_DISPENSED_VALUE)
    		return NOT_NOTE_SUCCESSFULLY_DISPENSED;
    	if (value == SUSPECTED_DISAGREEMENT_VALUE)
    		return SUSPECTED_DISAGREEMENT;
    	
    	return new ClearingState(value);
	}
	
	@Override
	public String toString() {
		return state+"";
	}
}
