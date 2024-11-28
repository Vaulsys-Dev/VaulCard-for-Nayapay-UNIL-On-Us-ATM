package vaulsys.protocols.ifx;

import java.util.HashMap;

/**
 * @author noroozi
 */
public class IFXReturnCode {

    static private HashMap<Long, String> error = new HashMap<Long, String>();
    static private HashMap<String, Long> descriptions = new HashMap<String, Long>();
    static private HashMap<Long, Long> ifxToIso = new HashMap<Long, Long>();

    static public String NoError = "NoError";
    static public String Not_Applicable_Message = "Not Applicable Message";
    static public String Invalid_IFX_to_protocol = "Not Mapped IFX to Protocol";
    static public String Invalid_Protocol_to_Ifx = "Not Mapped Protocol to IFX";
    static public String Invalid_Msg_length = "Invalid Message Length";
    static public String Invalid_Protocol_to_Binary = "No Binary Produced From Protocol";
    static public String Invalid_Binary_Format = "Binary Stream Cannot be Mapped to Protocol";

    static public String General_Error = "General Error";
    static public String Client_Up_to_Date = "Client Up to Date";
    static public String General_Data_Error = "General Data Error";
    static public String System_Not_Available = "System Not Available";
    static public String Function_Not_Available = "Function Not Available";
    static public String Unsupported_Service = "Unsupported Service";
    static public String No_Destination = "No Destination";
    static public String Unsupported_Message = "Unsupported Message";
    static public String Unsupported_Function = "Unsupported Function";
    static public String Object_Already_Committed = "Object Already Committed";
    static public String Message_Cannot_Be_Reversed = "Message Cannot Be Reversed";
    static public String Asynchoronous_Message = "Asynchoronous Message";
    static public String MsgRq_Not_match = "MsgRq_doesn't_match";
    static public String Duplicate_RqUID = "Duplicate <RqUID>";
    static public String Required_Element_Not_Included = "Required Element Not Included";
    static public String Request_Declined = "Request Declined";
    static public String Invalid_Enum_Value = "Invalid Enum Value";
    static public String Cannot_Modify_Element = "Cannot Modify Element";


    static {
        error.put(0L, IFXReturnCode.NoError);
        descriptions.put(IFXReturnCode.NoError, 0L);
        ifxToIso.put(0L, 0L);

        error.put(2410L, "Destination Account Not Available");
        descriptions.put(IFXReturnCode.No_Destination, 2410L); // ISO 908
        ifxToIso.put(2410L, 908L);

        error.put(1L, "Client Up to Date");
        descriptions.put(IFXReturnCode.Client_Up_to_Date, 1l);

        error.put(100L, "General Error");
        descriptions.put(IFXReturnCode.General_Error, 100L);

        error.put(200L, "General Data Error");
        descriptions.put(IFXReturnCode.General_Data_Error, 200L);

        error.put(300L, "System Not Available");
        descriptions.put(IFXReturnCode.System_Not_Available, 300L);

        error.put(400L, "Function Not Available");
        descriptions.put(IFXReturnCode.Function_Not_Available, 400L);

        error.put(500L, "Unsupported Service");
        descriptions.put(IFXReturnCode.Unsupported_Service, 500L);

        error.put(600L, "Unsupported Message");
        descriptions.put(IFXReturnCode.Unsupported_Message, 600L);

        error.put(700L, "Unsupported Function");
        descriptions.put(IFXReturnCode.Unsupported_Function, 700L);

        error.put(800L, "Object Already Committed");
        descriptions.put(IFXReturnCode.Object_Already_Committed, 800L);

        error.put(810L, "Message Cannot Be Reversed");
        descriptions.put(IFXReturnCode.Message_Cannot_Be_Reversed, 810L);

        error.put(900L, "Message Accepted for Asynchronous Processing");
        descriptions.put(IFXReturnCode.Asynchoronous_Message, 900L);

        error.put(910L, "Asynchronous Request Does Not Match Original Request");
        descriptions.put(IFXReturnCode.MsgRq_Not_match, 910L);

        error.put(1000L, "Duplicate <RqUID>");
        descriptions.put(IFXReturnCode.Duplicate_RqUID, 1000L);

        error.put(1020L, "Required Element Not Included");
        descriptions.put(IFXReturnCode.Required_Element_Not_Included, 1020L);

        error.put(1040L, "Request Declined");
        descriptions.put(IFXReturnCode.Request_Declined, 1040L);

        error.put(1050L, "Invalid Enum Value");
        descriptions.put(IFXReturnCode.Invalid_Enum_Value, 1050L);

        error.put(1060L, "Cannot Modify Element");
        descriptions.put(IFXReturnCode.Cannot_Modify_Element, 1060L);

        error.put(-1L, IFXReturnCode.Not_Applicable_Message);
        descriptions.put(IFXReturnCode.Not_Applicable_Message, -1L);

        error.put(-2L, IFXReturnCode.Invalid_IFX_to_protocol);
        descriptions.put(IFXReturnCode.Invalid_IFX_to_protocol, -2L);

        error.put(-3L, IFXReturnCode.Invalid_Protocol_to_Ifx);
        descriptions.put(IFXReturnCode.Invalid_Protocol_to_Ifx, -3l);

        error.put(-4l, IFXReturnCode.Invalid_Binary_Format);
        descriptions.put(IFXReturnCode.Invalid_Binary_Format, -4L);

        error.put(-5l, IFXReturnCode.Invalid_Msg_length);
        descriptions.put(IFXReturnCode.Invalid_Msg_length, -5L);

        error.put(-6L, IFXReturnCode.Invalid_Protocol_to_Binary);
        descriptions.put(IFXReturnCode.Invalid_Protocol_to_Binary, -6L);

    }

    public static String getErrorDsc(Long errorCode) {
        return error.get(errorCode);
    }

    public static Long getErrorCode(String errorDsc) {
        if (descriptions.containsKey(errorDsc)) {
            return descriptions.get(errorDsc);
        }
        return -1L;
    }

}
