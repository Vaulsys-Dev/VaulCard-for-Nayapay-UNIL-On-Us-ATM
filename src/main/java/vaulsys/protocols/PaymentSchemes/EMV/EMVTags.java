package vaulsys.protocols.PaymentSchemes.EMV;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 10/4/2018.
 */
public class EMVTags {
    public static final String TXN_STATUS_INFO = "9B";
    public static final String APP_LABEL = "50";
    public static final String TERM_CAP = "9F33";
    public static final String TVR = "95";
    public static final String UNPRED_NO = "9F37";
    public static final String IFD_NO = "9F1E";
    public static final String ISS_APP_DATA = "9F10";
    public static final String APP_CRYPT = "9F26";
    public static final String ATC = "9F36";
    public static final String APP_INTER_PROF = "82";
    public static final String TRAN_TYPE = "9C";
    public static final String TERM_CTRY_CODE = "9F1A";
    public static final String TRAN_DATE = "9A";
    public static final String AMT_AUTH = "9F02";
    public static final String OTHER_AMT = "9F03";
    public static final String TRAN_CURR_CODE = "5F2A";
    public static final String AID_TERM = "9F06";
    public static final String TRK2_EQUIV_DATA = "57";
    public static final String PAN = "5A";
    public static final String PAN_SEQ_NO = "5F34";
    public static final String CRYP_INFO_DATA = "9F27";
    public static final String CDOL1 = "8C";
    public static final String CVM = "9F34";
    public static final String DED_FILE_NAME = "84";
    public static final String CDOL2 = "8D";
    public static final String ADD_TERM_CAP = "9F40";
    public static final String TERM_FLOOR_LIMIT = "9F1B";
    public static final String TXN_SEQ_MO = "9F41";
    public static final String TXN_TIME = "9F21";
    public static final String APP_VER_NO = "9F09";
    public static final String TERM_TYPE = "9F35";
    public static final String ISS_AUTH_DATA = "91";
    public static final String APP_ID_CARD = "4F";
    public static final String ARPC = "91";

    public static List<String> getTranDataList() {
        List<String> tranDataList = new ArrayList<String>();
        tranDataList.add(EMVTags.AMT_AUTH);
        tranDataList.add(EMVTags.OTHER_AMT);
        tranDataList.add(EMVTags.TERM_CTRY_CODE);
        tranDataList.add(EMVTags.TVR);
        tranDataList.add(EMVTags.TRAN_CURR_CODE);
        tranDataList.add(EMVTags.TRAN_DATE);
        tranDataList.add(EMVTags.TRAN_TYPE);
        tranDataList.add(EMVTags.UNPRED_NO);
        tranDataList.add(EMVTags.APP_INTER_PROF);
        tranDataList.add(EMVTags.ATC);
        tranDataList.add(EMVTags.ISS_APP_DATA);

        return tranDataList;
    }
}
