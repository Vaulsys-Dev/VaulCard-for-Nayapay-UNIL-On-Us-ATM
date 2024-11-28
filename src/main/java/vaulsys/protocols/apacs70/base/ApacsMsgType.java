package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TrnType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApacsMsgType {
	public static final String FIN_REQ = "FIN_REQ";
	public static final String FIN_RES = "FIN_RES";
	
	public static final String  CONF = "CONF";
	public static final Integer CONF_OK = 0x0;
	public static final Integer CONF_FAILED_ON_TERMINAL = 0x1;
	public static final Integer CONF_CANCELLED_ON_TERMINAL = 0x3;
	public static final Integer CONF_TIMEOUT = 0xe;
	
	public static final String RECON_REQ = "RECON_REQ";
	public static final String RECON_RES = "RECON_RES";
	
	public static final String NET_REQ = "NET_REQ";
	public static final String NET_RES = "NET_RES";
	
	//ghasedak
	public static final String INFO_REQ = "INFO_REQ";
	public static final String INFO_RES = "INFO_RES";

	static final List<String> FIN_REQ_LIST = Arrays.asList("11", "40", "70", "D0", "E0", "E2", "E4", "E6", "E8", "F0", "F2", "F4", "F6", "F8", "F9", "FB", "FC", "FE", "31", "A2");
	static final List<String> FIN_RES_LIST = Arrays.asList("13", "41", "71", "D1", "E1", "E3", "E5", "E7", "E9", "F1", "F3", "F5", "F7", "F8", "FA", "FB", "FD", "FF", "32", "A3");
	static final List<String> CONF_LIST = Arrays.asList("82");

	static final List<String> RECON_REQ_LIST = Arrays.asList("60");
	static final List<String> RECON_RES_LIST = Arrays.asList("62");
	
	static final List<String> NET_REQ_LIST = Arrays.asList("D5", "D9");
	static final List<String> NET_RES_LIST = Arrays.asList("D6", "DA");
	
	//ghasedak
	static final List<String> INFO_REQ_LIST = Arrays.asList("A0");
	static final List<String> INFO_RES_LIST = Arrays.asList("A1");

	static Map<String, TrnType> APACS_2_TRN_TYPE = new HashMap<String, TrnType>();
	static {
		APACS_2_TRN_TYPE.put("11", TrnType.PURCHASE);
		APACS_2_TRN_TYPE.put("13", TrnType.PURCHASE);

		APACS_2_TRN_TYPE.put("40", TrnType.RETURN);
		APACS_2_TRN_TYPE.put("41", TrnType.RETURN);

		APACS_2_TRN_TYPE.put("70", TrnType.BALANCEINQUIRY);
		APACS_2_TRN_TYPE.put("71", TrnType.BALANCEINQUIRY);
		
		APACS_2_TRN_TYPE.put("D0", TrnType.PURCHASECHARGE);
		APACS_2_TRN_TYPE.put("D1", TrnType.PURCHASECHARGE);
		
		APACS_2_TRN_TYPE.put("E0", TrnType.BILLPAYMENT);
		APACS_2_TRN_TYPE.put("E1", TrnType.BILLPAYMENT);
		APACS_2_TRN_TYPE.put("E2", TrnType.SADERAT_AUTH_BILLPAYMENT);
		APACS_2_TRN_TYPE.put("E3", TrnType.SADERAT_AUTH_BILLPAYMENT);
		APACS_2_TRN_TYPE.put("E4", TrnType.SADERAT_BILLPAYMENT);
		APACS_2_TRN_TYPE.put("E5", TrnType.SADERAT_BILLPAYMENT);
		
		APACS_2_TRN_TYPE.put("60", TrnType.RECONCILIATION);
		APACS_2_TRN_TYPE.put("62", TrnType.RECONCILIATION);
		
		//network
		APACS_2_TRN_TYPE.put("D5", TrnType.NETWORKMANAGEMENT);
		APACS_2_TRN_TYPE.put("D6", TrnType.NETWORKMANAGEMENT);
		APACS_2_TRN_TYPE.put("D9", TrnType.NETWORKMANAGEMENT);
		APACS_2_TRN_TYPE.put("DA", TrnType.NETWORKMANAGEMENT);
		
		
		// Fund Transfer
		APACS_2_TRN_TYPE.put("F0", TrnType.TRANSFER);
		APACS_2_TRN_TYPE.put("F1", TrnType.TRANSFER);
		
		// Fund Transfer Authentication
		APACS_2_TRN_TYPE.put("F2", TrnType.CHECKACCOUNT);
		APACS_2_TRN_TYPE.put("F3", TrnType.CHECKACCOUNT);

		// PIN1 change
		APACS_2_TRN_TYPE.put("F4", TrnType.CHANGEPINBLOCK);
		APACS_2_TRN_TYPE.put("F5", TrnType.CHANGEPINBLOCK);

		// PIN2 change
		APACS_2_TRN_TYPE.put("F6", TrnType.CHANGEINTERNETPINBLOCK);
		APACS_2_TRN_TYPE.put("F7", TrnType.CHANGEINTERNETPINBLOCK);

		// Ref Payment
		APACS_2_TRN_TYPE.put("F8", TrnType.PREPARE_ONLINE_BILLPAYMENT);
		APACS_2_TRN_TYPE.put("F9", TrnType.ONLINE_BILLPAYMENT);
		APACS_2_TRN_TYPE.put("FA", TrnType.ONLINE_BILLPAYMENT);
		APACS_2_TRN_TYPE.put("FB", TrnType.ONLINE_BILLPAYMENT);

		// Credit (Mizan)
		APACS_2_TRN_TYPE.put("FC", TrnType.PURCHASE);
		APACS_2_TRN_TYPE.put("FD", TrnType.PURCHASE);
		APACS_2_TRN_TYPE.put("FE", TrnType.PURCHASE);
		APACS_2_TRN_TYPE.put("FF", TrnType.PURCHASE);
		
		//ThirdParty Purchase
		APACS_2_TRN_TYPE.put("31", TrnType.THIRD_PARTY_PAYMENT);
		APACS_2_TRN_TYPE.put("32", TrnType.THIRD_PARTY_PAYMENT);
		
		//Transfer to account
		APACS_2_TRN_TYPE.put("E6", TrnType.TRANSFER_CARD_TO_ACCOUNT);
		APACS_2_TRN_TYPE.put("E7", TrnType.TRANSFER_CARD_TO_ACCOUNT);
		
		//TRansfer to Account Authorization
		APACS_2_TRN_TYPE.put("E8", TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		APACS_2_TRN_TYPE.put("E9", TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);

		APACS_2_TRN_TYPE.put("A2", TrnType.BANKSTATEMENT);
		APACS_2_TRN_TYPE.put("A3", TrnType.BANKSTATEMENT);
		
		//ghasedak
		APACS_2_TRN_TYPE.put("A0", TrnType.NONFINANCIAL_INFO);
		APACS_2_TRN_TYPE.put("A1", TrnType.NONFINANCIAL_INFO);
		
		
	}

	static Map<String, IfxType> APACS_2_IFX_TYPE = new HashMap<String, IfxType>();
	static {
		APACS_2_IFX_TYPE.put("11", IfxType.PURCHASE_RQ);
		APACS_2_IFX_TYPE.put("13", IfxType.PURCHASE_RS);

		APACS_2_IFX_TYPE.put("40", IfxType.RETURN_RQ);
		APACS_2_IFX_TYPE.put("41", IfxType.RETURN_RS);

		APACS_2_IFX_TYPE.put("70", IfxType.BAL_INQ_RQ);
		APACS_2_IFX_TYPE.put("71", IfxType.BAL_INQ_RS);

		APACS_2_IFX_TYPE.put("D0", IfxType.PURCHASE_CHARGE_RQ);
		APACS_2_IFX_TYPE.put("D1", IfxType.PURCHASE_CHARGE_RS);

		APACS_2_IFX_TYPE.put("E0", IfxType.BILL_PMT_RQ);
		APACS_2_IFX_TYPE.put("E1", IfxType.BILL_PMT_RS);

		APACS_2_IFX_TYPE.put("E2", IfxType.SADERAT_AUTHORIZATION_BILL_PMT_RQ);
		APACS_2_IFX_TYPE.put("E3", IfxType.SADERAT_AUTHORIZATION_BILL_PMT_RS);
		APACS_2_IFX_TYPE.put("E4", IfxType.SADERAT_BILL_PMT_RQ);
		APACS_2_IFX_TYPE.put("E5", IfxType.SADERAT_BILL_PMT_RS);
		
		APACS_2_IFX_TYPE.put("60", IfxType.ACQUIRER_REC_RQ); //old value: IfxType.RECONCILIATION_RQ
		APACS_2_IFX_TYPE.put("62", IfxType.ACQUIRER_REC_RS); // old value: IfxType.RECONCILIATION_RS
		
		// Network
		APACS_2_IFX_TYPE.put("D5", IfxType.LOG_ON_RQ);
		APACS_2_IFX_TYPE.put("D6", IfxType.LOG_ON_RS);

		APACS_2_IFX_TYPE.put("D9", IfxType.RESET_PASSWORD_RQ);
		APACS_2_IFX_TYPE.put("DA", IfxType.RESET_PASSWORD_RS);

		// Fund Transfer
		APACS_2_IFX_TYPE.put("F0", IfxType.TRANSFER_RQ);
		APACS_2_IFX_TYPE.put("F1", IfxType.TRANSFER_RS);

		// Fund Transfer Authentication
		APACS_2_IFX_TYPE.put("F2", IfxType.TRANSFER_CHECK_ACCOUNT_RQ);
		APACS_2_IFX_TYPE.put("F3", IfxType.TRANSFER_CHECK_ACCOUNT_RS);

		// Pin1 Change
		APACS_2_IFX_TYPE.put("F4", IfxType.CHANGE_PIN_BLOCK_RQ);
		APACS_2_IFX_TYPE.put("F5", IfxType.CHANGE_PIN_BLOCK_RS);

		// Pin2 Change
		APACS_2_IFX_TYPE.put("F6", IfxType.CHANGE_PIN_BLOCK_RQ);
		APACS_2_IFX_TYPE.put("F7", IfxType.CHANGE_PIN_BLOCK_RS);

		// Reference Payment
		APACS_2_IFX_TYPE.put("F8", IfxType.PREPARE_ONLINE_BILLPAYMENT);
		APACS_2_IFX_TYPE.put("F9", IfxType.ONLINE_BILLPAYMENT_RQ);
		APACS_2_IFX_TYPE.put("FA", IfxType.ONLINE_BILLPAYMENT_RS);
		APACS_2_IFX_TYPE.put("FB", IfxType.ONLINE_BILLPAYMENT_TRACKING);

		// Credit (Mizan)
		APACS_2_IFX_TYPE.put("FC", IfxType.CREDIT_PURCHASE_RQ);
		APACS_2_IFX_TYPE.put("FD", IfxType.CREDIT_PURCHASE_RS);
		APACS_2_IFX_TYPE.put("FE", IfxType.CREDIT_BAL_INQ_RQ);
		APACS_2_IFX_TYPE.put("FF", IfxType.CREDIT_BAL_INQ_RS);
		
		//ThirdParty Purchase
		APACS_2_IFX_TYPE.put("31", IfxType.THIRD_PARTY_PURCHASE_RQ);
		APACS_2_IFX_TYPE.put("32", IfxType.THIRD_PARTY_PURCHASE_RS);
		
		// Transfer to Account
		APACS_2_IFX_TYPE.put("E6", IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		APACS_2_IFX_TYPE.put("E7", IfxType.TRANSFER_CARD_TO_ACCOUNT_RS);
		
		//Transfer to Account Authentication
		APACS_2_IFX_TYPE.put("E8", IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ);
		APACS_2_IFX_TYPE.put("E9", IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS);

		APACS_2_IFX_TYPE.put("A2", IfxType.BANK_STATEMENT_RQ);
		APACS_2_IFX_TYPE.put("A3", IfxType.BANK_STATEMENT_RS);
		
		//ghasedak
		APACS_2_IFX_TYPE.put("A0", IfxType.GHASEDAK_RQ);
		APACS_2_IFX_TYPE.put("A1", IfxType.GHASEDAK_RS);
	}

	static Map<IfxType, String> IFX_TYPE_2_APACS = new HashMap<IfxType, String>();
	static {
		IFX_TYPE_2_APACS.put(IfxType.PURCHASE_RQ, "11");
		IFX_TYPE_2_APACS.put(IfxType.PURCHASE_RS, "13");

		IFX_TYPE_2_APACS.put(IfxType.RETURN_RQ, "40");
		IFX_TYPE_2_APACS.put(IfxType.RETURN_RS, "41");

		IFX_TYPE_2_APACS.put(IfxType.BAL_INQ_RQ, "70");
		IFX_TYPE_2_APACS.put(IfxType.BAL_INQ_RS, "71");

		IFX_TYPE_2_APACS.put(IfxType.PURCHASE_CHARGE_RQ, "D0");
		IFX_TYPE_2_APACS.put(IfxType.PURCHASE_CHARGE_RS, "D1");

		IFX_TYPE_2_APACS.put(IfxType.BILL_PMT_RQ, "E0");
		IFX_TYPE_2_APACS.put(IfxType.BILL_PMT_RS, "E1");
		IFX_TYPE_2_APACS.put(IfxType.SADERAT_AUTHORIZATION_BILL_PMT_RQ, "E2");
		IFX_TYPE_2_APACS.put(IfxType.SADERAT_AUTHORIZATION_BILL_PMT_RS, "E3");
		IFX_TYPE_2_APACS.put(IfxType.SADERAT_BILL_PMT_RQ, "E4");
		IFX_TYPE_2_APACS.put(IfxType.SADERAT_BILL_PMT_RS, "E5");

		IFX_TYPE_2_APACS.put(IfxType.ACQUIRER_REC_RQ, "60");
		IFX_TYPE_2_APACS.put(IfxType.ACQUIRER_REC_RS, "62");

		IFX_TYPE_2_APACS.put(IfxType.LOG_ON_RQ, "D5");
		IFX_TYPE_2_APACS.put(IfxType.LOG_ON_RS, "D6");

		IFX_TYPE_2_APACS.put(IfxType.RESET_PASSWORD_RQ, "D9");
		IFX_TYPE_2_APACS.put(IfxType.RESET_PASSWORD_RS, "DA");

		// Fund Transfer
		IFX_TYPE_2_APACS.put(IfxType.TRANSFER_RQ, "F0");
		IFX_TYPE_2_APACS.put(IfxType.TRANSFER_RS, "F1");

		// Fund Transfer Authentication
		IFX_TYPE_2_APACS.put(IfxType.TRANSFER_CHECK_ACCOUNT_RQ, "F2");
		IFX_TYPE_2_APACS.put(IfxType.TRANSFER_CHECK_ACCOUNT_RS, "F3");

		// Pin1 Change
		IFX_TYPE_2_APACS.put(IfxType.CHANGE_PIN_BLOCK_RQ, "F4");
		IFX_TYPE_2_APACS.put(IfxType.CHANGE_PIN_BLOCK_RS, "F5");

		// Pin2 Change
		IFX_TYPE_2_APACS.put(IfxType.CHANGE_PIN_BLOCK_RQ, "F6");
		IFX_TYPE_2_APACS.put(IfxType.CHANGE_PIN_BLOCK_RQ, "F7");

		// Reference Payment
		IFX_TYPE_2_APACS.put(IfxType.PREPARE_ONLINE_BILLPAYMENT, "F8");
		IFX_TYPE_2_APACS.put(IfxType.ONLINE_BILLPAYMENT_RQ, "F9");
		IFX_TYPE_2_APACS.put(IfxType.ONLINE_BILLPAYMENT_RS, "FA");
		IFX_TYPE_2_APACS.put(IfxType.ONLINE_BILLPAYMENT_TRACKING, "FB");

		// Credit (Mizan)
		IFX_TYPE_2_APACS.put(IfxType.CREDIT_PURCHASE_RQ, "FC");
		IFX_TYPE_2_APACS.put(IfxType.CREDIT_PURCHASE_RS, "FD");
		IFX_TYPE_2_APACS.put(IfxType.CREDIT_BAL_INQ_RQ, "FE");
		IFX_TYPE_2_APACS.put(IfxType.CREDIT_BAL_INQ_RS, "FF");
		
		//ThirdParty Purchase
		IFX_TYPE_2_APACS.put(IfxType.THIRD_PARTY_PURCHASE_RQ, "31");
		IFX_TYPE_2_APACS.put(IfxType.THIRD_PARTY_PURCHASE_RS, "32");
		
		//Transfer to Account
		IFX_TYPE_2_APACS.put(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ, "E6");
		IFX_TYPE_2_APACS.put(IfxType.TRANSFER_CARD_TO_ACCOUNT_RS, "E7");
		
		//Transfer to Account Authentication
		IFX_TYPE_2_APACS.put(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ, "E8");
		IFX_TYPE_2_APACS.put(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS, "E9");

		IFX_TYPE_2_APACS.put(IfxType.BANK_STATEMENT_RQ, "A2");
		IFX_TYPE_2_APACS.put(IfxType.BANK_STATEMENT_RS, "A3");
		
		//ghasedak
		IFX_TYPE_2_APACS.put(IfxType.GHASEDAK_RQ, "A0");
		IFX_TYPE_2_APACS.put(IfxType.GHASEDAK_RS, "A1");
		
	}

	static Map<String, String> APACS_RQ_2_RS = new HashMap<String, String>();
	static {
		APACS_RQ_2_RS.put("11", "13");
		APACS_RQ_2_RS.put("40", "41");
		APACS_RQ_2_RS.put("70", "71");
		APACS_RQ_2_RS.put("D0", "D1");
		APACS_RQ_2_RS.put("E0", "E1");
		APACS_RQ_2_RS.put("E2", "E3");
		APACS_RQ_2_RS.put("E4", "E5");
		APACS_RQ_2_RS.put("60", "62");
		APACS_RQ_2_RS.put("D5", "D6");
		APACS_RQ_2_RS.put("D9", "DA");
		APACS_RQ_2_RS.put("F0", "F1");
		APACS_RQ_2_RS.put("F2", "F3");
		APACS_RQ_2_RS.put("F4", "F5");
		APACS_RQ_2_RS.put("F6", "F7");
		APACS_RQ_2_RS.put("F8", "F8");
		APACS_RQ_2_RS.put("F9", "FA");
		APACS_RQ_2_RS.put("FB", "FB");
		APACS_RQ_2_RS.put("FC", "FD");
		APACS_RQ_2_RS.put("FE", "FF");
		APACS_RQ_2_RS.put("31", "32");
		APACS_RQ_2_RS.put("E6", "E7");
		APACS_RQ_2_RS.put("E8", "E9");
		APACS_RQ_2_RS.put("A2", "A3");
		APACS_RQ_2_RS.put("A0", "A1");	//ghasedak
	}

	public static String toApacsType(IfxType ifxType, TrnType trnType) {
		if(TrnType.CHANGEINTERNETPINBLOCK.equals(trnType))
			return "F7";
		if(TrnType.CHANGEPINBLOCK.equals(trnType))
			return "F5";
		return IFX_TYPE_2_APACS.get(ifxType);
	}

	public static TrnType toTrnType(String code) {
		return APACS_2_TRN_TYPE.get(code);
	}

	public static IfxType toIfxType(String code) {
		return APACS_2_IFX_TYPE.get(code);
	}
	
	public static String getRqType(String code) {
		if (FIN_REQ_LIST.contains(code))
			return FIN_REQ;
		else if (CONF_LIST.contains(code))
			return CONF;
		
		else if (NET_REQ_LIST.contains(code))
			return NET_REQ;

		else if (RECON_REQ_LIST.contains(code))
			return RECON_REQ;
		
		//ghasedak
		else if(INFO_REQ_LIST.contains(code))
			return INFO_REQ;
		
		return "UNKNOWN";
	}

	public static String getRsType(String code) {
		if (FIN_RES_LIST.contains(code))
			return FIN_RES;

		else if (CONF_LIST.contains(code))
			return CONF;

		else if (NET_RES_LIST.contains(code))
			return NET_RES;

		else if (RECON_RES_LIST.contains(code))
			return RECON_RES;
		
		//ghasedak
		else if(INFO_RES_LIST.contains(code))
			return INFO_RES;

		return "UNKNOWN";
	}

	public static String toRs(String rqCode) {
		return APACS_RQ_2_RS.get(rqCode);
	}

/*	public static boolean isFinancialMsg(Apacs70Msg apacsMsg) {
		if (FIN_REQ_LIST.contains(apacsMsg.messageType) || FIN_RES_LIST.contains(apacsMsg.messageType)
				|| CONF_LIST.contains(apacsMsg.messageType))
			return true;
		return false;
	}

	public static boolean isRequestMsg(Apacs70Msg apacsMsg) {
		if (FIN_REQ_LIST.contains(apacsMsg.messageType) || RECON_REQ_LIST.contains(apacsMsg.messageType) ||
				NET_REQ_LIST.contains(apacsMsg.messageType))
			return true;
		return false;
	}*/

	public static String toString(String msgCode) {
		int code = Integer.parseInt(msgCode, 16);
		switch(code) {
			case 0x11:
				return "PURCHASE_RQ";
			case 0x13:
				return "PURCHASE_RS";

			case 0x40:
				return "RETURN_RQ";
			case 0x41:
				return "RETURN_RS";

			case 0x70:
				return "BALANCE_INQUIRY_RQ";
			case 0x71:
				return "BALANCE_INQUIRY_RS";

			case 0xD0:
				return "E_VOUCHER_RQ";
			case 0xD1:
				return "E_VOUCHER_RS";

			case 0xE0:
				return "BILL_PAYMENT_RQ";
			case 0xE1:
				return "BILL_PAYMENT_RS";
			case 0xE2:
				return "SADERAT_AUTHORIZATION_BILL_PMT_RQ";
			case 0xE3:
				return "SADERAT_AUTHORIZATION_BILL_PMT_RS";
			case 0xE4:
				return "SADERAT_BILL_PMT_RQ";
			case 0xE5:
				return "SADERAT_BILL_PMT_RS";

			case 0x60:
				return "ACQUIRER_REC_RQ";
			case 0x62:
				return "ACQUIRER_REC_RS";

			case 0xD5:
				return "LOG_ON_RQ";
			case 0xD6:
				return "LOG_ON_RS";

			case 0xD9:
				return "RESET_PASSWORD_RQ";
			case 0xDA:
				return "RESET_PASSWORD_RS";

			case 0xF0:
				return "FUND_TRANSFER_RQ";
			case 0xF1:
				return "FUND_TRANSFER_RS";

			case 0xF2:
				return "FUND_TRANSFER_AUTHENTICATION_RQ";
			case 0xF3:
				return "FUND_TRANSFER_AUTHENTICATION_RS";

			case 0xF4:
				return "PIN1_CHANGE_RQ";
			case 0xF5:
				return "PIN1_CHANGE_RS";

			case 0xF6:
				return "PIN2_CHANGE_RQ";
			case 0xF7:
				return "PIN2_CHANGE_RS";
				
			case 0xF8:
				return "PREPARE_ONLINE_BILLPAYMENT";
			case 0xF9:
				return "ONLINE_BILLPAYMENT_RQ";
			case 0xFA:
				return "ONLINE_BILLPAYMENT_RS";
			case 0xFB:
				return "ONLINE_BILLPAYMENT_TRACKING";

			case 0xFC:
				return "CREDIT_PURCHASE_RQ";
			case 0xFD:
				return "CREDIT_PURCHASE_RS";
			case 0xFE:
				return "CREDIT_BAL_INQ_RQ";
			case 0xFF:
				return "CREDIT_BAL_INQ_RS";

			case 0x82:
				return "CONFIRMATION";
				
			case 0x31:
				return "THIRD_PARTY_PURCHASE_RQ";
			case 0x32:
				return "THIRD_PARTY_PURCHASE_RS";
				
			case 0xE6:
				return "TRANSFER_CARD_TO_ACCOUNT_RQ";
			case 0xE7:
				return "TRANSFER_CARD_TO_ACCOUNT_RS";
				
			case 0xE8:
				return "TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ";
			case 0xE9:
				return "TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS";

			case 0xA2:
				return "BANK_STATEMENT_RQ";				
			case 0xA3:
				return "BANK_STATEMENT_RS";	
				
				//ghasedak	
			case 0xA0:
				return "GHASEDAK_RQ";
			case 0xA1:
				return "GHASEDAK_RS";
		}
		return "?";
	}
}
