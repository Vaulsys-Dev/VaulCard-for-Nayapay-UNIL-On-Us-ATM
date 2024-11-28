package vaulsys.protocols.PaymentSchemes.base;

import vaulsys.util.StringFormat;

public class ISOTransactionCodes {
    public static final String PURCHASE = "00";
    public static final String WITHDRAWAL = "01";
    public static final String ADJUSTMENT = "02";
    public static final String PREAUTH = "03";
    public static final String DIRECT_DEBIT = "18";
    public static final String REFUND = "20";
    public static final String DEPOSIT = "21";
    public static final String ORIGINAL_CREDIT = "26";
    public static final String MONEY_SEND = "28";
    public static final String BALANCE_INQUIRY_30 = "30";
    public static final String BALANCE_INQUIRY_31 = "31";
    public static final String ACCOUNTS_TRANSFER = "40";
    public static final String IBFT = "48";
    public static final String TITLE_FETCH = "62";
    public static final String PIN_CHANGE = "70";
    public static final String PIN_UNBLOCK = "72";
    //Raza Adding start
    public static final String PURCHASECHARGE = "19";
    public static final String SDAERAT_AUTH_BILL = "19";
    public static final String LASTPURCHASECHARGE = "39";
    public static final String RETURN = "20";
    public static final String BALANCE_INQUERY_31 = "31";
    public static final String BALANCE_INQUERY = "30";
    public static final String BILL_PAYMENT_87 = "17";
    public static final String BILL_PAYMENT_NEGIN_87 = "50";
    public static final String BILL_PAYMENT_93 = "50";

    public static final String CHECK_ACCOUNT = "33";
    public static final String GET_ACCOUNT = "38";
    public static final String TRANSFER = "40";
    public static final String TRANSFER_FROM_ACCOUNT = "46";
    public static final String TRANSFER_TO_ACCOUNT = "47";
    public static final String GET_STATEMENT = "37";
    public static final String FULL_STATEMENT = "32";

    public static final String CHANGE_PIN2 = "87";
    public static final String CHANGE_PIN = "97";
    public static final String DEPOSITE = "21";
    public static final String DEPOSIT_CHECK_ACCOUNT = "35";
    public static final String HOTCARD_INQ = "98";
    /*** INGENICO specific ***/
    public static final String LOG_ON = "90";
    public static final String RESET_PASSWORD = "91";
    public static final String MERCHANT_BALANCE = "92";
    public static final String BATCH_UPLOAD = "93";

    public static final String PURCHASETOPUP = "94";

    public static final String CHECK_ACCOUNT_CARD_TO_ACCOUNT = "34";
    public static final String TRANSFER_CARD_TO_ACCOUNT = "41";
    public static final String THIRDPARTY_PAYMENT = "18";

//    public static final int CUTOVER = 201;
//    public static final int ECHOTEST = 301;
//    public static final int PIN2Change = 166;

    /*public static String getString(int t, int len) {
//        StringFormat formatRow = new StringFormat(len, StringFormat.JUST_RIGHT);
        return StringFormat.formatNew(len, StringFormat.JUST_RIGHT, Integer.toString(t), '0');
    }*/
    //Raza Adding end

    public static Boolean isFinancialTransaction (String transactionCode) {
        Boolean result;
        if (transactionCode.equals(PURCHASE) || transactionCode.equals(WITHDRAWAL) ||
                transactionCode.equals(ADJUSTMENT) || transactionCode.equals(PREAUTH) ||
                transactionCode.equals(REFUND) || transactionCode.equals(DEPOSIT) ||
                transactionCode.equals(ORIGINAL_CREDIT) || transactionCode.equals(ACCOUNTS_TRANSFER) ||
                transactionCode.equals(DIRECT_DEBIT) || transactionCode.equals(MONEY_SEND) ||
                transactionCode.equals(IBFT))
            result = Boolean.TRUE;
        else
            result  = Boolean.FALSE;

        return result;
    }
}
