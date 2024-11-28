package vaulsys.protocols.PaymentSchemes.base;

import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;
import org.apache.log4j.Logger;

import static vaulsys.protocols.ifx.enums.IfxType.*;

/**
 * Created by m.rehman on 4/18/2016.
 */
public class ISOMessageTypes {
    public static final String AUTHORIZATION_REQUEST_87 = "0100";
    public static final String AUTHORIZATION_REQUEST_REPEAT_87 = "0101";
    public static final String AUTHORIZATION_REQUEST_93 = "1100";
    public static final String AUTHORIZATION_RESPONSE_87 = "0110";
    public static final String AUTHORIZATION_RESPONSE_93 = "1110";
    
    public static final String FINANCIAL_REQUEST_87 = "0200";
    public static final String FINANCIAL_REQUEST_93 = "1200";
    public static final String FINANCIAL_ADVICE_87 = "0220";
    public static final String FINANCIAL_ADVICE_93 = "1220";
    public static final String FINANCIAL_ADVICE_REPEAT_87 = "0221";
    public static final String FINANCIAL_ADVICE_REPEAT_93 = "1221";
    //m.rehman: adding for Loro
    public static final String LORO_ADVICE_87 = "0224";
    public static final String LORO_ADVICE_93 = "1224";
    public static final String LORO_ADVICE_REPEAT_87 = "0225";
    public static final String LORO_ADVICE_REPEAT_93 = "1225";
    public static final String FINANCIAL_RESPONSE_87 = "0210";
    public static final String FINANCIAL_RESPONSE_93 = "1210";
    public static final String FINANCIAL_ADVICE_RESPONSE_87 = "0230";
    public static final String FINANCIAL_ADVICE_RESPONSE_93 = "1230";
    //m.rehman: adding for Loro
    public static final String LORO_ADVICE_RESPONSE_87 = "0234";
    public static final String LORO_ADVICE_RESPONSE_93 = "1234";

    //m.rehman: for batch transction from NAC
    public static final String BATCH_UPLOAD_ADVICE_87 = "0320";
    public static final String BATCH_UPLOAD_ADVICE_93 = "1320";
    public static final String BATCH_UPLOAD_ADVICE_RESPONSE_87 = "0330";
    public static final String BATCH_UPLOAD_ADVICE_RESPONSE_93 = "1330";

    public static final String REVERSAL_REQUEST_87 = "0400";
    public static final String REVERSAL_REQUEST_93 = "1400";
    public static final String REVERSAL_ADVICE_87 = "0420";
    public static final String REVERSAL_ADVICE_93 = "1420";
    public static final String REVERSAL_ADVICE_REPEAT_87 = "0421";
    public static final String REVERSAL_ADVICE_REPEAT_93 = "1421";
    //m.rehman: adding for Loro
    public static final String LORO_REVERSAL_ADVICE_87 = "0424";
    public static final String LORO_REVERSAL_ADVICE_93 = "1424";
    public static final String LORO_REVERSAL_ADVICE_REPEAT_87 = "0425";
    public static final String LORO_REVERSAL_ADVICE_REPEAT_93 = "1425";
    public static final String REVERSAL_RESPONSE_87 = "0410";
    public static final String REVERSAL_RESPONSE_93 = "1410";
    public static final String REVERSAL_ADVICE_RESPONSE_87 = "0430";
    public static final String REVERSAL_ADVICE_RESPONSE_93 = "1430";
    //m.rehman: adding for Loro
    public static final String LORO_REVERSAL_ADVICE_RESPONSE_87 = "0434";
    public static final String LORO_REVERSAL_ADVICE_RESPONSE_93 = "1434";

    // Clearing and settlement
    public static final String ACQUIRER_RECON_REQUEST_87 = "0500";
    public static final String ACQUIRER_RECON_REQUEST_93 = "1500";
    public static final String ACQUIRER_RECON_RESPONSE_87 = "0510";
    public static final String ACQUIRER_RECON_RESPONSE_93 = "1510";
    public static final String ISSUER_RECON_REQUEST_87 = "0502";
    public static final String ISSUER_RECON_REQUEST_93 = "1502";
    public static final String ISSUER_RECON_RESPONSE_87 = "0512";
    public static final String ISSUER_RECON_RESPONSE_93 = "1512";
    public static final String ACQUIRER_RECON_ADVICE_87 = "0520";
    public static final String ACQUIRER_RECON_ADVICE_93 = "1520";
    public static final String ISSUER_RECON_ADVICE_87 = "0522";
    public static final String ISSUER_RECON_ADVICE_93 = "1522";
    public static final String ACQUIRER_RECON_ADVICE_RESPONSE_87 = "0530";
    public static final String ACQUIRER_RECON_ADVICE_RESPONSE_93 = "1530";
    public static final String ISSUER_RECON_ADVICE_RESPONSE_87 = "0532";
    public static final String ISSUER_RECON_ADVICE_RESPONSE_93 = "1532";

    public static final String ADMINISTRATIVE_REQUEST_87 = "0600";
    public static final String ADMINISTRATIVE_REQUEST_93 = "1600";
    public static final String ADMINISTRATIVE_ADVICE_87 = "0620";
    public static final String ADMINISTRATIVE_ADVICE_93 = "1620";
    public static final String ADMINISTRATIVE_RESPONSE_87 = "0610";
    public static final String ADMINISTRATIVE_RESPONSE_93 = "1610";
    public static final String ADMINISTRATIVE_ADVICE_RESPONSE_87 = "0630";
    public static final String ADMINISTRATIVE_ADVICE_RESPONSE_93 = "1630";

    // Network management
    public static final String NETWORK_MANAGEMENT_REQUEST_87 = "0800";
    public static final String NETWORK_MANAGEMENT_REQUEST_93 = "1800";
    public static final String NETWORK_MANAGEMENT_RESPONSE_87 = "0810";

    public static final String NETWORK_MANAGEMENT_RESPONSE_93 = "1810";
    public static final String NETWORK_MANAGEMENT_ADVICE_REPEAT_87 = "0821";
    public static final String NETWORK_MANAGEMENT_ADVICE_93 = "1820";
    public static final String NETWORK_MANAGEMENT_ADVICE_RESPONSE_87 = "0830";
    public static final String NETWORK_MANAGEMENT_ADVICE_RESPONSE_93 = "1830";
    public static final String NETWORK_MANAGEMENT_ADVICE_87 = "0820";

    private static Logger logger = Logger.getLogger(ISOMessageTypes.class);

    /*public static String getResponseMTI(String incomingMTI) { //Raza commenitng
        String outgoingMTI = incomingMTI;

        try {
            switch (Integer.parseInt(incomingMTI)) {
                case 100:
                    outgoingMTI = AUTHORIZATION_RESPONSE_87;
                    break;
                case 1100:
                    outgoingMTI = AUTHORIZATION_RESPONSE_93;
                    break;
                case 200:
                    outgoingMTI = FINANCIAL_RESPONSE_87;
                    break;
                case 1200:
                    outgoingMTI = FINANCIAL_ADVICE_93;
                    break;
                case 220:
                    outgoingMTI = FINANCIAL_ADVICE_RESPONSE_87;
                    break;
                case 221:
                    outgoingMTI = FINANCIAL_ADVICE_RESPONSE_87;
                    break;
                case 1220:
                    outgoingMTI = FINANCIAL_ADVICE_RESPONSE_93;
                    break;
                case 1221:
                    outgoingMTI = FINANCIAL_ADVICE_RESPONSE_93;
                    break;
                case 400:
                    outgoingMTI = REVERSAL_RESPONSE_87;
                    break;
                case 1400:
                    outgoingMTI = REVERSAL_RESPONSE_93;
                    break;
                case 420:
                    outgoingMTI = REVERSAL_ADVICE_RESPONSE_87;
                    break;
                case 421:
                    outgoingMTI = REVERSAL_ADVICE_RESPONSE_87;
                    break;
                case 1420:
                    outgoingMTI = REVERSAL_ADVICE_RESPONSE_93;
                    break;
                case 1421:
                    outgoingMTI = REVERSAL_ADVICE_RESPONSE_93;
                    break;
                case 500:
                    outgoingMTI = ACQUIRER_RECON_RESPONSE_87;
                    break;
                case 1500:
                    outgoingMTI = ACQUIRER_RECON_RESPONSE_93;
                    break;
                case 502:
                    outgoingMTI = ISSUER_RECON_RESPONSE_87;
                    break;
                case 1502:
                    outgoingMTI = ISSUER_RECON_RESPONSE_93;
                    break;
                case 520:
                    outgoingMTI = ACQUIRER_RECON_ADVICE_RESPONSE_87;
                    break;
                case 1520:
                    outgoingMTI = ACQUIRER_RECON_ADVICE_RESPONSE_93;
                    break;
                case 522:
                    outgoingMTI = ISSUER_RECON_ADVICE_RESPONSE_87;
                    break;
                case 1522:
                    outgoingMTI = ISSUER_RECON_ADVICE_RESPONSE_93;
                    break;
                case 600:
                    outgoingMTI = ADMINISTRATIVE_RESPONSE_87;
                    break;
                case 1600:
                    outgoingMTI = ADMINISTRATIVE_RESPONSE_93;
                    break;
                case 620:
                    outgoingMTI = ADMINISTRATIVE_ADVICE_RESPONSE_87;
                    break;
                case 1620:
                    outgoingMTI = ADMINISTRATIVE_ADVICE_RESPONSE_93;
                    break;
                case 800:
                    outgoingMTI = NETWORK_MANAGEMENT_RESPONSE_87;
                    break;
                case 1800:
                    outgoingMTI = NETWORK_MANAGEMENT_RESPONSE_93;
                    break;
                case 820:
                    outgoingMTI = NETWORK_MANAGEMENT_ADVICE_RESPONSE_87;
                    break;
                case 1820:
                    outgoingMTI = NETWORK_MANAGEMENT_ADVICE_RESPONSE_93;
                    break;
                default:
                    logger.error("Unable to find response for message type.");
                    break;
            }
        } catch (NumberFormatException e) {
            logger.error("Unable to parse Message Type. Error: " + e.getMessage());
        }

        return outgoingMTI;
    }*/ //Raza commenting

    public static boolean isResponseMessage(IfxType ifxType) {
        if(ifxType == null)
            return false;

        return IfxType.getRsOrdinals().contains(ifxType.getType());
    }

    public static boolean isRequestMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return IfxType.getRqOrdinals().contains(ifxType.getType());
    }

    public static boolean isReversalRequestMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return IfxType.getRevRqOrdinals().contains(ifxType.getType());
    }

    public static boolean isRequestMessage(String mti) {
        if (mti.equals(ISOMessageTypes.FINANCIAL_REQUEST_87) ||
                mti.equals(ISOMessageTypes.FINANCIAL_REQUEST_93) ||
                mti.equals(ISOMessageTypes.AUTHORIZATION_REQUEST_87) ||
                mti.equals(ISOMessageTypes.AUTHORIZATION_REQUEST_93) ||
				mti.equals(ISOMessageTypes.AUTHORIZATION_REQUEST_REPEAT_87))
            return true;
        else
            return false;
    }

    public static boolean isResponseMessage(String mti) {
        if (mti.equals(ISOMessageTypes.FINANCIAL_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.FINANCIAL_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.AUTHORIZATION_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.AUTHORIZATION_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.FINANCIAL_ADVICE_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.FINANCIAL_ADVICE_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.LORO_ADVICE_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.LORO_ADVICE_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.BATCH_UPLOAD_ADVICE_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.BATCH_UPLOAD_ADVICE_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.REVERSAL_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.REVERSAL_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.REVERSAL_ADVICE_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.REVERSAL_ADVICE_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.LORO_REVERSAL_ADVICE_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.LORO_REVERSAL_ADVICE_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.ACQUIRER_RECON_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.ACQUIRER_RECON_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.ISSUER_RECON_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.ISSUER_RECON_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.ACQUIRER_RECON_ADVICE_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.ISSUER_RECON_ADVICE_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.ISSUER_RECON_ADVICE_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.ADMINISTRATIVE_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.ADMINISTRATIVE_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.ADMINISTRATIVE_ADVICE_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.ADMINISTRATIVE_ADVICE_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_93) ||
                mti.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_RESPONSE_87) ||
                mti.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_RESPONSE_93))
            return true;
        else
            return false;
    }

    public static boolean isFinancialAdviceResponseMessage(String mti) {
        if (mti.equals(ISOMessageTypes.FINANCIAL_ADVICE_RESPONSE_87)
                || mti.equals(ISOMessageTypes.FINANCIAL_ADVICE_RESPONSE_93)
                )
            return true;
        else
            return false;
    }

    //m.rehman: for Loro advice response
    public static boolean isLoroAdviceResponseMessage(String mti) {
        if (mti.equals(ISOMessageTypes.LORO_ADVICE_RESPONSE_87)
                || mti.equals(ISOMessageTypes.LORO_ADVICE_RESPONSE_93)
                )
            return true;
        else
            return false;
    }

    public static String getReversalRequestMTI(String mti) {
        String outMTI = mti;

        switch (Integer.parseInt(mti)) {
            case 100:
                outMTI = REVERSAL_ADVICE_87;
                break;
            case 200:
                outMTI = REVERSAL_ADVICE_87;
                break;
            case 1100:
                outMTI = REVERSAL_ADVICE_93;
                break;
            case 1200:
                outMTI = REVERSAL_ADVICE_93;
                break;
            default:
                break;
        }

        return outMTI;
    }

    //Raza Adding start
    public static boolean isFinancialMessage(IfxType msgType) {
        if (msgType == null)
            return false;


        if (	msgType.equals(BAL_INQ_RQ) || msgType.equals(BAL_INQ_RS)
                || msgType.equals(BAL_REV_REPEAT_RQ) || msgType.equals(BAL_REV_REPEAT_RS)

                ||msgType.equals(ONLINE_BILLPAYMENT_RQ) || msgType.equals(ONLINE_BILLPAYMENT_RS)
                || msgType.equals(ONLINE_BILLPAYMENT_REV_REPEAT_RQ)|| msgType.equals(ONLINE_BILLPAYMENT_REV_REPEAT_RS)

                || msgType.equals(THIRD_PARTY_PURCHASE_RQ) || msgType.equals(THIRD_PARTY_PURCHASE_RS)
                || msgType.equals(THIRD_PARTY_PURCHASE_REV_REPEAT_RQ) || msgType.equals(THIRD_PARTY_PURCHASE_REV_REPEAT_RS)

//				|| msgType.equals(DEPOSIT_RQ) || msgType.equals(DEPOSIT_RS)

//				|| msgType.equals(DEPOSIT_REV_REPEAT_RQ)|| msgType.equals(DEPOSIT_REV_REPEAT_RS)

                || msgType.equals(BILL_PMT_RQ) || msgType.equals(BILL_PMT_RS)
                || msgType.equals(BILL_PMT_REV_REPEAT_RQ) || msgType.equals(BILL_PMT_REV_REPEAT_RS)

                || msgType.equals(TRANSFER_RQ) || msgType.equals(TRANSFER_RS)
                || msgType.equals(TRANSFER_REV_REPEAT_RQ) || msgType.equals(TRANSFER_REV_REPEAT_RS)

                || msgType.equals(TRANSFER_FROM_ACCOUNT_RQ) || msgType.equals(TRANSFER_FROM_ACCOUNT_RS)
                || msgType.equals(TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ) || msgType.equals(TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS)

                || msgType.equals(TRANSFER_TO_ACCOUNT_RQ) || msgType.equals(TRANSFER_TO_ACCOUNT_RS)
                || msgType.equals(TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ) || msgType.equals(TRANSFER_TO_ACCOUNT_REV_REPEAT_RS)

                || msgType.equals(RETURN_RQ) || msgType.equals(RETURN_RS)
                || msgType.equals(RETURN_REV_REPEAT_RQ) || msgType.equals(RETURN_REV_REPEAT_RS)

                || msgType.equals(PURCHASE_RQ) || msgType.equals(PURCHASE_RS)
                || msgType.equals(PURCHASE_REV_REPEAT_RQ) || msgType.equals(PURCHASE_REV_REPEAT_RS)

                || msgType.equals(PURCHASE_CHARGE_RQ) || msgType.equals(PURCHASE_CHARGE_RS)
                || msgType.equals(PURCHASE_CHARGE_REV_REPEAT_RQ) || msgType.equals(PURCHASE_CHARGE_REV_REPEAT_RS)

                || msgType.equals(LAST_PURCHASE_CHARGE_RQ) || msgType.equals(LAST_PURCHASE_CHARGE_RS)

                || msgType.equals(PURCHASE_TOPUP_RQ) || msgType.equals(PURCHASE_TOPUP_RS)
                || msgType.equals(PURCHASE_TOPUP_REV_REPEAT_RQ) || msgType.equals(PURCHASE_TOPUP_REV_REPEAT_RS)

                || msgType.equals(WITHDRAWAL_RQ) || msgType.equals(WITHDRAWAL_RS)
                || msgType.equals(WITHDRAWAL_REV_REPEAT_RQ) || msgType.equals(WITHDRAWAL_REV_REPEAT_RS)

                || msgType.equals(WITHDRAWAL_CUR_RQ) || msgType.equals(WITHDRAWAL_CUR_RS)
                || msgType.equals(WITHDRAWAL_CUR_REV_REPEAT_RQ) || msgType.equals(WITHDRAWAL_CUR_REV_REPEAT_RS)	//Mirkamali(Task179)

                || msgType.equals(PARTIAL_DISPENSE_RQ) || msgType.equals(PARTIAL_DISPENSE_RS)
                || msgType.equals(PARTIAL_DISPENSE_REV_REPEAT_RQ) || msgType.equals(PARTIAL_DISPENSE_REV_REPEAT_RS)

                || msgType.equals(TRANSFER_CARD_TO_ACCOUNT_RQ) || msgType.equals(TRANSFER_CARD_TO_ACCOUNT_RS)
                || msgType.equals(TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ) || msgType.equals(TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS)

                || msgType.equals(TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ)|| msgType.equals(TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS)
                || msgType.equals(TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ) || msgType.equals(TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS)

                || msgType.equals(SHEBA_INQ_RQ) ||  msgType.equals(SHEBA_INQ_RS)
                || msgType.equals(SHEBA_REV_REPEAT_RQ) ||  msgType.equals(SHEBA_REV_REPEAT_RS)

                || msgType.equals(HOTCARD_INQ_RQ) ||  msgType.equals(HOTCARD_INQ_RS)
                || msgType.equals(HOTCARD_REV_REPEAT_RQ) ||  msgType.equals(HOTCARD_REV_REPEAT_RS)

                //TASK Task081 : ATM Saham Feature
                //AldTODO Task081 : Think Bayad isFinantial Bashad Ya Na
                || msgType.equals(STOCK_INQ_RQ) ||  msgType.equals(STOCK_INQ_RS)
                || msgType.equals(STOCK_REV_REPEAT_RQ) ||  msgType.equals(STOCK_REV_REPEAT_RS)

                // TASK Task129 [26604] - Authenticate Cart (Pasargad)
                || msgType.equals(CARD_AUTHENTICATE_RQ) ||  msgType.equals(CARD_AUTHENTICATE_RS)
                || msgType.equals(CARD_AUTHENTICATE_REV_REPEAT_RQ) ||  msgType.equals(CARD_AUTHENTICATE_REV_REPEAT_RS)

                //Moosavi: Add restriction trx to financial inorder to put flag on them
                //if not problem happens in reverse sencario when next trx should make reverse for this trx
                || msgType.equals(RESTRICTION_RQ) ||  msgType.equals(RESTRICTION_RS)
                || msgType.equals(RESTRICTION_REV_REPEAT_RQ) ||  msgType.equals(RESTRICTION_REV_REPEAT_RS)

                )
            return true;

        return false;
    }

    public static boolean isATMMessage(IfxType ifxType) {
        return isFinancialMessage(ifxType) ||
                isGetAccountMessage(ifxType) ||
                isBankStatementMessage(ifxType) ||
                isChangePinBlockMessage(ifxType) ||
                isCreditCardStatementMessage(ifxType) ||
                isSubsidiaryAccountMessage(ifxType) ||
                isPrepareBillPaymentRqMessage(ifxType) ||
                isPrepareTranferCardToAccountMessage(ifxType) ||
                isTransferCheckAccountMessage(ifxType)||
                isTransferToacChechAccountMessage(ifxType) ||
                isTrackingMessage(ifxType);
    }

    public static boolean isSubsidiaryAccountMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(IfxType.SUBSIDIARY_ACCOUNT_RQ) || ifxType.equals(IfxType.SUBSIDIARY_ACCOUNT_RS) ||
                ifxType.equals(IfxType.SUBSIDIARY_ACCOUNT_REPEAT_RQ);
    }

    public static boolean isWithdrawalOrPartialMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(WITHDRAWAL_RQ) || ifxType.equals(WITHDRAWAL_RS) ||
                ifxType.equals(PARTIAL_DISPENSE_RQ) || ifxType.equals(PARTIAL_DISPENSE_RS) ||
                ifxType.equals(WITHDRAWAL_CUR_RQ) || ifxType.equals(WITHDRAWAL_CUR_RS)	//Mirkamali(Task179)
                ;
    }

    public static boolean isWithdrawalMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(WITHDRAWAL_RQ) || ifxType.equals(WITHDRAWAL_RS);
    }

    public static boolean isWithdrawalRqMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(WITHDRAWAL_RQ);
    }

    public static boolean isWithdrawalRevMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(WITHDRAWAL_REV_REPEAT_RQ) || ifxType.equals(WITHDRAWAL_REV_REPEAT_RS);
    }

    /**************** Mirkamali(Task179)************************/
    public static boolean isWithdrawalCurMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(WITHDRAWAL_CUR_RQ) || ifxType.equals(WITHDRAWAL_CUR_RS);
    }

    public static boolean isWithdrawalCurRqMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(WITHDRAWAL_CUR_RQ);
    }

    public static boolean isWithdrawalCurRevMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(WITHDRAWAL_CUR_REV_REPEAT_RQ) || ifxType.equals(WITHDRAWAL_CUR_REV_REPEAT_RS);
    }
    /***************************************************************/
    public static boolean isPartialDispenseMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(PARTIAL_DISPENSE_RQ) || ifxType.equals(PARTIAL_DISPENSE_RS);
    }

    public static boolean isPartialDispenseRqMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(PARTIAL_DISPENSE_RQ);
    }

    public static boolean isPartialDispenseRevMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(PARTIAL_DISPENSE_REV_REPEAT_RQ) || ifxType.equals(PARTIAL_DISPENSE_REV_REPEAT_RS);
    }

    public static boolean isDepositChechAccountMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(DEPOSIT_CHECK_ACCOUNT_RQ) || ifxType.equals(DEPOSIT_CHECK_ACCOUNT_RS);
    }

    public static boolean isDepositMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(DEPOSIT_RQ) || ifxType.equals(DEPOSIT_RS);
    }

    public static boolean isDepositRevMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(DEPOSIT_REV_REPEAT_RQ) || ifxType.equals(DEPOSIT_REV_REPEAT_RS);
    }

    public static boolean isTransferFromMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return ifxType.equals(TRANSFER_FROM_ACCOUNT_RQ) || ifxType.equals(TRANSFER_FROM_ACCOUNT_RS);
    }

    public static boolean isTransferFromRevMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

//    	return ifxType.equals(TRANSFER_FROM_ACCOUNT_REV_RQ) || ifxType.equals(TRANSFER_FROM_ACCOUNT_REV_RS)
        return ifxType.equals(TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ) || ifxType.equals(TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS);
    }

    public static boolean isClearingMessage(IfxType ifxType){
        if (ifxType == null)
            return false;

        if ( ifxType.equals(CUTOVER_RQ) || ifxType.equals(CUTOVER_RS)
                || ifxType.equals(CUTOVER_REPEAT_RQ) || ifxType.equals(CUTOVER_REPEAT_RS)
                || ifxType.equals(CARD_ISSUER_REC_RQ) || ifxType.equals(CARD_ISSUER_REC_RS)
                || ifxType.equals(CARD_ISSUER_REC_REPEAT_RQ) || ifxType.equals(CARD_ISSUER_REC_REPEAT_RS)
                || ifxType.equals(ACQUIRER_REC_RQ) || ifxType.equals(ACQUIRER_REC_RS)
                || ifxType.equals(ACQUIRER_REC_REPEAT_RQ) || ifxType.equals(ACQUIRER_REC_REPEAT_RS)
                )
            return true;

        return false;
    }

    public static boolean isNetworkMessage(IfxType ifxType){
        if (ifxType == null)
            return false;

        if ( ifxType.equals(MAC_KEY_CHANGE_RQ) || ifxType.equals(MAC_KEY_CHANGE_RS)
                || ifxType.equals(PIN_KEY_CHANGE_RQ) || ifxType.equals(PIN_KEY_CHANGE_RS)
                )
            return true;

        return false;
    }

    public static boolean isMessageNotToBeReverse(IfxType msgType) {

        if (BAL_INQ_RQ.equals(msgType)|| BAL_INQ_RS.equals(msgType)
                || TRANSFER_CHECK_ACCOUNT_RQ.equals(msgType)|| TRANSFER_CHECK_ACCOUNT_RS.equals(msgType)
                || BANK_STATEMENT_RQ.equals(msgType)|| BANK_STATEMENT_RS.equals(msgType)
                || CREDIT_CARD_DATA_RQ.equals(msgType)|| CREDIT_CARD_DATA_RS.equals(msgType)
                || GET_ACCOUNT_RQ.equals(msgType)|| GET_ACCOUNT_RS.equals(msgType)
                || LAST_PURCHASE_CHARGE_RQ.equals(msgType)|| LAST_PURCHASE_CHARGE_RS.equals(msgType)
                || PREPARE_BILL_PMT.equals(msgType)|| PREPARE_BILL_PMT_REV_REPEAT.equals(msgType) || CANCEL.equals(msgType)
//			|| PREPARE_TRANSFER_CARD_TO_ACCOUNT.equals(msgType)
//			|| ShetabFinalMessageType.isPrepareTranferCardToAccountMessage(msgType)
                || UI_ISSUE_SHETAB_DOCUMENT_RQ.equals(msgType) || UI_ISSUE_SHETAB_DOCUMENT_RS.equals(msgType)
                || DEPOSIT_CHECK_ACCOUNT_RQ.equals(msgType) || DEPOSIT_CHECK_ACCOUNT_RS.equals(msgType)
//			|| DEPOSIT_RQ.equals(msgType) || DEPOSIT_RS.equals(msgType)
                || CHANGE_PIN_BLOCK_RQ.equals(msgType) || CHANGE_PIN_BLOCK_RS.equals(msgType)
                || LOG_ON_RQ.equals(msgType) || LOG_ON_RS.equals(msgType)
                // NOTE: no time-out trigger on reversal request
                || isReversalRqMessage(msgType)
                || PREPARE_ONLINE_BILLPAYMENT.equals(msgType) || PREPARE_BILL_PMT_REV_REPEAT.equals(msgType)
                || ONLINE_BILLPAYMENT_TRACKING.equals(msgType) || ONLINE_BILLPAYMENT_TRACKING_REV_REPEAT.equals(msgType)
                || PREPARE_THIRD_PARTY_PURCHASE.equals(msgType) ||  PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT.equals(msgType)
                || TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(msgType) || TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(msgType)
                || SADERAT_AUTHORIZATION_BILL_PMT_RQ.equals(msgType) || SADERAT_AUTHORIZATION_BILL_PMT_RS.equals(msgType)
                || SADERAT_BILL_PMT_RQ.equals(msgType) || SADERAT_BILL_PMT_RS.equals(msgType)
                || SHEBA_INQ_RQ.equals(msgType) || SHEBA_INQ_RS.equals(msgType)
                || GHASEDAK_RQ.equals(msgType) || GHASEDAK_RS.equals(msgType)
                || HOTCARD_INQ_RQ.equals(msgType) || HOTCARD_INQ_RS.equals(msgType)
                || ISOFinalMessageType.isPrepareTranferCardToAccountMessage(msgType)
                || ISOFinalMessageType.isPrepareTranferCardToAccountReversalMessage(msgType)
                || STOCK_INQ_RQ.equals(msgType) || STOCK_INQ_RS.equals(msgType) //TASK Task081 : ATM Saham Feature
                || CARD_AUTHENTICATE_RQ.equals(msgType) || CARD_AUTHENTICATE_RS.equals(msgType) // TASK Task129 [26604] - Authenticate Cart (Pasargad)
                || PREPARE_RESTRICTION.equals(msgType) || PREPARE_RESTRICTION_REV_REPEAT.equals(msgType)	//Mirkamali(Task175): RESTRICTION
                || RESTRICTION_RQ.equals(msgType) || RESTRICTION_RS.equals(msgType)
                || PREPARE_WITHDRAWAL.equals(msgType) ||  PREPARE_WITHDRAWAL_REV_REPEAT.equals(msgType)	//Mirkamali(Task179)

                )
            return true;
        return false;
    }

    public static boolean isNeedReverseTrigger(IfxType msgType) {
        if ( BAL_REV_REPEAT_RQ.equals(msgType)|| BAL_REV_REPEAT_RS.equals(msgType)
                || TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ.equals(msgType)|| TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RS.equals(msgType)
                || BANK_STATEMENT_REV_REPEAT_RQ.equals(msgType)|| BANK_STATEMENT_REV_REPEAT_RS.equals(msgType)
                || PURCHASE_TOPUP_REV_REPEAT_RQ.equals(msgType) || PURCHASE_TOPUP_REV_REPEAT_RS.equals(msgType)
                || CREDIT_CARD_REV_REPEAT_RQ.equals(msgType)|| CREDIT_CARD_REV_REPEAT_RS.equals(msgType)
                || ONLINE_BILLPAYMENT_REV_REPEAT_RQ.equals(msgType) || ONLINE_BILLPAYMENT_REV_REPEAT_RS.equals(msgType)
                || GET_ACCOUNT_REV_REPEAT_RQ.equals(msgType) || GET_ACCOUNT_REV_REPEAT_RS.equals(msgType)
//			)
//			|| DEPOSIT_REV_REPEAT_RQ.equals(msgType)|| DEPOSIT_CHECK_ACCOUNT_RS.equals(msgType)
                || TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ.equals(msgType) || TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RS.equals(msgType)
                || SHEBA_REV_REPEAT_RQ.equals(msgType) || SHEBA_REV_REPEAT_RS.equals(msgType)
                || HOTCARD_REV_REPEAT_RQ.equals(msgType) || HOTCARD_REV_REPEAT_RS.equals(msgType)
                || STOCK_REV_REPEAT_RQ.equals(msgType) || STOCK_REV_REPEAT_RS.equals(msgType) //TASK Task081 : ATM Saham feature
                || CARD_AUTHENTICATE_REV_REPEAT_RQ.equals(msgType) || CARD_AUTHENTICATE_REV_REPEAT_RS.equals(msgType) // TASK Task129 [26604] - Authenticate Cart (Pasargad)

                )

//			   || BAL_REV_RQ.equals(msgType)|| BAL_REV_RS.equals(msgType))
            return false;
        else
            return true;
    }

    public static boolean isMessageToBeSent(IfxType msgType) {
        if (msgType == null)
            return false;

        if (isRequestMessage(msgType)
                || isPrepareMessage(msgType)
                || isPrepareReversalMessage(msgType)
                || BAL_INQ_RS.equals(msgType)
                || BANK_STATEMENT_RS.equals(msgType)
                || GET_ACCOUNT_RS.equals(msgType)
                || TRANSFER_CHECK_ACCOUNT_RS.equals(msgType)
                || LAST_PURCHASE_CHARGE_RS.equals(msgType)
//				|| PREPARE_BILL_PMT.equals(msgType)
                || CANCEL.equals(msgType)
                || UI_ISSUE_SHETAB_DOCUMENT_RQ.equals(msgType)
                || UI_ISSUE_SHETAB_DOCUMENT_RS.equals(msgType)
                || DEPOSIT_CHECK_ACCOUNT_RS.equals(msgType)
//				|| DEPOSIT_RS.equals(msgType)
                || PURCHASE_TOPUP_RS.equals(msgType)
                || ONLINE_BILLPAYMENT_RS.equals(msgType)
                || TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(msgType)
                || SADERAT_AUTHORIZATION_BILL_PMT_RS.equals(msgType)
                || SADERAT_BILL_PMT_RS.equals(msgType)
                || SHEBA_INQ_RS.equals(msgType)
                || HOTCARD_INQ_RS.equals(msgType)
                || STOCK_INQ_RS.equals(msgType) //TASK Task081 : ATM Saham feature
                || CARD_AUTHENTICATE_RS.equals(msgType) // TASK Task129 [26604] - Authenticate Cart (Pasargad)
                )
            return false;

        return true;
    }

    public static boolean isTransferMessage(IfxType msgType) {
        if (msgType == null)
            return false;

//        if (msgType.toString().toUpperCase().contains("TRANSFER"))
//            return true;
//        return false;
        return IfxType.getTransferOrdinals().contains(msgType.getType());
    }

    public static boolean isTransferToMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        return msgType.equals(TRANSFER_TO_ACCOUNT_RQ) || msgType.equals(TRANSFER_TO_ACCOUNT_RS);
    }

    public static boolean isTransferToAccountTransferToMessage(IfxType msgType){
        if(msgType == null)
            return false;
        return msgType.equals(TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ) || msgType.equals(TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS);
    }

    public static boolean isTransferToRevMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        return msgType.equals(TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ) || msgType.equals(TRANSFER_TO_ACCOUNT_REV_REPEAT_RS);
//    	|| msgType.equals(TRANSFER_TO_ACCOUNT_REV_RQ) || msgType.equals(TRANSFER_TO_ACCOUNT_REV_RS);
    }

    public static boolean isTransferAccountToRevMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        return msgType.equals(TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ) || msgType.equals(TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS);
//    	|| msgType.equals(TRANSFER_TO_ACCOUNT_REV_RQ) || msgType.equals(TRANSFER_TO_ACCOUNT_REV_RS);
    }

    public static boolean isForwardingTransferRevRq(Long destBankId, Long recvBankId, IfxType ifxType, Long myBin) {
        if (ifxType == null)
            return false;

        if (!(IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifxType)) && !IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ.equals(ifxType))
            return false;

        return !(destBankId.equals(myBin) ^ recvBankId.equals(myBin));
    }

    public static boolean isForwardingTransferMessage(Long destBankId, Long recvBankId, IfxType ifxType, Long myBin) {
        if (ifxType == null)
            return false;

        if (!(/*IfxType.TRANSFER_REV_RQ.equals(ifxType)||*/
                IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifxType)
                        || IfxType.TRANSFER_RQ.equals(ifxType))
                || IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifxType)
                || IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ.equals(ifxType)){
            return false;
        }
        return !(destBankId.equals(myBin) ^ recvBankId.equals(myBin));
    }

    public static boolean isForwardingTransferRq(Long destBankId, Long recvBankId, IfxType ifxType, Long myBin, Ifx ifx) {
        if (ifxType == null)
            return false;

        if (!IfxType.TRANSFER_RQ.equals(ifxType) && !IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifxType))
            return false;

        return !(destBankId.equals(myBin) ^ recvBankId.equals(myBin));
    }

    public static boolean isPurchasePaymentMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        if ( isPurchaseMessage(msgType) ||
                isBillPaymentMessage(msgType) ||
                isReturnMessage(msgType))
            return true;

        return false;
    }

    public static boolean isPurchaseMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        if (msgType.equals(PURCHASE_RQ) || msgType.equals(PURCHASE_RS))
            return true;

        return false;
    }

    public static boolean isPurchaseChargeMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        if (msgType.equals(PURCHASE_CHARGE_RQ) || msgType.equals(PURCHASE_CHARGE_RS))
            return true;

        return false;
    }

    public static boolean isPurchaseChargeReverseMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        if (msgType.equals(PURCHASE_CHARGE_REV_REPEAT_RQ) || msgType.equals(PURCHASE_CHARGE_REV_REPEAT_RS))
            return true;

        return false;
    }

    public static boolean isPurchaseChargeAndReversalMessage(IfxType ifxType) {
        return isPurchaseChargeMessage(ifxType) || isPurchaseChargeReverseMessage(ifxType);
    }

    public static boolean isLastPurchaseChargeMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        if (msgType.equals(LAST_PURCHASE_CHARGE_RQ) || msgType.equals(LAST_PURCHASE_CHARGE_RS))
            return true;

        return false;
    }

    public static boolean isPurchaseTopupMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        if (msgType.equals(PURCHASE_TOPUP_RQ) || msgType.equals(PURCHASE_TOPUP_RS))
            return true;

        return false;
    }

    public static boolean isPurchaseTopupReverseMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        if (msgType.equals(PURCHASE_TOPUP_REV_REPEAT_RQ) || msgType.equals(PURCHASE_TOPUP_REV_REPEAT_RS))
            return true;

        return false;
    }

    public static boolean isPurchaseBalanceMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        if (ifxType.equals(PURCHASE_RQ) || ifxType.equals(PURCHASE_RS) ||
                ifxType.equals(BAL_INQ_RQ) || ifxType.equals(BAL_INQ_RS))
            return true;

        return false;
    }

    public static boolean isBillPaymentMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        if (msgType.equals(BILL_PMT_RQ) || msgType.equals(BILL_PMT_RS))
            return true;

        return false;
    }

    public static boolean isOnlineBillPaymentMessage(IfxType msgType){
        if(msgType == null)
            return false;
        if(msgType.equals(ONLINE_BILLPAYMENT_RQ) || msgType.equals(ONLINE_BILLPAYMENT_RS))
            return true;
        return false;
    }

    public static boolean isThirdPartyPurchaseMessage(IfxType msgType){
        if(msgType == null)
            return false;
        if(msgType.equals(THIRD_PARTY_PURCHASE_RQ) ||  msgType.equals(THIRD_PARTY_PURCHASE_RS))
            return true;
        return false;
    }

    public static boolean isThirdPartyPaymentReverseMessage(IfxType msgType){
        if(msgType == null)
            return false;
        if(msgType.equals(THIRD_PARTY_PURCHASE_REV_REPEAT_RQ) || msgType.equals(THIRD_PARTY_PURCHASE_REV_REPEAT_RS))
            return true;
        return false;
    }

    public static boolean isOnlineBillPaymentReverseMessage(IfxType msgType){
        if(msgType == null)
            return false;
        if(msgType.equals(ONLINE_BILLPAYMENT_REV_REPEAT_RQ) || msgType.equals(ONLINE_BILLPAYMENT_REV_REPEAT_RS))
            return true;
        return false;
    }

    public static boolean isBillPaymentRqMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        if (ifxType.equals(BILL_PMT_RQ))
            return true;

        return false;
    }

    public static boolean isPrepareBillPaymentRqMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        if (ifxType.equals(PREPARE_BILL_PMT))
            return true;

        return false;
    }

    public static boolean isPrepareTranferCardToAccountMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        if (ifxType.equals(PREPARE_TRANSFER_CARD_TO_ACCOUNT))
            return true;

        if (ifxType.equals(PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP))
            return true;

        return false;
    }

    public static boolean isPrepareTranferCardToAccountReversalMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        if (ifxType.equals(PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT))
            return true;

        if (ifxType.equals(PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_REV_REPEAT))
            return true;


        return false;
    }

    public static boolean isTransferCardToAccountMessage(IfxType ifxtype){
        if(ifxtype == null)
            return false;
        return ifxtype.equals(TRANSFER_CARD_TO_ACCOUNT_RQ) || ifxtype.equals(TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);
    }

    public static boolean isTransferCardToAccountRevMessage(IfxType ifxtype){
        if(ifxtype == null)
            return false;
        return ifxtype.equals(TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);
    }

    public static boolean isPurchaseReverseMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        if (/*msgType.equals(PURCHASE_REV_RQ) || msgType.equals(PURCHASE_REV_RS) ||*/
                ifxType.equals(PURCHASE_REV_REPEAT_RQ) || ifxType.equals(PURCHASE_REV_REPEAT_RS))
            return true;

        return false;
    }

    public static boolean isBillPaymentReverseMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        if (/*msgType.equals(BILL_PMT_REV_RQ) ||
        		msgType.equals(BILL_PMT_REV_RS) ||*/
                msgType.equals(BILL_PMT_REV_REPEAT_RQ) ||
                        msgType.equals(BILL_PMT_REV_REPEAT_RS))
            return true;

        return false;
    }

    public static boolean isBalanceInqueryMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        if (msgType.equals(BAL_INQ_RQ) || msgType.equals(BAL_INQ_RS))
            return true;

        return false;
    }

    public static boolean isBalanceInqueryRevMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        if (msgType.equals(BAL_REV_REPEAT_RQ) || msgType.equals(BAL_REV_REPEAT_RS))
            return true;

        return false;
    }

    public static boolean isReturnReverseMessage(IfxType msgType) {
        if (msgType == null)
            return false;

        if (/*msgType.equals(RETURN_REV_RQ) || msgType.equals(RETURN_REV_RS) ||*/
                msgType.equals(RETURN_REV_REPEAT_RQ) || msgType.equals(RETURN_REV_REPEAT_RS))
            return true;

        return false;
    }

    public static boolean isReturnResponseMessage(IfxType msgType) {
        if (msgType == null)
            return false;
        if (msgType.equals(RETURN_RS) ||msgType.equals(RETURN_REV_REPEAT_RS) )
            return true;
        return false;
    }
    public static boolean isReversalMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return isReversalRqMessage(ifxType) || isReversalRsMessage(ifxType);
    }

    public static boolean isReversalRqMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;
        return IfxType.getRevRqOrdinals().contains(ifxType.getType());
    }

    public static boolean isTrackingMessage(IfxType ifxtype){
        if(ifxtype == null)
            return false;
        if(ifxtype.equals(ONLINE_BILLPAYMENT_TRACKING))
            return true;
        return false;
    }
    public static boolean isOnlineBillPayment(IfxType ifxType){
        if(ifxType == null)
            return false;
        if(isTrackingMessage(ifxType) || IfxType.ONLINE_BILLPAYMENT_RQ.equals(ifxType)||
                IfxType.ONLINE_BILLPAYMENT_RS.equals(ifxType)||
                IfxType.ONLINE_BILLPAYMENT_REV_REPEAT_RQ.equals(ifxType)||
                IfxType.ONLINE_BILLPAYMENT_REV_REPEAT_RS.equals(ifxType))

            return true;
        return false;
    }

    public static boolean isPrepareOnlineBillPayment(IfxType ifxType){
        if(ifxType == null)
            return false;
        if(IfxType.PREPARE_ONLINE_BILLPAYMENT.equals(ifxType))
            return true;
        return false;
    }



    public static boolean isPrepareMessage(IfxType ifxType){
        return IfxType.PREPARE_BILL_PMT.equals(ifxType)
//				|| IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT.equals(ifxType)
                || ISOFinalMessageType.isPrepareTranferCardToAccountMessage(ifxType)
                || IfxType.PREPARE_ONLINE_BILLPAYMENT.equals(ifxType)
                || IfxType.ONLINE_BILLPAYMENT_TRACKING.equals(ifxType)
                || IfxType.PREPARE_THIRD_PARTY_PURCHASE.equals(ifxType)
                || IfxType.PREPARE_RESTRICTION.equals(ifxType)	//Mirkamali(Task175): RESTRICTION
                || IfxType.PREPARE_WITHDRAWAL.equals(ifxType)	//Mirkamali(Task179): Currency ATM
                ;
    }

    public static boolean isPrepareReversalMessage(IfxType ifxType){
        return IfxType.PREPARE_BILL_PMT_REV_REPEAT.equals(ifxType)
                || IfxType.PREPARE_ONLINE_BILLPAYMENT_REV_REPEAT.equals(ifxType)
                || IfxType.ONLINE_BILLPAYMENT_TRACKING_REV_REPEAT.equals(ifxType)
                || IfxType.PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT.equals(ifxType)
                || ISOFinalMessageType.isPrepareTranferCardToAccountReversalMessage(ifxType)

                ;
    }

    public static boolean isReversalRsMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;
        return IfxType.getRevRsOrdinals().contains(ifxType.getType());
    }

    public static boolean isReversalOrRepeatRsMessage(IfxType ifxType){
        return
                isReversalRsMessage(ifxType) || isRepeatRsMessage(ifxType);
    }

    public static boolean isRepeatRsMessage(IfxType ifxType){
        return isRepeatMessage(ifxType)&& isResponseMessage(ifxType);
    }

    public static boolean isReversalOrRepeatMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;

        return isReversalMessage(ifxType) || isRepeatMessage(ifxType);
    }

    public static boolean isReversalOrRepeatWithoutReturnMessage(IfxType ifxType)
    {
        return (isReversalMessage(ifxType) || isRepeatMessage(ifxType)) && !isReturnReverseMessage(ifxType);

    }

    public static boolean isRepeatMessage(IfxType ifxType) {
        return IfxType.getRepeatOrdinals().contains(ifxType.getType());
    }

    public static boolean isReturnRq(IfxType ifxType) {
        return RETURN_RQ.equals(ifxType);
    }

    public static boolean isReturnMessage(IfxType ifxType) {
        return RETURN_RQ.equals(ifxType) || RETURN_RS.equals(ifxType);
    }
    //    public static boolean isOnlineBillPaymentRefoundRq(IfxType ifxType){
//    	return ONLINE_BILLPAYMENT_REFOUND_RQ.equals(ifxType);
//    }
    public static boolean isReconcilementRs(IfxType ifxtype){

        return IfxType.ACQUIRER_REC_RS.equals(ifxtype)
                || IfxType.ACQUIRER_REC_REPEAT_RS.equals(ifxtype)
                || IfxType.CARD_ISSUER_REC_RS.equals(ifxtype)
                || IfxType.CARD_ISSUER_REC_REPEAT_RS.equals(ifxtype);
    }

    public static boolean isGetAccountMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;
        return IfxType.GET_ACCOUNT_RQ.equals(ifxType) || IfxType.GET_ACCOUNT_RS.equals(ifxType)
    			/*|| IfxType.GET_ACCOUNT_REV_RQ.equals(ifxType) || IfxType.GET_ACCOUNT_REV_RS .equals(ifxType)*/
                || IfxType.GET_ACCOUNT_REV_REPEAT_RQ.equals(ifxType) || IfxType.GET_ACCOUNT_REV_REPEAT_RS.equals(ifxType)
                ;
    }

    public static boolean isTransferCheckAccountMessage(IfxType ifxType) {
        return IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(ifxType) || IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifxType)
                || IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ.equals(ifxType) || IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RS.equals(ifxType)
                ;
    }


    public static boolean isTransferToacChechAccountMessage(IfxType ifxType){
        return IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(ifxType) || IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(ifxType)
                || IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ.equals(ifxType) || IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RS.equals(ifxType)
                ;
    }
    public static boolean isNeedThirdParty(IfxType ifxType) {
        return isBillPaymentMessage(ifxType) || isBillPaymentReverseMessage(ifxType) ||
                isPurchaseChargeMessage(ifxType) || isPurchaseChargeReverseMessage(ifxType) ||
                isPurchaseTopupMessage(ifxType) || isPurchaseTopupReverseMessage(ifxType) ||
                isOnlineBillPayment(ifxType)||
                isThirdPartyPurchaseMessage(ifxType);
    }

    public static boolean isReturnable(IfxType ifxType){
        return ifxType.equals(PURCHASE_RQ);
    }

//	public static boolean isCellChargeAndReversalMessage(IfxType ifxType) {
//		return isCellChargeMessage(ifxType) || isCellChargeReversalMessage(ifxType);
//	}
//
//	public static boolean isCellChargeMessage(IfxType ifxType) {
//		return IfxType.PURCHASE_CHARGE_RQ.equals(ifxType) || IfxType.PURCHASE_CHARGE_RS.equals(ifxType);
//	}

//	public static boolean isCellChargeReversalMessage(IfxType ifxType) {
//		return IfxType.PURCHASE_CHARGE_REV_RQ.equals(ifxType) || IfxType.PURCHASE_CHARGE_REV_RS.equals(ifxType)	||
//		return IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ.equals(ifxType) || IfxType.PURCHASE_CHARGE_REV_REPEAT_RS.equals(ifxType);
//	}

    public static boolean isChangePinBlockMessage(IfxType ifxType) {
        return IfxType.CHANGE_PIN_BLOCK_RQ.equals(ifxType) || IfxType.CHANGE_PIN_BLOCK_RS.equals(ifxType)
//		|| IfxType.CHANGE_PIN_BLOCK_REV_RQ.equals(ifxType) || IfxType.CHANGE_PIN_BLOCK_REV_RS.equals(ifxType)
                || IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ.equals(ifxType) || IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS.equals(ifxType);
    }

    public static boolean isBankStatementMessage(IfxType ifxType) {
        return IfxType.BANK_STATEMENT_RQ.equals(ifxType)|| IfxType.BANK_STATEMENT_RS.equals(ifxType);
    }

    public static boolean isBankStatementRevMessage(IfxType ifxType) {
        if (ifxType == null)
            return false;
        return IfxType.BANK_STATEMENT_REV_REPEAT_RQ.equals(ifxType)|| IfxType.BANK_STATEMENT_REV_REPEAT_RS.equals(ifxType);
    }

    public static boolean isCreditCardStatementMessage(IfxType ifxType) {
        return IfxType.CREDIT_CARD_DATA_RQ.equals(ifxType)|| IfxType.CREDIT_CARD_DATA_RS.equals(ifxType);
    }

    public static boolean isUnlockCharge(IfxType ifxType){
        return IfxType.PURCHASE_CHARGE_RS.equals(ifxType)
                || IfxType.PURCHASE_CHARGE_RQ.equals(ifxType)
                || IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ.equals(ifxType)
                || IfxType.PURCHASE_CHARGE_REV_REPEAT_RS.equals(ifxType);
    }

    public static boolean isKeepRepeatTrigger(IfxType ifxType){
        return isReversalRqMessage(ifxType) ||
                CUTOVER_RQ.equals(ifxType) || CUTOVER_REPEAT_RQ.equals(ifxType)||
                ACQUIRER_REC_RQ.equals(ifxType)|| ACQUIRER_REC_REPEAT_RQ.equals(ifxType) ||
                CARD_ISSUER_REC_RQ.equals(ifxType)|| CARD_ISSUER_REC_REPEAT_RQ.equals(ifxType);

    }


    public static boolean isSettlementTransfer(IfxType ifxType) {
        return IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_RQ.equals(ifxType)
                || IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_RS.equals(ifxType)
                || IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ.equals(ifxType)
                || IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_REV_REPEAT_RS.equals(ifxType);
    }

    public static boolean isDepositTypeRevMessage(IfxType ifxType) {
        if (!isReversalMessage(ifxType))
            return false;

        return isDepositRevMessage(ifxType) || isReturnReverseMessage(ifxType) || isTransferToRevMessage(ifxType) || isTransferAccountToRevMessage(ifxType);
    }


    public static boolean isCutOfRev(IfxType ifxtype){
        return IfxType.CUTOVER_REPEAT_RQ.equals(ifxtype) ||
                IfxType.CUTOVER_REPEAT_RS.equals(ifxtype);
    }

    public static boolean isShebaMessage(IfxType msgType) {
        if (msgType == null)
            return false;


        if (	msgType.equals(SHEBA_INQ_RQ) || msgType.equals(SHEBA_INQ_RS)
                || msgType.equals(SHEBA_REV_REPEAT_RQ) || msgType.equals(SHEBA_REV_REPEAT_RS)

                )
            return true;

        return false;
    }

    public static boolean isReceiptOrShowRsMessage(IfxType ifxtype) {
        if (ifxtype == null)
            return false;

        if (	ifxtype.equals(BAL_INQ_RS) || ifxtype.equals(SHEBA_INQ_RS)

                )
            return true;

        return false;
    }

    public static boolean isForceShowIfReceiptErrorRsMessage(IfxType ifxtype)
    {
        if (ifxtype == null)
            return false;

        //TASK Task081 : ATM Saham Feautre
        if (	ifxtype.equals(BAL_INQ_RS) || ifxtype.equals(SHEBA_INQ_RS) || ifxtype.equals(STOCK_INQ_RS)

                )
            return true;

        return false;
    }

    public static boolean isHotCardMessage(IfxType ifxType){
        return IfxType.HOTCARD_INQ_RQ.equals(ifxType)
                || IfxType.HOTCARD_REV_REPEAT_RQ.equals(ifxType)
                || IfxType.HOTCARD_INQ_RS.equals(ifxType)
                || IfxType.HOTCARD_REV_REPEAT_RS.equals(ifxType)
                ;
    }

    public static boolean isContinueIfReceiptErrorRevMessage(IfxType ifxType)
    {
        return IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ.equals(ifxType)
                || IfxType.BILL_PMT_REV_REPEAT_RQ.equals(ifxType);  //TASK Task019 : Receipt Option
        //|| IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifxType) //TASK Task021 : Transfer to Card Sub account bug
    }

    //TASK Task081 : ATM Saham feature
    public static boolean isStockMessage(IfxType ifxType)
    {
        return IfxType.STOCK_INQ_RQ.equals(ifxType) || IfxType.STOCK_REV_REPEAT_RQ.equals(ifxType)
                || IfxType.STOCK_INQ_RS.equals(ifxType) || IfxType.STOCK_REV_REPEAT_RS.equals(ifxType);
    }

    public static String getResponseMTI(String MTI)
    {
        if(isResponseMessage(MTI)) //Raza adding for failed Response
            return MTI;


        Integer mti = new Integer(MTI);
        //m.rehman: change for repeat messages for ex: 0101
        if (mti % 2 == 0)
            mti += 10;
        else
            mti += 9;
        MTI = "0" +  mti.toString();
        return MTI;
    }
    //Raza Adding end

    //m.rehman: change and set class of mti

    public static String setMessageClass(String sourceMti, String currentMti) {

        if (Util.hasText(sourceMti) && Util.hasText(currentMti)) {
            if (ISOMessageTypes.isResponseMessage(currentMti)) {
                if (sourceMti.equals(ISOMessageTypes.AUTHORIZATION_REQUEST_87)
                        && currentMti.equals(ISOMessageTypes.FINANCIAL_RESPONSE_87))
                    currentMti = ISOMessageTypes.AUTHORIZATION_RESPONSE_87;
                else if (sourceMti.equals(ISOMessageTypes.AUTHORIZATION_REQUEST_93)
                        && currentMti.equals(ISOMessageTypes.FINANCIAL_RESPONSE_93))
                    currentMti = ISOMessageTypes.AUTHORIZATION_RESPONSE_93;
            }
        }

        return currentMti;
    }
}
