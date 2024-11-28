package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.enums;

import vaulsys.protocols.ndc.base.config.StatusQualifierType;

public class HardwareFailureErrorStatusQualifierType extends StatusQualifierType {

	private static final String ENCRYPTION_FAILURE_DURING_KEY_CHANGE_MESSAGE_VALUE = "01";
	private static final String INVALID_DATA_SENT_VALUE = "02";
	private static final String INSUFFICIENT_DISK_SPACE_VALUE = "06";
	
	public static final HardwareFailureErrorStatusQualifierType ENCRYPTION_FAILURE_DURING_KEY_CHANGE_MESSAGE = new HardwareFailureErrorStatusQualifierType(ENCRYPTION_FAILURE_DURING_KEY_CHANGE_MESSAGE_VALUE);
	public static final HardwareFailureErrorStatusQualifierType INVALID_DATA_SENT = new HardwareFailureErrorStatusQualifierType(INVALID_DATA_SENT_VALUE);
	public static final HardwareFailureErrorStatusQualifierType INSUFFICIENT_DISK_SPACE = new HardwareFailureErrorStatusQualifierType(INSUFFICIENT_DISK_SPACE_VALUE);
	

	public HardwareFailureErrorStatusQualifierType(String type) {
		super(type);
	}
	
	public static StatusQualifierType getByType(String type) {
		if (ENCRYPTION_FAILURE_DURING_KEY_CHANGE_MESSAGE_VALUE.equals(type))
			return ENCRYPTION_FAILURE_DURING_KEY_CHANGE_MESSAGE;
		
		if (INVALID_DATA_SENT_VALUE.equals(type))
			return INVALID_DATA_SENT;
		
		if (INSUFFICIENT_DISK_SPACE_VALUE.equals(type))
			return INSUFFICIENT_DISK_SPACE;
		
		return UNKNOWN;
	}
	
	@Override
	public String getDescription() {
		if (ENCRYPTION_FAILURE_DURING_KEY_CHANGE_MESSAGE_VALUE.equals(getType()))
			return "ENCRYPTION_FAILURE_DURING_KEY_CHANGE_MESSAGE";
		
		if (INVALID_DATA_SENT_VALUE.equals(getType()))
			return "INVALID_DATA_SENT";
		
		if (INSUFFICIENT_DISK_SPACE_VALUE.equals(getType()))
			return "INSUFFICIENT_DISK_SPACE";
		return super.getDescription();	
	}
}
