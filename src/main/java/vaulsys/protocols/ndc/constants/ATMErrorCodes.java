package vaulsys.protocols.ndc.constants;


public class ATMErrorCodes {

	public static int DEFAULT_RESPONSE_CODE = -1;
	public static int ATM_SUCCESS_PARTIAL_DISPENSE = 100;
	public static int ATM_NOT_SUFFICIENT_AMOUNT = 110;
	public static int ATM_NOT_ROUND_AMOUNT = 111;
	public static int ATM_CACH_HANDLER = 112;
	public static int ATM_NOT_PAPER_RECEIPT = 113;
	public static int ATM_NOT_PAPER_RECEIPT_END_TRANSACTION = 114;
	public static int CANCEL = 115;
	public static int PREPARE_BILL_PMT = 116;
	public static int NO_SUBSIDIARY_ACCOUNT = 117;
	public static int ATM_NO_CARD_REJECTED = 118;
	public static int ATM_UNDEFINED_OPKEY = 119;
	public static int PREPARE_TRANSFER_CARD_TO_ACCOUNT = 120;
	public static int PREPARE_ONLINE_BILLPAYMENT = 121;
	public static int ONLINE_BILLPAYMNET_TRACKING = 122;
	public static int PREPARE_THIRD_PARTY_PAYMENT = 123;
	public static int ATM_EJECT_CARD = 127;
	public static int PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP = 128;
	public static int PREPARE_TRANSFER_CARD_TO_ACCOUNT_MIDDLE_STEP = 129;
	public static int PREPARE_RESTRICTION = 130;	//Mirkamali(Task175): Restriction
	public static int PREPARE_WITHDRAWAL = 131;	//Mirkamali(Task179): Currency ATM
}