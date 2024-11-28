package vaulsys.protocols.epay.base;

import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

import java.util.HashMap;

public class EpayErrorCodes {
    static private HashMap<String, Integer> fromIfxToProtocol = new HashMap<String, Integer>();

    public static Integer getCode(String isodscr) {
        Integer err = fromIfxToProtocol.get(isodscr);
        if(err == null)
        	return new Integer(0);
        return err;
    }

    static {
    	fromIfxToProtocol.put(ISOResponseCodes.APPROVED, 1);
    	fromIfxToProtocol.put(ISOResponseCodes.INVALID_ACCOUNT, 2);
    	fromIfxToProtocol.put(ISOResponseCodes.INVALID_CARD_STATUS, 3);
//    	fromIfxToProtocol.put(ErrorCodes.INVALID_CARD_STATUS, 4);
    	fromIfxToProtocol.put(ISOResponseCodes.INTERNAL_DATABASE_ERROR, 5);
    	fromIfxToProtocol.put(ISOResponseCodes.CARD_EXPIRED, 6);
    	fromIfxToProtocol.put(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED, 7);
    	fromIfxToProtocol.put(ISOResponseCodes.EXPIRY_DATE_MISMATCH, 8);
    	fromIfxToProtocol.put(ISOResponseCodes.ACQUIRER_NACK, 9);
    	fromIfxToProtocol.put(ISOResponseCodes.HOST_LINK_DOWN, 10);
    	fromIfxToProtocol.put(ISOResponseCodes.BANK_LINK_DOWN, 11);
    	fromIfxToProtocol.put(ISOResponseCodes.TRANSACTION_TIMEOUT, 12);
    	fromIfxToProtocol.put(ISOResponseCodes.TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE, 13);
    	fromIfxToProtocol.put(ISOResponseCodes.TRANSACTION_REJECTED_PERFORM_WITH_CARDHOLDER_AUTHENTICATION, 14);
    	fromIfxToProtocol.put(ISOResponseCodes.ACQUIRER_REVERSAL, 14);
    	
    	fromIfxToProtocol.put(ISOResponseCodes.CASH_TRANSACTION_NOT_ALLOWED, 15);
    	fromIfxToProtocol.put(ISOResponseCodes.UNKNOWN_TRANSACTION_SOURCE, 16);
    	fromIfxToProtocol.put(ISOResponseCodes.TRANSACTION_CODE_MISMATCH, 16);
    	fromIfxToProtocol.put(ISOResponseCodes.MANUAL_ENTRY_NOT_ALLOWED, 17);
    	fromIfxToProtocol.put(ISOResponseCodes.REFER_TO_ISSUER, 18);
    	fromIfxToProtocol.put(ISOResponseCodes.INVALID_MERCHANT, 19);
    	fromIfxToProtocol.put(ISOResponseCodes.MESSAGE_FORMAT_ERROR, 20);
    	fromIfxToProtocol.put(ISOResponseCodes.ORIGINAL_ALREADY_REJECTED, 20);
    	fromIfxToProtocol.put(ISOResponseCodes.NO_PIN_KEY, 21);
    	fromIfxToProtocol.put(ISOResponseCodes.INVALID_TO_ACCOUNT, 22);
    	fromIfxToProtocol.put(ISOResponseCodes.INVALID_TO_ACCOUNT, 22);
    	fromIfxToProtocol.put(ISOResponseCodes.PERMISSION_DENIED, 23);
    	fromIfxToProtocol.put(ISOResponseCodes.INVALID_CURRENCY_CODE, 23);
    	fromIfxToProtocol.put(ISOResponseCodes.BAD_EXPIRY_DATE, 24);
    	fromIfxToProtocol.put(ISOResponseCodes.ORIGINAL_AMOUNT_INCORRECT, 25);
    	fromIfxToProtocol.put(ISOResponseCodes.HOST_NOT_PROCESSING, 26);
    	fromIfxToProtocol.put(ISOResponseCodes.ACCOUNT_LOCKED, 27);
    	fromIfxToProtocol.put(ISOResponseCodes.CUSTOMER_RELATION_NOT_FOUND, 28);
    	fromIfxToProtocol.put(ISOResponseCodes.WARM_CARD, 29);
    	fromIfxToProtocol.put(ISOResponseCodes.SENT_TO_HOST, 30);
    	fromIfxToProtocol.put(ISOResponseCodes.RESTRICTED_MIN_WITHDRAWAL_AMOUNT, 31);
    	fromIfxToProtocol.put(ISOResponseCodes.TRANSACTION_REJECTED, 32);
    	fromIfxToProtocol.put(ISOResponseCodes.INCORRECT_ONLINE_REFNUMBER, 33);
    	fromIfxToProtocol.put(ISOResponseCodes.REFRENCE_NUMBER_IS_EXPIRE, 34);
    	fromIfxToProtocol.put(ISOResponseCodes.INVALID_COMPANY_CODE, 35); //for thirdpartypayment
    	fromIfxToProtocol.put(ISOResponseCodes.BAD_TRANSACTION_TYPE,36); //for transfer to account   //TASK Task049 : Epay TransferToCard From subsidiary Account
    	fromIfxToProtocol.put(ISOResponseCodes.NO_TRANSACTION_ALLOWED, 37); //for transfer to account  //TASK Task049 : Epay TransferToCard From subsidiary Account
    	fromIfxToProtocol.put(ISOResponseCodes.HONOUR_WITH_ID, 37); //TASK Task049 : Epay TransferToCard From subsidiary Account
    	fromIfxToProtocol.put(ISOResponseCodes.DUPLICATE_LINKED_ACCOUNT, 38);
    	fromIfxToProtocol.put(ISOResponseCodes.SAF_TRANSMIT_MODE, 38);
    	
    	fromIfxToProtocol.put(ISOResponseCodes.ACCOUNT_INACTIVE, 39);
    	fromIfxToProtocol.put(ISOResponseCodes.BAD_AMOUNT, 40);
    	
    	fromIfxToProtocol.put(ISOResponseCodes.ORIGINAL_DATA_ELEMENT_MISMATCH, 41);
    	fromIfxToProtocol.put(ISOResponseCodes.ORIGINAL_ALREADY_NACKED, 41);
   }
}