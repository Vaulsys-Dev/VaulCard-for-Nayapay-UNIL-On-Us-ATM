package vaulsys.security.hsm.safenet;

/**
 * Created by HP on 8/10/2016.
 */
public class SafeNetHSMConst {
    public static final String FUNC_CODE = "00";
    public static final String TPK_TYPE = "TPK";
    public static final String TMK_TYPE = "TMK";
    public static final String ZPK_TYPE = "ZPK";
    public static final String ZMK_TYPE = "ZMK";
    public static final String MACKEY_TYPE = "MACKEY_TYPE";

    public static class FunctionCode {
        public static final String PIN_TRANSLATE = "EE0602";
        public static final String PIN_VERIFY = "EE0642"; //Raza -- For NayaPay PIN verification
        public static final String PIN_GENERATE_CHANGE = "EE0643"; //Raza -- For NayaPay PIN generation,change,reset
        public static final String GENERATE_KEY = "EE0400";
        public static final String TRANSLATE_KEY = "EE0403";
        public static final String ARQC_VALIDATE_ARPC_GENERATE_KEY = "EE2018";
        public static final String CVV_GENERATE = "EE0802";
        public static final String CVV_VERIFY = "EE0803";
        public static final String CLEAR_PIN_ENCRYPT = "EE0600";
    }

    public static class KeySpecifierFormat {
        public static final String SINGLE_LEN_KEY_SPEC = "10";
        public static final String DOUBLE_LEN_KEY_SPEC_EBC = "11";
        public static final String DOUBLE_LEN_KEY_SPEC_CBC = "13";
    }

    public static class PINFormat {
        public static final String PIN_FORMAT_01 = "01";
    }

    public static class KeyFlags {
        public static final String SINGLE_LEN_PPK_FLAG = "0002";
        public static final String DOUBLE_LEN_PPK_FLAG = "0200";
        public static final String SINGLE_LEN_MPK_FLAG = "0004";
        public static final String DOUBLE_LEN_MPK_FLAG = "0400";
    }

    public static class ARQCAction {
        public static final String VERIFY_ARQC_ONLY = "01";
        public static final String GENERATE_ARPC_ONLY = "02";
        public static final String VERIFY_ARQC_GENERATE_ARPC = "03";
    }

    public static class MacKeyMethod {
        public static final String COMMON = "00";
        public static final String SECCOS = "01";
    }
}
