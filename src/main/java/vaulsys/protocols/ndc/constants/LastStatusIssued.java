package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class LastStatusIssued implements IEnum {
	
	private static final char UNKNOWN_VALUE = '?';
	private static final char NONE_SENT_VALUE = '0';
	private static final char GOOD_TERMINATION_SENT_VALUE = '1';
	private static final char ERROR_STATUS_SENT_VALUE = '2';
	private static final char TRANSACTION_REPLY_REJECTED_VALUE = '3';
	
	
	public static final LastStatusIssued UNKNOWN = new LastStatusIssued(UNKNOWN_VALUE);
	public static final LastStatusIssued NONE_SENT = new LastStatusIssued(NONE_SENT_VALUE);
	public static final LastStatusIssued GOOD_TERMINATION_SENT = new LastStatusIssued(GOOD_TERMINATION_SENT_VALUE);
	public static final LastStatusIssued ERROR_STATUS_SENT = new LastStatusIssued(ERROR_STATUS_SENT_VALUE);
	public static final LastStatusIssued TRANSACTION_REPLY_REJECTED = new LastStatusIssued(TRANSACTION_REPLY_REJECTED_VALUE);
	
	private char value;
	
	public LastStatusIssued() {
	}
	
	public LastStatusIssued(char value) {
		this.value = value;
	}
	

	@Override
	public String toString() {
		switch (value) {
		case '0':
			return "NONE_SENT";
		case '1':
			return "GOOD_TERMINATION_SENT";
		case '2':
			return "ERROR_STATUS_SENT";
		case '3':
			return "TRANSACTION_REPLY_REJECTED";
		case '?':
			return "UNKNOWN";
		}
		return this.value+"";
	}
	
	public static LastStatusIssued get(char v){
		switch (v) {
		case '0':
			return NONE_SENT;
		case '1':
			return GOOD_TERMINATION_SENT;
		case '2':
			return ERROR_STATUS_SENT;
		case '3':
			return TRANSACTION_REPLY_REJECTED;
		case '?':
			return UNKNOWN;
		}
		return new LastStatusIssued(v);
	}
	
}
