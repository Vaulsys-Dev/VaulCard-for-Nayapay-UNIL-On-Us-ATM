package vaulsys.protocols.ndc.base.config;

import vaulsys.persistence.IEnum;

public class TransactionStatusType implements IEnum {

	private static final char UNKNOWN_VALUE = '?';
	private static final char SUCCESSFUL_OPERATION_VALUE = '0';
	private static final char SHORT_DISPENSE_VALUE = '1';
	private static final char NO_NOTE_DISPENSED_VALUE = '2';
	private static final char NOTES_DISPENSED_UNKNOWN_VALUE = '3';
	private static final char NO_NOTE_DISPENSED_CARD_NOT_REJECTED_VALUE = '4';
	private static final char SOME_NOTES_RETRACTED_VALUE = '5';
	private static final char SOME_NOTES_VALUE = '6';
	
	public static final TransactionStatusType UNKNOWN = new TransactionStatusType(UNKNOWN_VALUE);
	public static final TransactionStatusType SUCCESSFUL_OPERATION = new TransactionStatusType(SUCCESSFUL_OPERATION_VALUE);
	public static final TransactionStatusType SHORT_DISPENSE = new TransactionStatusType(SHORT_DISPENSE_VALUE);
	public static final TransactionStatusType NO_NOTE_DISPENSED = new TransactionStatusType(NO_NOTE_DISPENSED_VALUE);
	public static final TransactionStatusType NOTES_DISPENSED_UNKNOWN = new TransactionStatusType(NOTES_DISPENSED_UNKNOWN_VALUE);
	public static final TransactionStatusType NO_NOTE_DISPENSED_CARD_NOT_REJECTED = new TransactionStatusType(NO_NOTE_DISPENSED_CARD_NOT_REJECTED_VALUE);
	public static final TransactionStatusType SOME_NOTES_RETRACTED = new TransactionStatusType(SOME_NOTES_RETRACTED_VALUE);
	public static final TransactionStatusType SOME_NOTES = new TransactionStatusType(SOME_NOTES_VALUE);
	
	private char type;
	
    public TransactionStatusType() {
    }

    public static TransactionStatusType getByType(char type) {
    	if (type == '0')
    		return SUCCESSFUL_OPERATION;

    	if (type == '1')
    		return SHORT_DISPENSE;
    	
    	if (type == '2')
    		return NO_NOTE_DISPENSED;
    	
    	if (type == '3')
    		return NOTES_DISPENSED_UNKNOWN;
    	
    	if (type == '4')
    		return NO_NOTE_DISPENSED_CARD_NOT_REJECTED;
    	
    	if (type == '5')
    		return SOME_NOTES_RETRACTED;
    	
    	if (type == '6')
    		return SOME_NOTES;
    	
    	return UNKNOWN;
    }
    
    public TransactionStatusType(char type) {
        this.type = type;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionStatusType that = (TransactionStatusType) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type;
    }

	public char getType() {
		return type;
	}

	@Override
	public String toString() {
		if (type == '0')
    		return "SUCCESSFUL_OPERATION";

    	if (type == '1')
    		return "SHORT_DISPENSE";
    	
    	if (type == '2')
    		return "NO_NOTE_DISPENSED";
    	
    	if (type == '3')
    		return "NOTES_DISPENSED_UNKNOWN";
    	
    	if (type == '4')
    		return "NO_NOTE_DISPENSED_OR_CARD_NOT_EJECTED";
    	
    	if (type == '5')
    		return "SOME_NOTES_RETRACTED";
    	
    	if (type == '6')
    		return "SOME_NOTES_ENTERING_PURGE_BIN";
    	
    	return "UNKNOWN";
	}
}
