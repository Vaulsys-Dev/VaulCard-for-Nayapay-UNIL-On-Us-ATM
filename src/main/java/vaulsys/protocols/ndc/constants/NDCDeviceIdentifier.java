package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCDeviceIdentifier implements IEnum {
   
	private static final char UNKNOWN_VALUE = '?';
	private static final char DIGITAL_AUDIO_SERVICE_VALUE = 'a';
	private static final char TIME_OF_DAY_CLOCK_VALUE = 'A';
	private static final char POWER_FAILURE_VALUE = 'B';
	private static final char SYSTEM_DISK_VALUE = 'C';
	private static final char CARD_READER_WRITER_VALUE = 'D';
	private static final char CASH_HANDLER_VALUE = 'E';
	private static final char DEPOSIT_VALUE = 'F';
	private static final char RECEIPT_PRINTER_VALUE = 'G';
	private static final char JOURNAL_PRINTER_VALUE = 'H';
	private static final char NIGH_SAFE_DEPOSITIRY_VALUE = 'K';
    private static final char ENCRYPTOR_VALUE = 'L';
    private static final char SECURITY_CAMERA_VALUE = 'M';
    private static final char DOOR_ACCESS_VALUE = 'N';
    private static final char OFFLINE_DISK_VALUE = 'O';
    private static final char SENSORS_VALUE = 'P';
    private static final char CARDHOLDER_KEYBOARD_VALUE = 'Q';
    private static final char SUPERVISOR_KEYS_VALUE = 'R';
    private static final char CARD_HOLDER_DISPLAY_VALUE = 'S';
    private static final char STATEMENT_VALUE = 'V';
    private static final char COIN_DISPENCER_VALUE = 'Y';
    private static final char SYSTEM_DISPLAY_VALUE = 'Z';
    private static final char MEDIA_ENTRY_EXIT_INDICATOR_VALUE = '[';
    private static final char ENVELOPE_DISPENSER_VALUE = '\\';
    private static final char DOCUMENT_PROCESSING_MODULE_VALUE = ']';
    private static final char CDM_TAMPER_INDICATOR_VALUE = '^';
    private static final char DPM_TAMPER_INDICATOR_VALUE = '-';



    
    public static NDCDeviceIdentifier UNKNOWN = new NDCDeviceIdentifier(UNKNOWN_VALUE);
    public static NDCDeviceIdentifier CASH_HANDLER = new NDCDeviceIdentifier(CASH_HANDLER_VALUE);
    public static NDCDeviceIdentifier CARD_READER_WRITER = new NDCDeviceIdentifier(CARD_READER_WRITER_VALUE);
    public static NDCDeviceIdentifier DEPOSIT = new NDCDeviceIdentifier(DEPOSIT_VALUE);
    public static NDCDeviceIdentifier ENCRYPTOR = new NDCDeviceIdentifier(ENCRYPTOR_VALUE);
    public static NDCDeviceIdentifier RECEIPT_PRINTER = new NDCDeviceIdentifier(RECEIPT_PRINTER_VALUE);
    public static NDCDeviceIdentifier JOURNAL_PRINTER = new NDCDeviceIdentifier(JOURNAL_PRINTER_VALUE);
    public static NDCDeviceIdentifier DOOR_ACCESS = new NDCDeviceIdentifier(DOOR_ACCESS_VALUE);
    public static NDCDeviceIdentifier SENSORS = new NDCDeviceIdentifier(SENSORS_VALUE);
    public static NDCDeviceIdentifier COIN_DISPENCER = new NDCDeviceIdentifier(COIN_DISPENCER_VALUE);
    public static NDCDeviceIdentifier DIGITAL_AUDIO_SERVICE = new NDCDeviceIdentifier(DIGITAL_AUDIO_SERVICE_VALUE);
    public static NDCDeviceIdentifier POWER_FAILURE = new NDCDeviceIdentifier(POWER_FAILURE_VALUE);
    public static NDCDeviceIdentifier SUPERVISOR_KEYS = new NDCDeviceIdentifier(SUPERVISOR_KEYS_VALUE);
    public static NDCDeviceIdentifier TIME_OF_DAY_CLOCK = new NDCDeviceIdentifier(TIME_OF_DAY_CLOCK_VALUE);
    public static NDCDeviceIdentifier SYSTEM_DISK = new NDCDeviceIdentifier(SYSTEM_DISK_VALUE);
    public static NDCDeviceIdentifier NIGH_SAFE_DEPOSITIRY = new NDCDeviceIdentifier(NIGH_SAFE_DEPOSITIRY_VALUE);
    public static NDCDeviceIdentifier SECURITY_CAMERA = new NDCDeviceIdentifier(SECURITY_CAMERA_VALUE);
    public static NDCDeviceIdentifier OFFLINE_DISK = new NDCDeviceIdentifier(OFFLINE_DISK_VALUE);

    public static NDCDeviceIdentifier CARDHOLDER_KEYBOARD = new NDCDeviceIdentifier(CARDHOLDER_KEYBOARD_VALUE);
    public static NDCDeviceIdentifier CARD_HOLDER_DISPLAY = new NDCDeviceIdentifier(CARD_HOLDER_DISPLAY_VALUE);
    public static NDCDeviceIdentifier STATEMENT = new NDCDeviceIdentifier(STATEMENT_VALUE);
    public static NDCDeviceIdentifier SYSTEM_DISPLAY = new NDCDeviceIdentifier(SYSTEM_DISPLAY_VALUE);
    public static NDCDeviceIdentifier MEDIA_ENTRY_EXIT_INDICATOR = new NDCDeviceIdentifier(MEDIA_ENTRY_EXIT_INDICATOR_VALUE);
    public static NDCDeviceIdentifier ENVELOPE_DISPENSER = new NDCDeviceIdentifier(ENVELOPE_DISPENSER_VALUE);
    public static NDCDeviceIdentifier DOCUMENT_PROCESSING_MODULE = new NDCDeviceIdentifier(DOCUMENT_PROCESSING_MODULE_VALUE);
    public static NDCDeviceIdentifier CDM_TAMPER_INDICATOR = new NDCDeviceIdentifier(CDM_TAMPER_INDICATOR_VALUE);
    public static NDCDeviceIdentifier DPM_TAMPER_INDICATOR = new NDCDeviceIdentifier(DPM_TAMPER_INDICATOR_VALUE);

    
    private char code;

    public NDCDeviceIdentifier() {
    }

    public NDCDeviceIdentifier(char code) {
        this.code = code;
    }

    public static NDCDeviceIdentifier getByCode(char type) {
    	if (type == 'a')
    		return DIGITAL_AUDIO_SERVICE;
    	
    	if (type == 'A')
    		return TIME_OF_DAY_CLOCK;
    	
    	if (type == 'B')
    		return POWER_FAILURE;
    	
    	if (type == 'C')
    		return SYSTEM_DISK;

    	if (type == 'D')
    		return CARD_READER_WRITER;

    	if (type == 'E')
    		return CASH_HANDLER;
    	
    	if (type == 'F')
    		return DEPOSIT;
    	
    	if (type == 'G')
    		return RECEIPT_PRINTER;
    	
    	if (type == 'H')
    		return JOURNAL_PRINTER;

    	if (type == 'K')
    		return NIGH_SAFE_DEPOSITIRY;
    	
    	if (type == 'L')
    		return ENCRYPTOR;
    	
    	if (type == 'M')
    		return SECURITY_CAMERA;
    	
    	if (type == 'N')
    		return DOOR_ACCESS;
    	
    	if (type == 'O')
    		return OFFLINE_DISK;
    	
    	if (type == 'P')
    		return SENSORS;
    	
    	if (type == 'Q')
    		return CARDHOLDER_KEYBOARD;
    	
    	if (type == 'R')
    		return SUPERVISOR_KEYS;
    	
    	if (type == 'S')
    		return CARD_HOLDER_DISPLAY;
    	
    	if (type == 'V')
    		return STATEMENT;

    	if (type == 'Y')
    		return COIN_DISPENCER;
    	
    	if (type == 'Z')
    		return SYSTEM_DISPLAY;

    	if (type == '[')
    		return MEDIA_ENTRY_EXIT_INDICATOR;

    	if (type == '\\')
    		return ENVELOPE_DISPENSER;

    	if (type == '^')
    		return CDM_TAMPER_INDICATOR;

    	if (type == '-')
    		return DPM_TAMPER_INDICATOR;
	
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
		NDCDeviceIdentifier other = (NDCDeviceIdentifier) obj;
		if (code != other.code)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		if (code == 'a')
    		return "DIGITAL_AUDIO_SERVICE";
    	
		if (code == 'A')
    		return "TIME_OF_DAY_CLOCK";

		if (code == 'B')
    		return "POWER_FAILURE";
    	
		if (code == 'C')
    		return "SYSTEM_DISK";
		
    	if (code == 'D')
    		return "CARD_READER_WRITER";

    	if (code == 'E')
    		return "CASH_HANDLER";
    	
    	if (code == 'F')
    		return "DEPOSIT";
    	
    	if (code == 'G')
    		return "RECEIPT_PRINTER";
    	
    	if (code == 'H')
    		return "JOURNAL_PRINTER";

    	if (code == 'K')
    		return "NIGH_SAFE_DEPOSITIRY";
    	
    	if (code == 'L')
    		return "ENCRYPTOR";
    	
    	if (code == 'M')
    		return "SECURITY_CAMERA";

    	if (code == 'N')
    		return "DOOR_ACCESS";
    	
    	if (code == 'O')
    		return "OFFLINE_DISK";

    	if (code == 'P')
    		return "SENSORS";
    	
    	if (code == 'Q')
    		return "CARDHOLDER_KEYBOARD";

    	if (code == 'R')
    		return "SUPERVISOR_KEYS";
    	
    	if (code == 'S')
    		return "CARD_HOLDER_DISPLAY";

    	if (code == 'V')
    		return "STATEMENT";

    	if (code == 'Y')
    		return "COIN_DISPENCER";
    	
    	if (code == 'Z')
    		return "SYSTEM_DISPLAY";

    	if (code == '[')
    		return "MEDIA_ENTRY_EXIT_INDICATOR";

    	if (code == '\\')
    		return "ENVELOPE_DISPENSER";

    	if (code == '^')
    		return "CDM_TAMPER_INDICATOR";

    	if (code == '-')
    		return "DPM_TAMPER_INDICATOR";

    	
    	return "UNKNOWN";
	}
}
