package vaulsys.protocols.ifx.enums;

import vaulsys.persistence.IEnum;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Embeddable;

@Embeddable
public class IfxType implements IEnum, Cloneable {
	private static final long serialVersionUID = 6319934299373578229L;
	
	private static final int UNDEFINED_VALUE = -1;
	private static final int BAL_INQ_RQ_VALUE = 1;
	private static final int BAL_INQ_RS_VALUE = 3;
	private static final int BILL_PMT_RQ_VALUE = 10;
	private static final int BILLPMT_RS_VALUE = 13;
//	private static final int BAL_REV_RQ_VALUE = 15;
	private static final int BAL_REV_REPEAT_RQ_VALUE = 16;
	private static final int BAL_REV_REPEAT_RS_VALUE = 17;
//	private static final int BAL_REV_RS_VALUE = 18;
//	private static final int BILL_PMT_REV_RQ_VALUE = 27;
	private static final int BILL_PMT_REV_REPEAT_RQ_VALUE = 29;
	private static final int BILL_PMT_REV_REPEAT_RS_VALUE = 30;
//	private static final int BILL_PMT_REV_RS_VALUE = 33;
	private static final int ACQUIRER_REC_RQ_VALUE = 35;
	private static final int ACQUIRER_REC_RS_VALUE = 36;
	private static final int ACQUIRER_REC_RQ_REPEAT_VALUE = 37;
	private static final int ACQUIRER_REC_RQ_REPEAT_RS_VALUE = 38;
	private static final int CARD_ISSUER_REC_RQ_VALUE = 39;
	private static final int CARD_ISSUER_REC_RS_VALUE = 40;
	private static final int CARD_ISSUER_REC_REPEAT_RQ_VALUE = 41;
	private static final int CARD_ISSUER_REC_REPEAT_RS_VALUE = 42;
	private static final int NETWORK_MGR_RQ_VALUE = 43;
	private static final int NETWORK_MGR_RS_VALUE = 44;
	private static final int NETWORK_MGR_RQ_REPEAT_VALUE = 45;
	private static final int NETWORK_MGR_RQ_REPEAT_RS_VALUE = 46;
	private static final int RECONCILIATION_RQ_VALUE = 47;
	private static final int RECONCILIATION_REPEAT_RQ_VALUE = 48;
	private static final int RECONCILIATION_REPEAT_RS_VALUE = 49;
	private static final int RECONCILIATION_RS_VALUE = 50;
	private static final int TRANSFER_RQ_VALUE = 51;
	private static final int TRANSFER_TO_ACCOUNT_RQ_VALUE = 52;
	private static final int TRANSFER_FROM_ACCOUNT_RQ_VALUE = 53;
	private static final int TRANSFER_RS_VALUE = 54;
	private static final int TRANSFER_TO_ACCOUNT_RS_VALUE = 55;
	private static final int TRANSFER_FROM_ACCOUNT_RS_VALUE = 56;
	private static final int TRANSFER_CHECK_ACCOUNT_RQ_VALUE = 57;
	private static final int TRANSFER_CHECK_ACCOUNT_RS_VALUE = 58;
//	private static final int TRANSFER_REV_RQ_VALUE = 59;
//	private static final int TRANSFER_TO_ACCOUNT_REV_RQ_VALUE = 60;
//	private static final int TRANSFER_FROM_ACCOUNT_REV_RQ_VALUE = 61;
//	private static final int TRANSFER_REV_RS_VALUE = 62;
//	private static final int TRANSFER_TO_ACCOUNT_REV_RS_VALUE = 63;
//	private static final int TRANSFER_FROM_ACCOUNT_REV_RS_VALUE = 64;
	private static final int TRANSFER_REV_REPEAT_RQ_VALUE = 65;
	private static final int TRANSFER_REV_REPEAT_RS_VALUE = 66;
	private static final int TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ_VALUE = 67;
	private static final int TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS_VALUE = 68;
	private static final int TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ_VALUE = 69;
	private static final int TRANSFER_TO_ACCOUNT_REV_REPEAT_RS_VALUE = 70;
	private static final int RETURN_RQ_VALUE = 71;
	private static final int RETURN_RS_VALUE = 72;
//	private static final int RETURN_REV_RS_VALUE = 73;
//	private static final int RETURN_REV_RQ_VALUE = 74;
	private static final int RETURN_REV_RQ_REPEAT_VALUE = 75;
	private static final int RETURN_REV_REPEAT_RS_VALUE = 76;
	private static final int CUTOVER_RQ_VALUE = 79;
	private static final int CUTOVER_RS_VALUE = 80;
	private static final int BANK_STATEMENT_RQ_VALUE = 81;
	private static final int BANK_STATEMENT_RS_VALUE = 82;
	private static final int SUBSIDIARY_ACCOUNT_RQ_VALUE = 83;
	private static final int SUBSIDIARY_ACCOUNT_RS_VALUE = 84;
	private static final int SUBSIDIARY_ACCOUNT_REPEAT_RQ_VALUE = 85;
	private static final int PREPARE_BILL_PMT_VALUE = 86;
	private static final int CUT_OVER_REPEAT_RQ_VALUE = 87;
	private static final int CUT_OVER_REPEAT_RS_VALUE = 88;
	private static final int MAC_KEY_CAHNGE_RQ_VALUE = 89;
	private static final int MAC_KEY_CAHNGE_RS_VALUE = 90;
	private static final int PIN_KEY_CAHNGE_RQ_VALUE = 91;
	private static final int PIN_KEY_CAHNGE_RS_VALUE = 92;
	private static final int PURCHASE_RQ_VALUE = 100;
	private static final int PURCHASE_RS_VALUE = 101;
//	private static final int PURCHASE_REV_RQ_VALUE = 102;
//	private static final int PURCHASE_REV_RS_VALUE = 103;
	private static final int PURCHASE_REV_REPEAT_RQ_VALUE = 104;
	private static final int PURCHASE_REV_REPEAT_RS_VALUE = 105;
	private static final int WITHDRAWAL_RQ_VALUE = 106;
	private static final int WITHDRAWAL_RS_VALUE = 107;
//	private static final int WITHDRAWAL_REV_RQ_VALUE = 108;
//	private static final int WITHDRAWAL_REV_RS_VALUE = 109;
	private static final int WITHDRAWAL_REV_REPEAT_RQ_VALUE = 110;
	private static final int WITHDRAWAL_REV_REPEAT_RS_VALUE = 111;
	private static final int GET_ACCOUNT_RQ_VALUE = 112;
	private static final int GET_ACCOUNT_RS_VALUE = 113;
//	private static final int GET_ACCOUNT_REV_RQ_VALUE = 114;
//	private static final int GET_ACCOUNT_REV_RS_VALUE = 115;
	private static final int GET_ACCOUNT_REV_REPEAT_RQ_VALUE = 116;
	private static final int GET_ACCOUNT_REV_REPEAT_RS_VALUE = 117;
	private static final int CANCEL_VALUE = 118;
	
	private static final int PURCHASE_CHARGE_RQ_VALUE = 119;
	private static final int PURCHASE_CHARGE_RS_VALUE = 120;
//	private static final int PURCHASE_CHARGE_REV_RQ_VALUE = 121;
//	private static final int PURCHASE_CHARGE_REV_RS_VALUE = 122;
	private static final int PURCHASE_CHARGE_REV_REPEAT_RQ_VALUE = 123;
	private static final int PURCHASE_CHARGE_REV_REPEAT_RS_VALUE = 124;
	
	private static final int ATM_ACKNOWLEDGE_VALUE = 125;
	private static final int ATM_GO_IN_SERVICE_VALUE = 126;
	private static final int ATM_CONFIG_ID_LOAD_VALUE = 127;
	private static final int ATM_GO_OUT_OF_SERVICE_VALUE = 128;
	private static final int ATM_FIT_TABLE_LOAD_VALUE = 129;
	private static final int ATM_STATE_TABLE_LOAD_VALUE = 130;
	private static final int ATM_SCREEN_TABLE_LOAD_VALUE = 131;
	private static final int ATM_ENHANCED_PARAMETER_TABLE_LOAD_VALUE = 132;
	private static final int ATM_FUNCTION_COMMAND_VALUE = 133;
	private static final int ATM_SUPPLY_COUNTER_REQUEST_VALUE = 134;
	
	private static final int CHANGE_PIN_BLOCK_RQ_VALUE = 135;
	private static final int CHANGE_PIN_BLOCK_RS_VALUE = 136;
//	private static final int CHANGE_PIN_BLOCK_REV_RQ_VALUE = 137;
//	private static final int CHANGE_PIN_BLOCK_REV_RS_VALUE = 138;
	private static final int CHANGE_PIN_BLOCK_REV_REPEAT_RQ_VALUE = 139;
	private static final int CHANGE_PIN_BLOCK_REV_REPEAT_RS_VALUE = 140;

	private static final int PAYMENT_STATEMENT_RQ_VALUE = 141;
	private static final int PAYMENT_STATEMENT_RS_VALUE = 142;
//	private static final int PAYMENT_STATEMENT_REV_RQ_VALUE = 143;
//	private static final int PAYMENT_STATEMENT_REV_RS_VALUE = 144;
	private static final int PAYMENT_STATEMENT_REV_REPEAT_RQ_VALUE = 145;
	private static final int PAYMENT_STATEMENT_REV_REPEAT_RS_VALUE = 146;
	
	private static final int PARTIAL_DISPENSE_RQ_VALUE = 147;
	private static final int PARTIAL_DISPENSE_RS_VALUE = 148;
	private static final int PARTIAL_DISPENSE_REV_REPEAT_RQ_VALUE = 149;
	private static final int PARTIAL_DISPENSEREV_REPEAT_RS_VALUE = 150;

	private static final int LAST_PURCHASE_CHARGE_RQ_VALUE = 151;
	private static final int LAST_PURCHASE_CHARGE_RS_VALUE = 152;
	
	
	private static final int SIGN_ON_RQ_VALUE = 153;
	private static final int SIGN_OFF_RQ_VALUE = 154;
	private static final int ECHO_RQ_VALUE = 155;
	private static final int ECHO_RS_VALUE = 208;
	
//	private static final int TRANSFER_DEPOSIT_RQ_VALUE = 149;
	
//	private static final int ATM_FIT_TABLE_LOAD_VALUE = 129;
//	private static final int ATM_FIT_TABLE_LOAD_VALUE = 129;

	
	private static final int UI_ISSUE_SHETAB_DOCUMENT_RQ_VALUE = 156;
	private static final int UI_ISSUE_SHETAB_DOCUMENT_RS_VALUE = 157;
	
	private static final int CREDIT_CARD_DATA_RQ_VALUE = 158;
	private static final int CREDIT_CARD_DATA_RS_VALUE = 159;
	
	
	private static final int ATM_SEND_CONFIG_ID_VALUE = 160;
	private static final int DEPOSIT_RQ_VALUE = 161;
	private static final int DEPOSIT_RS_VALUE = 162;
	private static final int DEPOSIT_REV_REPEAT_RQ_VALUE = 163;
	private static final int DEPOSIT_REV_REPEAT_RS_VALUE = 164;
	
	private static final int TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ_VALUE = 165;
	private static final int TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RS_VALUE = 166;
	
	private static final int DEPOSIT_CHECK_ACCOUNT_RQ_VALUE = 167;
	private static final int DEPOSIT_CHECK_ACCOUNT_RS_VALUE = 168;
	
	private static final int SETTLEMENT_TRANSFER_TO_ACCOUNT_RQ_VALUE = 169;
	private static final int SETTLEMENT_TRANSFER_TO_ACCOUNT_RS_VALUE = 170;
	private static final int SETTLEMENT_TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ_VALUE = 171;
	private static final int SETTLEMENT_TRANSFER_TO_ACCOUNT_REV_REPEAT_RS_VALUE = 172;
	
	private static final int UI_SETTLE_RQ_VALUE = 173;
	private static final int UI_SETTLE_RS_VALUE = 174;
	
	private static final int UI_ISSUE_CORE_DOCUMENT_RQ_VALUE = 175;
	private static final int UI_ISSUE_CORE_DOCUMENT_RS_VALUE = 176;
	
	private static final int CARD_READER_WRITER_VALUE = 177;
	private static final int POWER_FAILURE_VALUE = 178;
	private static final int SENSOR_VALUE = 179;
	private static final int CASH_HANDLER_VALUE = 180;
	private static final int COMMAND_REJECT_VALUE = 181;
	private static final int MAC_REJECT_VALUE = 182;
	private static final int ENCRYPTOR_STATE_VALUE = 183;
	private static final int RECEIPT_PRINTER_STATE_VALUE = 184;
	private static final int JOURNAL_PRINTER_STATE_VALUE = 185;
	private static final int DEVICE_LOCATION_VALUE = 186;
	private static final int SUPERVISOR_ENTRY_VALUE = 187;
	private static final int SUPERVISOR_EXIT_VALUE = 188;
	private static final int CONFIG_ID_RESPONSE_VALUE = 189;
	private static final int CASH_HANDLER_RESPONSE_VALUE = 190;
	private static final int CONFIG_INFO_REQUEST_VALUE = 191;
	private static final int CONFIG_INFO_RESPONSE_VALUE = 192;
	private static final int SUPPLY_COUNTER_RESPONSE_VALUE = 193;
	private static final int ATM_DATE_TIME_LOAD_VALUE = 194;
	private static final int MASTER_KEY_CHANGE_RQ_VALUE = 195;
	private static final int MASTER_KEY_CHANGE_RS_VALUE = 196;

	private static final int PREPARE_TRANSFER_CARD_TO_ACCOUNT_VALUE = 197;
	private static final int ATM_GET_ALL_KVV_VALUE = 198;
	
	private static final int PREPARE_BILL_PMT_REV_REPEAT_VALUE = 199;
	private static final int PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_VALUE = 219;
	
	/*** INGENICO Protocol ***/
	private static final int LOG_ON_RQ_VALUE = 200;
	private static final int LOG_ON_RS_VALUE = 201;
	
	private static final int RESET_PASSWORD_RQ_VALUE = 202;
	private static final int RESET_PASSWORD_RS_VALUE = 203;

	private static final int MERCHANT_BALANCE_RQ_VALUE = 204;
	private static final int MERCHANT_BALANCE_RS_VALUE = 205;
	
	private static final int BATCH_UPLOAD_RQ_VALUE = 206;
	private static final int BATCH_UPLOAD_RS_VALUE = 207;
	//private static final int ECHO_RS_VALUE = 208; defined upper
	
	private static final int BANK_STATEMENT_REV_REPEAT_RQ_VALUE = 209;
	private static final int BANK_STATEMENT_REV_REPEAT_RS_VALUE = 210;
	
	private static final int POS_FAILURE_RQ_VALUE = 211;
	private static final int POS_CONFIRMATION_VALUE = 212;
	
	private static final int PURCHASE_TOPUP_RQ_VALUE = 213;
	private static final int PURCHASE_TOPUP_RS_VALUE = 214;
	private static final int PURCHASE_TOPUP_REV_REPEAT_RQ_VALUE = 215;
	private static final int PURCHASE_TOPUP_REV_REPEAT_RS_VALUE = 216;
	
	private static final int CREDIT_CARD_DATA_REV_REPEAT_RQ_VALUE=217;
	private static final int CREDIT_CARD_DATA_REV_REPEAT_RS_VALUE=218;
	
	private static final int ONLINE_BILLPAYMENT_RQ_VALUE = 220;
	private static final int ONLINE_BILLPAYMENT_RS_VALUE = 221;
	private static final int ONLINE_BILLPAYMENT_TRACKING_VALUE = 222;
	private static final int PREPARE_ONLINE_BILLPAYMENT_VALUE = 224;
	private static final int ONLINE_BILLPAYMENT_REV_REPEAT_RQ_VALUE = 226;
	private static final int ONLINE_BILLPAYMENT_REV_REPEAT_RS_VALUE = 227;
	private static final int PREPARE_ONLINE_BILLPAYMENT_REV_REPEAT_VALUE = 228;
	private static final int ONLINE_BILLPAYMENT_TRACKING_REV_REPEAT_VALUE = 229;
	
/***ATM_CASHIN***/
	private static final int ATM_CASHIN_SUPPLY_COUNTER_REQUEST_VALUE = 236;  
	private static final int CASHIN_SUPPLY_COUNTER_RESPONSE_VALUE = 237;
	private static final int ATM_CASHIN_STATUS_INFORMATION_REQUEST_VALUE = 238;
	private static final int CASHIN_STATUS_INFORMATION_RESPONSE_VALUE = 239;
	private static final int ATM_CASHIN_CONFIGURE_NOTEID_REQUEST_VALUE = 240;
	private static final int CASHIN_CONFIGURE_NOTEID_RESPONSE_VALUE = 241;
	
	private static final int CREDIT_PURCHASE_RQ_VALUE = 230;
	private static final int CREDIT_PURCHASE_RS_VALUE = 231;
	private static final int CREDIT_PURCHASE_REV_REPEAT_RQ_VALUE = 232;
	private static final int CREDIT_PURCHASE_REV_REPEAT_RS_VALUE = 233;
	private static final int CREDIT_BAL_INQ_RQ_VALUE = 234;
	private static final int CREDIT_BAL_INQ_RS_VALUE = 235;
	
	private static final int TRANSFER_CARD_TO_ACCOUNT_RQ_VALUE = 242;
	private static final int TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ_VALUE = 243;
	private static final int TRANSFER_CARD_TO_ACCOUNT_RS_VALUE = 244;
	private static final int TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS_VALUE = 245;
	private static final int TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ_VALUE = 246;
	private static final int TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ_VALUE = 247;
	private static final int TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS_VALUE = 248;
	private static final int TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RS_VALUE = 249;
	private static final int TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ_VLAUE = 250;
	private static final int TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ_VLAUE = 251;
	private static final int TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS_VLAUE = 252;
	private static final int TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS_VLAUE = 253;
	
	private static final int SADERAT_AUTHORIZATION_BILL_PMT_RQ_VALUE = 254;
	private static final int SADERAT_AUTHORIZATION_BILL_PMT_RS_VALUE = 255;
	
	private static final int SADERAT_BILL_PMT_RQ_VALUE = 256;
	private static final int SADERAT_BILL_PMT_RS_VALUE = 257;
	
	private static final int THIRD_PARTY_PURCHASE_RQ_VALUE = 258;
	private static final int THIRD_PARTY_PURCHASE_REV_REPEAT_RQ_VALUE = 259;
	private static final int THIRD_PARTY_PURCHASE_RS_VALUE = 260;
	private static final int THIRD_PARTY_PURCHASE_REV_REPEAT_RS_VALUE = 261;
	
	private static final int PREPARE_THIRD_PARTY_PURCHASE_VALUE = 262;
	private static final int PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT_VALUE = 263;
	
	private static final int SORUSH_REV_REPEAT_RQ_VALUE = 264;
	private static final int SORUSH_REV_REPEAT_RS_VALUE = 265;
	
	private static final int SHAPARAK_CONFIRM_RQ_VALUE = 266;
	private static final int SHAPARAK_CONFIRM_RS_VALUE = 267;
	
	private static final int MASKAN_DEPOSIT_RQ_VALUE = 268;
	private static final int MASKAN_DEPOSIT_RS_VALUE = 269;
	
	private static final int PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_VALUE = 270;
	private static final int PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_REV_REPEAT_VALUE = 271;
	
	private static final int SHEBA_INQ_RQ_VALUE = 300;
	private static final int SHEBA_REV_REPEAT_RQ_VALUE = 301;
	private static final int SHEBA_INQ_RS_VALUE = 302;
	private static final int SHEBA_REV_REPEAT_RS_VALUE = 303;
	
	//ghasedak
	private static final int GHASEDAK_RQ_VAULE = 14;
	private static final int GHASEDAK_RS_VAULE = 15;
	
	private static final int HOTCARD_INQ_RQ_VALUE = 304;
	private static final int HOTCARD_REV_REPEAT_RQ_VALUE = 305;
	private static final int HOTCARD_INQ_RS_VALUE = 306;
	private static final int HOTCARD_REV_REPEAT_RS_VALUE = 307;
	
	//TASK Task074 : GetAtmsStatus
	private static final int ATM_STATUS_MONITOR_REQUEST_VALUE = 310;
	private static final int ATM_STATUS_MONITOR_RESPONSE_VALUE = 311;
	
	//TASK Task081 : ATM Saham Feautre
	private static final int STOCK_INQ_RQ_VALUE = 312; //سهام  
	private static final int STOCK_REV_REPEAT_RQ_VALUE = 313; 
	private static final int STOCK_INQ_RS_VALUE = 314; 
	private static final int STOCK_REV_REPEAT_RS_VALUE = 315; 	
	
	// TASK Task129 [26604] - Authenticate Cart (Pasargad)
	private static final int CARD_AUTHENTICATE_RQ_VALUE = 324; // تایید رمز کارت  
	private static final int CARD_AUTHENTICATE_REV_REPEAT_RQ_VALUE = 325; 
	private static final int CARD_AUTHENTICATE_RS_VALUE = 326; 
	private static final int CARD_AUTHENTICATE_REV_REPEAT_RS_VALUE = 327; 	
	
	
	//Mirkamali(Task175): RESTRICTION
	private static final int PREPARE_RESTRICTION_VALUE = 331;
	private static final int PREPARE_RESTRICTION_REV_REPEAT_VALUE = 332;
	private static final int RESTRICTION_RQ_VALUE = 333;
	private static final int RESTRICTION_REV_REPEAT_RQ_VALUE = 334; 
	private static final int RESTRICTION_RS_VALUE = 335; 
	private static final int RESTRICTION_REV_REPEAT_RS_VALUE = 336;
	//Mirkamali(Task179): Currency ATM
	private static final int PREPARE_WITHDRAWAL_VALUE = 337;
	private static final int PREPARE_WITHDRAWAL_REV_REPEAT_VALUE = 338;
	private static final int WITHDRAWAL_CUR_RQ_VALUE = 339;
	private static final int WITHDRAWAL_CUR_RS_VALUE = 340;
	private static final int WITHDRAWAL_CUR_REV_REPEAT_RQ_VALUE = 341;
	private static final int WITHDRAWAL_CUR_REV_REPEAT_RS_VALUE = 342;
	
	//by m.rehman
	private static final int RESET_CONFIG_ID_BY_SUPPLY_COUNTER_REQUEST_VALUE = 343;
	private static final int PREAUTH_RQ_VALUE = 345;
	private static final int PREAUTH_RS_VALUE = 346;
	private static final int PREAUTH_COMPLET_RQ_VALUE = 347;
	private static final int PREAUTH_COMPLET_RS_VALUE = 348;
	private static final int PREAUTH_COMPLET_REV_REPEAT_RQ_VALUE = 349;
	private static final int PREAUTH_COMPLET_REV_REPEAT_RS_VALUE = 350;
	private static final int PREAUTH_COMPLET_CANCEL_RQ_VALUE = 351;
	private static final int PREAUTH_COMPLET_CANCEL_RS_VALUE = 352;
	private static final int PREAUTH_COMPLET_CANCEL_REV_REPEAT_RQ_VALUE = 353;
	private static final int PREAUTH_COMPLET_CANCEL_REV_REPEAT_RS_VALUE = 354;
	private static final int PREAUTH_CANCEL_REV_REPEAT_RQ_VALUE = 355;
	private static final int PREAUTH_CANCEL_REV_REPEAT_RS_VALUE = 356;
	private static final int PURCHASE_CANCEL_RQ_VALUE = 357;
	private static final int PURCHASE_CANCEL_RS_VALUE = 358;
	private static final int PURCHASE_CANCEL_REV_REPEAT_RQ_VALUE = 359;
	private static final int PURCHASE_CANCEL_REV_REPEAT_RS_VALUE = 360;
	private static final int REFUND_ADVICE_RQ_VALUE = 361;
	private static final int REFUND_ADVICE_RS_VALUE = 362;
	private static final int PREAUTH_CANCEL_RQ_VALUE = 363;
	private static final int PREAUTH_CANCEL_RS_VALUE = 364;
	private static final int PREAUTH_REV_REPEAT_RQ_VALUE = 365;
	private static final int PREAUTH_REV_REPEAT_RS_VALUE = 366;
	private static final int PREAUTH_COMPLET_ADVICE_RQ_VALUE = 367;
	private static final int PREAUTH_COMPLET_ADVICE_RS_VALUE = 368;
	private static final int SIGN_ON_RS_VALUE = 369;
	private static final int SIGN_OFF_RS_VALUE = 370;
	private static final int KEY_EXCHANGE_RQ_VALUE = 371;
	private static final int KEY_EXCHANGE_RS_VALUE = 372;
	private static final int SYSTEM_INITIALIZE_RQ_VALUE = 373; //Raza INITIALIZE
	private static final int SYSTEM_INITIALIZE_RS_VALUE = 396; //Raza INITIALIZE
	private static final int DIRECT_DEBIT_RQ_VALUE = 374;
	private static final int DIRECT_DEBIT_RS_VALUE = 375;
	private static final int MONEY_SEND_RQ_VALUE = 376;
	private static final int MONEY_SEND_RS_VALUE = 377;
	private static final int IBFT_ADVICE_RQ_VALUE = 378;
	private static final int IBFT_ADVICE_RS_VALUE = 379;
	private static final int TITLE_FETCH_RQ_VALUE = 380;
	private static final int TITLE_FETCH_RS_VALUE = 381;
	private static final int ORIGINAL_CREDIT_RQ_VALUE = 382;
	private static final int ORIGINAL_CREDIT_RS_VALUE = 383;
	//m.rehman: for Loro advice
	private static final int LORO_ADVICE_RQ_VALUE = 384;
	private static final int LORO_ADVICE_RS_VALUE = 385;
	private static final int LORO_REVERSAL_REPEAT_RQ_VALUE = 386;
	private static final int LORO_REVERSAL_REPEAT_RS_VALUE = 387;

	private static final int WALLET_TOPUP_RQ_VALUE = 388;
	private static final int WALLET_TOPUP_RS_VALUE = 389;
	private static final int WALLET_TOPUP_REV_REPEAT_RQ_VALUE = 390;
	private static final int WALLET_TOPUP_REV_REPEAT_RS_VALUE = 391;

	private static final int CVV_GENERATION_RQ_VALUE = 392;
	private static final int CVV_GENERATION_RS_VALUE = 393;
	//m.rehman: for void transaction from NAC
	private static final int VOID_RQ_VALUE = 394;
	private static final int VOID_RS_VALUE = 395;
	private static final int REFUND_RQ_VALUE = 396;
	private static final int REFUND_RS_VALUE = 397;
	private static final int REFUND_REVERSAL_REPEAT_RQ_VALUE = 398;
	private static final int REFUND_REVERSAL_REPEAT_RS_VALUE = 399;
	private static final int OFFLINE_TIP_ADJUST_REPEAT_RQ_VALUE = 400;
	private static final int OFFLINE_TIP_ADJUST_REPEAT_RS_VALUE = 401;

	//Raza NayaPay start
	private static final int CREATEWALLETLEVEL_ZERO_RQ_VALUE = 402;
	private static final int CREATEWALLETLEVEL_ONE_RQ_VALUE = 403;
	private static final int CREATEWALLETLEVEL_TWO_RQ_VALUE = 404;
	private static final int UPDATEUSERPROFILE_RQ_VALUE = 405;
	private static final int LINKBANKACCOUNT_RQ_VALUE = 406;
	private static final int UNLINKBANKACCOUNT_RQ_VALUE = 407;
	private static final int CONFIRMOTP_RQ_VALUE = 408;
	private static final int REQUESTDEBITCARDTXN_RQ_VALUE = 409;
	private static final int REQUESTDEBITCARD_RQ_VALUE = 410;
	private static final int ACTIVATEDEBITCARD_RQ_VALUE = 411;
	private static final int ENABLEDEBITCARD_RQ_VALUE = 412;
	private static final int LOADWALLET_RQ_VALUE = 413;
	private static final int WALLETTXN_RQ_VALUE = 414;
	private static final int WALLETBALANCE_RQ_VALUE = 415;
	private static final int UNLOADWALLET_RQ_VALUE = 416;
	private static final int MERCHANTCORETXN_RQ_VALUE = 417;
	private static final int MERCHANTTXN_RQ_VALUE = 418;
	private static final int ATMTXNLOG_RQ_VALUE = 419;
	private static final int GETDEBITCARDREQUEST_RQ_VALUE = 420;
	private static final int GETWALLETSTATUS_RQ_VALUE = 421;
	private static final int CONFIRMFRAUDOTP_RQ_VALUE = 422;
	//Raza NayaPay end

	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public IfxType(int type) {
		super();
		this.type = type;
	}

	public IfxType() {
		super();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		IfxType that = (IfxType) obj;
		return type == that.type;
	}

	@Override
	public int hashCode() {
		return type;
	}

	@Override
	protected Object clone() {
		return new IfxType(this.type);
	}

	public IfxType copy() {
		return (IfxType) clone();
	}

	public static final IfxType UNDEFINED = new IfxType(UNDEFINED_VALUE);
	public static final IfxType PREPARE_BILL_PMT = new IfxType(PREPARE_BILL_PMT_VALUE);
	public static final IfxType PREPARE_BILL_PMT_REV_REPEAT = new IfxType(PREPARE_BILL_PMT_REV_REPEAT_VALUE);
	public static final IfxType CANCEL = new IfxType(CANCEL_VALUE);
	public static final IfxType BAL_INQ_RQ = new IfxType(BAL_INQ_RQ_VALUE);
	public static final IfxType BAL_INQ_RS = new IfxType(BAL_INQ_RS_VALUE);
	public static final IfxType BILL_PMT_RQ = new IfxType(BILL_PMT_RQ_VALUE);
	public static final IfxType BILL_PMT_RS = new IfxType(BILLPMT_RS_VALUE);
	
	public static final IfxType SADERAT_AUTHORIZATION_BILL_PMT_RQ = new IfxType(SADERAT_AUTHORIZATION_BILL_PMT_RQ_VALUE);
	public static final IfxType SADERAT_AUTHORIZATION_BILL_PMT_RS = new IfxType(SADERAT_AUTHORIZATION_BILL_PMT_RS_VALUE);
	public static final IfxType SADERAT_BILL_PMT_RQ = new IfxType(SADERAT_BILL_PMT_RQ_VALUE);
	public static final IfxType SADERAT_BILL_PMT_RS = new IfxType(SADERAT_BILL_PMT_RS_VALUE);
	
//	public static final IfxType BAL_REV_RQ = new IfxType(BAL_REV_RQ_VALUE);
//	public static final IfxType BAL_REV_RS = new IfxType(BAL_REV_RS_VALUE);
	public static final IfxType BAL_REV_REPEAT_RQ = new IfxType(BAL_REV_REPEAT_RQ_VALUE);
	public static final IfxType BAL_REV_REPEAT_RS = new IfxType(BAL_REV_REPEAT_RS_VALUE);
//	public static final IfxType BILL_PMT_REV_RQ = new IfxType(BILL_PMT_REV_RQ_VALUE);
//	public static final IfxType BILL_PMT_REV_RS = new IfxType(BILL_PMT_REV_RS_VALUE);
	public static final IfxType BILL_PMT_REV_REPEAT_RQ = new IfxType(BILL_PMT_REV_REPEAT_RQ_VALUE);
	public static final IfxType BILL_PMT_REV_REPEAT_RS = new IfxType(BILL_PMT_REV_REPEAT_RS_VALUE);
	public static final IfxType ACQUIRER_REC_RQ = new IfxType(ACQUIRER_REC_RQ_VALUE);
	public static final IfxType ACQUIRER_REC_RS = new IfxType(ACQUIRER_REC_RS_VALUE);
	public static final IfxType ACQUIRER_REC_REPEAT_RQ = new IfxType(ACQUIRER_REC_RQ_REPEAT_VALUE);
	public static final IfxType ACQUIRER_REC_REPEAT_RS = new IfxType(ACQUIRER_REC_RQ_REPEAT_RS_VALUE);
	public static final IfxType CARD_ISSUER_REC_RQ = new IfxType(CARD_ISSUER_REC_RQ_VALUE);
	public static final IfxType CARD_ISSUER_REC_RS = new IfxType(CARD_ISSUER_REC_RS_VALUE);
	public static final IfxType CARD_ISSUER_REC_REPEAT_RQ = new IfxType(CARD_ISSUER_REC_REPEAT_RQ_VALUE);
	public static final IfxType CARD_ISSUER_REC_REPEAT_RS = new IfxType(CARD_ISSUER_REC_REPEAT_RS_VALUE);
	public static final IfxType NETWORK_MGR_RQ = new IfxType(NETWORK_MGR_RQ_VALUE);
	public static final IfxType NETWORK_MGR_RS = new IfxType(NETWORK_MGR_RS_VALUE);
	public static final IfxType NETWORK_MGR_REPEAT_RQ = new IfxType(NETWORK_MGR_RQ_REPEAT_VALUE);
	public static final IfxType NETWORK_MGR_REPEAT_RS = new IfxType(NETWORK_MGR_RQ_REPEAT_RS_VALUE);
	public static final IfxType RECONCILIATION_RQ = new IfxType(RECONCILIATION_RQ_VALUE);
	public static final IfxType RECONCILIATION_REPEAT_RQ = new IfxType(RECONCILIATION_REPEAT_RQ_VALUE);
	public static final IfxType RECONCILIATION_REPEAT_RS = new IfxType(RECONCILIATION_REPEAT_RS_VALUE);
	public static final IfxType RECONCILIATION_RS = new IfxType(RECONCILIATION_RS_VALUE);
	
	public static final IfxType TRANSFER_RQ = new IfxType(TRANSFER_RQ_VALUE);

	public static final IfxType TRANSFER_TO_ACCOUNT_RQ = new IfxType(TRANSFER_TO_ACCOUNT_RQ_VALUE);
	public static final IfxType TRANSFER_FROM_ACCOUNT_RQ = new IfxType(TRANSFER_FROM_ACCOUNT_RQ_VALUE);
	public static final IfxType TRANSFER_RS = new IfxType(TRANSFER_RS_VALUE);
	public static final IfxType TRANSFER_TO_ACCOUNT_RS = new IfxType(TRANSFER_TO_ACCOUNT_RS_VALUE);
	public static final IfxType TRANSFER_FROM_ACCOUNT_RS = new IfxType(TRANSFER_FROM_ACCOUNT_RS_VALUE);
	
	public static final IfxType TRANSFER_CHECK_ACCOUNT_RQ = new IfxType(TRANSFER_CHECK_ACCOUNT_RQ_VALUE);
	public static final IfxType TRANSFER_CHECK_ACCOUNT_RS = new IfxType(TRANSFER_CHECK_ACCOUNT_RS_VALUE);
	public static final IfxType TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ = new IfxType(TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ_VALUE);
	public static final IfxType TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RS = new IfxType(TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RS_VALUE);
	
//	public static final IfxType TRANSFER_REV_RQ = new IfxType(TRANSFER_REV_RQ_VALUE);
//	public static final IfxType TRANSFER_TO_ACCOUNT_REV_RQ = new IfxType(TRANSFER_TO_ACCOUNT_REV_RQ_VALUE);
//	public static final IfxType TRANSFER_FROM_ACCOUNT_REV_RQ = new IfxType(TRANSFER_FROM_ACCOUNT_REV_RQ_VALUE);
//	public static final IfxType TRANSFER_REV_RS = new IfxType(TRANSFER_REV_RS_VALUE);
//	public static final IfxType TRANSFER_TO_ACCOUNT_REV_RS = new IfxType(TRANSFER_TO_ACCOUNT_REV_RS_VALUE);
//	public static final IfxType TRANSFER_FROM_ACCOUNT_REV_RS = new IfxType(TRANSFER_FROM_ACCOUNT_REV_RS_VALUE);
	public static final IfxType TRANSFER_REV_REPEAT_RQ = new IfxType(TRANSFER_REV_REPEAT_RQ_VALUE);
	public static final IfxType TRANSFER_REV_REPEAT_RS = new IfxType(TRANSFER_REV_REPEAT_RS_VALUE);
	public static final IfxType TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ = new IfxType(TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ_VALUE);
	public static final IfxType TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS = new IfxType(TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS_VALUE);
	public static final IfxType TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ = new IfxType(TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ_VALUE);
	public static final IfxType TRANSFER_TO_ACCOUNT_REV_REPEAT_RS = new IfxType(TRANSFER_TO_ACCOUNT_REV_REPEAT_RS_VALUE);
	public static final IfxType RETURN_RQ = new IfxType(RETURN_RQ_VALUE);
	public static final IfxType RETURN_RS = new IfxType(RETURN_RS_VALUE);
//	public static final IfxType RETURN_REV_RS = new IfxType(RETURN_REV_RS_VALUE);
//	public static final IfxType RETURN_REV_RQ = new IfxType(RETURN_REV_RQ_VALUE);
	public static final IfxType RETURN_REV_REPEAT_RQ = new IfxType(RETURN_REV_RQ_REPEAT_VALUE);
	public static final IfxType RETURN_REV_REPEAT_RS = new IfxType(RETURN_REV_REPEAT_RS_VALUE);
	public static final IfxType CUTOVER_RQ = new IfxType(CUTOVER_RQ_VALUE);
	public static final IfxType CUTOVER_RS = new IfxType(CUTOVER_RS_VALUE);	
	public static final IfxType CUTOVER_REPEAT_RQ = new IfxType(CUT_OVER_REPEAT_RQ_VALUE);
	public static final IfxType CUTOVER_REPEAT_RS = new IfxType(CUT_OVER_REPEAT_RS_VALUE);
	
	public static final IfxType BANK_STATEMENT_RQ = new IfxType(BANK_STATEMENT_RQ_VALUE);
	public static final IfxType BANK_STATEMENT_RS = new IfxType(BANK_STATEMENT_RS_VALUE);
	public static final IfxType BANK_STATEMENT_REV_REPEAT_RQ = new IfxType(BANK_STATEMENT_REV_REPEAT_RQ_VALUE);
	public static final IfxType BANK_STATEMENT_REV_REPEAT_RS = new IfxType(BANK_STATEMENT_REV_REPEAT_RS_VALUE);
	public static final IfxType SUBSIDIARY_ACCOUNT_RQ = new IfxType(SUBSIDIARY_ACCOUNT_RQ_VALUE);
	public static final IfxType SUBSIDIARY_ACCOUNT_RS = new IfxType(SUBSIDIARY_ACCOUNT_RS_VALUE);
	public static final IfxType SUBSIDIARY_ACCOUNT_REPEAT_RQ = new IfxType(SUBSIDIARY_ACCOUNT_REPEAT_RQ_VALUE);
	public static final IfxType MAC_KEY_CHANGE_RQ = new IfxType(MAC_KEY_CAHNGE_RQ_VALUE);
	public static final IfxType MAC_KEY_CHANGE_RS = new IfxType(MAC_KEY_CAHNGE_RS_VALUE);
	public static final IfxType PIN_KEY_CHANGE_RQ = new IfxType(PIN_KEY_CAHNGE_RQ_VALUE);
	public static final IfxType PIN_KEY_CHANGE_RS = new IfxType(PIN_KEY_CAHNGE_RS_VALUE);
	
	public static final IfxType PURCHASE_RQ = new IfxType( PURCHASE_RQ_VALUE);
	public static final IfxType PURCHASE_RS = new IfxType( PURCHASE_RS_VALUE);
//	public static final IfxType PURCHASE_REV_RQ = new IfxType( PURCHASE_REV_RQ_VALUE);
//	public static final IfxType PURCHASE_REV_RS = new IfxType( PURCHASE_REV_RS_VALUE);
	public static final IfxType PURCHASE_REV_REPEAT_RQ = new IfxType( PURCHASE_REV_REPEAT_RQ_VALUE);
	public static final IfxType PURCHASE_REV_REPEAT_RS = new IfxType( PURCHASE_REV_REPEAT_RS_VALUE);
	
	public static final IfxType WITHDRAWAL_RQ = new IfxType( WITHDRAWAL_RQ_VALUE);
	public static final IfxType WITHDRAWAL_RS = new IfxType( WITHDRAWAL_RS_VALUE);
//	public static final IfxType WITHDRAWAL_REV_RQ = new IfxType( WITHDRAWAL_REV_RQ_VALUE);
//	public static final IfxType WITHDRAWAL_REV_RS = new IfxType( WITHDRAWAL_REV_RS_VALUE);
	public static final IfxType WITHDRAWAL_REV_REPEAT_RQ = new IfxType( WITHDRAWAL_REV_REPEAT_RQ_VALUE);
	public static final IfxType WITHDRAWAL_REV_REPEAT_RS = new IfxType( WITHDRAWAL_REV_REPEAT_RS_VALUE);
	
	public static final IfxType GET_ACCOUNT_RQ = new IfxType( GET_ACCOUNT_RQ_VALUE);
	public static final IfxType GET_ACCOUNT_RS = new IfxType( GET_ACCOUNT_RS_VALUE);
//	public static final IfxType GET_ACCOUNT_REV_RQ = new IfxType( GET_ACCOUNT_REV_RQ_VALUE);
//	public static final IfxType GET_ACCOUNT_REV_RS = new IfxType( GET_ACCOUNT_REV_RS_VALUE);
	public static final IfxType GET_ACCOUNT_REV_REPEAT_RQ = new IfxType( GET_ACCOUNT_REV_REPEAT_RQ_VALUE);
	public static final IfxType GET_ACCOUNT_REV_REPEAT_RS = new IfxType( GET_ACCOUNT_REV_REPEAT_RS_VALUE);

	public static final IfxType PURCHASE_CHARGE_RQ = new IfxType( PURCHASE_CHARGE_RQ_VALUE);
	public static final IfxType PURCHASE_CHARGE_RS = new IfxType( PURCHASE_CHARGE_RS_VALUE);
//	public static final IfxType PURCHASE_CHARGE_REV_RQ = new IfxType( PURCHASE_CHARGE_REV_RQ_VALUE);
//	public static final IfxType PURCHASE_CHARGE_REV_RS = new IfxType( PURCHASE_CHARGE_REV_RS_VALUE);
	public static final IfxType PURCHASE_CHARGE_REV_REPEAT_RQ = new IfxType( PURCHASE_CHARGE_REV_REPEAT_RQ_VALUE);
	public static final IfxType PURCHASE_CHARGE_REV_REPEAT_RS = new IfxType( PURCHASE_CHARGE_REV_REPEAT_RS_VALUE);
	
	public static final IfxType PAYMENT_STATEMENT_RQ = new IfxType( PAYMENT_STATEMENT_RQ_VALUE);
	public static final IfxType PAYMENT_STATEMENT_RS = new IfxType( PAYMENT_STATEMENT_RS_VALUE);
//	public static final IfxType PAYMENT_STATEMENT_REV_RQ = new IfxType( PAYMENT_STATEMENT_REV_RQ_VALUE);
//	public static final IfxType PAYMENT_STATEMENT_REV_RS = new IfxType( PAYMENT_STATEMENT_REV_RS_VALUE);
	public static final IfxType PAYMENT_STATEMENT_REV_REPEAT_RQ = new IfxType( PAYMENT_STATEMENT_REV_REPEAT_RQ_VALUE);
	public static final IfxType PAYMENT_STATEMENT_REV_REPEAT_RS = new IfxType( PAYMENT_STATEMENT_REV_REPEAT_RS_VALUE);
	
	public static final IfxType CHANGE_PIN_BLOCK_RQ = new IfxType( CHANGE_PIN_BLOCK_RQ_VALUE);
	public static final IfxType CHANGE_PIN_BLOCK_RS = new IfxType( CHANGE_PIN_BLOCK_RS_VALUE);
//	public static final IfxType CHANGE_PIN_BLOCK_REV_RQ = new IfxType( CHANGE_PIN_BLOCK_REV_RQ_VALUE);
//	public static final IfxType CHANGE_PIN_BLOCK_REV_RS = new IfxType( CHANGE_PIN_BLOCK_REV_RS_VALUE);
	public static final IfxType CHANGE_PIN_BLOCK_REV_REPEAT_RQ = new IfxType( CHANGE_PIN_BLOCK_REV_REPEAT_RQ_VALUE);
	public static final IfxType CHANGE_PIN_BLOCK_REV_REPEAT_RS = new IfxType( CHANGE_PIN_BLOCK_REV_REPEAT_RS_VALUE);
	
	public static final IfxType ATM_ACKNOWLEDGE = new IfxType( ATM_ACKNOWLEDGE_VALUE);
	public static final IfxType ATM_GO_IN_SERVICE = new IfxType( ATM_GO_IN_SERVICE_VALUE);
	public static final IfxType ATM_SEND_CONFIG_ID = new IfxType( ATM_SEND_CONFIG_ID_VALUE);
	public static final IfxType ATM_CONFIG_ID_LOAD = new IfxType(ATM_CONFIG_ID_LOAD_VALUE);
	public static final IfxType ATM_GO_OUT_OF_SERVICE = new IfxType(ATM_GO_OUT_OF_SERVICE_VALUE);
	public static final IfxType ATM_FIT_TABLE_LOAD = new IfxType(ATM_FIT_TABLE_LOAD_VALUE);
	public static final IfxType ATM_STATE_TABLE_LOAD = new IfxType(ATM_STATE_TABLE_LOAD_VALUE);
	public static final IfxType ATM_SCREEN_TABLE_LOAD = new IfxType(ATM_SCREEN_TABLE_LOAD_VALUE);
	public static final IfxType ATM_ENHANCED_PARAMETER_TABLE_LOAD = new IfxType(ATM_ENHANCED_PARAMETER_TABLE_LOAD_VALUE);
	public static final IfxType ATM_FUNCTION_COMMAND = new IfxType(ATM_FUNCTION_COMMAND_VALUE);
	public static final IfxType ATM_SUPPLY_COUNTER_REQUEST = new IfxType(ATM_SUPPLY_COUNTER_REQUEST_VALUE);
	public static final IfxType ATM_DATE_TIME_LOAD = new IfxType(ATM_DATE_TIME_LOAD_VALUE);
	public static final IfxType MASTER_KEY_CHANGE_RQ = new IfxType(MASTER_KEY_CHANGE_RQ_VALUE);
	public static final IfxType MASTER_KEY_CHANGE_RS = new IfxType(MASTER_KEY_CHANGE_RS_VALUE);
	public static final IfxType ATM_GET_ALL_KVV = new IfxType(ATM_GET_ALL_KVV_VALUE);
	
	public static final IfxType PARTIAL_DISPENSE_RQ = new IfxType(PARTIAL_DISPENSE_RQ_VALUE);
	public static final IfxType PARTIAL_DISPENSE_RS= new IfxType(PARTIAL_DISPENSE_RS_VALUE);
	public static final IfxType PARTIAL_DISPENSE_REV_REPEAT_RQ = new IfxType(PARTIAL_DISPENSE_REV_REPEAT_RQ_VALUE);
	public static final IfxType PARTIAL_DISPENSE_REV_REPEAT_RS = new IfxType(PARTIAL_DISPENSEREV_REPEAT_RS_VALUE );
	
	public static final IfxType LAST_PURCHASE_CHARGE_RQ = new IfxType( LAST_PURCHASE_CHARGE_RQ_VALUE);
	public static final IfxType LAST_PURCHASE_CHARGE_RS = new IfxType( LAST_PURCHASE_CHARGE_RS_VALUE);
	
	public static final IfxType SIGN_ON_RQ = new IfxType( SIGN_ON_RQ_VALUE);
	public static final IfxType SIGN_OFF_RQ = new IfxType( SIGN_OFF_RQ_VALUE);
	
	public static final IfxType ECHO_RQ = new IfxType( ECHO_RQ_VALUE);
	public static final IfxType ECHO_RS = new IfxType( ECHO_RS_VALUE);
	
	public static final IfxType UI_ISSUE_SHETAB_DOCUMENT_RQ = new IfxType(UI_ISSUE_SHETAB_DOCUMENT_RQ_VALUE);
	public static final IfxType UI_ISSUE_SHETAB_DOCUMENT_RS = new IfxType(UI_ISSUE_SHETAB_DOCUMENT_RS_VALUE);
	
	public static final IfxType CREDIT_CARD_DATA_RQ = new IfxType(CREDIT_CARD_DATA_RQ_VALUE);
	public static final IfxType CREDIT_CARD_DATA_RS = new IfxType(CREDIT_CARD_DATA_RS_VALUE);
	public static final IfxType CREDIT_CARD_REV_REPEAT_RQ = new IfxType(CREDIT_CARD_DATA_REV_REPEAT_RQ_VALUE);
	public static final IfxType CREDIT_CARD_REV_REPEAT_RS = new IfxType(CREDIT_CARD_DATA_REV_REPEAT_RS_VALUE);

	public static final IfxType DEPOSIT_RQ = new IfxType(DEPOSIT_RQ_VALUE);
	public static final IfxType DEPOSIT_RS = new IfxType(DEPOSIT_RS_VALUE );
	public static final IfxType DEPOSIT_REV_REPEAT_RQ = new IfxType(DEPOSIT_REV_REPEAT_RQ_VALUE );
	public static final IfxType DEPOSIT_REV_REPEAT_RS = new IfxType(DEPOSIT_REV_REPEAT_RS_VALUE);
	
	public static final IfxType DEPOSIT_CHECK_ACCOUNT_RQ = new IfxType(DEPOSIT_CHECK_ACCOUNT_RQ_VALUE);
	public static final IfxType DEPOSIT_CHECK_ACCOUNT_RS = new IfxType(DEPOSIT_CHECK_ACCOUNT_RS_VALUE );
	
	public static final IfxType SETTLEMENT_TRANSFER_TO_ACCOUNT_RQ = new IfxType(SETTLEMENT_TRANSFER_TO_ACCOUNT_RQ_VALUE);
	public static final IfxType SETTLEMENT_TRANSFER_TO_ACCOUNT_RS = new IfxType(SETTLEMENT_TRANSFER_TO_ACCOUNT_RS_VALUE);
	public static final IfxType SETTLEMENT_TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ = new IfxType(SETTLEMENT_TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ_VALUE);
	public static final IfxType SETTLEMENT_TRANSFER_TO_ACCOUNT_REV_REPEAT_RS = new IfxType(SETTLEMENT_TRANSFER_TO_ACCOUNT_REV_REPEAT_RS_VALUE);
	
	public static final IfxType UI_SETTLE_RQ = new IfxType(UI_SETTLE_RQ_VALUE);
	public static final IfxType UI_SETTLE_RS = new IfxType(UI_SETTLE_RS_VALUE);
	
	//public static final IfxType UI_RETURN_RQ = new IfxType(UI_RETURN_RQ_VALUE);
	//public static final IfxType UI_RETURN_RS = new IfxType(UI_RETURN_RS_VALUE);
	
	public static final IfxType UI_ISSUE_CORE_DOCUMENT_RQ = new IfxType(UI_ISSUE_CORE_DOCUMENT_RQ_VALUE);
	public static final IfxType UI_ISSUE_CORE_DOCUMENT_RS = new IfxType(UI_ISSUE_CORE_DOCUMENT_RS_VALUE);
	
	public static final IfxType CARD_READER_WRITER = new IfxType(CARD_READER_WRITER_VALUE);
	public static final IfxType POWER_FAILURE = new IfxType(POWER_FAILURE_VALUE);
	public static final IfxType SENSOR = new IfxType(SENSOR_VALUE);
	public static final IfxType CASH_HANDLER = new IfxType(CASH_HANDLER_VALUE);
	public static final IfxType COMMAND_REJECT = new IfxType(COMMAND_REJECT_VALUE);
	public static final IfxType MAC_REJECT = new IfxType(MAC_REJECT_VALUE);
	public static final IfxType ENCRYPTOR_STATE = new IfxType(ENCRYPTOR_STATE_VALUE);
	public static final IfxType RECEIPT_PRINTER_STATE = new IfxType(RECEIPT_PRINTER_STATE_VALUE);
	public static final IfxType JOURNAL_PRINTER_STATE = new IfxType(JOURNAL_PRINTER_STATE_VALUE);
	public static final IfxType DEVICE_LOCATION = new IfxType(DEVICE_LOCATION_VALUE);
	public static final IfxType SUPERVISOR_ENTRY = new IfxType(SUPERVISOR_ENTRY_VALUE);
	public static final IfxType SUPERVISOR_EXIT = new IfxType(SUPERVISOR_EXIT_VALUE);
	public static final IfxType CONFIG_ID_RESPONSE = new IfxType(CONFIG_ID_RESPONSE_VALUE);
	public static final IfxType CASH_HANDLER_RESPONSE = new IfxType(CASH_HANDLER_RESPONSE_VALUE);
	public static final IfxType CONFIG_INFO_REQUEST = new IfxType(CONFIG_INFO_REQUEST_VALUE);
	public static final IfxType CONFIG_INFO_RESPONSE = new IfxType(CONFIG_INFO_RESPONSE_VALUE);
	public static final IfxType SUPPLY_COUNTER_RESPONSE = new IfxType(SUPPLY_COUNTER_RESPONSE_VALUE);
	public static final IfxType PREPARE_TRANSFER_CARD_TO_ACCOUNT = new IfxType(PREPARE_TRANSFER_CARD_TO_ACCOUNT_VALUE);
	public static final IfxType PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT= new IfxType(PREPARE_TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_VALUE);
		
	public static final IfxType PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP = new IfxType(PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_VALUE);//TASK Task002 : Transfer Card To Account
	public static final IfxType PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_REV_REPEAT= new IfxType(PREPARE_TRANSFER_CARD_TO_ACCOUNT_FIRST_STEP_REV_REPEAT_VALUE);
	
	/*** INGENICO Protocol ***/	
	public static final IfxType LOG_ON_RQ = new IfxType(LOG_ON_RQ_VALUE);
	public static final IfxType LOG_ON_RS = new IfxType(LOG_ON_RS_VALUE);
	public static final IfxType RESET_PASSWORD_RQ = new IfxType(RESET_PASSWORD_RQ_VALUE);
	public static final IfxType RESET_PASSWORD_RS = new IfxType(RESET_PASSWORD_RS_VALUE);
	public static final IfxType MERCHANT_BALANCE_RQ = new IfxType(MERCHANT_BALANCE_RQ_VALUE);
	public static final IfxType MERCHANT_BALANCE_RS = new IfxType(MERCHANT_BALANCE_RS_VALUE);
	public static final IfxType BATCH_UPLOAD_RQ = new IfxType(BATCH_UPLOAD_RQ_VALUE);
	public static final IfxType BATCH_UPLOAD_RS = new IfxType(BATCH_UPLOAD_RS_VALUE);
	
	public static final IfxType POS_FAILURE = new IfxType(POS_FAILURE_RQ_VALUE);
	public static final IfxType POS_CONFIRMATION = new IfxType(POS_CONFIRMATION_VALUE);

	public static final IfxType PURCHASE_TOPUP_RQ = new IfxType(PURCHASE_TOPUP_RQ_VALUE);
	public static final IfxType PURCHASE_TOPUP_RS = new IfxType(PURCHASE_TOPUP_RS_VALUE);
	public static final IfxType PURCHASE_TOPUP_REV_REPEAT_RQ = new IfxType(PURCHASE_TOPUP_REV_REPEAT_RQ_VALUE);
	public static final IfxType PURCHASE_TOPUP_REV_REPEAT_RS = new IfxType(PURCHASE_TOPUP_REV_REPEAT_RS_VALUE);
	
	public static final IfxType ONLINE_BILLPAYMENT_RQ = new IfxType(ONLINE_BILLPAYMENT_RQ_VALUE);
	public static final IfxType ONLINE_BILLPAYMENT_RS = new IfxType(ONLINE_BILLPAYMENT_RS_VALUE);
	public static final IfxType ONLINE_BILLPAYMENT_TRACKING = new IfxType(ONLINE_BILLPAYMENT_TRACKING_VALUE);
	public static final IfxType PREPARE_ONLINE_BILLPAYMENT= new IfxType(PREPARE_ONLINE_BILLPAYMENT_VALUE);
	public static final IfxType ONLINE_BILLPAYMENT_REV_REPEAT_RQ = new IfxType(ONLINE_BILLPAYMENT_REV_REPEAT_RQ_VALUE);
	public static final IfxType ONLINE_BILLPAYMENT_REV_REPEAT_RS = new IfxType(ONLINE_BILLPAYMENT_REV_REPEAT_RS_VALUE);
	public static final IfxType PREPARE_ONLINE_BILLPAYMENT_REV_REPEAT =  new IfxType(PREPARE_ONLINE_BILLPAYMENT_REV_REPEAT_VALUE);
	public static final IfxType ONLINE_BILLPAYMENT_TRACKING_REV_REPEAT = new IfxType(ONLINE_BILLPAYMENT_TRACKING_REV_REPEAT_VALUE);
	
	public static final IfxType ATM_CASHIN_SUPPLY_COUNTER_REQUEST = new IfxType(ATM_CASHIN_SUPPLY_COUNTER_REQUEST_VALUE) ;
	public static final IfxType CASHIN_SUPPLY_COUNTER_RESPONSE = new IfxType(CASHIN_SUPPLY_COUNTER_RESPONSE_VALUE);
	
	public static final IfxType ATM_CASHIN_STATUS_INFORMATION_REQUEST = new IfxType(ATM_CASHIN_STATUS_INFORMATION_REQUEST_VALUE);
	public static final IfxType CASHIN_STATUS_INFORMATION_REPONSE = new IfxType(CASHIN_STATUS_INFORMATION_RESPONSE_VALUE);
	
	public static final IfxType ATM_CASHIN_CONFIGURE_NOTEID_REQUEST = new IfxType(ATM_CASHIN_CONFIGURE_NOTEID_REQUEST_VALUE);
	public static final IfxType CASHIN_CONFIGURE_NOTEID_RESPONSE = new IfxType(CASHIN_CONFIGURE_NOTEID_RESPONSE_VALUE);
	
	public static final IfxType CREDIT_PURCHASE_RQ = new IfxType(CREDIT_PURCHASE_RQ_VALUE);
	public static final IfxType CREDIT_PURCHASE_RS = new IfxType(CREDIT_PURCHASE_RS_VALUE);
	public static final IfxType CREDIT_PURCHASE_REV_REPEAT_RQ = new IfxType(CREDIT_PURCHASE_REV_REPEAT_RQ_VALUE);
	public static final IfxType CREDIT_PURCHASE_REV_REPEAT_RS = new IfxType(CREDIT_PURCHASE_REV_REPEAT_RS_VALUE);
	public static final IfxType CREDIT_BAL_INQ_RQ = new IfxType(CREDIT_BAL_INQ_RQ_VALUE);
	public static final IfxType CREDIT_BAL_INQ_RS = new IfxType(CREDIT_BAL_INQ_RS_VALUE);
	
	public static final IfxType SORUSH_REV_REPEAT_RQ = new IfxType(SORUSH_REV_REPEAT_RQ_VALUE);
	public static final IfxType SORUSH_REV_REPEAT_RS = new IfxType(SORUSH_REV_REPEAT_RS_VALUE);
	
	public static final IfxType SHAPARAK_CONFIRM_RQ = new IfxType(SHAPARAK_CONFIRM_RQ_VALUE);
	public static final IfxType SHAPARAK_CONFIRM_RS = new IfxType(SHAPARAK_CONFIRM_RS_VALUE);
	
	public static final IfxType MASKAN_DEPOSIT_RQ = new IfxType(MASKAN_DEPOSIT_RQ_VALUE);
	public static final IfxType MASKAN_DEPOSIT_RS = new IfxType(MASKAN_DEPOSIT_RS_VALUE);
	

	public static final IfxType SHEBA_INQ_RQ = new IfxType(SHEBA_INQ_RQ_VALUE);
	public static final IfxType SHEBA_REV_REPEAT_RQ = new IfxType(SHEBA_REV_REPEAT_RQ_VALUE);
	public static final IfxType SHEBA_INQ_RS = new IfxType(SHEBA_INQ_RS_VALUE);
	public static final IfxType SHEBA_REV_REPEAT_RS = new IfxType(SHEBA_REV_REPEAT_RS_VALUE);
	
	public static final IfxType HOTCARD_INQ_RQ = new IfxType(HOTCARD_INQ_RQ_VALUE);
	public static final IfxType HOTCARD_REV_REPEAT_RQ = new IfxType(HOTCARD_REV_REPEAT_RQ_VALUE);
	public static final IfxType HOTCARD_INQ_RS = new IfxType(HOTCARD_INQ_RS_VALUE);
	public static final IfxType HOTCARD_REV_REPEAT_RS = new IfxType(HOTCARD_REV_REPEAT_RS_VALUE);
	
	//TASK Task074 : 
	public static final IfxType ATM_STATUS_MONITOR_REQUEST = new IfxType(ATM_STATUS_MONITOR_REQUEST_VALUE);
	public static final IfxType ATM_STATUS_MONITOR_RESPONSE = new IfxType(ATM_STATUS_MONITOR_RESPONSE_VALUE);
	
	//TASK Task081 : ATM Saham feautre
	public static final IfxType STOCK_INQ_RQ = new IfxType(STOCK_INQ_RQ_VALUE);
	public static final IfxType STOCK_REV_REPEAT_RQ = new IfxType(STOCK_REV_REPEAT_RQ_VALUE);
	public static final IfxType STOCK_INQ_RS = new IfxType(STOCK_INQ_RS_VALUE);
	public static final IfxType STOCK_REV_REPEAT_RS = new IfxType(STOCK_REV_REPEAT_RS_VALUE);
	
	// TASK Task129 [26604] - Authenticate Cart (Pasargad)
	public static final IfxType CARD_AUTHENTICATE_RQ = new IfxType(CARD_AUTHENTICATE_RQ_VALUE);
	public static final IfxType CARD_AUTHENTICATE_REV_REPEAT_RQ = new IfxType(CARD_AUTHENTICATE_REV_REPEAT_RQ_VALUE);
	public static final IfxType CARD_AUTHENTICATE_RS = new IfxType(CARD_AUTHENTICATE_RS_VALUE);
	public static final IfxType CARD_AUTHENTICATE_REV_REPEAT_RS = new IfxType(CARD_AUTHENTICATE_REV_REPEAT_RS_VALUE);
	
	//ghasedak
	public static final IfxType GHASEDAK_RQ = new IfxType(GHASEDAK_RQ_VAULE);
	public static final IfxType GHASEDAK_RS = new IfxType(GHASEDAK_RS_VAULE);

	public static final List<Integer> RsOrdinals = new ArrayList<Integer>();
	public static final List<Integer> RqOrdinals = new ArrayList<Integer>();
	public static final List<Integer> RevRsOrdinals = new ArrayList<Integer>();
	public static final List<Integer> RevRqOrdinals = new ArrayList<Integer>();
	public static final List<Integer> RepeatOrdinals = new ArrayList<Integer>();
	public static final List<Integer> transferOrdinals = new ArrayList<Integer>();
	public static final List<Integer> AdviceRsOrdinals = new ArrayList<Integer>();
	public static final Map<Integer, String> valueToNameMap = new HashMap<Integer, String>();
	public static final Map<IfxType,IfxType> rqToRs = new HashMap<IfxType, IfxType>();
	public static final Map<IfxType,IfxType> rqToRevRq = new HashMap<IfxType, IfxType>();
	
	
	public static final String strRsOrdinals;
	public static final String strRqOrdinals;
	public static final String strRevRsOrdinals;
	public static final String strRevRqOrdinals;
	public static final String strRepeatOrdinals;
	public static final String strAdviceRsOrdinals;
	
	
	public static final IfxType TRANSFER_CARD_TO_ACCOUNT_RQ = new IfxType(TRANSFER_CARD_TO_ACCOUNT_RQ_VALUE);
	public static final IfxType TRANSFER_CARD_TO_ACCOUNT_RS = new IfxType(TRANSFER_CARD_TO_ACCOUNT_RS_VALUE);
	public static final IfxType TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ = new IfxType(TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ_VALUE);
	public static final IfxType TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS = new IfxType(TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS_VALUE);
	public static final IfxType TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ = new IfxType(TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ_VALUE);
	public static final IfxType TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ= new IfxType(TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ_VALUE);
	public static final IfxType TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS = new IfxType(TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS_VALUE);
	public static final IfxType TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RS = new IfxType(TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RS_VALUE);
	public static final IfxType TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ = new IfxType(TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ_VLAUE);
	public static final IfxType TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ = new IfxType(TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ_VLAUE);
	public static final IfxType TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS = new IfxType(TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS_VLAUE);
	public static final IfxType TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS = new IfxType(TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS_VLAUE);
	
	public static final IfxType THIRD_PARTY_PURCHASE_RQ = new IfxType(THIRD_PARTY_PURCHASE_RQ_VALUE);
	public static final IfxType THIRD_PARTY_PURCHASE_REV_REPEAT_RQ = new IfxType(THIRD_PARTY_PURCHASE_REV_REPEAT_RQ_VALUE);
	public static final IfxType THIRD_PARTY_PURCHASE_RS = new IfxType(THIRD_PARTY_PURCHASE_RS_VALUE);
	public static final IfxType THIRD_PARTY_PURCHASE_REV_REPEAT_RS = new IfxType(THIRD_PARTY_PURCHASE_REV_REPEAT_RS_VALUE);
	
	public static final IfxType PREPARE_THIRD_PARTY_PURCHASE = new IfxType(PREPARE_THIRD_PARTY_PURCHASE_VALUE);
	public static final IfxType PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT = new IfxType(PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT_VALUE);
	
	//Mirkamali(Task175): RESTRICTION
	public static final IfxType PREPARE_RESTRICTION = new IfxType(PREPARE_RESTRICTION_VALUE);
	public static final IfxType PREPARE_RESTRICTION_REV_REPEAT = new IfxType(PREPARE_RESTRICTION_REV_REPEAT_VALUE);
	public static final IfxType RESTRICTION_RQ = new IfxType(RESTRICTION_RQ_VALUE);
	public static final IfxType RESTRICTION_REV_REPEAT_RQ = new IfxType(RESTRICTION_REV_REPEAT_RQ_VALUE);
	public static final IfxType RESTRICTION_RS = new IfxType(RESTRICTION_RS_VALUE);
	public static final IfxType RESTRICTION_REV_REPEAT_RS = new IfxType(RESTRICTION_REV_REPEAT_RS_VALUE);
	
	//Mirkamali(Task179): Currency ATM
	public static final IfxType PREPARE_WITHDRAWAL = new IfxType(PREPARE_WITHDRAWAL_VALUE);
	public static final IfxType PREPARE_WITHDRAWAL_REV_REPEAT = new IfxType(PREPARE_WITHDRAWAL_REV_REPEAT_VALUE);
	public static final IfxType WITHDRAWAL_CUR_RQ = new IfxType(WITHDRAWAL_CUR_RQ_VALUE);
	public static final IfxType WITHDRAWAL_CUR_RS = new IfxType(WITHDRAWAL_CUR_RS_VALUE);
	public static final IfxType WITHDRAWAL_CUR_REV_REPEAT_RQ = new IfxType(WITHDRAWAL_CUR_REV_REPEAT_RQ_VALUE);
	public static final IfxType WITHDRAWAL_CUR_REV_REPEAT_RS = new IfxType(WITHDRAWAL_CUR_REV_REPEAT_RS_VALUE);
	
	public static final IfxType KEY_EXCHANGE_RQ = new IfxType(KEY_EXCHANGE_RQ_VALUE);
	public static final IfxType KEY_EXCHANGE_RS = new IfxType(KEY_EXCHANGE_RS_VALUE);
	public static final IfxType SYSTEM_INITIALIZE_RQ = new IfxType(SYSTEM_INITIALIZE_RQ_VALUE); //Raza INITIALIZE
	public static final IfxType SYSTEM_INITIALIZE_RS = new IfxType(SYSTEM_INITIALIZE_RS_VALUE); //Raza INITIALIZE
	public static final IfxType PREAUTH_RQ = new IfxType(PREAUTH_RQ_VALUE);
	public static final IfxType PREAUTH_RS = new IfxType(PREAUTH_RS_VALUE);
	public static final IfxType PREAUTH_COMPLET_RQ = new IfxType(PREAUTH_COMPLET_RQ_VALUE);
	public static final IfxType PREAUTH_COMPLET_RS = new IfxType(PREAUTH_COMPLET_RS_VALUE);
	public static final IfxType PREAUTH_COMPLET_REV_REPEAT_RQ = new IfxType(PREAUTH_COMPLET_REV_REPEAT_RQ_VALUE);
	public static final IfxType PREAUTH_COMPLET_REV_REPEAT_RS = new IfxType(PREAUTH_COMPLET_REV_REPEAT_RS_VALUE);
	public static final IfxType PREAUTH_COMPLET_CANCEL_RQ = new IfxType(PREAUTH_COMPLET_CANCEL_RQ_VALUE);
	public static final IfxType PREAUTH_COMPLET_CANCEL_RS = new IfxType(PREAUTH_COMPLET_CANCEL_RS_VALUE);
	public static final IfxType PREAUTH_COMPLET_CANCEL_REV_REPEAT_RQ = new IfxType(PREAUTH_COMPLET_CANCEL_REV_REPEAT_RQ_VALUE);
	public static final IfxType PREAUTH_COMPLET_CANCEL_REV_REPEAT_RS = new IfxType(PREAUTH_COMPLET_CANCEL_REV_REPEAT_RS_VALUE);
	public static final IfxType PREAUTH_CANCEL_RQ = new IfxType(PREAUTH_CANCEL_RQ_VALUE);
	public static final IfxType PREAUTH_CANCEL_RS = new IfxType(PREAUTH_CANCEL_RS_VALUE);
	public static final IfxType PREAUTH_CANCEL_REV_REPEAT_RQ = new IfxType(PREAUTH_CANCEL_REV_REPEAT_RQ_VALUE);
	public static final IfxType PREAUTH_CANCEL_REV_REPEAT_RS = new IfxType(PREAUTH_CANCEL_REV_REPEAT_RS_VALUE);
	public static final IfxType PURCHASE_CANCEL_RQ = new IfxType(PURCHASE_CANCEL_RQ_VALUE);
	public static final IfxType PURCHASE_CANCEL_RS = new IfxType(PURCHASE_CANCEL_RS_VALUE);
	public static final IfxType PURCHASE_CANCEL_REV_REPEAT_RQ = new IfxType(PURCHASE_CANCEL_REV_REPEAT_RQ_VALUE);
	public static final IfxType PURCHASE_CANCEL_REV_REPEAT_RS = new IfxType(PURCHASE_CANCEL_REV_REPEAT_RS_VALUE);
	public static final IfxType REFUND_ADVICE_RQ = new IfxType(REFUND_ADVICE_RQ_VALUE);
	public static final IfxType REFUND_ADVICE_RS = new IfxType(REFUND_ADVICE_RS_VALUE);
	public static final IfxType PREAUTH_REV_REPEAT_RQ = new IfxType(PREAUTH_REV_REPEAT_RQ_VALUE);
	public static final IfxType PREAUTH_REV_REPEAT_RS = new IfxType(PREAUTH_REV_REPEAT_RS_VALUE);
	public static final IfxType PREAUTH_COMPLET_ADVICE_RQ = new IfxType(PREAUTH_COMPLET_ADVICE_RQ_VALUE);
	public static final IfxType PREAUTH_COMPLET_ADVICE_RS = new IfxType(PREAUTH_COMPLET_ADVICE_RS_VALUE);
	public static final IfxType SIGN_ON_RS = new IfxType(SIGN_ON_RS_VALUE);
	public static final IfxType SIGN_OFF_RS = new IfxType(SIGN_OFF_RS_VALUE);
	public static final IfxType RESET_CONFIG_ID_BY_SUPPLY_COUNTER_REQUEST = new IfxType(RESET_CONFIG_ID_BY_SUPPLY_COUNTER_REQUEST_VALUE);
	public static final IfxType DIRECT_DEBIT_RQ = new IfxType(DIRECT_DEBIT_RQ_VALUE);
	public static final IfxType DIRECT_DEBIT_RS = new IfxType(DIRECT_DEBIT_RS_VALUE);
	public static final IfxType MONEY_SEND_RQ = new IfxType(MONEY_SEND_RQ_VALUE);
	public static final IfxType MONEY_SEND_RS = new IfxType(MONEY_SEND_RS_VALUE);
	public static final IfxType IBFT_ADVICE_RQ = new IfxType(IBFT_ADVICE_RQ_VALUE);
	public static final IfxType IBFT_ADVICE_RS = new IfxType(IBFT_ADVICE_RS_VALUE);
	public static final IfxType TITLE_FETCH_RQ = new IfxType(TITLE_FETCH_RQ_VALUE);
	public static final IfxType TITLE_FETCH_RS = new IfxType(TITLE_FETCH_RS_VALUE);
	public static final IfxType ORIGINAL_CREDIT_RQ = new IfxType(ORIGINAL_CREDIT_RQ_VALUE);
	public static final IfxType ORIGINAL_CREDIT_RS = new IfxType(ORIGINAL_CREDIT_RS_VALUE);
	//m.rehman: for Loro advice
	public static final IfxType LORO_ADVICE_RQ = new IfxType(LORO_ADVICE_RQ_VALUE);
	public static final IfxType LORO_ADVICE_RS = new IfxType(LORO_ADVICE_RS_VALUE);
	public static final IfxType LORO_REVERSAL_REPEAT_RQ = new IfxType(LORO_REVERSAL_REPEAT_RQ_VALUE);
	public static final IfxType LORO_REVERSAL_REPEAT_RS = new IfxType(LORO_REVERSAL_REPEAT_RS_VALUE);

	public static final IfxType WALLET_TOPUP_RQ = new IfxType(WALLET_TOPUP_RQ_VALUE);
	public static final IfxType WALLET_TOPUP_RS = new IfxType(WALLET_TOPUP_RS_VALUE);
	public static final IfxType WALLET_TOPUP_REV_REPEAT_RQ = new IfxType(WALLET_TOPUP_REV_REPEAT_RQ_VALUE);
	public static final IfxType WALLET_TOPUP_REV_REPEAT_RS = new IfxType(WALLET_TOPUP_REV_REPEAT_RS_VALUE);

	public static final IfxType CVV_GENERATION_RQ = new IfxType(CVV_GENERATION_RQ_VALUE);
	public static final IfxType CVV_GENERATION_RS = new IfxType(CVV_GENERATION_RS_VALUE);
	//m.rehman: for void transaction from NAC
	public static final IfxType VOID_RQ = new IfxType(VOID_RQ_VALUE);
	public static final IfxType VOID_RS = new IfxType(VOID_RS_VALUE);
	public static final IfxType REFUND_RQ = new IfxType(REFUND_RQ_VALUE);
	public static final IfxType REFUND_RS = new IfxType(REFUND_RS_VALUE);
	public static final IfxType REFUND_REVERSAL_REPEAT_RQ = new IfxType(REFUND_REVERSAL_REPEAT_RQ_VALUE);
	public static final IfxType REFUND_REVERSAL_REPEAT_RS = new IfxType(REFUND_REVERSAL_REPEAT_RS_VALUE);
	public static final IfxType OFFLINE_TIP_ADJUST_REPEAT_RQ = new IfxType(OFFLINE_TIP_ADJUST_REPEAT_RQ_VALUE);
	public static final IfxType OFFLINE_TIP_ADJUST_REPEAT_RS = new IfxType(OFFLINE_TIP_ADJUST_REPEAT_RS_VALUE);

	//Raza NayaPay start
	public static final IfxType CREATEWALLETLEVEL_ZERO_RQ = new IfxType(CREATEWALLETLEVEL_ZERO_RQ_VALUE);
	public static final IfxType CREATEWALLETLEVEL_ONE_RQ = new IfxType(CREATEWALLETLEVEL_ONE_RQ_VALUE);
	public static final IfxType CREATEWALLETLEVEL_TWO_RQ = new IfxType(CREATEWALLETLEVEL_TWO_RQ_VALUE);
	public static final IfxType UPDATEUSERPROFILE_RQ = new IfxType(UPDATEUSERPROFILE_RQ_VALUE);
	public static final IfxType LINKBANKACCOUNT_RQ = new IfxType(LINKBANKACCOUNT_RQ_VALUE);
	public static final IfxType UNLINKBANKACCOUNT_RQ = new IfxType(UNLINKBANKACCOUNT_RQ_VALUE);
	public static final IfxType CONFIRMOTP_RQ = new IfxType(CONFIRMOTP_RQ_VALUE);
	public static final IfxType REQUESTDEBITCARDTXN_RQ = new IfxType(REQUESTDEBITCARDTXN_RQ_VALUE);
	public static final IfxType REQUESTDEBITCARD_RQ = new IfxType(REQUESTDEBITCARD_RQ_VALUE);
	public static final IfxType ACTIVATEDEBITCARD_RQ = new IfxType(ACTIVATEDEBITCARD_RQ_VALUE);
	public static final IfxType ENABLEDEBITCARD_RQ = new IfxType(ENABLEDEBITCARD_RQ_VALUE);
	public static final IfxType LOADWALLET_RQ = new IfxType(LOADWALLET_RQ_VALUE);
	public static final IfxType WALLETTXN_RQ = new IfxType(WALLETTXN_RQ_VALUE);
	public static final IfxType WALLETBALANCE_RQ = new IfxType(WALLETBALANCE_RQ_VALUE);
	public static final IfxType UNLOADWALLET_RQ = new IfxType(UNLOADWALLET_RQ_VALUE);
	public static final IfxType MERCHANTCORETXN_RQ = new IfxType(MERCHANTCORETXN_RQ_VALUE);
	public static final IfxType MERCHANTTXN_RQ = new IfxType(MERCHANTTXN_RQ_VALUE);
	public static final IfxType ATMTXNLOG_RQ = new IfxType(ATMTXNLOG_RQ_VALUE);
	public static final IfxType GETDEBITCARDREQUEST_RQ = new IfxType(GETDEBITCARDREQUEST_RQ_VALUE);
	public static final IfxType GETWALLETSTATUS_RQ = new IfxType(GETWALLETSTATUS_RQ_VALUE);
	public static final IfxType CONFIRMFRAUDOTP_RQ = new IfxType(CONFIRMFRAUDOTP_RQ_VALUE);
	//Raza NayaPay end

	static {
		Field[] list = IfxType.class.getFields();
		Method getType = null;
		try {
			getType = IfxType.class.getMethod("getType");
		} catch (SecurityException e2) {
			e2.printStackTrace();
		} catch (NoSuchMethodException e2) {
			e2.printStackTrace();
		}
		for (Field e : list) {
			String name = e.getName().toUpperCase();
			try {
				if (e.getName().equalsIgnoreCase("RSORDINALS") 
						|| e.getName().equalsIgnoreCase("RQORDINALS")
						|| e.getName().equalsIgnoreCase("REVRSORDINALS")
						|| e.getName().equalsIgnoreCase("REVRQORDINALS")
						|| e.getName().equalsIgnoreCase("REPEATORDINALS")
						|| e.getName().equalsIgnoreCase("TRANSFERORDINALS")
						|| e.getName().equalsIgnoreCase("VALUETONAMEMAP") 
						|| e.getName().equalsIgnoreCase("RQTORS")
						|| e.getName().equalsIgnoreCase("RQTOREVRQ")
						|| e.getName().equalsIgnoreCase("ADVICERSORDINALS")
						|| e.getName().equalsIgnoreCase("strRsOrdinals")
						|| e.getName().equalsIgnoreCase("strRqOrdinals")
						|| e.getName().equalsIgnoreCase("strRevRsOrdinals")
						|| e.getName().equalsIgnoreCase("strRevRqOrdinals")
						|| e.getName().equalsIgnoreCase("strRepeatOrdinals")
						|| e.getName().equalsIgnoreCase("strAdviceRsOrdinals")
						)
					continue;
				
				Integer value = (Integer) getType.invoke(e.get(null), (Object[])null);
				if (name.contains(("REV_RS")) || name.contains("REPEAT_RS")) {
					RevRsOrdinals.add(value);
				}
				if (name.contains("REV_RQ") || name.contains("REPEAT_RQ")) {
					RevRqOrdinals.add(value);
				}
				if(name.contains("ADVICE_RS"))
				{
					AdviceRsOrdinals.add(value);
				}
				
				if (name.contains("REPEAT")) {
					RepeatOrdinals.add(value);
				}
				if (name.startsWith("TRANSFER")) {
					transferOrdinals.add(value);
				}
				// An enum might have Rs in it's name!!!
				if (name.lastIndexOf("RQ") > name.lastIndexOf("RS")) {
					RqOrdinals.add(value);
					int lastIndex = name.lastIndexOf("RQ");
			        String rs = name.substring(0, lastIndex) + name.substring(lastIndex + 2)+ "RS";
			        String rev = name.substring(0, lastIndex) + name.substring(lastIndex + 2)+ "REV_REPEAT_RQ";
					rqToRs.put((IfxType) e.get(null), valueOf(rs));
					rqToRevRq.put((IfxType) e.get(null), valueOf(rev));
				} else if (name.endsWith("RS")) {
					RsOrdinals.add(value);
				}
				valueToNameMap.put(value, name);
			} catch (Exception ex) {
				Exception e1 = new Exception("error in getting value of "+ name, ex);
				e1.printStackTrace();
			}
		}
		
		strRsOrdinals = getRsOrdinalsCollectionString();
		strRqOrdinals = getRqOrdinalsCollectionString();
		strRevRsOrdinals = getRevRsOrdinalsCollectionString();
		strRevRqOrdinals = getRevRqOrdinalsCollectionString();
		strRepeatOrdinals = getRepeatOrdinalsCollectionString();
		strAdviceRsOrdinals = getAdviceRsOrdinalsCollectionString();
	}

	
	public String getName() {
		String name = "";

		if (ISOFinalMessageType.isReversalMessage(this))
			name += "بازگشت: ";

		if (ISOFinalMessageType.isPurchaseMessage(this) || ISOFinalMessageType.isPurchaseReverseMessage(this)) {
			name += "خرید";
		} else if (ISOFinalMessageType.isLastPurchaseChargeMessage(this)) {
			name += "پرينت آخرین شارژ";
		} else if (ISOFinalMessageType.isBillPaymentMessage(this) || ISOFinalMessageType.isBillPaymentReverseMessage(this)) {
			name += "پرداخت قبض";
		} else if (ISOFinalMessageType.isReturnMessage(this) || ISOFinalMessageType.isReturnReverseMessage(this)) {
			name += "برگشت";
		} else if (ISOFinalMessageType.isPurchaseChargeMessage(this) || ISOFinalMessageType.isPurchaseChargeReverseMessage(this)) {
			name += "خريد شارژ";
		} else if (ISOFinalMessageType.isPurchaseTopupMessage(this) || ISOFinalMessageType.isPurchaseTopupReverseMessage(this)) {
			name += "خريد شارژ topup";
		} else if (ISOFinalMessageType.isBalanceInqueryMessage((this))) {
			name += "مانده";
		} else if (ISOFinalMessageType.isTransferFromMessage(this) || ISOFinalMessageType.isTransferFromRevMessage(this)){
			name += "انتقال از کارت";
		} else if (ISOFinalMessageType.isTransferToMessage(this) || ISOFinalMessageType.isTransferToMessage(this)){
			name += "انتقال به کارت";
		} else if (ISOFinalMessageType.isWithdrawalMessage(this) || ISOFinalMessageType.isWithdrawalRevMessage(this)){
			name += "برداشت";
		} else if (ISOFinalMessageType.isTransferMessage(this)){
			name += "انتقال";
		}

		return name;
	}
	
	public static Map<Integer, String> getValueToNameMap() {
		return valueToNameMap;
	}

	public static List<Integer> getRsOrdinals() {
		return RsOrdinals;
	}

	public static List<Integer> getRqOrdinals() {
		return RqOrdinals;
	}

	public static List<Integer> getRevRsOrdinals() {
		return RevRsOrdinals;
	}

	public static List<Integer> getAdviceRsOrdinals() {
		return AdviceRsOrdinals;
	}
	public static List<Integer> getRevRqOrdinals() {
		return RevRqOrdinals;
	}

	public static List<Integer> getRepeatOrdinals() {
		return RepeatOrdinals;
	}

	private static String getRepeatOrdinalsCollectionString() {
		StringBuilder out = new StringBuilder();
		out.append("(");
		for (Integer i : RepeatOrdinals)
			out.append(i.toString() + " ,");

		return out.substring(0, out.length() - 1) + ")";
	}

	private static String getRevRqOrdinalsCollectionString() {
		StringBuilder out = new StringBuilder();
		out.append("(");
		for (Integer i : RevRqOrdinals)
			out.append(i.toString() + " ,");

		return out.substring(0, out.length() - 1) + ")";
	}

	public static String getRsOrdinalsCollectionString() {
		StringBuilder out = new StringBuilder();
		out.append("(");
		for (Integer i : RsOrdinals)
			out.append(i.toString() + " ,");

		return out.substring(0, out.length() - 1) + ")";
	}

	private static String getRqOrdinalsCollectionString() {
		StringBuilder out = new StringBuilder();
		out.append("(");
		for (Integer i : RqOrdinals)
			out.append(i.toString() + " ,");

		return out.substring(0, out.length() - 1) + ")";
	}

	private static String getRevRsOrdinalsCollectionString() {
		StringBuilder out = new StringBuilder();
		out.append("(");
		for (Integer i : RevRsOrdinals)
			out.append(i.toString() + " ,");

		return out.substring(0, out.length() - 1) + ")";
	}

	private static String getAdviceRsOrdinalsCollectionString() {
		StringBuilder out = new StringBuilder();
		out.append("(");
		for (Integer i : AdviceRsOrdinals)
			out.append(i.toString() + " ,");

		return out.substring(0, out.length() - 1) + ")";
	}
	
	public static List<Integer> getTransferOrdinals() {
		return transferOrdinals;
	}

	@Override
	public String toString() {
		return valueToNameMap.get(this.type);
	}

	public static IfxType valueOf(String name) {
		try {
			Field field = null;
			field = IfxType.class.getField(name);

			if (field != null)
				return (IfxType) field.get(null);
			else
				return IfxType.UNDEFINED;
		} catch (SecurityException e) {
			return IfxType.UNDEFINED;
		} catch (NoSuchFieldException e) {
			return IfxType.UNDEFINED;
		} catch (IllegalArgumentException e) {
			return IfxType.UNDEFINED;
		} catch (IllegalAccessException e) {
			return IfxType.UNDEFINED;
		}
	}

	public static Map<IfxType, IfxType> getRqToRs() {
		return rqToRs;
	}
	
	public static IfxType getResponseIfxType(IfxType requestType){
		return rqToRs.get(requestType);
	}

	public static Map<IfxType, IfxType> getRqToRevRq() {
		return rqToRevRq;
	}
	
	public static IfxType getReversalIfxType(IfxType requestType){
		return rqToRevRq.get(requestType);
	}
	
	public static TrnType getTrnType(IfxType ifxType){
		
		if(IfxType.PREPARE_BILL_PMT.equals(ifxType)||
				IfxType.PREPARE_BILL_PMT_REV_REPEAT.equals(ifxType))
			return TrnType.PREPARE_BILL_PMT;
		
		if(IfxType.PREPARE_THIRD_PARTY_PURCHASE.equals(ifxType) ||
				IfxType.PREPARE_THIRD_PARTY_PURCHASE_REV_REPEAT.equals(ifxType))
			return TrnType.PREPARE_THIRD_PARTY_PAYMENT;
		
		if(IfxType.THIRD_PARTY_PURCHASE_RQ.equals(ifxType) ||
				IfxType.THIRD_PARTY_PURCHASE_REV_REPEAT_RQ.equals(ifxType)||
				IfxType.THIRD_PARTY_PURCHASE_RS.equals(ifxType)||
				IfxType.THIRD_PARTY_PURCHASE_RS.equals(ifxType))
			return TrnType.THIRD_PARTY_PAYMENT;
		
		if(ISOFinalMessageType.isBillPaymentMessage(ifxType)||
				ISOFinalMessageType.isBillPaymentReverseMessage(ifxType))
			return TrnType.BILLPAYMENT;
		
		if(ISOFinalMessageType.isBalanceInqueryMessage(ifxType)||
				ISOFinalMessageType.isBalanceInqueryRevMessage(ifxType))
			return TrnType.BALANCEINQUIRY;
		
		if(IfxType.TRANSFER_RQ.equals(ifxType)||
				IfxType.TRANSFER_RS.equals(ifxType)||
				IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifxType)||
				IfxType.TRANSFER_REV_REPEAT_RS.equals(ifxType))
			return TrnType.TRANSFER;
		
		if(IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(ifxType)|
				IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifxType)||
				IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RQ.equals(ifxType)||
				IfxType.TRANSFER_CHECK_ACCOUNT_REV_REPEAT_RS.equals(ifxType))
			return TrnType.CHECKACCOUNT;
		if(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ.equals(ifxType)||
				IfxType.TRANSFER_CARD_TO_ACCOUNT_RS.equals(ifxType)||
				IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ.equals(ifxType)||
				IfxType.TRANSFER_CARD_TO_ACCOUNT_RS.equals(ifxType))
			return TrnType.TRANSFER_CARD_TO_ACCOUNT;
		if(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ.equals(ifxType)||
				IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS.equals(ifxType)||
				IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ.equals(ifxType)||
				IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RS.equals(ifxType))
			return TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT;

		
		if(ISOFinalMessageType.isTransferToMessage(ifxType)||
				ISOFinalMessageType.isTransferToRevMessage(ifxType))
			return TrnType.INCREMENTALTRANSFER;
		if(ISOFinalMessageType.isTransferToAccountTransferToMessage(ifxType)||
				ISOFinalMessageType.isTransferAccountToRevMessage(ifxType))
			return TrnType.INCREMENTALTRANSFER_CARD_TO_ACCOUNT;
		
		if(	ISOFinalMessageType.isTransferFromMessage(ifxType)||
				ISOFinalMessageType.isTransferFromRevMessage(ifxType))
			return TrnType.DECREMENTALTRANSFER;
		
		if(ISOFinalMessageType.isDepositMessage(ifxType)||
				IfxType.DEPOSIT_REV_REPEAT_RQ.equals(ifxType)||
				IfxType.DEPOSIT_REV_REPEAT_RS.equals(ifxType))
			return TrnType.DEPOSIT;
		
		if(ISOFinalMessageType.isDepositChechAccountMessage(ifxType))
			return TrnType.DEPOSIT_CHECK_ACCOUNT;
		
		if(ISOFinalMessageType.isReturnMessage(ifxType)||
				ISOFinalMessageType.isReturnReverseMessage(ifxType))
			return TrnType.RETURN;
		
		if(ISOFinalMessageType.isPurchaseMessage(ifxType)||
				ISOFinalMessageType.isPurchaseReverseMessage(ifxType))
			return TrnType.PURCHASE;
		
		if(ISOFinalMessageType.isPurchaseChargeMessage(ifxType)||
				ISOFinalMessageType.isPurchaseChargeReverseMessage(ifxType))
			return TrnType.PURCHASECHARGE;
		
		if(ISOFinalMessageType.isPurchaseTopupMessage(ifxType)||
				ISOFinalMessageType.isPurchaseTopupReverseMessage(ifxType))
			return TrnType.PURCHASETOPUP;
		
		if(ISOFinalMessageType.isLastPurchaseChargeMessage(ifxType))
			return TrnType.LASTPURCHASECHARGE;
	
		
		if(ISOFinalMessageType.isWithdrawalMessage(ifxType)||
				ISOFinalMessageType.isWithdrawalRevMessage(ifxType)||
				ISOFinalMessageType.isPartialDispenseMessage(ifxType)||
				ISOFinalMessageType.isPartialDispenseRevMessage(ifxType))
			return TrnType.WITHDRAWAL;
		
		if(ISOFinalMessageType.isOnlineBillPayment(ifxType)||
				IfxType.ONLINE_BILLPAYMENT_TRACKING_REV_REPEAT.equals(ifxType))
			return TrnType.ONLINE_BILLPAYMENT;
		
		if(ISOFinalMessageType.isPrepareOnlineBillPayment(ifxType)||
				IfxType.PREPARE_ONLINE_BILLPAYMENT_REV_REPEAT.equals(ifxType))
			return TrnType.PREPARE_ONLINE_BILLPAYMENT;
		
		if(ISOFinalMessageType.isCreditCardStatementMessage(ifxType)||
				IfxType.CREDIT_CARD_REV_REPEAT_RQ.equals(ifxType)||
				IfxType.CREDIT_CARD_REV_REPEAT_RS.equals(ifxType))
			return TrnType.CREDITCARDDATA;
		
		if(ISOFinalMessageType.isGetAccountMessage(ifxType))
			return TrnType.GETACCOUNT;
		
		if(IfxType.RECONCILIATION_RQ.equals(ifxType)||
				IfxType.RECONCILIATION_RS.equals(ifxType)||
				IfxType.RECONCILIATION_REPEAT_RQ.equals(ifxType)||
				IfxType.RECONCILIATION_REPEAT_RS.equals(ifxType)||
				IfxType.ACQUIRER_REC_RQ.equals(ifxType)||
				IfxType.ACQUIRER_REC_RS.equals(ifxType)||
				IfxType.ACQUIRER_REC_REPEAT_RQ.equals(ifxType)||
				IfxType.ACQUIRER_REC_REPEAT_RS.equals(ifxType)||
				IfxType.CARD_ISSUER_REC_RQ.equals(ifxType)||
				IfxType.CARD_ISSUER_REC_RS.equals(ifxType)||
				IfxType.CARD_ISSUER_REC_REPEAT_RQ.equals(ifxType)||
				IfxType.CARD_ISSUER_REC_REPEAT_RS.equals(ifxType))
			return TrnType.RECONCILIATION;
		//mac_key_change va pin_ky_change da in isnetworkmessage umade bagheie ham bayad ezafe beshe
		if(ISOFinalMessageType.isNetworkMessage(ifxType)||
				IfxType.MAC_REJECT.equals(ifxType)||
				IfxType.NETWORK_MGR_REPEAT_RQ.equals(ifxType)||
				IfxType.NETWORK_MGR_REPEAT_RS.equals(ifxType)||
				IfxType.NETWORK_MGR_RQ.equals(ifxType)||
				IfxType.NETWORK_MGR_RS.equals(ifxType)||
				IfxType.CUTOVER_RQ.equals(ifxType)||
				IfxType.CUTOVER_RS.equals(ifxType)||
				IfxType.CUTOVER_REPEAT_RQ.equals(ifxType)||
				IfxType.CUTOVER_REPEAT_RS.equals(ifxType)||
				IfxType.LOG_ON_RQ.equals(ifxType)||
				IfxType.LOG_ON_RS.equals(ifxType)||
				IfxType.RESET_PASSWORD_RQ.equals(ifxType)||
				IfxType.RESET_PASSWORD_RS.equals(ifxType)||
				IfxType.SIGN_OFF_RQ.equals(ifxType)||
				IfxType.SIGN_ON_RQ.equals(ifxType)||
				IfxType.ECHO_RQ.equals(ifxType)||
				IfxType.ECHO_RS.equals(ifxType)||
				IfxType.POS_CONFIRMATION.equals(ifxType)||
				IfxType.POS_FAILURE.equals(ifxType))
			return TrnType.NETWORKMANAGEMENT;
		
		if(IfxType.CANCEL.equals(ifxType))
			return TrnType.CANCEL;
//		TODO:felan change pin block ro nazashtim ta inke badan bezarim.
//		if(ShetabFinalMessageType.isChangePinBlockMessage(ifxType)){
//			List<TrnType> trntype=new ArrayList<TrnType>();
//			trntype.add(TrnType.CHANGEINTERNETPINBLOCK);
//			trntype.add(TrnType.CHANGEPINBLOCK);
//			return trntype;
//		}
			
		
		if(IfxType.PAYMENT_STATEMENT_RQ.equals(ifxType)||
				IfxType.PAYMENT_STATEMENT_RS.equals(ifxType)||
				IfxType.PAYMENT_STATEMENT_REV_REPEAT_RQ.equals(ifxType)||
				IfxType.PAYMENT_STATEMENT_REV_REPEAT_RS.equals(ifxType))
			return TrnType.PAYMENTSTATEMENT;
		//TODO: card to account ro nazahstam hanoz.bad az piade sazi bayad ezafe beshe!
		if(ISOFinalMessageType.isBankStatementMessage(ifxType)||
				ISOFinalMessageType.isBankStatementRevMessage(ifxType))
			return TrnType.BANKSTATEMENT;
		


		if (IfxType.SHEBA_INQ_RQ.equals(ifxType)||
				IfxType.SHEBA_INQ_RS.equals(ifxType)||
				IfxType.SHEBA_REV_REPEAT_RQ.equals(ifxType)||
				IfxType.SHEBA_REV_REPEAT_RS.equals(ifxType))
			return TrnType.SHEBAINQUIRY;
		
		if (ISOFinalMessageType.isPrepareTranferCardToAccountMessage(ifxType) ||
				ISOFinalMessageType.isPrepareTranferCardToAccountReversalMessage(ifxType)){
			return TrnType.PREPARE_TRANSFER_CARD_TO_ACCOUNT;
		}
		
		if (IfxType.HOTCARD_INQ_RQ.equals(ifxType)||
				IfxType.HOTCARD_INQ_RS.equals(ifxType)||
				IfxType.HOTCARD_REV_REPEAT_RQ.equals(ifxType)||
				IfxType.HOTCARD_REV_REPEAT_RS.equals(ifxType))
			return TrnType.HOTCARD;
		
		//TASK Task081 : ATM Saham Feature
		if (IfxType.STOCK_INQ_RQ.equals(ifxType)||
				IfxType.STOCK_INQ_RS.equals(ifxType)||
				IfxType.STOCK_REV_REPEAT_RQ.equals(ifxType)||
				IfxType.STOCK_REV_REPEAT_RS.equals(ifxType))
			return TrnType.STOCK;		
		
		//ghasedak
		if(IfxType.GHASEDAK_RQ.equals(ifxType) || IfxType.GHASEDAK_RS.equals(ifxType))
			return TrnType.NONFINANCIAL_INFO;
		
		// TASK Task129 [26604] - Authenticate Cart (Pasargad)
		if(IfxType.CARD_AUTHENTICATE_RQ.equals(ifxType) ||
				IfxType.CARD_AUTHENTICATE_RS.equals(ifxType) ||
				IfxType.CARD_AUTHENTICATE_REV_REPEAT_RQ.equals(ifxType) ||
				IfxType.CARD_AUTHENTICATE_REV_REPEAT_RS.equals(ifxType))
			return TrnType.CARD_AUTENTICATE;		

		//Mirkamali(Task175): Restriction
		if(IfxType.PREPARE_RESTRICTION.equals(ifxType) ||
				IfxType.PREPARE_RESTRICTION_REV_REPEAT.equals(ifxType))
			return TrnType.PREPARE_RESTRICTION;
		
		if(IfxType.RESTRICTION_RQ.equals(ifxType) ||
				IfxType.RESTRICTION_REV_REPEAT_RQ.equals(ifxType) ||
				IfxType.RESTRICTION_RS .equals(ifxType) ||
				IfxType.RESTRICTION_REV_REPEAT_RS.equals(ifxType))
			return TrnType.RESTRICTION;
		
		//Mirkamali(Task179): Currency ATM
		if(IfxType.PREPARE_WITHDRAWAL.equals(ifxType)||
				IfxType.PREPARE_WITHDRAWAL_REV_REPEAT.equals(ifxType))
			return TrnType.PREPARE_WITHDRAWAL;
		
		if(IfxType.WITHDRAWAL_CUR_RQ.equals(ifxType) ||
				IfxType.WITHDRAWAL_CUR_RS.equals(ifxType) ||
				IfxType.WITHDRAWAL_CUR_REV_REPEAT_RQ.equals(ifxType) ||
				IfxType.WITHDRAWAL_CUR_REV_REPEAT_RS.equals(ifxType))
			return TrnType.WITHDRAWAL_CUR;

		//m.rehman: for wallet transaction
		if(IfxType.WALLET_TOPUP_RQ.equals(ifxType) ||
				IfxType.WALLET_TOPUP_RS.equals(ifxType) ||
				IfxType.WALLET_TOPUP_REV_REPEAT_RQ.equals(ifxType) ||
				IfxType.WALLET_TOPUP_REV_REPEAT_RS.equals(ifxType))
			return TrnType.WALLET_TOPUP;

		//m.rehman: for void transaction from NAC
		if(IfxType.VOID_RQ.equals(ifxType) ||
				IfxType.VOID_RS.equals(ifxType))
			return TrnType.VOID;

		return TrnType.UNKNOWN;
	}
	
	public static String getIfxTypeOrdinalsOfList(List<IfxType> list) {
		StringBuilder queryString = new StringBuilder(); 
		queryString.append("(");
		boolean isFirst = true;
		for(IfxType ifxType:list){
			if(!isFirst){
				queryString.append(",");
			}
			isFirst = false;
			queryString.append(ifxType.getType());
		}
		
		queryString.append(")");
		return queryString.toString();
	}
}
