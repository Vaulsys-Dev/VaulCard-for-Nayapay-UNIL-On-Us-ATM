package vaulsys.protocols.saderat87;

import vaulsys.protocols.ifx.IFXReturnCode;
import vaulsys.protocols.saderat87.SaderatReturnCode;

import java.util.HashMap;


public class SaderatReturnCode {

    // ISO : Action Codes

    static public String approved = "approved";
    static public String do_not_honour = "do_not_honour";
    static public String expired_card = "expired card";
    static public String suspected_fraud = "suspected fraud";
    static public String restricted_card = "restricted card";
    static public String no_destination = "transaction destination cannot be found for routing";

    static private HashMap<String, String> returnCodes = new HashMap<String, String>();
    static private HashMap<Long, String> ifxToiso = new HashMap<Long, String>();

    public static String getCode(String isodscr) {
        return returnCodes.get(isodscr);
    }

    public static String getCode(Long ifxCode) {
        return ifxToiso.get(ifxCode);
    }

    public static void addCode(String dscr, String code) {
        returnCodes.put(dscr, code);
    }

    public static void map(Long ifxCode, String isoCode) {
        ifxToiso.put(ifxCode, isoCode);
    }


    static {
        addCode(SaderatReturnCode.approved, "000");
        map(IFXReturnCode.getErrorCode(IFXReturnCode.NoError), "000");

        addCode(SaderatReturnCode.do_not_honour, "100");


        addCode(SaderatReturnCode.expired_card, "101");
        addCode(SaderatReturnCode.suspected_fraud, "102");
        addCode(SaderatReturnCode.restricted_card, "104");

        addCode(SaderatReturnCode.no_destination, "908");
        map(IFXReturnCode.getErrorCode(IFXReturnCode.No_Destination), "908");

    }




}
