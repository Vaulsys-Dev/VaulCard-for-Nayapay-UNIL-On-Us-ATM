package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.enums;

import vaulsys.protocols.ndc.base.config.StatusQualifierType;

public class NotSupportedErrorStatusQualifierType extends StatusQualifierType {

	private static final String NOT_SUPPORTED_BY_SOFTWARE_VALUE = "01";
	private static final String NOT_SUPPORTED_BY_HARDWARE_VALUE = "02";
	private static final String JOURNAL_PRINTER_BACKUP_IS_INACTIVE_VALUE = "05";
	
	public static final NotSupportedErrorStatusQualifierType NOT_SUPPORTED_BY_SOFTWARE = new NotSupportedErrorStatusQualifierType(NOT_SUPPORTED_BY_SOFTWARE_VALUE);
	public static final NotSupportedErrorStatusQualifierType NOT_SUPPORTED_BY_HARDWARE = new NotSupportedErrorStatusQualifierType(NOT_SUPPORTED_BY_HARDWARE_VALUE);
	public static final NotSupportedErrorStatusQualifierType JOURNAL_PRINTER_BACKUP_IS_INACTIVE = new NotSupportedErrorStatusQualifierType(JOURNAL_PRINTER_BACKUP_IS_INACTIVE_VALUE);
	
	
	public NotSupportedErrorStatusQualifierType(String type) {
		super(type);
	}
	
	public static StatusQualifierType getByType(String type) {
		if (NOT_SUPPORTED_BY_SOFTWARE_VALUE.equals(type))
			return NOT_SUPPORTED_BY_SOFTWARE;
		
		if (NOT_SUPPORTED_BY_HARDWARE_VALUE.equals(type))
			return NOT_SUPPORTED_BY_HARDWARE;
		
		if (JOURNAL_PRINTER_BACKUP_IS_INACTIVE_VALUE.equals(type))
			return JOURNAL_PRINTER_BACKUP_IS_INACTIVE;
		
		return UNKNOWN;
	}
	
	@Override
	public String getDescription() {
		if (NOT_SUPPORTED_BY_SOFTWARE_VALUE.equals(getType()))
			return "NOT_SUPPORTED_BY_SOFTWARE";
		
		if (NOT_SUPPORTED_BY_HARDWARE_VALUE.equals(getType()))
			return "NOT_SUPPORTED_BY_HARDWARE";
		
		if (JOURNAL_PRINTER_BACKUP_IS_INACTIVE_VALUE.equals(getType()))
			return "JOURNAL_PRINTER_BACKUP_IS_INACTIVE";
		
		return super.getDescription();	
	}
}
