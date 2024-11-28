package vaulsys.protocols.ndc.constants;

import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;

public class RestrictionOnTrxAndTermType {
	
	
	public static final int TRANSFER_ON_ATM = 100;
	public static final int TRANSFER_ON_PINPAD = 101;
	public static final int TRANSFER_ON_EPAY = 102;
	public static final int PURCHASE_ON_EPAY = 103;
	public static final int WITHDRAWALS_ON_PINPAD = 104;
	public static final int WITHDRAWALS_ON_ATM = 105;
	public static final int PURCHASE_ON_POS = 106;
	
	
	public static String getFarsiName(String code){
		int type =  Integer.valueOf(code);
		switch (type) {
		case TRANSFER_ON_ATM:
			return "انتقال بر روی خودپرداز";
		case TRANSFER_ON_PINPAD:
			return "انتقال بر روی کارت خوان شعبه";
		case TRANSFER_ON_EPAY:
			return "انتقال بر روی اینترنت";
		case PURCHASE_ON_EPAY:
			return "خرید بر روی اینترنت";
		case WITHDRAWALS_ON_PINPAD:
			return "برداشت بر روی کارت خوان شعبه";
		case WITHDRAWALS_ON_ATM: 
			return "برداشت بر روی خودپرداز";
		case PURCHASE_ON_POS: 
			return "خرید بر روی پایانه فروش";
		default:
			return "ناشناخته";
		}
	}
	
	public static String getEnglishName(String code){
		int type =  Integer.valueOf(code);
		switch (type) {
		case TRANSFER_ON_ATM:
			return "TRANSFER ON ATM"/*"Transfer on ATM"*/;
		case TRANSFER_ON_PINPAD:
			return "TRANSFER ON PINPAD";/*"Transfer on PINPAD";*/
		case TRANSFER_ON_EPAY:
			return "TRANSFER ON EPAY";/*"Transfer on EPAY";*/
		case PURCHASE_ON_EPAY:
			return "PURCHASE ON EPAY";/*"Purchase on EPAY";*/
		case WITHDRAWALS_ON_PINPAD:
			return "WITHDRAWALS ON PINPAD";/*"Withdrawals on PINPAD";*/
		case WITHDRAWALS_ON_ATM: 
			return "WITHDRAWALS ON ATM"; /*"Withdrawals on ATM";*/
		case PURCHASE_ON_POS: 
			return "PURCHASE ON POS";/*"Purchase on POS";*/
		default:
			return "UNKNOWN";
		}
	}
	
	public static TerminalType getTerminalType(String code) {
		int type =  Integer.valueOf(code);
		
		if(type == TRANSFER_ON_ATM || type == WITHDRAWALS_ON_ATM)
			return TerminalType.ATM;
		if(type == TRANSFER_ON_PINPAD || type == WITHDRAWALS_ON_PINPAD)
			return TerminalType.PINPAD;
		if(type == TRANSFER_ON_EPAY  || type == PURCHASE_ON_EPAY)
			return TerminalType.INTERNET;
		if(type == PURCHASE_ON_POS)
			return TerminalType.POS;
		
		return TerminalType.UNKNOWN;
	}
	
	public static TrnType getTransactionType(String code) {
		int type =  Integer.valueOf(code);
		if(type == TRANSFER_ON_ATM || type == TRANSFER_ON_PINPAD || type == TRANSFER_ON_EPAY)
			return TrnType.DECREMENTALTRANSFER;
		if(type == WITHDRAWALS_ON_ATM || type == WITHDRAWALS_ON_PINPAD)
			return TrnType.WITHDRAWAL;
		if(type == PURCHASE_ON_EPAY || type == PURCHASE_ON_POS)
			return TrnType.PURCHASE;
		
		return TrnType.UNKNOWN;
	}
	
}
