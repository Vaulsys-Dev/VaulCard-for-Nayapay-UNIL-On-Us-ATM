package vaulsys.protocols.epay.base;

import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;

public class EpayConstants {
    public static final int FINANCIAL_MSG = 1;
    public static final int REVERSAL_MSG = 2;

    public static final int BILL_PAY_MSG = 1;
    public static final int PURCHASE_CHARGE_MSG = 2;
    public static final int PURCHASE_MSG = 3;
    public static final int RETRUN_MSG = 4;
    public static final int TRANSFER_MSG = 5;
    public static final int AUTHORIZATION_MSG = 6;
    public static final int BALANCE_INQ_MSG = 7;
    public static final int PIN_CHANGE_MSG = 9;
    public static final int STATEMENT_MSG = 10;
    public static final int PURCHASE_TOPUP_MSG = 11;
    public static final int AUTHORIZATION_CARD_TO_ACCOUNT_MSG = 12;
    public static final int TRANSFER_CARD_TO_ACCOUNT_MSG = 13;
    public static final int ONLINE_BILLPAYMENT_MSG = 14;
    public static final int PREPARE_ONLINE_BILLPAYMENT_MSG = 15;
    public static final int ONLINE_BILLPAYMENT_TRACKING_MSG = 16;
    public static final int THIRD_PARTY_PAYMENT_MSG = 17;
    //TASK Task051 : Epay GetAccounts Feature
    public static final int GETACCOUNT_MSG = 18; //1018
    // TASK Task129 [26604] - Authenticate Cart (Pasargad)
    public static final int CARD_AUTHENTICATE_MSG = 19; //1019


    public static final Byte PIN1 = 1;
    public static final Byte PIN2 = 2;

    public static final String FA_LANG = "Fa";
    public static final String EN_LANG = "En";

    public static final int BAL_TYPE_UNKNOWN = 0;
    public static final int BAL_TYPE_LEDGER = 1;
    public static final int BAL_TYPE_AVAILABLE = 2;

    public static final int ACC_TYPE_UNKOWN = 0;
    public static final int ACC_TYPE_MAIN = 1;
    public static final int ACC_TYPE_SUBSIDIARY = 2;

    public static final int ACC_KIND_UNKNOWN = 0;
    public static final int ACC_KIND_CURRENT = 1;
    public static final int ACC_KIND_SAVING = 2;



    public static int getAccountKind(AccType accType){
        if(accType.equals(AccType.CURRENT))
            return ACC_KIND_CURRENT;

        if(accType.equals(AccType.SAVING))
            return ACC_KIND_SAVING;

        return ACC_KIND_UNKNOWN;
    }

    public static int getBalType(BalType balType){
        if(balType.equals(BalType.AVAIL))
            return BAL_TYPE_AVAILABLE;

        if(balType.equals(BalType.LEDGER))
            return BAL_TYPE_LEDGER;

        return BAL_TYPE_UNKNOWN;
    }

    public static Integer getCommandIdByIfxType(IfxType inIfx , TrnType inTrn){
        Integer commandId = null;

        if (IfxType.BILL_PMT_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + BILL_PAY_MSG);

        else if(IfxType.BILL_PMT_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + BILL_PAY_MSG);

        else if (IfxType.PURCHASE_CHARGE_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + PURCHASE_CHARGE_MSG);

        else if(IfxType.PURCHASE_CHARGE_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + PURCHASE_CHARGE_MSG);

        else if (IfxType.PURCHASE_TOPUP_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + PURCHASE_TOPUP_MSG);

        else if(IfxType.PURCHASE_TOPUP_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + PURCHASE_TOPUP_MSG);

        else if (IfxType.PURCHASE_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + PURCHASE_MSG);

        else if(IfxType.PURCHASE_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + PURCHASE_MSG);

        else if (IfxType.RETURN_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + RETRUN_MSG);

        else if(IfxType.RETURN_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + RETRUN_MSG);

        else if (IfxType.CHANGE_PIN_BLOCK_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + PIN_CHANGE_MSG);

        else if(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + PIN_CHANGE_MSG);

        else if (IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + AUTHORIZATION_MSG);

        else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + AUTHORIZATION_CARD_TO_ACCOUNT_MSG);

        else if (IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + AUTHORIZATION_MSG);

        else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + AUTHORIZATION_CARD_TO_ACCOUNT_MSG);

        else if (IfxType.TRANSFER_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + TRANSFER_MSG);

        else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + TRANSFER_CARD_TO_ACCOUNT_MSG);

        else if(IfxType.TRANSFER_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + TRANSFER_MSG);

        else if (IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + TRANSFER_CARD_TO_ACCOUNT_MSG);

        else if (IfxType.BAL_INQ_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + BALANCE_INQ_MSG);

        else if(IfxType.BAL_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + BALANCE_INQ_MSG);

        else if(IfxType.BANK_STATEMENT_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + STATEMENT_MSG);

        else if(IfxType.BANK_STATEMENT_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer (REVERSAL_MSG * 1000 + STATEMENT_MSG);

        else if(IfxType.ONLINE_BILLPAYMENT_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000+ONLINE_BILLPAYMENT_MSG);

        else if(IfxType.ONLINE_BILLPAYMENT_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000+ONLINE_BILLPAYMENT_MSG);

        else if(IfxType.PREPARE_ONLINE_BILLPAYMENT.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000+PREPARE_ONLINE_BILLPAYMENT_MSG);

        else if(IfxType.PREPARE_ONLINE_BILLPAYMENT_REV_REPEAT.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + PREPARE_ONLINE_BILLPAYMENT_MSG);

        else if(IfxType.ONLINE_BILLPAYMENT_TRACKING.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + ONLINE_BILLPAYMENT_TRACKING_MSG);

        else if(IfxType.ONLINE_BILLPAYMENT_TRACKING_REV_REPEAT.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + ONLINE_BILLPAYMENT_TRACKING_MSG);

        else if (IfxType.THIRD_PARTY_PURCHASE_RQ.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + THIRD_PARTY_PAYMENT_MSG);

        else if (IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + THIRD_PARTY_PAYMENT_MSG);

        else if(IfxType.THIRD_PARTY_PURCHASE_RS.equals(inIfx))
            commandId = new  Integer(FINANCIAL_MSG * 1000 + THIRD_PARTY_PAYMENT_MSG);

        else if (IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + THIRD_PARTY_PAYMENT_MSG);

            //TASK Task051 : Epay GetAccounts Feature
        else if (IfxType.GET_ACCOUNT_RS.equals(inIfx))
            commandId = new Integer(FINANCIAL_MSG * 1000 + GETACCOUNT_MSG);

            //TASK Task051 : Epay GetAccounts Feature
        else if (IfxType.GET_ACCOUNT_REV_REPEAT_RS.equals(inIfx))
            commandId = new Integer(REVERSAL_MSG * 1000 + GETACCOUNT_MSG);
        
        // TASK Task129 [26604] - Authenticate Cart (Pasargad)
        else if (IfxType.CARD_AUTHENTICATE_RS.equals(inIfx)) 
            commandId = new Integer(FINANCIAL_MSG * 1000 + CARD_AUTHENTICATE_MSG);
        // TASK Task129 [26604] - Authenticate Cart (Pasargad)
        else if (IfxType.CARD_AUTHENTICATE_REV_REPEAT_RS.equals(inIfx)) 
            commandId = new Integer(REVERSAL_MSG * 1000 + CARD_AUTHENTICATE_MSG);

        return commandId;
    }

    public static IfxType getIfxTypeByCommandID(Integer commandID){
        IfxType ifxType = IfxType.UNDEFINED;

        if (commandID == 1001)
            ifxType = IfxType.BILL_PMT_RQ;
        else if(commandID == 2001)
            ifxType = IfxType.BILL_PMT_REV_REPEAT_RQ;
        else if(commandID == 1002)
            ifxType = IfxType.PURCHASE_CHARGE_RQ;
        else if(commandID == 2002)
            ifxType = IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ;
        else if(commandID == 1003)
            ifxType = IfxType.PURCHASE_RQ;
        else if(commandID == 2003)
            ifxType = IfxType.PURCHASE_REV_REPEAT_RQ;
        else if(commandID == 1004)
            ifxType = IfxType.RETURN_RQ;
        else if(commandID == 2004)
            ifxType = IfxType.RETURN_REV_REPEAT_RQ;
        else if(commandID == 1009)
            ifxType = IfxType.CHANGE_PIN_BLOCK_RQ;
        else if(commandID == 2009)
            ifxType = IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ;
        else if(commandID.equals(new Integer(FINANCIAL_MSG * 1000 + BALANCE_INQ_MSG)))
            ifxType = IfxType.BAL_INQ_RQ;
        else if(commandID.equals(new Integer(REVERSAL_MSG * 1000 + BALANCE_INQ_MSG)))
            ifxType = IfxType.BAL_REV_REPEAT_RQ;
//			ifxType = IfxType.BAL_REV_RQ;
        else if(commandID.equals(new Integer(FINANCIAL_MSG * 1000 + TRANSFER_MSG)))
            ifxType = IfxType.TRANSFER_RQ;
        else if(commandID.equals(new Integer(REVERSAL_MSG * 1000 + TRANSFER_MSG)))
            ifxType = IfxType.TRANSFER_REV_REPEAT_RQ;

        else if(commandID.equals(new Integer(FINANCIAL_MSG*1000 + AUTHORIZATION_CARD_TO_ACCOUNT_MSG)))
            ifxType= IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ;

        else if(commandID.equals(new Integer(REVERSAL_MSG*1000 + AUTHORIZATION_CARD_TO_ACCOUNT_MSG)))
            ifxType= IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ;

        else if(commandID.equals(new Integer(FINANCIAL_MSG*1000 + TRANSFER_CARD_TO_ACCOUNT_MSG)))
            ifxType= IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ;

        else if(commandID.equals(new Integer(REVERSAL_MSG*1000 + TRANSFER_CARD_TO_ACCOUNT_MSG)))
            ifxType= IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ;

        else if(commandID.equals(new Integer(FINANCIAL_MSG * 1000 + AUTHORIZATION_MSG)))
            ifxType = IfxType.TRANSFER_CHECK_ACCOUNT_RQ;

        else if(commandID.equals(new Integer(REVERSAL_MSG * 1000 + AUTHORIZATION_MSG)))
            ifxType = IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ;

        else if(commandID == 1010)
            ifxType = IfxType.BANK_STATEMENT_RQ;

        else if(commandID == 2010)
            ifxType = IfxType.BANK_STATEMENT_REV_REPEAT_RQ;

        else if(commandID.equals(new Integer(FINANCIAL_MSG * 1000 + PURCHASE_TOPUP_MSG)))
            ifxType = IfxType.PURCHASE_TOPUP_RQ;

        else if(commandID.equals(new Integer(REVERSAL_MSG * 1000 + PURCHASE_TOPUP_MSG)))
            ifxType = IfxType.PURCHASE_TOPUP_REV_REPEAT_RQ;

        else if(commandID.equals(new Integer(FINANCIAL_MSG * 1000 + ONLINE_BILLPAYMENT_MSG)))
            ifxType = IfxType.ONLINE_BILLPAYMENT_RQ;

        else if(commandID.equals(new Integer(REVERSAL_MSG * 1000 + ONLINE_BILLPAYMENT_MSG)))
            ifxType = IfxType.ONLINE_BILLPAYMENT_REV_REPEAT_RQ;

        else if(commandID.equals(new Integer(FINANCIAL_MSG * 1000 + PREPARE_ONLINE_BILLPAYMENT_MSG)))
            ifxType = IfxType.PREPARE_ONLINE_BILLPAYMENT;

        else if(commandID.equals(new Integer(REVERSAL_MSG * 1000 + PREPARE_ONLINE_BILLPAYMENT_MSG)))
            ifxType = IfxType.PREPARE_ONLINE_BILLPAYMENT_REV_REPEAT;

        else if(commandID.equals(new Integer(FINANCIAL_MSG * 1000 + ONLINE_BILLPAYMENT_TRACKING_MSG)))
            ifxType = IfxType.ONLINE_BILLPAYMENT_TRACKING;

        else if(commandID.equals(new Integer(REVERSAL_MSG * 1000 + ONLINE_BILLPAYMENT_TRACKING_MSG)))
            ifxType = IfxType.ONLINE_BILLPAYMENT_TRACKING_REV_REPEAT;

        else if (commandID.equals(new Integer(FINANCIAL_MSG * 1000 + THIRD_PARTY_PAYMENT_MSG)))
            ifxType = IfxType.THIRD_PARTY_PURCHASE_RQ;

        else if (commandID.equals(new Integer(REVERSAL_MSG * 1000 + THIRD_PARTY_PAYMENT_MSG)))
            ifxType = IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ;

            //TASK Task051 : Epay GetAccounts Feature
        else if (commandID.equals(new Integer(FINANCIAL_MSG * 1000 + GETACCOUNT_MSG)))
            ifxType = IfxType.GET_ACCOUNT_RQ;

            //TASK Task051 : Epay GetAccounts Feature
        else if (commandID.equals(new Integer(REVERSAL_MSG * 1000 + GETACCOUNT_MSG)))
            ifxType = IfxType.GET_ACCOUNT_REV_REPEAT_RQ;
        
        // TASK Task129 [26604] - Authenticate Cart (Pasargad)
        else if (commandID.equals(new Integer(FINANCIAL_MSG * 1000 + CARD_AUTHENTICATE_MSG)))
        	ifxType = IfxType.CARD_AUTHENTICATE_RQ;

        // TASK Task129 [26604] - Authenticate Cart (Pasargad)
        else if (commandID.equals(new Integer(REVERSAL_MSG * 1000 + CARD_AUTHENTICATE_MSG)))
        	ifxType = IfxType.CARD_AUTHENTICATE_REV_REPEAT_RQ;
        

        return ifxType;
    }

    public static boolean isFinancialMsg(EpayMsg epayMsg){
        return (epayMsg.commandID / 1000 == FINANCIAL_MSG);
    }

    public static boolean isReversalMsg(EpayMsg epayMsg){
        return (epayMsg.commandID / 1000 == REVERSAL_MSG);
    }

    private static boolean isOfTypeMsg(Integer commandID, int type){
        return (commandID % 1000 == type);
    }

    public static boolean isBillPayMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, BILL_PAY_MSG);
    }

    public static boolean isPurchaseChargeMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, PURCHASE_CHARGE_MSG);
    }

    public static boolean isPurchaseTopupMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, PURCHASE_TOPUP_MSG);
    }

    public static boolean isPurchaseMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, PURCHASE_MSG);
    }

    public static boolean isReturnMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, RETRUN_MSG);
    }

    public static boolean isBalInqMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, BALANCE_INQ_MSG);
    }

    public static boolean isTransferMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, TRANSFER_MSG);
    }

    public static boolean isTransferCardToAccountMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, TRANSFER_CARD_TO_ACCOUNT_MSG);
    }

    public static boolean isAuthorizationCardToAccountMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, AUTHORIZATION_CARD_TO_ACCOUNT_MSG);
    }

    public static boolean isAuthorizationMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, AUTHORIZATION_MSG);
    }

    public static boolean isGeneralPinChangeMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, PIN_CHANGE_MSG);
    }

    public static boolean isPin1ChangeMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, PIN_CHANGE_MSG) && (epayMsg.pinType == null || epayMsg.pinType.equals(PIN1));
    }

    public static boolean isPin2ChangeMsg(EpayMsg epayMsg) {
        return isOfTypeMsg(epayMsg.commandID, PIN_CHANGE_MSG) && (epayMsg.pinType != null || epayMsg.pinType.equals(PIN2));
    }

    public static boolean isStatementMsg(EpayMsg epayMsg) {
        return isOfTypeMsg(epayMsg.commandID, STATEMENT_MSG);
    }
    public static boolean isOnlineBillPaymentMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, ONLINE_BILLPAYMENT_MSG);
    }
    public static boolean isPrepareOnlineBillPaymentMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, PREPARE_ONLINE_BILLPAYMENT_MSG);
    }
    public static boolean isOnlineBillPaymentTrackingMsg(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, ONLINE_BILLPAYMENT_TRACKING_MSG);
    }
    public static boolean isThirdPartyPayment(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, THIRD_PARTY_PAYMENT_MSG);
    }
    //TASK Task051 : Epay GetAccounts Feature
    public static boolean isGetAccount(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, GETACCOUNT_MSG);
    }
    // TASK Task129 [26604] - Authenticate Cart (Pasargad)
    public static boolean isCardAuthenticate(EpayMsg epayMsg){
        return isOfTypeMsg(epayMsg.commandID, CARD_AUTHENTICATE_MSG);
    }    

}
