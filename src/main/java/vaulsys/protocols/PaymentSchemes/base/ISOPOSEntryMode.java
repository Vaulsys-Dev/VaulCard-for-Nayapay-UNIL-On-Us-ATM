package vaulsys.protocols.PaymentSchemes.base;

/**
 * Created by HP on 4/27/2017.
 */
public class ISOPOSEntryMode {
    public class PANEntryMode {
        public static final String UNKNOWN = "00";
        public static final String MANUAL_ENTRY = "01";
        public static final String MST_READ_CVV_NOT_POSSIBLE = "02";
        public static final String ICC_READ_CVV_POSSIBLE = "05";
        public static final String ICC_READ_CONTACTLESS = "07";
		//m.rehman: Euronet integration
        public static final String CREDENTIALS_ON_FILE = "10";
        public static final String ICC_READ_FAIL_MST_READ = "80";
        public static final String MST_READ_CVV_POSSIBLE = "90";
        public static final String ICC_READ_CVV_NOT_POSSIBLE = "95";
    }

    public class PINEntryCapability {
        public static final String UNKNOWN = "0";
        public static final String TERMINAL_CAN_ACCEPT_PINS = "1";
        public static final String TERMINAL_CANNOT_ACCEPT_PINS = "2";
    }
}
