package vaulsys.protocols.ifx.enums;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Embeddable
public class TrnType implements IEnum, Cloneable {
	private static final int UNKNOWN_VALUE = -1;
	private static final int DEBIT_VALUE = 0;
	private static final int CREDIT_VALUE = 1;
	private static final int WITHDRAWAL_VALUE = 2;
	private static final int RETURN_VALUE = 3;
	private static final int DEPOSIT_VALUE = 5;
	
	private static final int CHECKACCOUNT_VALUE = 4;
	
//	private static final int NONFINANCIAL_INFO_VALUE = 6;
	
	private static final int TRANSFER_VALUE = 40;
	private static final int INCREMENTALTRANSFER_VALUE = 47;
	private static final int DECREMENTALTRANSFER_VALUE = 46;
	
	private static final int PAYMENT_VALUE = 9;
	private static final int PURCHASE_VALUE = 10; // FANAP Specific
	private static final int BILLPAYMENT_VALUE = 11; // SHETAB Specific
	private static final int BALANCEINQUIRY_VALUE = 13; // ISO Specific
	private static final int RECONCILIATION_VALUE = 14;
	private static final int NETWORKMANAGEMENT_VALUE = 15;
	private static final int PREPARE_BILL_PMT_VALUE = 16;
	private static final int GETACCOUNT_VALUE = 17;
	private static final int CANCEL_VALUE = 18;
	private static final int PURCHASE_CHARGE_VALUE = 19;
	private static final int CHANGE_PIN_BLOCK_VALUE = 20;
	private static final int CHANGE_INTERNET_PIN_BLOCK_VALUE = 21;
	private static final int PAYMENT_STATEMENT_VALUE = 22;
	
	private static final int CHECKACCOUNT_CARD_TO_ACCOUNT_VALUE = 23;
	private static final int TRANSFER_CARD_TO_ACCOUNT_VALUE = 24;
	private static final int INCREMENTALTRANSFER_CARD_TO_ACCOUNT_VALUE = 25;
	private static final int DECREMENTALTRANSFER_CARD_TO_ACCOUNT_VALUE = 26;
	
	private static final int BANK_STATEMENT_VALUE = 27;
	private static final int LAST_PURCHASE_CHARGE_VALUE = 28;
	
	private static final int CREDIT_CARD_DATA_VALUE = 29;
	private static final int DEPOSIT_CHECK_ACCOUNT_VALUE = 30;
	private static final int CONFIRMATION_VALUE = 31;

	private static final int PURCHASE_TOPUP_VALUE = 32;
	
	private static final int ONLINE_BILLPAYMENT_VALUE = 33;
	private static final int PREPARE_ONLINE_BILLPAYMENT_VALUE = 34;
	
	private static final int SADERAT_AUTH_BILLPAYMENT_VALUE = 35;
	private static final int SADERAT_BILLPAYMENT_VALUE = 36;
	
	private static final int THIRD_PARTY_PAYMENT_VALUE = 37;
	
	private static final int PREPARE_THIRD_PARTY_PAYMENT_VALUE =38;
		
	private static final int SHEBAINQUIRY_VALUE =50;
	
	private static final int NONFINANCIAL_INFO_VALUE =49;

	private static final int HOTCARD_VALUE =51;
	
	private static final int PREPARE_TRANSFER_CARD_TO_ACCOUNT_VALUE = 52;
	
	//TASK Task081 : ATM Saham Feature
	private static final int STOCK_VALUE = 53;	
	
	// TASK Task129 [26604] - Authenticate Cart (Pasargad)
	private static final int CARD_AUTHENTICATE_VALUE = 56;

	//Mirkamali(Task175): Restriction
	private static final int PREPARE_RESTRICTION_VALUE = 60;
	private static final int RESTRICTION_VALUE = 61;
	
	//Mirkamali(Task179): Currency ATM
	private static final int PREPARE_WITHDRAWAL_VALUE = 62;
	private static final int WITHDRAWAL_CUR_VALUE = 63;

	//m.rehman: for Purchase Pre-auth and Refund
	private static final int PREAUTH_VALUE = 64;
	private static final int REFUND_VALUE = 65;
	private static final int DIRECT_DEBIT_VALUE = 66;
	private static final int MONEY_SEND_VALUE = 67;
	private static final int IBFT_VALUE = 68;
	private static final int TITLE_FETCH_VALUE = 69;
	private static final int ORIGINAL_CREDIT_VALUE = 70;

	//m.rehman: for Loro
	private static final int WITHDRAWAL_LORO_VALUE = 71;
	private static final int PURCHASE_LORO_VALUE = 72;

	private static final int WALLET_TOPUP_VALUE = 73;
	private static final int CVV_GENERATION_VALUE = 74;
	//m.rehman: for void transaction from NAC
	private static final int VOID_VALUE = 75;
	//Raza adding for NayaPay start
	private static final int CREATEWALLETLEVEL_ZERO_VALUE = 76;
	private static final int CREATEWALLETLEVEL_ONE_VALUE = 77;
	private static final int CREATEWALLETLEVEL_TWO_VALUE = 78;
	private static final int UPDATEUSERPROFILE_VALUE = 79;
	private static final int LINKBANKACCOUNT_VALUE = 80;
	private static final int UNLINKBANKACCOUNT_VALUE = 81;
	private static final int CONFIRMOTP_VALUE = 82;
	private static final int REQUESTDEBITCARDTXN_VALUE = 83;
	private static final int REQUESTDEBITCARD_VALUE = 84;
	private static final int ACTIVATEDEBITCARD_VALUE = 85;
	private static final int ENABLEDEBITCARD_VALUE = 86;
	private static final int LOADWALLET_VALUE = 87; //Raza segregating from IBFT for TXNRULE
	private static final int WALLETTXN_VALUE = 88; //Raza segregating from IBFT for TXNRULE
	private static final int WALLETBALANCE_VALUE = 89; //Raza segregating from BALANCE_INQUIRY for TXNRULE
	private static final int UNLOADWALLET_VALUE = 90; //Raza segregating from IBFT for TXNRULE
	private static final int MERCHANTCORETXN_VALUE = 91; //Raza segregating from IBFT for TXNRULE
	private static final int MERCHANTTXN_VALUE = 92; //Raza segregating from IBFT for TXNRULE
	private static final int ATMTXNLOG_VALUE = 93; //Raza segregating from IBFT for TXNRULE
	private static final int GETDEBITCARDSTATUS_VALUE = 94; //Raza segregating from IBFT for TXNRULE
	private static final int GETWALLETSTATUS_VALUE = 95; //Raza segregating from IBFT for TXNRULE
	private static final int CONFIRMFRAUDOTP_VALUE = 96; //Raza segregating from IBFT for TXNRULE
	//Raza adding for NayaPay end
	
	public static final TrnType UNKNOWN = new TrnType(UNKNOWN_VALUE);
	public static final TrnType DEBIT = new TrnType(DEBIT_VALUE);
	public static final TrnType CREDIT = new TrnType(CREDIT_VALUE);
	public static final TrnType WITHDRAWAL = new TrnType(WITHDRAWAL_VALUE);
	public static final TrnType RETURN = new TrnType(RETURN_VALUE);
	public static final TrnType CHECKACCOUNT = new TrnType(CHECKACCOUNT_VALUE);
	public static final TrnType DEPOSIT = new TrnType(DEPOSIT_VALUE);
	public static final TrnType TRANSFER = new TrnType(TRANSFER_VALUE);
	public static final TrnType INCREMENTALTRANSFER = new TrnType(INCREMENTALTRANSFER_VALUE);
	public static final TrnType DECREMENTALTRANSFER = new TrnType(DECREMENTALTRANSFER_VALUE);
	public static final TrnType PAYMENT = new TrnType(PAYMENT_VALUE);
	public static final TrnType RECONCILIATION = new TrnType(RECONCILIATION_VALUE);
	public static final TrnType NETWORKMANAGEMENT = new TrnType(NETWORKMANAGEMENT_VALUE);
	public static final TrnType PREPARE_BILL_PMT = new TrnType(PREPARE_BILL_PMT_VALUE);
	public static final TrnType GETACCOUNT = new TrnType(GETACCOUNT_VALUE);
	public static final TrnType CANCEL = new TrnType(CANCEL_VALUE);
	public static final TrnType PURCHASECHARGE = new TrnType(PURCHASE_CHARGE_VALUE);
	public static final TrnType LASTPURCHASECHARGE = new TrnType(LAST_PURCHASE_CHARGE_VALUE);
	public static final TrnType CHANGEPINBLOCK = new TrnType(CHANGE_PIN_BLOCK_VALUE);
	public static final TrnType CHANGEINTERNETPINBLOCK = new TrnType(CHANGE_INTERNET_PIN_BLOCK_VALUE);
	public static final TrnType PAYMENTSTATEMENT = new TrnType(PAYMENT_STATEMENT_VALUE);
	
	public static final TrnType CHECKACCOUNT_CARD_TO_ACCOUNT = new TrnType(CHECKACCOUNT_CARD_TO_ACCOUNT_VALUE);
	
	public static final TrnType TRANSFER_CARD_TO_ACCOUNT = new TrnType(TRANSFER_CARD_TO_ACCOUNT_VALUE);
	public static final TrnType INCREMENTALTRANSFER_CARD_TO_ACCOUNT = new TrnType(INCREMENTALTRANSFER_CARD_TO_ACCOUNT_VALUE);
	public static final TrnType DECREMENTALTRANSFER_CARD_TO_ACCOUNT = new TrnType(DECREMENTALTRANSFER_CARD_TO_ACCOUNT_VALUE);
	
	public static final TrnType BANKSTATEMENT = new TrnType(BANK_STATEMENT_VALUE);
	
	public static final TrnType CREDITCARDDATA = new TrnType(CREDIT_CARD_DATA_VALUE);
	public static final TrnType DEPOSIT_CHECK_ACCOUNT = new TrnType(DEPOSIT_CHECK_ACCOUNT_VALUE);
	
	// FANAP Specific
	public static final TrnType PURCHASE = new TrnType(PURCHASE_VALUE);
	
	// SHETAB Specific
	public static final TrnType BILLPAYMENT = new TrnType(BILLPAYMENT_VALUE);
	
	// NEGIN Specific
//	public static final TrnType BILLPAYMENT_NEGIN = new TrnType(BILLPAYMENT_NEGIN_VALUE);
	
	//Saderat Specific
	public static final TrnType SADERAT_AUTH_BILLPAYMENT =  new TrnType(SADERAT_AUTH_BILLPAYMENT_VALUE);
	public static final TrnType SADERAT_BILLPAYMENT =  new TrnType(SADERAT_BILLPAYMENT_VALUE);
	
	// ISO Specific
	public static final TrnType BALANCEINQUIRY = new TrnType(BALANCEINQUIRY_VALUE);
	
	public static final TrnType CONFIRMATION = new TrnType(CONFIRMATION_VALUE);

	public static final TrnType PURCHASETOPUP = new TrnType(PURCHASE_TOPUP_VALUE);
	
	public static final TrnType ONLINE_BILLPAYMENT =  new TrnType(ONLINE_BILLPAYMENT_VALUE);
	public static final TrnType PREPARE_ONLINE_BILLPAYMENT = new TrnType(PREPARE_ONLINE_BILLPAYMENT_VALUE);
	
	public static final TrnType THIRD_PARTY_PAYMENT = new TrnType(THIRD_PARTY_PAYMENT_VALUE);
	
	public static final TrnType PREPARE_THIRD_PARTY_PAYMENT =  new TrnType(PREPARE_THIRD_PARTY_PAYMENT_VALUE);

	public static final TrnType SHEBAINQUIRY = new TrnType(SHEBAINQUIRY_VALUE);
	
	public static final TrnType NONFINANCIAL_INFO = new TrnType(NONFINANCIAL_INFO_VALUE);

	public static final TrnType HOTCARD = new TrnType(HOTCARD_VALUE);
	
	public static final TrnType PREPARE_TRANSFER_CARD_TO_ACCOUNT = new TrnType(PREPARE_TRANSFER_CARD_TO_ACCOUNT_VALUE);
	
	//TASK Task081 : ATM Sheba feature
	public static final TrnType STOCK = new TrnType(STOCK_VALUE);
	
	// TASK Task129 [26604] - Authenticate Cart (Pasargad)
	public static final TrnType CARD_AUTENTICATE = new TrnType(CARD_AUTHENTICATE_VALUE);
	
	//Mirkamali(Task175): Restriction
	public static final TrnType PREPARE_RESTRICTION = new TrnType(PREPARE_RESTRICTION_VALUE);
	public static final TrnType RESTRICTION = new TrnType(RESTRICTION_VALUE);
	
	//Mirkamali(Task179)
	public static final TrnType PREPARE_WITHDRAWAL = new TrnType(PREPARE_WITHDRAWAL_VALUE);
	public static final TrnType WITHDRAWAL_CUR = new TrnType(WITHDRAWAL_CUR_VALUE);
	//m.rehman
	public static final TrnType PREAUTH = new TrnType(PREAUTH_VALUE);
	public static final TrnType REFUND = new TrnType(REFUND_VALUE);
	public static final TrnType DIRECT_DEBIT = new TrnType(DIRECT_DEBIT_VALUE);
	public static final TrnType MONEY_SEND = new TrnType(MONEY_SEND_VALUE);
	public static final TrnType IBFT = new TrnType(IBFT_VALUE);
	public static final TrnType TITLE_FETCH = new TrnType(TITLE_FETCH_VALUE);
	public static final TrnType ORIGINAL_CREDIT = new TrnType(ORIGINAL_CREDIT_VALUE);

	//m.rehman: for Loro
	public static final TrnType WITHDRAWAL_LORO = new TrnType(WITHDRAWAL_LORO_VALUE);
	public static final TrnType PURCHASE_LORO = new TrnType(PURCHASE_LORO_VALUE);

	public static final TrnType WALLET_TOPUP = new TrnType(WALLET_TOPUP_VALUE);
	public static final TrnType CVV_GENERATION = new TrnType(CVV_GENERATION_VALUE);
	//m.rehman: for void transaction from NAC
	public static final TrnType VOID = new TrnType(VOID_VALUE);
	//Raza NayaPay start
	public static final TrnType CREATEWALLETLEVEL_ZERO = new TrnType(CREATEWALLETLEVEL_ZERO_VALUE);
	public static final TrnType CREATEWALLETLEVEL_ONE = new TrnType(CREATEWALLETLEVEL_ONE_VALUE);
	public static final TrnType CREATEWALLETLEVEL_TWO = new TrnType(CREATEWALLETLEVEL_TWO_VALUE);
	public static final TrnType UPDATEUSERPROFILE = new TrnType(UPDATEUSERPROFILE_VALUE);
	public static final TrnType LINKBANKACCOUNT = new TrnType(LINKBANKACCOUNT_VALUE);
	public static final TrnType UNLINKBANKACCOUNT = new TrnType(UNLINKBANKACCOUNT_VALUE);
	public static final TrnType CONFIRMOTP = new TrnType(CONFIRMOTP_VALUE);
	public static final TrnType REQUESTDEBITCARDTXN = new TrnType(REQUESTDEBITCARDTXN_VALUE);
	public static final TrnType REQUESTDEBITCARD = new TrnType(REQUESTDEBITCARD_VALUE);
	public static final TrnType ACTIVATEDEBITCARD = new TrnType(ACTIVATEDEBITCARD_VALUE);
	public static final TrnType ENABLEDEBITCARD = new TrnType(ENABLEDEBITCARD_VALUE);
	public static final TrnType LOADWALLET = new TrnType(LOADWALLET_VALUE);
	public static final TrnType WALLETTXN = new TrnType(WALLETTXN_VALUE);
	public static final TrnType WALLETBALANCE = new TrnType(WALLETBALANCE_VALUE);
	public static final TrnType UNLOADWALLET = new TrnType(UNLOADWALLET_VALUE);
	public static final TrnType MERCHANTCORETXN = new TrnType(MERCHANTCORETXN_VALUE);
	public static final TrnType MERCHANTTXN = new TrnType(MERCHANTTXN_VALUE);
	public static final TrnType ATMTXNLOG = new TrnType(ATMTXNLOG_VALUE);
	public static final TrnType GETDEBITCARDSTATUS = new TrnType(GETDEBITCARDSTATUS_VALUE);
	public static final TrnType GETWALLETSTATUS = new TrnType(GETWALLETSTATUS_VALUE);
	public static final TrnType CONFIRMFRAUDOTP = new TrnType(CONFIRMFRAUDOTP_VALUE);
	//Raza NayaPay end

	public static final Map<Integer, String> valueToNameMap = new HashMap<Integer, String>();
	
	static{
		Field[] list = TrnType.class.getFields();
		Method getType = null;
		try {
			getType = TrnType.class.getMethod("getType");
		} catch (SecurityException e2) {
			e2.printStackTrace();
		} catch (NoSuchMethodException e2) {
			e2.printStackTrace();
		}
		for (Field e : list) {
			String name = e.getName().toUpperCase();
			try {
				if (e.getName().equalsIgnoreCase("VALUETONAMEMAP"))
					continue;
				
				Integer value = (Integer) getType.invoke(e.get(null), (Object[])null);
				valueToNameMap.put(value, name);
			} catch (Exception ex) {
				// TODO: handle exception
			}
		
		}
	}
	
	
	
	
	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public TrnType(int type) {
		super();
		this.type = type;
	}

	public TrnType() {
		super();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || !(obj instanceof TrnType))
			return false;
		TrnType that = (TrnType) obj;
		return type == that.type;
	}

	@Override
	public int hashCode() {
		return type;
	}

	@Override
	protected Object clone() {
		return new TrnType(this.type); 
	}
	
	public TrnType copy() {
		return (TrnType) clone();
	}
	
	@Override
	public String toString() {
		return valueToNameMap.get(this.type);
////		return valueToNameMap.get(this.type);
//		switch (type) {
//		case BALANCEINQUIRY_VALUE:
//			return "BALANCE";
//			
//		case BILLPAYMENT_VALUE:
//			return "BILLPAYMENT";
//			
////		case BILLPAYMENT_NEGIN_VALUE:
////			return "BILLPAYMENTNEGIN";
//			
//		case PURCHASE_VALUE:
//			return "PURCHASE";
//			
//		case RETURN_VALUE:
//			return "RETURN";
//			
//		case TRANSFER_VALUE:
//			return "TRANSFER";
//		case INCREMENTALTRANSFER_VALUE:
//			return "INCREMENTALTRANSFER";
//		case DECREMENTALTRANSFER_VALUE:
//			return "DECREMENTALTRANSFER";
//		default:
//			return "UNKNOWN";
//		}
		
	}
	
	public Boolean isDebitTrnType(){
		return TrnType.DEBIT.equals(this) ||
		TrnType.WITHDRAWAL.equals(this) ||
		TrnType.PURCHASE.equals(this) ||
		TrnType.PAYMENT.equals(this) ||
		TrnType.BALANCEINQUIRY.equals(this)||
		TrnType.BILLPAYMENT.equals(this) ||
		TrnType.DECREMENTALTRANSFER.equals(this) ||
		TrnType.DECREMENTALTRANSFER_CARD_TO_ACCOUNT.equals(this) ||
		TrnType.PURCHASECHARGE.equals(this)||
		TrnType.PURCHASETOPUP.equals(this)||
		TrnType.BANKSTATEMENT.equals(this) ||
		TrnType.TRANSFER.equals(this) ;
	}
	
	public static List<IfxType> getIfxType(TrnType trnType) {
		List<IfxType> result = new ArrayList<IfxType>();
		
		if (TrnType.BALANCEINQUIRY.equals(trnType)) {
			result.add(IfxType.BAL_INQ_RQ);
			result.add(IfxType.BAL_INQ_RS);
			result.add(IfxType.BAL_REV_REPEAT_RQ);
			result.add(IfxType.BAL_REV_REPEAT_RS);
			
			return result;
		}
		
		if (TrnType.BANKSTATEMENT.equals(trnType)) {
			result.add(IfxType.BANK_STATEMENT_RQ);
			result.add(IfxType.BANK_STATEMENT_RS);
			result.add(IfxType.BANK_STATEMENT_REV_REPEAT_RQ);
			result.add(IfxType.BANK_STATEMENT_REV_REPEAT_RS);
			
			return result;
		}
		
		if (TrnType.BILLPAYMENT.equals(trnType)) {
			result.add(IfxType.BILL_PMT_RQ);
			result.add(IfxType.BILL_PMT_RS);
			result.add(IfxType.BILL_PMT_REV_REPEAT_RQ);
			result.add(IfxType.BILL_PMT_REV_REPEAT_RS);
			
			return result;
		}
		
		if(TrnType.CANCEL.equals(trnType)){
			result.add(IfxType.CANCEL);
			
			return result;
		}
		
		if (TrnType.CHANGEINTERNETPINBLOCK.equals(trnType)) {
			result.add(IfxType.CHANGE_PIN_BLOCK_RQ);
			result.add(IfxType.CHANGE_PIN_BLOCK_RS);
			result.add(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
			result.add(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS);
			
			return result;
		}
		
		if (TrnType.CHANGEPINBLOCK.equals(trnType)) {
			result.add(IfxType.CHANGE_PIN_BLOCK_RQ);
			result.add(IfxType.CHANGE_PIN_BLOCK_RS);
			result.add(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
			result.add(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS);
			
			return result;
		}
		
		if (TrnType.CHECKACCOUNT.equals(trnType)) {
			result.add(IfxType.TRANSFER_CHECK_ACCOUNT_RQ);
			result.add(IfxType.TRANSFER_CHECK_ACCOUNT_RS);
			result.add(IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ);
			result.add(IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RS);
			
			return result;
		}
		
		if (TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT.equals(trnType)) {
			result.add(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ);
			result.add(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS);
			result.add(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ);
			result.add(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RS);
			
			return result;
		}
		
		if (TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT.equals(trnType)){
			result.add(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP);
			result.add(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_REV_REPEAT);
			result.add(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT);
			result.add(IfxType.PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT);
			return result;
		}
		
		//******
		if (TrnType.CONFIRMATION.equals(trnType)) {
			result.add(IfxType.TRANSFER_CHECK_ACCOUNT_RQ);
			result.add(IfxType.TRANSFER_CHECK_ACCOUNT_RS);
			result.add(IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ);
			result.add(IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RS);
			
			return result;
		}
		//credit va creditcard data va debit ro nazashtam 
		if(TrnType.DECREMENTALTRANSFER.equals(trnType)){
			result.add(IfxType.TRANSFER_FROM_ACCOUNT_RQ);
			result.add(IfxType.TRANSFER_FROM_ACCOUNT_RS);
			result.add(IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ);
			result.add(IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS);
			
			return result;
		}
		
		if(TrnType.DEPOSIT.equals(trnType)){
			result.add(IfxType.DEPOSIT_RQ);
			result.add(IfxType.DEPOSIT_RS);
			result.add(IfxType.DEPOSIT_REV_REPEAT_RQ);
			result.add(IfxType.DEPOSIT_REV_REPEAT_RS);
			
			return result;
		}
		
		if(TrnType.DEPOSIT_CHECK_ACCOUNT.equals(trnType)){
			result.add(IfxType.DEPOSIT_CHECK_ACCOUNT_RQ);
			result.add(IfxType.DEPOSIT_CHECK_ACCOUNT_RS);
			
			return result;
		}
		
		if(TrnType.GETACCOUNT.equals(trnType)){
			result.add(IfxType.GET_ACCOUNT_RQ);
			result.add(IfxType.GET_ACCOUNT_RS);
			result.add(IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
			result.add(IfxType.GET_ACCOUNT_REV_REPEAT_RS);
			
			return result;
		}
				
		if(TrnType.INCREMENTALTRANSFER.equals(trnType)){
			result.add(IfxType.TRANSFER_TO_ACCOUNT_RQ);
			result.add(IfxType.TRANSFER_TO_ACCOUNT_RS);
			result.add(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ);
			result.add(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS);
			
			return result;
		}
		if(TrnType.INCREMENTALTRANSFER_CARD_TO_ACCOUNT.equals(trnType)){
			result.add(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ);
			result.add(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS);
			result.add(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ);
			result.add(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS);
			return result;
		}
		
		if(TrnType.LASTPURCHASECHARGE.equals(trnType)){
			result.add(IfxType.LAST_PURCHASE_CHARGE_RQ);
			result.add(IfxType.LAST_PURCHASE_CHARGE_RS);
			
			return result;
		}
		
		if(TrnType.NETWORKMANAGEMENT.equals(trnType)){
			result.add(IfxType.MAC_REJECT);
			result.add(IfxType.NETWORK_MGR_REPEAT_RQ);
			result.add(IfxType.NETWORK_MGR_REPEAT_RS);
			result.add(IfxType.NETWORK_MGR_RQ);
			result.add(IfxType.NETWORK_MGR_RS);
			result.add(IfxType.CUTOVER_RQ);
			result.add(IfxType.CUTOVER_RS);
			result.add(IfxType.CUTOVER_REPEAT_RQ);
			result.add(IfxType.CUTOVER_REPEAT_RS);
			result.add(IfxType.LOG_ON_RQ);
			result.add(IfxType.LOG_ON_RS);
			result.add(IfxType.RESET_PASSWORD_RQ);
			result.add(IfxType.RESET_PASSWORD_RS);
			result.add(IfxType.SIGN_OFF_RQ);
			result.add(IfxType.SIGN_ON_RQ);
			result.add(IfxType.ECHO_RQ);
			result.add(IfxType.ECHO_RS);
			result.add(IfxType.POS_CONFIRMATION);
			result.add(IfxType.POS_FAILURE);
			
			return result;
		}
		
		if(TrnType.ONLINE_BILLPAYMENT.equals(trnType)){
			result.add(IfxType.ONLINE_BILLPAYMENT_RQ);
			result.add(IfxType.ONLINE_BILLPAYMENT_RS);
			result.add(IfxType.ONLINE_BILLPAYMENT_REV_REPEAT_RQ);
			result.add(IfxType.ONLINE_BILLPAYMENT_REV_REPEAT_RS);
			result.add(IfxType.ONLINE_BILLPAYMENT_TRACKING);
			result.add(IfxType.ONLINE_BILLPAYMENT_TRACKING_REV_REPEAT);
			
			return result;
			
		}
		//payment ro nazahstam
		if(TrnType.PAYMENTSTATEMENT.equals(trnType)){
			result.add(IfxType.PAYMENT_STATEMENT_RQ);
			result.add(IfxType.PAYMENT_STATEMENT_RS);
			result.add(IfxType.PAYMENT_STATEMENT_REV_REPEAT_RQ);
			result.add(IfxType.PAYMENT_STATEMENT_REV_REPEAT_RS);
			
			return result;
			
		}
		
		if(TrnType.PREPARE_BILL_PMT.equals(trnType)){
			result.add(IfxType.PREPARE_BILL_PMT);
			result.add(IfxType.PREPARE_BILL_PMT_REV_REPEAT);
			
			return result;
			
		}
		
		if(TrnType.PREPARE_ONLINE_BILLPAYMENT.equals(trnType)){
			result.add(IfxType.PREPARE_ONLINE_BILLPAYMENT);
			result.add(IfxType.PREPARE_ONLINE_BILLPAYMENT_REV_REPEAT);
			
			return result;			
		}
		
		if (TrnType.PURCHASE.equals(trnType)) {
			result.add(IfxType.PURCHASE_RQ);
			result.add(IfxType.PURCHASE_RS);
			result.add(IfxType.PURCHASE_REV_REPEAT_RQ);
			result.add(IfxType.PURCHASE_REV_REPEAT_RS);
			
			//m.rehman: for Pre-Auth Completion txn set
			result.add(IfxType.PREAUTH_COMPLET_RQ);
			result.add(IfxType.PREAUTH_COMPLET_RS);
			result.add(IfxType.PREAUTH_COMPLET_REV_REPEAT_RQ);
			result.add(IfxType.PREAUTH_COMPLET_REV_REPEAT_RS);
			result.add(IfxType.PREAUTH_COMPLET_ADVICE_RQ);
			result.add(IfxType.PREAUTH_COMPLET_ADVICE_RS);

			return result;
		}
		
		if(TrnType.PURCHASECHARGE.equals(trnType)){
			result.add(IfxType.PURCHASE_CHARGE_RQ);
			result.add(IfxType.PURCHASE_CHARGE_RS);
			result.add(IfxType.PURCHASE_CHARGE_REV_REPEAT_RQ);
			result.add(IfxType.PURCHASE_CHARGE_REV_REPEAT_RS);
			
			return result;
			
		}
		
		if(TrnType.PURCHASETOPUP.equals(trnType)){
			result.add(IfxType.PURCHASE_TOPUP_RQ);
			result.add(IfxType.PURCHASE_TOPUP_RS);
			result.add(IfxType.PURCHASE_TOPUP_REV_REPEAT_RQ);
			result.add(IfxType.PURCHASE_TOPUP_REV_REPEAT_RS);
			
			return result;
			
		}
		
		if(TrnType.RECONCILIATION.equals(trnType)){
			result.add(IfxType.RECONCILIATION_RQ);
			result.add(IfxType.RECONCILIATION_RS);
			result.add(IfxType.RECONCILIATION_REPEAT_RQ);
			result.add(IfxType.RECONCILIATION_REPEAT_RS);
			
			return result;
			
		}
		if(TrnType.RETURN.equals(trnType)){
			result.add(IfxType.RETURN_RQ);
			result.add(IfxType.RETURN_RS);
			result.add(IfxType.RETURN_REV_REPEAT_RQ);
			result.add(IfxType.RETURN_REV_REPEAT_RS);
			
			return result;
		}
		
		if(TrnType.TRANSFER.equals(trnType)){
			result.add(IfxType.TRANSFER_RQ);
			result.add(IfxType.TRANSFER_RS);
			result.add(IfxType.TRANSFER_REV_REPEAT_RQ);
			result.add(IfxType.TRANSFER_REV_REPEAT_RS);
			
			return result;
		}
		
		if(TrnType.TRANSFER_CARD_TO_ACCOUNT.equals(trnType)){
			result.add(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
			result.add(IfxType.TRANSFER_CARD_TO_ACCOUNT_RS);
			result.add(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);
			result.add(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS);
			
			return result;
		}
		
		if(TrnType.WITHDRAWAL.equals(trnType)){
			result.add(IfxType.WITHDRAWAL_RQ);
			result.add(IfxType.WITHDRAWAL_RS);
			result.add(IfxType.WITHDRAWAL_REV_REPEAT_RQ);
			result.add(IfxType.WITHDRAWAL_REV_REPEAT_RS);
			
			return result;
		}
		if(TrnType.THIRD_PARTY_PAYMENT.equals(trnType)){
			result.add(IfxType.THIRD_PARTY_PURCHASE_RQ);
			result.add(IfxType.THIRD_PARTY_PURCHASE_RS);
			result.add(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ);
			result.add(IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RS);
			
			return result;
		}
		
		if(TrnType.PREPARE_THIRD_PARTY_PAYMENT.equals(trnType)){
			result.add(IfxType.PREPARE_THIRD_PARTY_PURCHASE);
			result.add(IfxType.PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT);
			
			return result;
		}
		
		if (TrnType.SADERAT_AUTH_BILLPAYMENT.equals(trnType)) {
			result.add(IfxType.SADERAT_AUTHORIZATION_BILL_PMT_RQ);
			result.add(IfxType.SADERAT_AUTHORIZATION_BILL_PMT_RS);

			
			return result;
		}
		
		if (TrnType.SADERAT_BILLPAYMENT.equals(trnType)) {
			result.add(IfxType.SADERAT_BILL_PMT_RQ);
			result.add(IfxType.SADERAT_BILL_PMT_RS);
			
			return result;
		}


		if (TrnType.SHEBAINQUIRY.equals(trnType)) {
			result.add(IfxType.SHEBA_INQ_RQ);
			result.add(IfxType.SHEBA_INQ_RS);
			result.add(IfxType.SHEBA_REV_REPEAT_RQ);
			result.add(IfxType.SHEBA_REV_REPEAT_RS);
			return result;
		}
		
		if (TrnType.HOTCARD.equals(trnType)) {
			result.add(IfxType.HOTCARD_INQ_RQ);
			result.add(IfxType.HOTCARD_REV_REPEAT_RQ);
			result.add(IfxType.HOTCARD_INQ_RS);
			result.add(IfxType.HOTCARD_REV_REPEAT_RS);
			return result;
		}
		
		//TASK Task081 : ATM Saham Feature
		if (TrnType.STOCK.equals(trnType))
		{
			result.add(IfxType.STOCK_INQ_RQ);
			result.add(IfxType.STOCK_REV_REPEAT_RQ);
			result.add(IfxType.STOCK_INQ_RS);
			result.add(IfxType.STOCK_REV_REPEAT_RS);
			return result;
		}	
		
		// TASK Task129 [26604] - Authenticate Cart (Pasargad)
		if (TrnType.CARD_AUTENTICATE.equals(trnType))
		{
			result.add(IfxType.CARD_AUTHENTICATE_RQ);
			result.add(IfxType.CARD_AUTHENTICATE_RS);
			result.add(IfxType.CARD_AUTHENTICATE_REV_REPEAT_RQ);
			result.add(IfxType.CARD_AUTHENTICATE_REV_REPEAT_RS);
			return result;
		}			
		
		if(TrnType.NONFINANCIAL_INFO.equals(trnType)) {
			result.add(IfxType.GHASEDAK_RQ);
			result.add(IfxType.GHASEDAK_RS);
			return result;
		}
		
		//Mirkamali(Task175): Restriction
		if(TrnType.PREPARE_RESTRICTION.equals(trnType)) {
			result.add(IfxType.PREPARE_RESTRICTION);
			result.add(IfxType.PREPARE_RESTRICTION_REV_REPEAT);
			return result;
		}
		
		if(TrnType.RESTRICTION.equals(trnType)) {
			result.add(IfxType.RESTRICTION_RQ);
			result.add(IfxType.RESTRICTION_RS);
			result.add(IfxType.RESTRICTION_REV_REPEAT_RQ);
			result.add(IfxType.RESTRICTION_REV_REPEAT_RS);
			return result;
		}
		
		//Mirkamali(Task179): Currency ATM
		if(TrnType.PREPARE_WITHDRAWAL.equals(trnType)) {
			result.add(IfxType.PREPARE_WITHDRAWAL);
			result.add(IfxType.PREPARE_WITHDRAWAL_REV_REPEAT);
			return result;
		}
		
		if(TrnType.WITHDRAWAL_CUR.equals(trnType)) {
			result.add(IfxType.WITHDRAWAL_CUR_RQ);
			result.add(IfxType.WITHDRAWAL_CUR_RS);
			result.add(IfxType.WITHDRAWAL_CUR_REV_REPEAT_RQ);
			result.add(IfxType.WITHDRAWAL_CUR_REV_REPEAT_RS);
			return result;
		}

		//m.rehman: for Pre-Auth txn set
		if (TrnType.PREAUTH.equals(trnType)) {
			result.add(IfxType.PREAUTH_RQ);
			result.add(IfxType.PREAUTH_RS);
			result.add(IfxType.PREAUTH_REV_REPEAT_RQ);
			result.add(IfxType.PREAUTH_REV_REPEAT_RS);
			return result;
		}

		//m.rehman: for Cancellation and Refund txn set
		if (TrnType.REFUND.equals(trnType)) {
			result.add(IfxType.PREAUTH_COMPLET_CANCEL_RQ);
			result.add(IfxType.PREAUTH_COMPLET_CANCEL_RS);
			result.add(IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RQ);
			result.add(IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RS);
			result.add(IfxType.PREAUTH_CANCEL_RQ);
			result.add(IfxType.PREAUTH_CANCEL_RS);
			result.add(IfxType.PREAUTH_CANCEL_REV_REPEAT_RQ);
			result.add(IfxType.PREAUTH_CANCEL_REV_REPEAT_RS);
			result.add(IfxType.PURCHASE_CANCEL_RQ);
			result.add(IfxType.PURCHASE_CANCEL_RS);
			result.add(IfxType.PURCHASE_CANCEL_REV_REPEAT_RQ);
			result.add(IfxType.PURCHASE_CANCEL_REV_REPEAT_RS);
			result.add(IfxType.REFUND_ADVICE_RQ);
			result.add(IfxType.REFUND_ADVICE_RQ);

			return result;
		}

        if (TrnType.DIRECT_DEBIT.equals(trnType)) {
            result.add(IfxType.DIRECT_DEBIT_RQ);
            result.add(IfxType.DIRECT_DEBIT_RS);

            return result;
        }

        if (TrnType.MONEY_SEND.equals(trnType)) {
            result.add(IfxType.MONEY_SEND_RQ);
            result.add(IfxType.MONEY_SEND_RS);

            return result;
        }

        if (TrnType.IBFT.equals(trnType)) {
            result.add(IfxType.IBFT_ADVICE_RQ);
            result.add(IfxType.IBFT_ADVICE_RS);

            return result;
        }

        if (TrnType.TITLE_FETCH.equals(trnType)) {
            result.add(IfxType.TITLE_FETCH_RQ);
            result.add(IfxType.TITLE_FETCH_RS);

            return result;
        }

        if (TrnType.ORIGINAL_CREDIT.equals(trnType)) {
            result.add(IfxType.ORIGINAL_CREDIT_RQ);
            result.add(IfxType.ORIGINAL_CREDIT_RS);

            return result;
        }

		//m.rehman: for Loro Advice/Loro Reversal
		if (TrnType.WITHDRAWAL_LORO.equals(trnType)
				|| TrnType.PURCHASE_LORO.equals(trnType)) {
			result.add(IfxType.LORO_ADVICE_RQ);
			result.add(IfxType.LORO_ADVICE_RS);
			result.add(IfxType.LORO_REVERSAL_REPEAT_RQ);
			result.add(IfxType.LORO_REVERSAL_REPEAT_RS);

			return result;
		}

		if (TrnType.WALLET_TOPUP.equals(trnType)) {
			result.add(IfxType.WALLET_TOPUP_RQ);
			result.add(IfxType.WALLET_TOPUP_RS);
			result.add(IfxType.WALLET_TOPUP_REV_REPEAT_RQ);
			result.add(IfxType.WALLET_TOPUP_REV_REPEAT_RS);

			return result;
		}

		//m.rehman: for void transaction from NAC
		if (TrnType.VOID.equals(trnType)) {
			result.add(IfxType.VOID_RQ);
			result.add(IfxType.VOID_RS);

			return result;
		}

		result.add(IfxType.UNDEFINED);
		return result;
	}
}
