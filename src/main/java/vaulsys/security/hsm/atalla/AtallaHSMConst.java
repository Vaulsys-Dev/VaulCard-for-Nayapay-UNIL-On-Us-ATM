package vaulsys.security.hsm.atalla;

/**
 * Created by HP on 8/10/2016.
 */
public class AtallaHSMConst {
    public static final String TPK_TYPE = "TPK";
    public static final String TMK_TYPE = "TMK";
    public static final String ZPK_TYPE = "ZPK";
    public static final String ZMK_TYPE = "ZMK";
    public static final String MACKEY_TYPE = "MACKEY_TYPE";
    public static final String MAX_PIN_LENGTH = "12";
    public static final String LOCAL_NETWORK = "LOCAL";
    public static final String ZONAL_NETWORK = "ZONAL";
    public static final String SEPARATOR = "#";
    public static final String COMMAND_START = "<";
    public static final String COMMAND_END = ">";

    public static class Commands {
        public static final String PIN_GENERATE = "3D";
        public static final String PIN_GENERATE_RESP = "4D";
        public static final String PIN_VERIFY = "32";
        public static final String PIN_VERIFY_RESP = "42";
        public static final String PIN_TRANSLATE = "31";
        public static final String PIN_TRANSLATE_RESP = "41";
        public static final String CVV_GENERATE = "5D";
        public static final String CVV_VERIFY = "5E";
        public static final String CVV_VERIFY_RESP = "6E";

        // Asim Shahzad, Date : 18th Jan 2021, Tracking ID : VC-NAP-202101071 / VP-NAP-202101071 / VG-NAP-202101071 (Release # 2)
        public static final String CVV_VERIFY_GENERATE = "6D";
        // ========================================================================================================

        public static final String PIN_CHANGE = "37";
        public static final String PIN_CHANGE_RESP = "47";
        public static final String ARQC_VALIDATE_ARPC_GENERATE = "350";
        public static final String ARQC_VALIDATE_ARPC_GENERATE_RESP = "450";

        public static final String TRANSLATE_ZPK = "FA";
        public static final String GENERATE_TPK = "HC";
    }

    public static class KeySpecifierFormat {
        public static final String DOUBLE_LEN_KEY_SPEC = "U";
    }

    public static class PINFormat {
        public static final String ANSI = "1";
        public static final String IBM3624 = "2";
        public static final String PIN_PAD_DIEBOLD_DOCTUEL = "3";
        public static final String IBM_ENCRYPT_PIN_PAD = "4";
        public static final String BURROUGHS = "5";
        public static final String DUKPT = "7";
        public static final String IBM4731 = "9";
    }

    public static class ARQCAction {
        public static final String VERIFY_ARQC_ONLY = "0";
        public static final String VERIFY_ARQC_GENERATE_ARPC = "1";
        public static final String GENERATE_ARPC_ONLY = "2";
    }

    public static class SchemeID {
        public static final String EUROPAY_MASTERCARD = "0";
        public static final String VISA = "1";
        public static final String COMMON_SESSION = "2";
        public static final String UNIONPAY_INTL = "4";
        public static final String EMV2000 = "8";
        public static final String EMVTREE = "9";
    }

    public static class PINGenerationMethod {
        public static final String IBM3624 = "1";
    }

    public static class PINVerificationMethod {
        public static final String IBM3624 = "2";
    }

    public static class CVVAlgorithm {
        public static final String OLD_ALGO = "2";
        public static final String STANDARD_ALGO = "3";
    }
}
