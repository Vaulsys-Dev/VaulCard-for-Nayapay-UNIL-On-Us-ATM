package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.enums;

import vaulsys.protocols.ndc.base.config.StatusQualifierType;

public class MessageFormatErrorStatusQualifierType extends StatusQualifierType {

	private static final String MESSAGE_lENGTH_ERROR_VALUE = "01";
	private static final String FIELD_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND_VALUE = "02";
	private static final String TRANSACTION_REPLY_MESSAGE_HAS_TOO_MANY_PRINT_GROUP_VALUE = "03";
	private static final String GROUP_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND_VALUE = "04";
	private static final String TOO_MANY_DPM_WRITE_LISTS_OR_WRITE_AND_CONFIRM_LISTS_IN_THE_TRANSACTION_REPLY_VALUE = "05";
	
	public static final MessageFormatErrorStatusQualifierType MESSAGE_lENGTH_ERROR = new MessageFormatErrorStatusQualifierType(MESSAGE_lENGTH_ERROR_VALUE);
	public static final MessageFormatErrorStatusQualifierType FIELD_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND = new MessageFormatErrorStatusQualifierType(FIELD_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND_VALUE);
	public static final MessageFormatErrorStatusQualifierType TRANSACTION_REPLY_MESSAGE_HAS_TOO_MANY_PRINT_GROUP = new MessageFormatErrorStatusQualifierType(TRANSACTION_REPLY_MESSAGE_HAS_TOO_MANY_PRINT_GROUP_VALUE);
	public static final MessageFormatErrorStatusQualifierType GROUP_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND = new MessageFormatErrorStatusQualifierType(GROUP_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND_VALUE);
	public static final MessageFormatErrorStatusQualifierType TOO_MANY_DPM_WRITE_LISTS_OR_WRITE_AND_CONFIRM_LISTS_IN_THE_TRANSACTION_REPLY = new MessageFormatErrorStatusQualifierType(TOO_MANY_DPM_WRITE_LISTS_OR_WRITE_AND_CONFIRM_LISTS_IN_THE_TRANSACTION_REPLY_VALUE);

	
	public MessageFormatErrorStatusQualifierType(String type) {
		super(type);
	}
	
	public static StatusQualifierType getByType(String type) {
		if (MESSAGE_lENGTH_ERROR_VALUE.equals(type))
			return MESSAGE_lENGTH_ERROR;
		
		if (FIELD_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND_VALUE.equals(type))
			return FIELD_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND;
		
		if (TRANSACTION_REPLY_MESSAGE_HAS_TOO_MANY_PRINT_GROUP_VALUE.equals(type))
			return TRANSACTION_REPLY_MESSAGE_HAS_TOO_MANY_PRINT_GROUP;
		
		if (GROUP_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND_VALUE.equals(type))
			return GROUP_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND;
		
		if (TOO_MANY_DPM_WRITE_LISTS_OR_WRITE_AND_CONFIRM_LISTS_IN_THE_TRANSACTION_REPLY_VALUE.equals(type))
			return TOO_MANY_DPM_WRITE_LISTS_OR_WRITE_AND_CONFIRM_LISTS_IN_THE_TRANSACTION_REPLY;
			
		return UNKNOWN;
//		return super.getByType(type);
	}
	
	@Override
	public String getDescription() {
		if (MESSAGE_lENGTH_ERROR_VALUE.equals(getType()))
			return "MESSAGE_LENGTH_ERROR";
		
		if (FIELD_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND_VALUE.equals(getType()))
			return "FIELD_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND";
		
		if (TRANSACTION_REPLY_MESSAGE_HAS_TOO_MANY_PRINT_GROUP_VALUE.equals(getType()))
			return "TRANSACTION_REPLY_MESSAGE_HAS_TOO_MANY_PRINT_GROUP";
		
		if (GROUP_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND_VALUE.equals(getType()))
			return "GROUP_SEPERATOR_MISSING_OR_UNEXPECTEDLY_FOUND";
		
		if (TOO_MANY_DPM_WRITE_LISTS_OR_WRITE_AND_CONFIRM_LISTS_IN_THE_TRANSACTION_REPLY_VALUE.equals(getType()))
			return "TOO_MANY_DPM_WRITE_LISTS_OR_WRITE_AND_CONFIRM_LISTS_IN_THE_TRANSACTION_REPLY";
		
		return super.getDescription();	
	}
}
