package vaulsys.protocols.PaymentSchemes.base;

import vaulsys.util.Util;

/**
 * Created by m.rehman on 4/18/2016.
 */
public class ISOResponseCodes {
    public static final String APPROVED = "00";
    public static final String LIMIT_EXCEEDED = "01";
    public static final String INVALID_ACCOUNT = "02";
    public static final String ACCOUNT_INACTIVE = "03";
    public static final String LOW_BALANCE = "04";
    public static final String INVALID_CARD = "05";
    public static final String INVALID_IMD = "06";
    public static final String INVALID_CARD_DATA = "07";
    public static final String INVALID_CARD_RECORD = "08";
    public static final String FIELD_ERROR = "09"; //used as FIELD_ERROR
    public static final String DUPLICATE_TRANSACTION = "10";
    public static final String BAD_TRANSACTION_CODE = "11";
    public static final String INVALID_CARD_STATUS = "12";
    public static final String INTERNAL_DATABASE_ERROR = "13";
    public static final String WARM_CARD = "14";
    public static final String HOT_CARD = "15";
    public static final String BAD_CARD_STATUS = "16";
    public static final String UNKNOWN_AUTH_MODE = "17";
    public static final String INVALID_TRANSACTION_DATE = "18";
    public static final String INVALID_CURRENCY_CODE = "19";
    public static final String NO_TRANSACTION_ON_IMD = "20";
    public static final String NO_TRANSACTION_ON_ACCOUNT = "21";
    public static final String BAD_CARD_CYCLE_DATE = "22";
    public static final String BAD_CARD_CYCLE_LENGTH = "23";
    public static final String BAD_PIN = "24";
    public static final String CARD_EXPIRED = "25"; //also used as CARD_EXPIRED
    public static final String ACCESS_TOKEN_EXPIRED_MISSING = "26";
    public static final String DUPLICATE_CUSTOMER = "27";
    public static final String NO_ACCOUNTS_LINKED = "28";
    public static final String INTERNAL_ERROR_29 = "29";
    public static final String ORIGINAL_TRANSACTION_NOT_FOUND = "30";
    public static final String WALLET_IN_PROVISIONAL_STATE = "31"; //also used as WALLET_IN_PROVISIONAL_STATE ; also as Account-In-Provisional State
    public static final String CUSTOMER_INACTIVE = "32";
    public static final String DUPLICATE_LINKED_ACCOUNT = "33";
    public static final String ORIGINAL_NOT_AUTHORIZED = "34";
    public static final String ORIGINAL_ALREADY_REVERSED = "35";
    public static final String ACQUIRER_REVERSAL = "36";
    public static final String INVALID_REPLACEMENT_AMOUNT = "37";
    public static final String TRANSACTION_CODE_MISMATCH = "38";
    public static final String BAD_TRANSACTION_TYPE = "39"; //also used as BAD_TRANSACTION_TYPE
    public static final String INTERNAL_ERROR_40 = "40";
    public static final String EXPIRY_DATE_MISMATCH = "41";
    public static final String ACQUIRER_ADJUSTMENT = "42";
    public static final String ACQUIRER_NACK = "43";
    public static final String ORIGINAL_ALREADY_NACKED = "44";
    public static final String T2_DATA_MISMATCH = "45";
    public static final String UNABLE_TO_PROCESS = "46";
    public static final String ERR_CURRENCY_CONVERSION = "47";
    public static final String BAD_AMOUNT = "48";
    public static final String INTERNAL_ERROR_49 = "49";
    public static final String HOST_STATUS_UNKNOWN = "50";
    public static final String HOST_NOT_PROCESSING = "51";
    public static final String HOST_IN_STANDIN_MODE = "52";
    public static final String HOST_IN_BAL_DOWNLD_MODE = "53";
    public static final String SAF_TRANSMIT_MODE = "54";
    public static final String HOST_LINK_DOWN = "55";
    public static final String SENT_TO_HOST = "56";
    public static final String BANK_LINK_DOWN = "57"; //also used as BANK_LINK_DOWN
    public static final String TRANSACTION_TIMEOUT = "58"; //also used as TRANSACTION_TIMEOUT
    public static final String HOST_REJECT = "59";
    public static final String PIN_RETRIES_EXHAUSTED = "60";
    public static final String TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE = "61"; //also used as TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE
    public static final String TRANSACTION_REJECTED_PERFORM_WITH_CARDHOLDER_AUTHENTICATION = "62";
    public static final String DESTINATION_NOT_FOUND = "63";
    public static final String DESTINATION_NOT_REGISTERED = "64";
    public static final String CASH_TRANSACTION_NOT_ALLOWED = "65"; //CASH_TRANSACTION_NOT_ALLOWED
    public static final String NO_TRANSACTION_ALLOWED = "66"; //also used as NO_TRANSACTION_ALLOWED
    public static final String INVALID_ACCOUNT_STATUS = "67";
    public static final String INVALID_TO_ACCOUNT = "68";
    public static final String BAD_PIN_COMPARE = "69";
    public static final String REFUSED_IMD = "70";
    public static final String NO_PROFILE_AVAILABLE = "71"; //Raza NayaPay
    public static final String CURRENCY_NOT_ALLOWED = "72";
    public static final String CHECK_DIGIT_FAILED = "73";
    public static final String TRANSACTION_SOURCE_NOT_ALLOWED = "74";
    public static final String UNKNOWN_TRANSACTION_SOURCE = "75";
    public static final String MANUAL_ENTRY_NOT_ALLOWED = "76";
    public static final String REFER_TO_ISSUER = "77";
    public static final String INVALID_MERCHANT = "78";
    public static final String HONOUR_WITH_ID = "79";
    public static final String MESSAGE_FORMAT_ERROR = "80";
    public static final String SECURITY_VIOLATION = "81";
    public static final String MAIL_ORDER_NOT_ALLOWED = "82";
    public static final String NO_COMMS_KEY = "83";
    public static final String NO_PIN_KEY = "84";
    public static final String NO_DEC_TAB = "85";
    public static final String INCORRECT_PIN_LENGTH = "86";
    public static final String CASH_RETRACT = "87";
    public static final String FAULTY_DISPENSE = "88";
    public static final String SHORT_DISPENSE = "89";
    public static final String CUSTOMER_NOT_FOUND = "90";
    public static final String ISSUER_REVERSAL = "91";
    public static final String ACCOUNT_LOCKED = "92";
    public static final String CUSTOMER_RELATION_NOT_FOUND = "93";
    public static final String PERMISSION_DENIED = "94";
    public static final String TRANSACTION_REJECTED = "95";
    public static final String ORIGINAL_ALREADY_REJECTED = "96";
    public static final String BAD_EXPIRY_DATE = "97";
    public static final String ORIGINAL_AMOUNT_INCORRECT = "98";
    public static final String ORIGINAL_DATA_ELEMENT_MISMATCH = "99";
    public static final String INVALID_COMPANY_CODE = "104";  //for third_party_Payment

    public static final String MANDATORY_CHANGE_PIN = "101";	//Mirkamali(Task174)
    public static final String REFRENCE_NUMBER_IS_EXPIRE = "102"; // onlineBillPayment
    public static final String INCORRECT_ONLINE_REFNUMBER = "103"; //onlineBillPayment
    public static final String HOTCARD_NOT_APPROVED = "105";
    public static final String RESTRICTED_MIN_WITHDRAWAL_AMOUNT = "161";

    //Raza Adding for NayaPay start
    public static final String ERROR_MOBILENUM = "E01";
    public static final String ERROR_CNIC = "E02";
    public static final String ERROR_CNIC_PIC = "E03";
    public static final String ERROR_CUST_PIC = "E04";
    public static final String ERROR_MOTHERNAME = "E05";
    public static final String ERROR_FATHERNAME = "E06";
    public static final String ERROR_DATEOFBIRTH = "E07";
    public static final String ERROR_NAYAPAYID = "E08";
    public static final String ERROR_ACCOUNTNUM = "E09";
    public static final String ERROR_ORIGPINDATA = "E10";
    public static final String ERROR_NEWPINDATA = "E11";
    public static final String ERROR_BANKCODE = "E12";
    public static final String ERROR_BANKNAME = "E13";
    public static final String ERROR_OTP = "E14";
    public static final String ERROR_WALLETCURRENCY = "E15";
    public static final String ERROR_PINDATA = "E16";
    public static final String ERROR_BIOMETRICDATA = "E17";
    public static final String ERROR_BANKFORMDATA = "E18";
    public static final String ERROR_AMOUNT = "E19";
    public static final String ERROR_WALLETACCOUNT = "E20";
    public static final String ERROR_ACCOUNTCURRENCY = "E21";
    public static final String ERROR_SRCCHARGES = "E22";
    public static final String ERROR_DESTCHARGES = "E23";
    public static final String ERROR_DESTWALLETACCOUNT = "E24";
    public static final String ERROR_DESTWALLETCURRENCY = "E25";
    public static final String ERROR_GENERALERROR = "E26";
    public static final String ERROR_TXNREFNUM = "E27";
    public static final String ERROR_TRANSDATETIME = "E28";
    public static final String ERROR_DUPLICATECUSTOMER = "E29";
    public static final String ERROR_INVALIDCNIC = "E30";
    public static final String ERROR_INVALIDMOBILENUMBER = "E31";
    public static final String ERROR_UNKOWN = "E32";
    public static final String ERROR_CNICEXPIRY = "E33";
    public static final String ERROR_PROVINCE = "E34";
    public static final String ERROR_PLACEOFBIRTH = "E35";
    public static final String ERROR_TELECOMPROVIDER = "E36";
    public static final String ERROR_DESTBANKCODE = "E37";
    public static final String ERROR_DESTACCOUNT = "E38";
    public static final String ERROR_DESTACCOUNTCURRENCY = "E39";
    public static final String ERROR_DESTCNIC = "E40";
    public static final String ERROR_CUSTOMERNAME = "E41";
    public static final String ERROR_INVALIDNAYAPAYID = "E42";
    public static final String ERROR_INVALIDBANKCODE = "E43";
    public static final String ERROR_ORIGDATAELEMENT = "E44";
    public static final String ERROR_ATMID = "E45";
    public static final String ERROR_RESPCODE = "E46";
    public static final String ERROR_COREBANKCODE = "E47";
    public static final String ERROR_COREACCOUNT = "E48";
    public static final String ERROR_CORECURRENCY = "E49";
    public static final String ERROR_CARDNUM = "E50";
    public static final String ERROR_CARDPIN = "E51";
    public static final String ERROR_ENCRYPTDATA = "E52";
    public static final String ERROR_ENABLEFLAG = "E53";
    public static final String ERROR_INVALIDCOREACCOUNT = "E54";
    public static final String ERROR_INVALIDDESTNAYAPAYID = "E55";
    public static final String ERROR_INVALIDDESTBANKCODE = "E56";
    //Raza Adding for NayaPay Askari start
    public static final String ERROR_TOTALAMOUNT = "E57";
    public static final String ERROR_BANKCHARGES = "E58";
    public static final String ERROR_BANKTAXAMOUNT = "E59";
    public static final String ERROR_NAYAPAYCHARGES = "E60";
    public static final String ERROR_NAYAPAYTAXAMOUNT = "E61";
    public static final String ERROR_DEPOSITAMOUNT = "E62";
    public static final String ERROR_CREDITTXNREFNUM = "E63";
    public static final String ERROR_CREDITTRNDATETIME = "E64";
    public static final String ERROR_BRANCHCODE = "E65";
    public static final String ERROR_BRANCHNAME = "E66";
    public static final String ERROR_DEPOSITSLIP = "E67";
    public static final String ERROR_TELLERID = "E68";
    public static final String ERROR_CHEQUEISS_BANKCODE = "E69";
    public static final String ERROR_CHEQUEISS_BANKNAME = "E70";
    public static final String ERROR_CHEQUEISS_BRANCHNAME = "E71";
    public static final String ERROR_INSTRUMENTNUMBER = "E72";
    public static final String ERROR_CHEQUEPIC = "E73";
    public static final String ERROR_CHEQUEBOUNCECODE = "E74";
    public static final String ERROR_CHEQUEBOUNCEDESC = "E75";
    public static final String ERROR_ETACHEQUECLEARING = "E76";
    public static final String ERROR_CHEQUETRANSDATETIME = "E77";
    public static final String ERROR_PRINCIPALAMOUNT = "E78";
    public static final String ERROR_TRANCURRENCY = "E79";
    //Raza Adding for NayaPay Askari end
    public static final String ERROR_MERCHANTID = "E80";
    public static final String ERROR_MERCHANTCATLIST = "E81";
    public static final String ERROR_MERCHANTLIST = "E82";
    public static final String ERROR_DAILYLIMIT = "E83";
    public static final String ERROR_MONTHLYLIMIT = "E84";
    public static final String ERROR_YEARLYLIMIT = "E85";
    public static final String ERROR_MERCHANTNOFOUND = "E86";
    public static final String ERROR_MERCHANTLISTNOFOUND = "E87";
    public static final String ERROR_MERCHANTCATLISTNOFOUND = "E88";
    public static final String ERROR_FRAUDPROFILENOTFOUND = "E89";
    public static final String ERROR_CHANNELID = "E90";
    public static final String ERROR_ALLOWEDFLAG = "E91";
    public static final String ERROR_DELETETYPE = "E92";
    public static final String ERROR_COMMENTS = "E93";
    public static final String ERROR_USERID = "E94";
    public static final String ERROR_STAN = "E95";
    public static final String ERROR_RRN = "E96";
    public static final String ERROR_ACCTID = "E97";
    public static final String ERROR_ACCTALIAS = "E98";
    public static final String ERROR_PRIMARY = "E99";
    public static final String ERROR_ACCTBALANCE = "E100";
    public static final String ERROR_ACCTLIMIT = "E101";
    public static final String ERROR_DESTUSERID = "E102";
    public static final String ERROR_ADDRESS = "E103";
    public static final String ERROR_CITY = "E104";
    public static final String ERROR_COUNTRY = "E105";
    public static final String ERROR_ADVFLAG = "E106";
    public static final String ERROR_SECNUM = "E107";
    public static final String ERROR_AVAILLIMIT = "E108";
    public static final String ERROR_AVAILFREQ = "E109";
    public static final String ERROR_STATE = "E110";
    public static final String ERROR_REQTIME = "E111";
    public static final String ERROR_ACTTIME = "E112";
    public static final String ERROR_STATUS = "E113";
    public static final String ERROR_NPLIMIT = "E114";
    public static final String ERROR_LNKACCOUNTS = "E115";
    public static final String ERROR_PROVWALLET = "E116";
    public static final String ERROR_CURRENCY = "E117";
    public static final String ERROR_TRANSACTIONS = "E118";
    public static final String ERROR_ACCTLIST = "E119";
    public static final String ERROR_USERTOKEN = "E120";
    public static final String ERROR_INOUTFILTER = "E121";
    public static final String ERROR_TYPEFILTER = "E122";
    public static final String ERROR_SEARCHTEXT = "E123";
    public static final String ERROR_USERNAME = "E124";
    public static final String ERROR_GPSLATITUDE = "E125";
    public static final String ERROR_GPSLONGITUDE = "E126";
    public static final String ERROR_PARENTID = "E127";
    public static final String ERROR_MERCHANTNAME = "E128";
    public static final String ERROR_MERCHANTCATID = "E129";
    public static final String ERROR_TRUSTEDFLAG = "E130";
    public static final String ERROR_PHONENUMBER = "E131";
    public static final String ERROR_TXNLIMIT = "E132";
    public static final String ERROR_MERCHANTCATNAME = "E133";
    public static final String ERROR_MERCHANTSTATE = "E134";
    public static final String ERROR_MERCHANTENABLED = "E135";
    public static final String ERROR_MERCHANTBLOCKED = "E136";
    public static final String ERROR_MINAMOUNT = "E137";
    public static final String ERROR_MAXAMOUNT = "E138";
    public static final String ERROR_SRCCHARGETYPE = "E139";
    public static final String ERROR_DESTCHARGETYPE = "E140";
    public static final String ERROR_CONSUMERNUM = "E141";
    public static final String ERROR_UTILCOMPID = "E142";
    public static final String ERROR_CONSUMERDETAIL = "E143";
    public static final String ERROR_BILLSTATUS = "E144";
    public static final String ERROR_DUEDATE = "E145";
    public static final String ERROR_AMTWITHINDUEDATE = "E146";
    public static final String ERROR_AMTAFTERDUEDATE = "E147";
    public static final String ERROR_BILLMONTH = "E148";
    public static final String ERROR_DATEPAID = "E149";
    public static final String ERROR_AMTPAID = "E150";
    public static final String ERROR_TRNAUTHID = "E151";
    public static final String ERROR_RESERVED = "E152";
    public static final String ERROR_IDENTNO = "E153";
    public static final String ERROR_PING = "E154";
    public static final String ERROR_NAYAPAYTRNTYPE = "E155";
    public static final String ERROR_DESTNAYAPAYID = "E156";
    public static final String ERROR_DESTUSERNAME = "E157";
    public static final String ERROR_AGENTID = "E158";
    public static final String ERROR_REFNO = "E159";
    public static final String ERROR_INVOICEID = "E160";
    public static final String ERROR_VERIFIEDFLAG = "E161";
    public static final String ERROR_STARTDATE = "E162";
    public static final String ERROR_ENDDATE = "E163";
    public static final String ERROR_BANKTXNFLAG = "E164";
    public static final String ERROR_BLOCKEDFLAG = "E165";
    public static final String ERROR_NAYAPAYTXNID = "E166";
    public static final String ERROR_USERTXNS = "E167";
    public static final String ERROR_CREATEDATE = "E168";
    public static final String ERROR_MERCHANTRATES = "E169";
    public static final String ERROR_MERCHANTCATEGORY = "E170";
    public static final String ERROR_MERCHANTCATEGORYRATES = "E171";
    public static final String ERROR_MERCHANTTRANSACTIONLIST = "E172";
    public static final String ERROR_MERCHANTTRANSACTIONNOTFOUND = "E173";
    public static final String ERROR_TXNDETAIL = "E174";
    public static final String ERROR_LIMIT_EXHAUSTED = "E175";
    public static final String ERROR_ACQBIN = "E176";
    public static final String ERROR_SLIPNUMBER = "E177";
    public static final String ERROR_ENCRYPTKEY = "E200";

    //Raza Adding for NayaPay end

    //m.rehman: adding new nayapay error code from document 4.4
    public static final String NP_SRC_WALLET_NOT_BIO_VERIFIED = "NP_2001";
    public static final String NP_DEST_WALLET_NOT_BIO_VERIFIED = "NP_2002";
    public static final String NP_SRC_WALLET_TRAN_NOT_ALLOWED = "NP_2021";
    public static final String NP_DEST_WALLET_TRAN_NOT_ALLOWED = "NP_2022";
    public static final String NP_BAD_CARD_PIN = "NP_3024";
    public static final String NP_NO_LINKED_ACCOUNT = "NP_4001";
    public static final String NP_CATEGORY_NOT_FOUND = "NP_5001";
    public static final String NP_PRI_DATA_ELEM_NOT_FOUND = "NP_6001";
    public static final String NP_SEC_DATA_ELEM_NOT_FOUND = "NP_6002";
    public static final String NP_PERMISSION_DENIED = "NP_6003";
    public static final String NP_AUTHENTICATION_FAILED = "NP_6004";
    public static final String NP_DATA_NOT_FOUND = "NP_6005";
    public static final String NP_ALREADY_EXIST = "NP_6006";
    public static final String NP_SRC_NOT_FOUND = "NP_6007";
    public static final String NP_DEST_NOT_FOUND = "NP_6008";
    public static final String NP_SRC_BLOCKED = "NP_6009";
    public static final String NP_DEST_BLOCKED = "NP_6010";
    public static final String NP_SRC_INVALID_STATE = "NP_6011";
    public static final String NP_DEST_INVALID_STATE = "NP_6012";
    public static final String NP_SRC_LOCKED = "NP_6013";
    public static final String NP_DEST_LOCKED = "NP_6014";
    public static final String NP_SRC_LIMIT_EXCEEDED = "NP_6015";
    public static final String NP_DEST_LIMIT_EXCEEDED = "NP_6016";
    public static final String NP_SRC_INSUFFICEIENT_BALANCE = "NP_6017";
    public static final String NP_INVALID_CURR_CODE = "NP_6019";
    public static final String NP_VERIFICATION_FAILED = "NP_6020";
    public static final String NP_BAD_PIN = "NP_6024";
    public static final String NP_ACCESS_TOKEN_EXPIRED_MISSING = "NP_6026";
    public static final String NP_INVALID_OPERATION = "NP_6031";
    public static final String NP_BAD_TRANSACTION_TYPE = "NP_6039";
    public static final String NP_PIN_RETRIES_EXHUASTED = "NP_6060";
    public static final String NP_TRAN_SRC_NOT_FOUND = "NP_6074";
    public static final String NP_TRAN_DEST_NOT_FOUND = "NP_6075";
    public static final String NP_BLOCKED_MERCHANT = "NP_6079";
    public static final String NP_INVALID_MERCHANT_STATE = "NP_6080";


    public static boolean isSuccess(String rscode) {
        return APPROVED.equals(rscode);
    }

    public static boolean isAlreadyReversed(String rscode) {
        return rscode.equals(INVALID_ACCOUNT);
    }

    public static boolean isReversalMessageDone(String rsCode) {
        if (!Util.hasText(rsCode))
            return true;
        return rsCode.equals(APPROVED) ||
                rsCode.equals(INVALID_ACCOUNT) ||
                rsCode.equals(CARD_EXPIRED) ||
                rsCode.equals(ORIGINAL_NOT_AUTHORIZED) ||

                /******* Shetab V5, Modified:2011/10/29  ******/
                rsCode.equals(FIELD_ERROR)
                ;
    }

    /******** created:2011/11/05 ********/
    public static boolean isMessageDone(String rsCode) {
        if (!Util.hasText(rsCode))
            return false;

        return !rsCode.equals(MESSAGE_FORMAT_ERROR) &&
                !rsCode.equals(INVALID_TO_ACCOUNT) &&

                /******* Shetab V5, Modified:2011/10/29  ******/
//        !rsCode.equals(NO_PIN_KEY) &&
                !rsCode.equals(CUSTOMER_RELATION_NOT_FOUND)
                ;
    }

    public static boolean cannotBeDone(String rsCode) {
        if (!Util.hasText(rsCode))
            return false;

        return !rsCode.equals(MESSAGE_FORMAT_ERROR) &&
                !rsCode.equals(INVALID_TO_ACCOUNT) &&

                /******* Shetab V5, Modified:2011/10/29  ******/
//                !rsCode.equals(NO_PIN_KEY) &&
                !rsCode.equals(CUSTOMER_RELATION_NOT_FOUND) &&

                !isReversalMessageDone(rsCode);
    }

    public static boolean shouldBeRepeated(String rsCode) {
        return rsCode.equals(MESSAGE_FORMAT_ERROR)
                || rsCode.equals(NO_PIN_KEY)
                || rsCode.equals(INVALID_TO_ACCOUNT)
                || rsCode.equals(FIELD_ERROR)

                /******* Shetab V5, Modified:2011/10/29  ******/
                || rsCode.equals(CUSTOMER_NOT_FOUND)
                || rsCode.equals(CUSTOMER_RELATION_NOT_FOUND)
                ;
    }

    public static boolean shouldNotBeReversedForTransfer(String rsCode) {
        return rsCode.equals(MESSAGE_FORMAT_ERROR)
                || rsCode.equals(NO_PIN_KEY)
                || rsCode.equals(INVALID_TO_ACCOUNT)
                || rsCode.equals(FIELD_ERROR)
                || rsCode.equals(ORIGINAL_ALREADY_REJECTED)
                ;
    }

    public static boolean shouldBeCaptured(String rsCode) {
        return rsCode.equals(ACQUIRER_NACK)
                || rsCode.equals(EXPIRY_DATE_MISMATCH)
                || rsCode.equals(SENT_TO_HOST)
                ;
    }

    public static boolean shouldBeRemovedFromSecurityMap(String rsCode) {
        return rsCode.equals(APPROVED)
                || rsCode.equals(INVALID_CARD_STATUS) //12
                || rsCode.equals(INVALID_CARD_STATUS) //12
                || rsCode.equals(WARM_CARD) //14
                || rsCode.equals(ORIGINAL_TRANSACTION_NOT_FOUND) //30
                || rsCode.equals(TRANSACTION_CODE_MISMATCH)//38
                || rsCode.equals(HOST_NOT_PROCESSING) //51
                || rsCode.equals(SENT_TO_HOST)//56
                || rsCode.equals(BANK_LINK_DOWN)//57
                || rsCode.equals(TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE)//61
                || rsCode.equals(NO_TRANSACTION_ALLOWED) //66
                || rsCode.equals(UNKNOWN_TRANSACTION_SOURCE)//75
                || rsCode.equals(MESSAGE_FORMAT_ERROR) //80
                || rsCode.equals(NO_PIN_KEY) //84
                || rsCode.equals(INVALID_TO_ACCOUNT) //91
                || rsCode.equals(ACCOUNT_LOCKED)//92

                ;
    }

    public static boolean isReplaceRsCode(String oldRsCode, String newRsCode) {
        if (!Util.hasText(oldRsCode))
            return true;

        if (ISOResponseCodes.APPROVED.equals(oldRsCode) || ISOResponseCodes.INVALID_ACCOUNT.equals(oldRsCode))
            return false;

        return true;
    }

    //Mirkamali(Task150)
    public static boolean shouldChangeFlagForTransferTo(String rsCode) {
        return rsCode.equals(HOST_NOT_PROCESSING) || rsCode.equals(MESSAGE_FORMAT_ERROR);
    }
    //Raza adding from ErrorCodes file temporary, will merge it later end


}