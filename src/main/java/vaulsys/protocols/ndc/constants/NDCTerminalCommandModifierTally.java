package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCTerminalCommandModifierTally implements NDCTerminalCommandModifier,IEnum{
		private static final char UNKNOWN_VALUE = '?';
	    private static final char PROCESSOR_AND_SYSTEM_VALUE = 'A';
	    private static final char HIGH_ORDER_COMMUNICATION_VALUE = 'B';
	    private static final char SYSTEM_DISK_VALUE = 'C';
	    private static final char CARD_READER_WRITER_VALUE = 'D';
	    private static final char CASH_HANDLER_VALUE = 'E';
	    private static final char DEPOSITORY_VALUE = 'F';
	    private static final char RECEIPT_PRINTER_VALUE = 'G';
	    private static final char JOURNAL_PRINTER_VALUE = 'H';
	    private static final char NIGH_SAFE_VALUE = 'K';
	    private static final char ENCRYPTOR_VALUE = 'L';
	    private static final char CAMERA_VALUE = 'M';
	    private static final char DOOR_ACCESS_VALUE = 'N';
	    private static final char OFFLINE_DISK_VALUE = 'O';
	    private static final char STATEMENT_PRINTER_VALUE = 'V';
	    private static final char CDM_VALUE = 'Y';
	    private static final char ENVELOPE_VALUE = '\\';
	    private static final char DPM_VALUE = ']';
	    
	    public static NDCTerminalCommandModifierTally UNKNOWN = new NDCTerminalCommandModifierTally(UNKNOWN_VALUE);
	    public static NDCTerminalCommandModifierTally PROCESSOR_AND_SYSTEM = new NDCTerminalCommandModifierTally(PROCESSOR_AND_SYSTEM_VALUE);
	    public static NDCTerminalCommandModifierTally HIGH_ORDER_COMMUNICATION = new NDCTerminalCommandModifierTally(HIGH_ORDER_COMMUNICATION_VALUE);
	    public static NDCTerminalCommandModifierTally SYSTEM_DISK = new NDCTerminalCommandModifierTally(SYSTEM_DISK_VALUE);
	    public static NDCTerminalCommandModifierTally CARD_READER_WRITER = new NDCTerminalCommandModifierTally(CARD_READER_WRITER_VALUE);
	    public static NDCTerminalCommandModifierTally CASH_HANDLER = new NDCTerminalCommandModifierTally(CASH_HANDLER_VALUE);
	    public static NDCTerminalCommandModifierTally DEPOSITORY = new NDCTerminalCommandModifierTally(DEPOSITORY_VALUE);
	    public static NDCTerminalCommandModifierTally RECEIPT_PRINTER = new NDCTerminalCommandModifierTally(RECEIPT_PRINTER_VALUE);
	    public static NDCTerminalCommandModifierTally JOURNAL_PRINTER = new NDCTerminalCommandModifierTally(JOURNAL_PRINTER_VALUE);
	    public static NDCTerminalCommandModifierTally NIGH_SAFE = new NDCTerminalCommandModifierTally(NIGH_SAFE_VALUE);
	    public static NDCTerminalCommandModifierTally ENCRYPTOR = new NDCTerminalCommandModifierTally(ENCRYPTOR_VALUE);
	    public static NDCTerminalCommandModifierTally CAMERA = new NDCTerminalCommandModifierTally(CAMERA_VALUE);
	    public static NDCTerminalCommandModifierTally DOOR_ACCESS = new NDCTerminalCommandModifierTally(DOOR_ACCESS_VALUE);
	    public static NDCTerminalCommandModifierTally OFFLINE_DISK = new NDCTerminalCommandModifierTally(OFFLINE_DISK_VALUE);
	    public static NDCTerminalCommandModifierTally STATEMENT_PRINTER = new NDCTerminalCommandModifierTally(STATEMENT_PRINTER_VALUE);
	    public static NDCTerminalCommandModifierTally CDM = new NDCTerminalCommandModifierTally(CDM_VALUE);
	    public static NDCTerminalCommandModifierTally ENVELOPE = new NDCTerminalCommandModifierTally(ENVELOPE_VALUE);
	    public static NDCTerminalCommandModifierTally DPM = new NDCTerminalCommandModifierTally(DPM_VALUE);
	    
	    private char code;

	    public NDCTerminalCommandModifierTally() {
	    }

	    public NDCTerminalCommandModifierTally(char code) {
	        this.code = code;
	    }

	    public static NDCTerminalCommandModifierTally getByCode(char type) {
	    	if (type == 'A')
	    		return PROCESSOR_AND_SYSTEM;
	    	
	    	if (type == 'B')
	    		return HIGH_ORDER_COMMUNICATION;
	    	
	    	if (type == 'C')
	    		return SYSTEM_DISK;

	    	if (type == 'D')
	    		return CARD_READER_WRITER;
	    	
	    	if (type == 'E')
	    		return CASH_HANDLER;
	    	
	    	if (type == 'F')
	    		return DEPOSITORY;
	    	
	    	if (type == 'G')
	    		return RECEIPT_PRINTER;
	    	
	    	if (type == 'H')
	    		return JOURNAL_PRINTER;
	    	
	    	if (type == 'K')
	    		return NIGH_SAFE;
	    	
	    	if (type == 'L')
	    		return ENCRYPTOR;
	    	
	    	if (type == 'M')
	    		return CAMERA;
	    	
	    	if (type == 'N')
	    		return DOOR_ACCESS;
	    	
	    	if (type == 'O')
	    		return OFFLINE_DISK;
	    	
	    	if (type == 'V')
	    		return STATEMENT_PRINTER;
	    	
	    	if (type == 'Y')
	    		return CDM;
	    	
	    	if (type == '\\')
	    		return ENVELOPE;
	    	
	    	if (type == ']')
	    		return DPM;

	    	return UNKNOWN;
	    }
	    
		public char getCode() {
			return code;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + code;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NDCTerminalCommandModifierTally other = (NDCTerminalCommandModifierTally) obj;
			if (code != other.code)
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			if (code == 'A')
	    		return "PROCESSOR_AND_SYSTEM";
	    	
	    	if (code == 'B')
	    		return "HIGH_ORDER_COMMUNICATION";
	    	
	    	if (code == 'C')
	    		return "SYSTEM_DISK";

	    	if (code == 'D')
	    		return "CARD_READER_WRITER";
	    	
	    	if (code == 'E')
	    		return "CASH_HANDLER";
	    	
	    	if (code == 'F')
	    		return "DEPOSITORY";
	    	
	    	if (code == 'G')
	    		return "RECEIPT_PRINTER"; 	
	    	
	    	if (code == 'H')
	    		return "JOURNAL_PRINTER";
	    	
	    	if (code == 'K')
	    		return "NIGH_SAFE";
	    	
	    	if (code == 'L')
	    		return "ENCRYPTOR";
	    	
	    	if (code == 'M')
	    		return "CAMERA";
	    	
	    	if (code == 'N')
	    		return "DOOR_ACCESS";
	    	
	    	if (code == 'O')
	    		return "OFFLINE_DISK";
	    	
	    	if (code == 'V')
	    		return "STATEMENT_PRINTER";
	    	
	    	if (code == 'Y')
	    		return "CDM";
	    	
	    	if (code == '\\')
	    		return "ENVELOPE";
	    	
	    	if (code == ']')
	    		return "DPM";
	    	
	    	return "UNKNOWN";
		}
}
