package vaulsys.protocols.ndc.constants;

import vaulsys.persistence.IEnum;

public class NDCConstants implements IEnum {
	
    public static final byte UNSOLICITED_MESSAGE = '1';
    public static final byte SOLICITED_MESSAGE = '2';

    public static final byte CONSUMER_REQUEST_OPERATIONAL_MESSAGE = '1';
    public static final byte STATUS_MESSAGE = '2';
    public static final byte WRITE_COMMAND = '3';
    public static final byte FUNCTION_COMMAND = '4';

    public static final byte TOP_OF_FORM_NOT_PRINT = '0';
    public static final byte TOP_OF_FORM_PRINT = '1';

    public static final byte WRITE_IDENTIFIER = '1';

    public static final byte MODIFIER_PARAMETER = '3';
    public static final byte MODIFIER_ENHANCED_PARAMETER = 'A';

    public static final byte NEED_TO_REVERS_MESSAGE = '9';
    
    public static final String KEYS_E = "E";
    public static final String KEYS_T = "T";
    
    public static final Long BILL_PAY_TYPE_MANUAL = 1L;
    public static final Long BILL_PAY_TYPE_BARCODE = 2L;
    public static final Long BILL_PAY_TYPE_MCI_PHONE_NUMBER = 3L;

    public static final Long BAL_TYPE_PRINT = 1L;
    public static final Long BAL_TYPE_SHOW = 2L;
}
