package vaulsys.security.hsm.thales;

/**
 * Created by HP on 8/10/2016.
 */
public class ThalesHSMConst {
    public static final String TPK_TYPE = "TPK";
    public static final String TMK_TYPE = "TMK";
    public static final String ZPK_TYPE = "ZPK";
    public static final String ZMK_TYPE = "ZMK";
    public static final String MACKEY_TYPE = "MACKEY_TYPE";
    public static final String MAX_PIN_LENGTH = "12";
    public static final String LOCAL_NETWORK = "LOCAL";
    public static final String ZONAL_NETWORK = "ZONAL";

    public static class Commands {
        public static final String PIN_GENERATE_LOCAL = "BA"; //Raza TODO: verify THIS
        public static final String PIN_VERIFY_LOCAL = "BC";
        public static final String PIN_VERIFY_ZONAL = "BE";
        public static final String PIN_TRANSLATE_LOCAL = "CA";
        public static final String PIN_TRANSLATE_ZONAL = "CC";
        public static final String CVV_VERIFY = "CY";
        public static final String CVV_GENERATE = "CW";
        public static final String TRANSLATE_ZPK = "FA";
        public static final String GENERATE_TPK = "HC";
        public static final String PIN_CHANGE = "JC";
        public static final String ARQC_VALIDATE_ARPC_GENERATE_KEY = "KQ";
    }

    public static class KeySpecifierFormat {
        public static final String DOUBLE_LEN_KEY_SPEC = "U";
    }

    public static class PINFormat {
        public static final String PIN_FORMAT_01 = "01";
    }

    public static class ARQCAction {
        public static final String VERIFY_ARQC_ONLY = "0";
        public static final String VERIFY_ARQC_GENERATE_ARPC = "1";
        public static final String GENERATE_ARPC_ONLY = "2";
    }

    public static class SchemeID {
        public static final String VISA = "0";
        public static final String EUROPAY_MASTERCARD = "1";
        public static final String AMERICAN_EXPRESS = "2";
    }
}
