package vaulsys.security.hsm.eracom.base;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ErrorCode {

    private static Map<String, Byte> descriptionToErrorCode = new HashMap<String, Byte>();
    private static Map<Byte, String> errorCodeToDscr = new HashMap<Byte, String>();
    private static Logger logger = Logger.getLogger(ErrorCode.class);


    public static byte getErrorCode(String dscr) {

        if (descriptionToErrorCode.containsKey(dscr)) {
            return descriptionToErrorCode.get(dscr).byteValue();
        }
        return (byte) 0xFF;
    }

    public static String getDescription(byte errorCode) {
        if (errorCodeToDscr.containsKey(new Byte(errorCode))) {
            return errorCodeToDscr.get(new Byte(errorCode));
        }
        return null;
    }


    public static void addErrorCode(String dscr, byte errorCode) {
        if (!descriptionToErrorCode.containsKey(dscr) && !errorCodeToDscr.containsKey(new Byte(errorCode))) {
            descriptionToErrorCode.put(dscr, new Byte(errorCode));
            errorCodeToDscr.put(new Byte(errorCode), dscr);
        } else {
            logger.info("This error code was defined before!");
        }
    }


    public static void removeErrorCode(byte errorCode) {
        byte error = new Byte(errorCode);
        if (errorCodeToDscr.containsKey(error)) {
            String dscr = errorCodeToDscr.get(error);
            errorCodeToDscr.remove(error);
            descriptionToErrorCode.remove(dscr);
        }
    }


    public static void updateErrorCode(String dscr, byte errorCode) {
        removeErrorCode(errorCode);
        addErrorCode(dscr, errorCode);
    }


    static {
        addErrorCode("No_error", (byte) 0x00);
        addErrorCode("DES_Fault: system disabled", (byte) 0x01);
        addErrorCode("Illegal Function Code. PIN mailing not enabled", (byte) 0x02);
        addErrorCode("Incorrect_message_length", (byte) 0x03);
        addErrorCode("Invalid_data_in_message: Character not in range (0-9, A-F)", (byte) 0x04);
        addErrorCode("Invalid_key_index: Index not defined, key with this Index not stored or incorrect key length", (byte) 0x05);
        addErrorCode("Invalid_PIN_format_specifier: only AS/ANSI,1 & PIN/PAD,3 specified", (byte) 0x06);

        /*
       * PIN does not comply with the AS2805.3 1985 specification, is in an invalid PIN/PAD format, or is in an invalid Docutel format
       */
        addErrorCode("PIN format error: " +
                "PIN does not comply with the AS2805.3 1985 specification, is in an invalid PIN/PAD format, or is in an invalid Docutel format"
                , (byte) 0x07);
        addErrorCode("Verification_failure", (byte) 0x08);

        /* : e.g. the ProtectHost White was tampered or all Keys deleted */
        addErrorCode("Contents_of_key_memory_destroyed", (byte) 0x09);

        /* Key or decimalization table (DT) is not stored in the ProtectHost White. */
        addErrorCode("Uninitiated_key_accessed", (byte) 0x0A);

        /*
       * Customer PIN length is less than the minimum PVK length or less than Checklen in function
       */
        addErrorCode("Checklength_Error", (byte) 0x0B);

        /* inconsistent field size. */
        addErrorCode("Inconsistent_Request_Fields", (byte) 0x0C);

        /* Invalid VISA PIN verification key indicator. */
        addErrorCode("Invalid_VISA_Index", (byte) 0x0F);
        addErrorCode("Zero_PIN_length", (byte) 0x0F);

        addErrorCode("Internal_Error", (byte) 0x10);
        addErrorCode("Errlog_file_does_not_exist", (byte) 0x11);
        addErrorCode("Errlog_internal_error", (byte) 0x12);
        addErrorCode("Errlog_request_length_invalid", (byte) 0x13);
        addErrorCode("Errlog_file_number_invalid", (byte) 0x14);
        addErrorCode("Errlog_index_number_invalid", (byte) 0x15);
        addErrorCode("Errlog_date_time_invalid", (byte) 0x16);
        addErrorCode("Errlog_before_after_flag_invalid", (byte) 0x17);
        addErrorCode("Unsupported_key_type", (byte) 0x19);
        addErrorCode("Invalid_key_specifier_length", (byte) 0x20);
        addErrorCode("Unsupported_key_specifier", (byte) 0x21);
        addErrorCode("Invalid_key_specifier_content", (byte) 0x22);
        addErrorCode("Invalid_key_specifier_format", (byte) 0x23);
        addErrorCode("Invalid", (byte) 0x24);        // =00
        addErrorCode("Invalid_key_attributes", (byte) 0x25);
        addErrorCode("Hash_process_failed", (byte) 0x27);
        addErrorCode("Invalid_Key_Type ", (byte) 0x28);         // - Not Triple DES
        addErrorCode("Unsupported_Triple_Des_Index", (byte) 0x29);
        addErrorCode("Invalid_administrator_signature", (byte) 0x30);
        addErrorCode("No_administration_session", (byte) 0x32);
        addErrorCode("Invalid_file_type", (byte) 0x33);
        addErrorCode("Invalid_signature", (byte) 0x34);
        addErrorCode("KKL_disabled", (byte) 0x35);
        addErrorCode("No_PIN_pad", (byte) 0x36);
        addErrorCode("Pin_pad_timeout", (byte) 0x37);


        addErrorCode("Invalid_Meta_Function_Code", (byte) 0xE3);
        addErrorCode("Invalid_Reserved_Byte", (byte) 0xE4);
        addErrorCode("Invalid_meta_Function_Id", (byte) 0xE5);
        addErrorCode("Invalid_version", (byte) 0xE6);
        addErrorCode("Invalid_Message_Id", (byte) 0xE7);
        addErrorCode("Invalid_Version_Commen_Header", (byte) 0xE8);
        addErrorCode("Invalid_Sequence", (byte) 0xF1);
        addErrorCode("Invalid_data_Length", (byte) 0xF2);

    }

}
