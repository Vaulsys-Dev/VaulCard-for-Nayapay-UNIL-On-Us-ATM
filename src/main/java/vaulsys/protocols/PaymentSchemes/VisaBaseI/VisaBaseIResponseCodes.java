package vaulsys.protocols.PaymentSchemes.VisaBaseI;

import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

/**
 * Created by m.rehman on 4/10/2016.
 */
public class VisaBaseIResponseCodes extends ISOResponseCodes {

    public static String mapRespCode(Integer msgReasonCode) {
        String respCode = null;

        switch (msgReasonCode) {
            case 4003:
                respCode = ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND;
                break;
            case 4004:
                respCode = ISOResponseCodes.CUSTOMER_INACTIVE;
                break;
            case 4005:
                respCode = ISOResponseCodes.DESTINATION_NOT_REGISTERED;
                break;
            case 4006:
                respCode = ISOResponseCodes.INVALID_TO_ACCOUNT;
                break;
            case 4007:
                respCode = ISOResponseCodes.INVALID_IMD;
                break;
            case 4008:
                respCode = ISOResponseCodes.HOST_NOT_PROCESSING;
                break;
            case 4010:
                respCode = ISOResponseCodes.HOST_NOT_PROCESSING;
                break;
            case 4011:
                respCode = ISOResponseCodes.HOST_NOT_PROCESSING;
                break;
            case 4012:
                respCode = ISOResponseCodes.HOST_NOT_PROCESSING;
                break;
            case 4013:
                respCode = ISOResponseCodes.INVALID_IMD;
                break;
            case 4014:
                respCode = ISOResponseCodes.INVALID_ACCOUNT_STATUS;
                break;
            case 4015:
                respCode = ISOResponseCodes.INVALID_ACCOUNT_STATUS;
                break;
            case 4016:
                respCode = ISOResponseCodes.INVALID_IMD;
                break;
            case 4017:
                respCode = ISOResponseCodes.INVALID_IMD;
                break;
            case 4018:
                respCode = ISOResponseCodes.INVALID_IMD;
                break;
            case 4019:
                respCode = ISOResponseCodes.INVALID_ACCOUNT_STATUS;
                break;
            case 4020:
                respCode = ISOResponseCodes.NO_TRANSACTION_ON_IMD;
                break;
            case 4021:
                respCode = ISOResponseCodes.INVALID_TO_ACCOUNT;
                break;
            default:
                break;
        }

        return respCode;
    }

    public static String mapMsgReasonCode (Integer respCode) {
        String rejectCode = null;

        switch (respCode) {
            case 30:
                rejectCode = "4003";
                break;
            case 31:
                rejectCode = "4006";
                break;
            case 32:
                rejectCode = "4007";
                break;
            case 33:
                rejectCode = "4013";
                break;
            case 35:
                rejectCode = "4014";
                break;
            case 36:
                rejectCode = "4017";
                break;
            case 37:
                rejectCode = "4021";
                break;
            default:
                rejectCode = "4020";
                break;
        }

        return rejectCode;
    }
}
