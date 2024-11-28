package vaulsys.protocols.cms.utils;

import static vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes.*;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class CMSMapperUtil {

	public static Map<Integer, IfxType> ToIfxType = new HashMap<Integer, IfxType>();
	static {
		ToIfxType.put(1, IfxType.BAL_INQ_RQ);
		ToIfxType.put(2, IfxType.WITHDRAWAL_RQ);
		ToIfxType.put(3, IfxType.BAL_INQ_RS);
		ToIfxType.put(6, IfxType.PURCHASE_RQ);
		ToIfxType.put(7, IfxType.WITHDRAWAL_RS);
		ToIfxType.put(8, IfxType.PURCHASE_RS);
		ToIfxType.put(9, IfxType.BILL_PMT_RQ);
		ToIfxType.put(11, IfxType.BILL_PMT_RS);
		ToIfxType.put(12, IfxType.BAL_REV_REPEAT_RQ);
		ToIfxType.put(13, IfxType.BAL_REV_REPEAT_RQ);
		ToIfxType.put(14, IfxType.BAL_REV_REPEAT_RS);
		ToIfxType.put(18, IfxType.PURCHASE_REV_REPEAT_RQ);
		ToIfxType.put(19, IfxType.PURCHASE_REV_REPEAT_RQ);
		ToIfxType.put(20, IfxType.PURCHASE_REV_REPEAT_RS);
		ToIfxType.put(21, IfxType.BILL_PMT_REV_REPEAT_RQ);
		ToIfxType.put(22, IfxType.BILL_PMT_REV_REPEAT_RQ);
		ToIfxType.put(23, IfxType.BILL_PMT_REV_REPEAT_RS);
		ToIfxType.put(24, IfxType.ACQUIRER_REC_RQ);
		ToIfxType.put(25, IfxType.ACQUIRER_REC_RS);
		ToIfxType.put(26, IfxType.ACQUIRER_REC_REPEAT_RQ);
		ToIfxType.put(27, IfxType.ACQUIRER_REC_REPEAT_RS);
		ToIfxType.put(28, IfxType.CARD_ISSUER_REC_RQ);
		ToIfxType.put(29, IfxType.CARD_ISSUER_REC_RS);
		ToIfxType.put(30, IfxType.CARD_ISSUER_REC_REPEAT_RQ);
		ToIfxType.put(31, IfxType.CARD_ISSUER_REC_REPEAT_RS);
		ToIfxType.put(32, IfxType.NETWORK_MGR_RQ);
		ToIfxType.put(33, IfxType.NETWORK_MGR_RS);
		ToIfxType.put(34, IfxType.NETWORK_MGR_REPEAT_RQ);
		ToIfxType.put(35, IfxType.NETWORK_MGR_REPEAT_RS);
		ToIfxType.put(36, IfxType.RECONCILIATION_RQ);
		ToIfxType.put(37, IfxType.RECONCILIATION_REPEAT_RQ);
		ToIfxType.put(38, IfxType.RECONCILIATION_RS);
		ToIfxType.put(39, IfxType.RETURN_RQ);
		ToIfxType.put(40, IfxType.RETURN_RS);
		ToIfxType.put(41, IfxType.RETURN_REV_REPEAT_RQ);
		ToIfxType.put(42, IfxType.RETURN_REV_REPEAT_RS);
		ToIfxType.put(43, IfxType.RETURN_REV_REPEAT_RQ);
		ToIfxType.put(44, IfxType.RETURN_REV_REPEAT_RS);
		ToIfxType.put(45, IfxType.BAL_REV_REPEAT_RS);
		ToIfxType.put(47, IfxType.PURCHASE_REV_REPEAT_RS);
		ToIfxType.put(48, IfxType.BILL_PMT_REV_REPEAT_RS);
		ToIfxType.put(49, IfxType.RECONCILIATION_REPEAT_RS);

		ToIfxType.put(50, IfxType.TRANSFER_RQ);
		ToIfxType.put(51, IfxType.TRANSFER_RS);
		ToIfxType.put(52, IfxType.TRANSFER_TO_ACCOUNT_RQ);
		ToIfxType.put(53, IfxType.TRANSFER_TO_ACCOUNT_RS);
		ToIfxType.put(54, IfxType.TRANSFER_FROM_ACCOUNT_RQ);
		ToIfxType.put(55, IfxType.TRANSFER_FROM_ACCOUNT_RS);
		ToIfxType.put(56, IfxType.TRANSFER_CHECK_ACCOUNT_RQ);
		ToIfxType.put(57, IfxType.TRANSFER_CHECK_ACCOUNT_RS);
		ToIfxType.put(58, IfxType.TRANSFER_REV_REPEAT_RQ);
		ToIfxType.put(59, IfxType.TRANSFER_REV_REPEAT_RS);
		ToIfxType.put(60, IfxType.TRANSFER_REV_REPEAT_RQ);
		ToIfxType.put(61, IfxType.TRANSFER_REV_REPEAT_RS);
		ToIfxType.put(62, IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ);
		ToIfxType.put(63, IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS);
		ToIfxType.put(64, IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ);
		ToIfxType.put(65, IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS);
		ToIfxType.put(66, IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ);
		ToIfxType.put(67, IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS);
		ToIfxType.put(68, IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ);
		ToIfxType.put(69, IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS);

//		ToIfxType.put(70, IfxType.TRANSFER_CHECK_ACCOUNT_RQ);
//		ToIfxType.put(71, IfxType.TRANSFER_CHECK_ACCOUNT_RS);
		ToIfxType.put(72, IfxType.WITHDRAWAL_REV_REPEAT_RQ);
		ToIfxType.put(73, IfxType.WITHDRAWAL_REV_REPEAT_RQ);
		ToIfxType.put(74, IfxType.WITHDRAWAL_REV_REPEAT_RS);
		ToIfxType.put(75, IfxType.WITHDRAWAL_REV_REPEAT_RS);
		ToIfxType.put(76, IfxType.GET_ACCOUNT_RQ);
		ToIfxType.put(77, IfxType.GET_ACCOUNT_RS);
		ToIfxType.put(78, IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		ToIfxType.put(79, IfxType.GET_ACCOUNT_REV_REPEAT_RS);
		ToIfxType.put(80, IfxType.GET_ACCOUNT_REV_REPEAT_RQ);
		ToIfxType.put(81, IfxType.GET_ACCOUNT_REV_REPEAT_RS);

		ToIfxType.put(82, IfxType.CHANGE_PIN_BLOCK_RQ);
		ToIfxType.put(83, IfxType.CHANGE_PIN_BLOCK_RS);
		ToIfxType.put(84, IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
		ToIfxType.put(85, IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS);
		ToIfxType.put(86, IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ);
		ToIfxType.put(87, IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS);

		ToIfxType.put(88, IfxType.PAYMENT_STATEMENT_RQ);
		ToIfxType.put(89, IfxType.PAYMENT_STATEMENT_RS);
		ToIfxType.put(90, IfxType.PAYMENT_STATEMENT_REV_REPEAT_RQ);
		ToIfxType.put(91, IfxType.PAYMENT_STATEMENT_REV_REPEAT_RS);
		ToIfxType.put(92, IfxType.PAYMENT_STATEMENT_REV_REPEAT_RQ);
		ToIfxType.put(93, IfxType.PAYMENT_STATEMENT_REV_REPEAT_RS);

		ToIfxType.put(94, IfxType.BANK_STATEMENT_RQ);
		ToIfxType.put(95, IfxType.BANK_STATEMENT_RS);

		ToIfxType.put(96, IfxType.CREDIT_CARD_DATA_RQ);
		ToIfxType.put(97, IfxType.CREDIT_CARD_DATA_RS);
		
		//Mirkamali(Task175): Restriction
//		ToIfxType.put(98, IfxType.RESTRICTION_RQ);
//		ToIfxType.put(99, IfxType.RESTRICTION_RS);
		//ghlolami
		ToIfxType.put(282, IfxType.RESTRICTION_RQ);
		ToIfxType.put(283, IfxType.RESTRICTION_RS);
		ToIfxType.put(284, IfxType.RESTRICTION_REV_REPEAT_RQ);
		ToIfxType.put(285, IfxType.RESTRICTION_REV_REPEAT_RS);
		
		ToIfxType.put(209, IfxType.BANK_STATEMENT_REV_REPEAT_RQ);
		ToIfxType.put(210, IfxType.BANK_STATEMENT_REV_REPEAT_RS);

		ToIfxType.put(IfxType.DEPOSIT_CHECK_ACCOUNT_RQ.getType(), IfxType.DEPOSIT_CHECK_ACCOUNT_RQ);
		ToIfxType.put(IfxType.DEPOSIT_CHECK_ACCOUNT_RS.getType(), IfxType.DEPOSIT_CHECK_ACCOUNT_RS);
		ToIfxType.put(IfxType.DEPOSIT_RQ.getType(), IfxType.DEPOSIT_RQ);
		ToIfxType.put(IfxType.DEPOSIT_RS.getType(), IfxType.DEPOSIT_RS);
//		ToIfxType.put(IfxType.DEPOSIT_REV_REPEAT_RQ.getType(), IfxType.DEPOSIT_REV_REPEAT_RQ);
//		ToIfxType.put(IfxType.DEPOSIT_REV_REPEAT_RS.getType(), IfxType.DEPOSIT_REV_REPEAT_RS);

		ToIfxType.put(246,IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ);
		ToIfxType.put(248,IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS);
		ToIfxType.put(247,IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ);
		ToIfxType.put(249,IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RS);
		ToIfxType.put(242,IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ);
		ToIfxType.put(244,IfxType.TRANSFER_CARD_TO_ACCOUNT_RS);
		ToIfxType.put(243,IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ);
		ToIfxType.put(245,IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS);

		ToIfxType.put(251, IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ);
		ToIfxType.put(253, IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS);
		ToIfxType.put(250, IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ);
		ToIfxType.put(252, IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS);

		//AD
		ToIfxType.put(266, IfxType.SHEBA_INQ_RQ);
//		ToIfxType.put(268, IfxType.SHEBA_REV_REPEAT_RQ);
		ToIfxType.put(267, IfxType.SHEBA_INQ_RS);
//		ToIfxType.put(269, IfxType.SHEBA_REV_REPEAT_RS);	

		ToIfxType.put(268, IfxType.HOTCARD_INQ_RQ);
		ToIfxType.put(269, IfxType.HOTCARD_INQ_RS);
		
		//TASK Task081 : ATM Saham Feature
		ToIfxType.put(270, IfxType.STOCK_INQ_RQ); 
		ToIfxType.put(271, IfxType.STOCK_INQ_RS);	
		
		// TASK Task129 [26604] - Authenticate Cart (Pasargad)
		ToIfxType.put(280, IfxType.CARD_AUTHENTICATE_RQ); 
		ToIfxType.put(281, IfxType.CARD_AUTHENTICATE_RS);

		//m.rehman
		ToIfxType.put(282, IfxType.IBFT_ADVICE_RQ);
		ToIfxType.put(283, IfxType.IBFT_ADVICE_RS);

		//m.rehman: for Loro advice
		ToIfxType.put(384, IfxType.LORO_ADVICE_RQ);
		ToIfxType.put(385, IfxType.LORO_ADVICE_RS);
		ToIfxType.put(386, IfxType.LORO_REVERSAL_REPEAT_RQ);
		ToIfxType.put(387, IfxType.LORO_REVERSAL_REPEAT_RS);
		//m.rehman: for pre-auth and refund cases
        ToIfxType.put(291, IfxType.PREAUTH_RS);
        ToIfxType.put(293, IfxType.PREAUTH_COMPLET_RS);
        ToIfxType.put(295, IfxType.PREAUTH_COMPLET_REV_REPEAT_RS);
        ToIfxType.put(297, IfxType.PREAUTH_COMPLET_CANCEL_RS);
        ToIfxType.put(299, IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RS);
        ToIfxType.put(301, IfxType.PREAUTH_CANCEL_RS);
        ToIfxType.put(303, IfxType.PREAUTH_CANCEL_REV_REPEAT_RS);
        ToIfxType.put(305, IfxType.PURCHASE_CANCEL_RS);
        ToIfxType.put(307, IfxType.PURCHASE_CANCEL_REV_REPEAT_RS);
        ToIfxType.put(309, IfxType.REFUND_ADVICE_RS);
        ToIfxType.put(311, IfxType.PREAUTH_COMPLET_ADVICE_RS);
	}

	public static Map<IfxType, Integer> FromIfxType = new HashMap<IfxType, Integer>();
	static {
		FromIfxType.put(IfxType.BAL_INQ_RQ, 1);
		FromIfxType.put(IfxType.WITHDRAWAL_RQ, 2);
		FromIfxType.put(IfxType.BAL_INQ_RS, 3);
		FromIfxType.put(IfxType.PURCHASE_RQ, 6);
		FromIfxType.put(IfxType.WITHDRAWAL_RS, 7);
		FromIfxType.put(IfxType.PURCHASE_RS, 8);
		FromIfxType.put(IfxType.BILL_PMT_RQ, 9);
		FromIfxType.put(IfxType.BILL_PMT_RS, 11);
//		FromIfxType.put(IfxType.BAL_REV_RQ, 12);
		FromIfxType.put(IfxType.BAL_REV_REPEAT_RQ, 13);
//		FromIfxType.put(IfxType.BAL_REV_RS, 14);
//		FromIfxType.put(IfxType.PURCHASE_REV_RQ, 18);
		FromIfxType.put(IfxType.PURCHASE_REV_REPEAT_RQ, 19);
//		FromIfxType.put(IfxType.PURCHASE_REV_RS, 20);
//		FromIfxType.put(IfxType.BILL_PMT_REV_RQ, 21);
		FromIfxType.put(IfxType.BILL_PMT_REV_REPEAT_RQ, 22);
//		FromIfxType.put(IfxType.BILL_PMT_REV_RS, 23);
		FromIfxType.put(IfxType.ACQUIRER_REC_RQ, 24);
		FromIfxType.put(IfxType.ACQUIRER_REC_RS, 25);
		FromIfxType.put(IfxType.ACQUIRER_REC_REPEAT_RQ, 26);
		FromIfxType.put(IfxType.ACQUIRER_REC_REPEAT_RS, 27);
		FromIfxType.put(IfxType.CARD_ISSUER_REC_RQ, 28);
		FromIfxType.put(IfxType.CARD_ISSUER_REC_RS, 29);
		FromIfxType.put(IfxType.CARD_ISSUER_REC_REPEAT_RQ, 30);
		FromIfxType.put(IfxType.CARD_ISSUER_REC_REPEAT_RS, 31);
		FromIfxType.put(IfxType.NETWORK_MGR_RQ, 32);
		FromIfxType.put(IfxType.NETWORK_MGR_RS, 33);
		FromIfxType.put(IfxType.NETWORK_MGR_REPEAT_RQ, 34);
		FromIfxType.put(IfxType.NETWORK_MGR_REPEAT_RS, 35);
		FromIfxType.put(IfxType.RECONCILIATION_RQ, 36);
		FromIfxType.put(IfxType.RECONCILIATION_REPEAT_RQ, 37);
		FromIfxType.put(IfxType.RECONCILIATION_RS, 38);
		FromIfxType.put(IfxType.RETURN_RQ, 39);
		FromIfxType.put(IfxType.RETURN_RS, 40);
//		FromIfxType.put(IfxType.RETURN_REV_RQ, 41);
//		FromIfxType.put(IfxType.RETURN_REV_RS, 42);
		FromIfxType.put(IfxType.RETURN_REV_REPEAT_RQ, 43);
		FromIfxType.put(IfxType.RETURN_REV_REPEAT_RS, 44);
		FromIfxType.put(IfxType.BAL_REV_REPEAT_RS, 45);
		FromIfxType.put(IfxType.PURCHASE_REV_REPEAT_RS, 47);
		FromIfxType.put(IfxType.BILL_PMT_REV_REPEAT_RS, 48);
		FromIfxType.put(IfxType.RECONCILIATION_REPEAT_RS, 49);

		FromIfxType.put(IfxType.TRANSFER_RQ, 50);
		FromIfxType.put(IfxType.TRANSFER_RS, 51);
		FromIfxType.put(IfxType.TRANSFER_TO_ACCOUNT_RQ, 52);
		FromIfxType.put(IfxType.TRANSFER_TO_ACCOUNT_RS, 53);
		FromIfxType.put(IfxType.TRANSFER_FROM_ACCOUNT_RQ, 54);
		FromIfxType.put(IfxType.TRANSFER_FROM_ACCOUNT_RS, 55);
		FromIfxType.put(IfxType.TRANSFER_CHECK_ACCOUNT_RQ, 56);
		FromIfxType.put(IfxType.TRANSFER_CHECK_ACCOUNT_RS, 57);
//		FromIfxType.put(IfxType.TRANSFER_REV_RQ, 58);
//		FromIfxType.put(IfxType.TRANSFER_REV_RS, 59);
		FromIfxType.put(IfxType.TRANSFER_REV_REPEAT_RQ, 60);
		FromIfxType.put(IfxType.TRANSFER_REV_REPEAT_RS, 61);

//		FromIfxType.put(IfxType.TRANSFER_TO_ACCOUNT_REV_RQ, 62);
//		FromIfxType.put(IfxType.TRANSFER_TO_ACCOUNT_REV_RS, 63);
		FromIfxType.put(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RQ, 64);
		FromIfxType.put(IfxType.TRANSFER_TO_ACCOUNT_REV_REPEAT_RS, 65);

//		FromIfxType.put(IfxType.TRANSFER_FROM_ACCOUNT_REV_RQ, 66);
//		FromIfxType.put(IfxType.TRANSFER_FROM_ACCOUNT_REV_RS, 67);
		FromIfxType.put(IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RQ, 68);
		FromIfxType.put(IfxType.TRANSFER_FROM_ACCOUNT_REV_REPEAT_RS, 69);
//		FromIfxType.put(IfxType.TRANSFER_CHECK_ACCOUNT_RQ, 70);
//		FromIfxType.put(IfxType.TRANSFER_CHECK_ACCOUNT_RS, 71);

//		FromIfxType.put(IfxType.WITHDRAWAL_REV_RQ, 72);
		FromIfxType.put(IfxType.WITHDRAWAL_REV_REPEAT_RQ, 73);
//		FromIfxType.put(IfxType.WITHDRAWAL_REV_RS, 74);
		FromIfxType.put(IfxType.WITHDRAWAL_REV_REPEAT_RS, 75);

		FromIfxType.put(IfxType.GET_ACCOUNT_RQ, 76);
		FromIfxType.put(IfxType.GET_ACCOUNT_RS, 77);
//		FromIfxType.put(IfxType.GET_ACCOUNT_REV_RQ, 78);
//		FromIfxType.put(IfxType.GET_ACCOUNT_REV_RS, 79);
		FromIfxType.put(IfxType.GET_ACCOUNT_REV_REPEAT_RQ, 80);
		FromIfxType.put(IfxType.GET_ACCOUNT_REV_REPEAT_RS, 81);

		FromIfxType.put(IfxType.CHANGE_PIN_BLOCK_RQ, 82);
		FromIfxType.put(IfxType.CHANGE_PIN_BLOCK_RS, 83);
//		FromIfxType.put(IfxType.CHANGE_PIN_BLOCK_REV_RQ, 84);
//		FromIfxType.put(IfxType.CHANGE_PIN_BLOCK_REV_RS, 85);
		FromIfxType.put(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RQ, 86);
		FromIfxType.put(IfxType.CHANGE_PIN_BLOCK_REV_REPEAT_RS, 87);

		FromIfxType.put(IfxType.PAYMENT_STATEMENT_RQ, 88);
		FromIfxType.put(IfxType.PAYMENT_STATEMENT_RS, 89);
//		FromIfxType.put(IfxType.PAYMENT_STATEMENT_REV_RQ, 90);
//		FromIfxType.put(IfxType.PAYMENT_STATEMENT_REV_RS, 91);
		FromIfxType.put(IfxType.PAYMENT_STATEMENT_REV_REPEAT_RQ, 92);
		FromIfxType.put(IfxType.PAYMENT_STATEMENT_REV_REPEAT_RS, 93);

		FromIfxType.put(IfxType.BANK_STATEMENT_RQ, 94);
		FromIfxType.put(IfxType.BANK_STATEMENT_RS, 95);

		FromIfxType.put(IfxType.CREDIT_CARD_DATA_RQ, 96);
		FromIfxType.put(IfxType.CREDIT_CARD_DATA_RS, 97);
		
		//Mirkamali(Task175): Restriction
//		FromIfxType.put(IfxType.RESTRICTION_RQ, 98);
//		FromIfxType.put(IfxType.RESTRICTION_RS, 99);
		//ghlolami
		FromIfxType.put(IfxType.RESTRICTION_RQ, 282 );
		FromIfxType.put(IfxType.RESTRICTION_RS, 283);
		FromIfxType.put(IfxType.RESTRICTION_REV_REPEAT_RQ, 284);
		FromIfxType.put(IfxType.RESTRICTION_REV_REPEAT_RS, 285);
		
		FromIfxType.put(IfxType.DEPOSIT_CHECK_ACCOUNT_RQ, 167);
		FromIfxType.put(IfxType.DEPOSIT_CHECK_ACCOUNT_RS, 168);
		FromIfxType.put(IfxType.DEPOSIT_RQ, IfxType.DEPOSIT_RQ.getType());
		FromIfxType.put(IfxType.DEPOSIT_RS, IfxType.DEPOSIT_RS.getType());
//		FromIfxType.put(IfxType.DEPOSIT_REV_REPEAT_RQ, IfxType.DEPOSIT_REV_REPEAT_RQ.getType());
//		FromIfxType.put(IfxType.DEPOSIT_REV_REPEAT_RS, IfxType.DEPOSIT_REV_REPEAT_RS.getType());

		FromIfxType.put(IfxType.BANK_STATEMENT_REV_REPEAT_RQ, 209);
		FromIfxType.put(IfxType.BANK_STATEMENT_REV_REPEAT_RS, 210);

		FromIfxType.put(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RQ, 246);
		FromIfxType.put(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_RS, 248);
		FromIfxType.put(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RQ, 247);
		FromIfxType.put(IfxType.TRANSFER_CARD_TO_ACCOUNT_CHECK_ACCOUNT_REV_REPEAT_RS, 249);
		FromIfxType.put(IfxType.TRANSFER_CARD_TO_ACCOUNT_RQ, 242);
		FromIfxType.put(IfxType.TRANSFER_CARD_TO_ACCOUNT_RS, 244);
		FromIfxType.put(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RQ, 243);
		FromIfxType.put(IfxType.TRANSFER_CARD_TO_ACCOUNT_REV_REPEAT_RS, 245);

		FromIfxType.put(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RQ, 251);
		FromIfxType.put(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_REV_REPEAT_RS, 253);
		FromIfxType.put(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RQ, 250);
		FromIfxType.put(IfxType.TRANSFER_CARDTOACCOUNT_TO_ACCOUNT_RS, 252);

		//AD
		FromIfxType.put(IfxType.SHEBA_INQ_RQ, 266);
//		FromIfxType.put(IfxType.SHEBA_REV_REPEAT_RQ, 267);
		FromIfxType.put(IfxType.SHEBA_INQ_RS, 267);
//		FromIfxType.put(IfxType.SHEBA_REV_REPEAT_RS, 269);

		FromIfxType.put(IfxType.HOTCARD_INQ_RQ, 268);
		FromIfxType.put(IfxType.HOTCARD_INQ_RS, 269);
		
		//TASK Task081 : ATM Saham feautre
		FromIfxType.put(IfxType.STOCK_INQ_RQ, 270); //AldTODO Task081 : Add Monaseb Ra JayGozin Konim
		FromIfxType.put(IfxType.STOCK_INQ_RS, 271); //AldTODO Task081 : Add Monaseb Ra JayGozin Konim		

		// TASK Task129 [26604] - Authenticate Cart (Pasargad)
		FromIfxType.put(IfxType.CARD_AUTHENTICATE_RQ, 280);
		FromIfxType.put(IfxType.CARD_AUTHENTICATE_RS, 281);

		//m.rehman
		FromIfxType.put(IfxType.IBFT_ADVICE_RQ, 282);
		FromIfxType.put(IfxType.IBFT_ADVICE_RS, 283);

		//m.rehman: for Loro advice
		FromIfxType.put(IfxType.LORO_ADVICE_RQ, 384);
		FromIfxType.put(IfxType.LORO_ADVICE_RS, 385);
		FromIfxType.put(IfxType.LORO_REVERSAL_REPEAT_RQ, 386);
		FromIfxType.put(IfxType.LORO_REVERSAL_REPEAT_RS, 387);
        //m.rehman: for pre-auth and refund cases
        FromIfxType.put(IfxType.PREAUTH_RQ, 290);
        FromIfxType.put(IfxType.PREAUTH_COMPLET_RQ, 292);
        FromIfxType.put(IfxType.PREAUTH_COMPLET_REV_REPEAT_RQ, 294);
        FromIfxType.put(IfxType.PREAUTH_COMPLET_CANCEL_RQ, 296);
        FromIfxType.put(IfxType.PREAUTH_COMPLET_CANCEL_REV_REPEAT_RQ, 298);
        FromIfxType.put(IfxType.PREAUTH_CANCEL_RQ, 300);
        FromIfxType.put(IfxType.PREAUTH_CANCEL_REV_REPEAT_RQ, 302);
        FromIfxType.put(IfxType.PURCHASE_CANCEL_RQ, 304);
        FromIfxType.put(IfxType.PURCHASE_CANCEL_REV_REPEAT_RQ, 306);
        FromIfxType.put(IfxType.REFUND_ADVICE_RQ, 308);
        FromIfxType.put(IfxType.PREAUTH_COMPLET_ADVICE_RQ, 310);
	}

	public static Map<Integer, TrnType> ToTrnType = new HashMap<Integer, TrnType>();
	static {
		ToTrnType.put(0, TrnType.DEBIT);
		ToTrnType.put(1, TrnType.CREDIT);
		ToTrnType.put(2, TrnType.WITHDRAWAL);
		ToTrnType.put(3, TrnType.CHECKACCOUNT);
		ToTrnType.put(4, TrnType.DEPOSIT);
		ToTrnType.put(5, TrnType.TRANSFER);
		ToTrnType.put(6, TrnType.PAYMENT);
		ToTrnType.put(7, TrnType.PURCHASE);
		ToTrnType.put(8, TrnType.BILLPAYMENT);
		ToTrnType.put(9, TrnType.BALANCEINQUIRY);
		ToTrnType.put(10, TrnType.RETURN);
		ToTrnType.put(11, TrnType.INCREMENTALTRANSFER);
		ToTrnType.put(12, TrnType.DECREMENTALTRANSFER);
		ToTrnType.put(13, TrnType.RECONCILIATION);
		ToTrnType.put(14, TrnType.GETACCOUNT);
		ToTrnType.put(15, TrnType.CHANGEPINBLOCK);
		ToTrnType.put(16, TrnType.CHANGEINTERNETPINBLOCK);
		ToTrnType.put(17, TrnType.PAYMENTSTATEMENT);
		ToTrnType.put(18, TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		ToTrnType.put(19, TrnType.TRANSFER_CARD_TO_ACCOUNT);
		ToTrnType.put(20, TrnType.INCREMENTALTRANSFER_CARD_TO_ACCOUNT);
		ToTrnType.put(21, TrnType.DECREMENTALTRANSFER_CARD_TO_ACCOUNT);
		ToTrnType.put(22, TrnType.BANKSTATEMENT);
		ToTrnType.put(23, TrnType.CREDITCARDDATA);
		ToTrnType.put(30, TrnType.DEPOSIT_CHECK_ACCOUNT);
		//AD
		ToTrnType.put(39, TrnType.SHEBAINQUIRY);

		ToTrnType.put(41, TrnType.HOTCARD);
		
		//TASK Task081 : ATM Saham Feature		
		ToTrnType.put(53, TrnType.STOCK);
		
		// TASK Task129 [26604] - Authenticate Cart (Pasargad)
		ToTrnType.put(56, TrnType.CARD_AUTENTICATE);
		
		//Mirkamali(Task175): Restriction
		ToTrnType.put(57, TrnType.RESTRICTION);
		
		//m.rehman
		ToTrnType.put(58, TrnType.IBFT);
		//m.rehman: for loro messages
		ToTrnType.put(59, TrnType.WITHDRAWAL_LORO);
		ToTrnType.put(60, TrnType.PURCHASE_LORO);
        //m.rehman: for preauth and refund cases
        ToTrnType.put(58, TrnType.PREAUTH);
        ToTrnType.put(59, TrnType.REFUND);
	}

	public static Map<TrnType, Integer> FromTrnType = new HashMap<TrnType, Integer>();
	static {
		FromTrnType.put(TrnType.DEBIT, 0);
		FromTrnType.put(TrnType.CREDIT, 1);
		FromTrnType.put(TrnType.WITHDRAWAL, 2);
		FromTrnType.put(TrnType.CHECKACCOUNT, 3);
		FromTrnType.put(TrnType.DEPOSIT, 4);
		FromTrnType.put(TrnType.TRANSFER, 5);

		FromTrnType.put(TrnType.PAYMENT, 6);
		FromTrnType.put(TrnType.PURCHASE, 7);
		FromTrnType.put(TrnType.BILLPAYMENT, 8);
		FromTrnType.put(TrnType.BALANCEINQUIRY, 9);
		FromTrnType.put(TrnType.RETURN, 10);
		FromTrnType.put(TrnType.INCREMENTALTRANSFER, 11);
		FromTrnType.put(TrnType.DECREMENTALTRANSFER, 12);
		FromTrnType.put(TrnType.RECONCILIATION, 13);
		FromTrnType.put(TrnType.GETACCOUNT, 14);
		FromTrnType.put(TrnType.CHANGEPINBLOCK, 15);
		FromTrnType.put(TrnType.CHANGEINTERNETPINBLOCK, 16);
		FromTrnType.put(TrnType.PAYMENTSTATEMENT, 17);
		FromTrnType.put(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT, 18);
		FromTrnType.put(TrnType.TRANSFER_CARD_TO_ACCOUNT, 19);
		FromTrnType.put(TrnType.INCREMENTALTRANSFER_CARD_TO_ACCOUNT, 20);
		FromTrnType.put(TrnType.DECREMENTALTRANSFER_CARD_TO_ACCOUNT, 21);
		FromTrnType.put(TrnType.BANKSTATEMENT, 22);
		FromTrnType.put(TrnType.CREDITCARDDATA, 23);
		FromTrnType.put(TrnType.DEPOSIT_CHECK_ACCOUNT, 30);
		//39
		FromTrnType.put(TrnType.SHEBAINQUIRY, 39);

		FromTrnType.put(TrnType.HOTCARD, 41);
		//TASK Task081 : ATM Saham Feature
		FromTrnType.put(TrnType.STOCK, 53);		
		// TASK Task129 [26604] - Authenticate Cart (Pasargad)
		FromTrnType.put(TrnType.CARD_AUTENTICATE, 56);
		
		//Mirkamali(Task175): Restriction
		FromTrnType.put(TrnType.RESTRICTION, 57);

		//m.rehman
		FromTrnType.put(TrnType.IBFT, 58);
		//m.rehman: for loro messages
		FromTrnType.put(TrnType.WITHDRAWAL_LORO, 59);
		FromTrnType.put(TrnType.PURCHASE_LORO, 60);
        //m.rehman: for preauth and refund cases
        FromTrnType.put(TrnType.PREAUTH, 58);
        FromTrnType.put(TrnType.REFUND, 59);
	}

	public static Map<Integer, TerminalType> ToTerminalType = new HashMap<Integer, TerminalType>();
	static {
		ToTerminalType.put(0, TerminalType.ATM);
		ToTerminalType.put(1, TerminalType.POS);
		ToTerminalType.put(2, TerminalType.VRU);
		ToTerminalType.put(3, TerminalType.PINPAD);
		ToTerminalType.put(4, TerminalType.INTERNET);
		ToTerminalType.put(5, TerminalType.MOBILE);
		ToTerminalType.put(6, TerminalType.INFOKIOSK);
		ToTerminalType.put(7, TerminalType.KIOSK_CARD_PRESENT);
        /**
         *@author khodadi
         */
        ToTerminalType.put(10,TerminalType.PAYPAL);
        ToTerminalType.put(9,TerminalType.MOBILEWAP);
        ToTerminalType.put(8,TerminalType.USSD);
        ToTerminalType.put(11,TerminalType.INTERNETBANK);
        ToTerminalType.put(12,TerminalType.EPAYPAYPAL);
        ToTerminalType.put(13,TerminalType.MOBILESMS);
        ToTerminalType.put(14,TerminalType.MOBILEBANK);
        ToTerminalType.put(15,TerminalType.PAYPALSMS);
        ToTerminalType.put(16,TerminalType.PAYPALUSSD);
        ToTerminalType.put(17,TerminalType.PAYPALTV);
        ToTerminalType.put(18,TerminalType.MOBILEBANKSMS);
        ToTerminalType.put(19,TerminalType.MOBILEGPRS);
        ToTerminalType.put(20,TerminalType.MOBILEBANKGPRS);
        /**
         * @author k.khodadi
         */
        ToTerminalType.put(21,TerminalType.PAYPALTEL);
        ToTerminalType.put(22,TerminalType.TV);
        ToTerminalType.put(-1,TerminalType.UNKNOWN);
        
		// terminalTypeMap.put(5, TerminalType.Vending);
		// terminalTypeMap.put(6, TerminalType.Payment);
	}

	public static Map<TerminalType, Integer> FromTerminalType = new HashMap<TerminalType, Integer>();
	static {

        FromTerminalType.put(TerminalType.UNKNOWN, -1);
		FromTerminalType.put(TerminalType.ATM, 0);
		FromTerminalType.put(TerminalType.POS, 1);
		FromTerminalType.put(TerminalType.VRU, 2);
		FromTerminalType.put(TerminalType.PINPAD, 3);
		FromTerminalType.put(TerminalType.INTERNET, 4);
		FromTerminalType.put(TerminalType.MOBILE, 5);
		FromTerminalType.put(TerminalType.INFOKIOSK, 6);
		FromTerminalType.put(TerminalType.KIOSK_CARD_PRESENT, 7);
        /**
         * @author khodadi
         */
        FromTerminalType.put(TerminalType.PAYPAL, 10);
        FromTerminalType.put(TerminalType.MOBILEWAP, 9);
        FromTerminalType.put(TerminalType.USSD, 8);
        FromTerminalType.put(TerminalType.INTERNETBANK, 11);
        FromTerminalType.put(TerminalType.EPAYPAYPAL, 12);
        FromTerminalType.put(TerminalType.MOBILESMS, 13);
        FromTerminalType.put(TerminalType.MOBILEBANK, 14);
        FromTerminalType.put(TerminalType.PAYPALSMS, 15);
        FromTerminalType.put(TerminalType.PAYPALUSSD, 16);
        FromTerminalType.put(TerminalType.PAYPALTV, 17);
        FromTerminalType.put(TerminalType.MOBILEBANKSMS, 18);
        FromTerminalType.put(TerminalType.MOBILEGPRS, 19);
        FromTerminalType.put(TerminalType.MOBILEBANKGPRS, 20);
        /**
         * @author k.khodadi
         */
        FromTerminalType.put(TerminalType.PAYPALTEL, 21);
        FromTerminalType.put(TerminalType.TV, 22);


		// terminalTypeMap.put(5, TerminalType.Vending);
		// terminalTypeMap.put(6, TerminalType.Payment);
	}

	public static Map<Integer, Severity> ToSeverity = new HashMap<Integer, Severity>();
	static {
		ToSeverity.put(0, Severity.INFO);
		ToSeverity.put(1, Severity.WARN);
		ToSeverity.put(2, Severity.ERROR);
	}

	public static Map<Severity, Integer> FromSeverity = new HashMap<Severity, Integer>();
	static {
		FromSeverity.put(Severity.INFO, 0);
		FromSeverity.put(Severity.WARN, 1);
		FromSeverity.put(Severity.ERROR, 2);
	}

	public static Map<Integer, AccType> ToAcctType = new HashMap<Integer, AccType>();
	static {
		ToAcctType.put(0, AccType.SAVING);
		ToAcctType.put(1, AccType.DEPOSIT);
		ToAcctType.put(2, AccType.CURRENT);
		ToAcctType.put(3, AccType.UNKNOWN);
        //m.rehman: to support Credit and Universal Account
        ToAcctType.put(4, AccType.CREDIT);
        ToAcctType.put(5, AccType.UNIVERSAL);
	}

	public static Map<AccType, Integer> FromAcctType = new HashMap<AccType, Integer>();
	static {
		FromAcctType.put(AccType.SAVING, 0);
		FromAcctType.put(AccType.DEPOSIT, 1);
		FromAcctType.put(AccType.CURRENT, 2);
		FromAcctType.put(AccType.UNKNOWN, 3);
        //m.rehman: to support Credit and Universal Account
        FromAcctType.put(AccType.CREDIT, 4);
        FromAcctType.put(AccType.UNIVERSAL, 5);
	}

	public static Map<Integer, BalType> ToBalType = new HashMap<Integer, BalType>();
	static {
		ToBalType.put(0, BalType.LEDGER);
		ToBalType.put(1, BalType.OPENINGLEDGER);
		ToBalType.put(2, BalType.CLOSINGLEDGER);
		ToBalType.put(3, BalType.MINLEDGER);
		ToBalType.put(4, BalType.AVGLEDGER);
		ToBalType.put(5, BalType.AVAIL);
		ToBalType.put(6, BalType.CURRENT);
		ToBalType.put(7, BalType.OUTSTANDING);
		ToBalType.put(8, BalType.OPENINGOUTSTANDING);
		ToBalType.put(9, BalType.CLOSINGOUTSTANDING);
		ToBalType.put(10, BalType.AVAILCREDIT);
		ToBalType.put(11, BalType.CREDITLIMIT);
		ToBalType.put(12, BalType.PAYOFFAMT);
		ToBalType.put(13, BalType.PRINCIPAL);
		ToBalType.put(14, BalType.ESCROW);
		ToBalType.put(15, BalType.CREDITHELD);
		ToBalType.put(16, BalType.DEBITHELD);
		ToBalType.put(17, BalType.TOTALHELD);
		ToBalType.put(18, BalType.UNKNOWN);
	}

	public static Map<BalType, Integer> FromBalType = new HashMap<BalType, Integer>();
	static {
		FromBalType.put(BalType.LEDGER, 0);
		FromBalType.put(BalType.OPENINGLEDGER, 1);
		FromBalType.put(BalType.CLOSINGLEDGER, 2);
		FromBalType.put(BalType.MINLEDGER, 3);
		FromBalType.put(BalType.AVGLEDGER, 4);
		FromBalType.put(BalType.AVAIL, 5);
		FromBalType.put(BalType.CURRENT, 6);
		FromBalType.put(BalType.OUTSTANDING, 7);
		FromBalType.put(BalType.OPENINGOUTSTANDING, 8);
		FromBalType.put(BalType.CLOSINGOUTSTANDING, 9);
		FromBalType.put(BalType.AVAILCREDIT, 10);
		FromBalType.put(BalType.CREDITLIMIT, 11);
		FromBalType.put(BalType.PAYOFFAMT, 12);
		FromBalType.put(BalType.PRINCIPAL, 13);
		FromBalType.put(BalType.ESCROW, 14);
		FromBalType.put(BalType.CREDITHELD, 15);
		FromBalType.put(BalType.DEBITHELD, 16);
		FromBalType.put(BalType.TOTALHELD, 17);
		FromBalType.put(BalType.UNKNOWN, 18);
	}

	public static Map<Integer, AccType> ToAccType = new HashMap<Integer, AccType>();
	static {
		ToAccType.put(1, AccType.MAIN_ACCOUNT);
		ToAccType.put(2, AccType.SUBSIDIARY_ACCOUNT);
		ToAccType.put(3, AccType.CARD);
	}

	public static Map<AccType, Integer> FromAccType = new HashMap<AccType, Integer>();
	static {
		FromAccType.put(AccType.UNKNOWN, 1);
		FromAccType.put(AccType.MAIN_ACCOUNT, 1);
		FromAccType.put(AccType.SUBSIDIARY_ACCOUNT, 2);
		FromAccType.put(AccType.CARD, 3);
	}

	public static Map<Integer, UserLanguage> ToUserLang = new HashMap<Integer, UserLanguage>();
	static {
		ToUserLang.put(1, UserLanguage.FARSI_LANG);
		ToUserLang.put(2, UserLanguage.ENGLISH_LANG);
	}

	public static Map<UserLanguage, Integer> FromUserLang = new HashMap<UserLanguage, Integer>();
	static {
		FromUserLang.put(UserLanguage.FARSI_LANG, 1);
		FromUserLang.put(UserLanguage.ENGLISH_LANG, 2);
	}


	public static Hashtable<String, String> ToErrorCode = new Hashtable<String, String>();
	static{
		ToErrorCode.put(Global.SUCCESS, APPROVED);
        ToErrorCode.put(Global.APPROVED_VIP, APPROVED);
        //////////////////////////
        ToErrorCode.put(Global.GENERAL_ERROR, ISOResponseCodes.MESSAGE_FORMAT_ERROR);
        ToErrorCode.put(Global.SYSTEM_NOT_AVAILABLE, ISOResponseCodes.MESSAGE_FORMAT_ERROR);
        ToErrorCode.put(Global.SYSTEM_BUSY, FIELD_ERROR);
        //////////////////////////
        ToErrorCode.put(Global.GENERAL_DATA_ERROR, INVALID_CARD_STATUS);
        ToErrorCode.put(Global.FUNCTION_NOT_AVAILABLE, INVALID_CARD_STATUS);
        ToErrorCode.put(Global.UNSUPPORTED_SERVICE, INVALID_CARD_STATUS);
        ToErrorCode.put(Global.UNSUPPORTED_MESSAGE, INVALID_CARD_STATUS);
//        ToErrorCode.put(Authorization.AUTHORIZATION_FAILURE, ORIGINAL_NOT_AUTHORIZED);
        ToErrorCode.put(Authorization.AUTHORIZATION_FAILURE, SENT_TO_HOST);
        ToErrorCode.put(DebitAdd.AMOUNT_TOO_SMALL, INVALID_CARD_STATUS);
        ToErrorCode.put(DebitAdd.INVALID_CURRENCY_CODE, INVALID_CARD_STATUS);
        ToErrorCode.put(DebitReversal.MESSAGE_CAN_NOT_BE_REVERSED, INVALID_CARD_STATUS);
        //////////////////////////
        ToErrorCode.put(Global.CAPTURE_CARD, INVALID_ACCOUNT_STATUS);
        //////////////////////////
        ToErrorCode.put(Authorization.UNRECOGNIZED_OR_INVALID_CARD_ISSUER, ACCOUNT_LOCKED);
        //////////////////////////
        ToErrorCode.put(Authorization.UNRECOGNIZED_CARD_NUMBER, WARM_CARD);
        //////////////////////////
        ToErrorCode.put(Authorization.STOLEN_CARD, ACQUIRER_NACK);
        //////////////////////////
        ToErrorCode.put(Authorization.LOST_CARD, EXPIRY_DATE_MISMATCH);
        //////////////////////////
        ToErrorCode.put(Authorization.EXPIRED_CARD, DUPLICATE_LINKED_ACCOUNT);
        //////////////////////////
        ToErrorCode.put(Authorization.CARD_ACCOUNT_ID_MATCHES_MULTIPLE_ACCOUNTS, BAD_TRANSACTION_TYPE);
        ToErrorCode.put(BalanceInquiry.ACCOUNT_BALANCE_INFORMATION_NOT_AVAILABLE, BAD_TRANSACTION_TYPE);
        ToErrorCode.put(BalanceInquiry.SINGLE_OR_SOURCE_ACCOUNT_INVALID, BAD_TRANSACTION_TYPE);
        //////////////////////////
        ToErrorCode.put(Authorization.INVALID_PIN_BLOCK, HOST_LINK_DOWN);
        ToErrorCode.put(Authorization.PIN_DATA_REQUIRED, HOST_LINK_DOWN);
        //////////////////////////
        ToErrorCode.put(DebitAdd.AMOUNT_TOO_LARGE, HOST_NOT_PROCESSING);
        ToErrorCode.put(DebitAdd.INSUFFICIENT_FUNDS, HOST_NOT_PROCESSING);
        //////////////////////////
        ToErrorCode.put(Authorization.INVALID_ACQUIRER_DATA, WALLET_IN_PROVISIONAL_STATE);
        //////////////////////////
        ToErrorCode.put(DebitReversal.MESSAGE_BEFORE_REVERSED, ISOResponseCodes.INVALID_ACCOUNT);
        //////////////////////////
        ToErrorCode.put(DebitReversal.REVERSAL_INVALID_AMOUNT, INTERNAL_DATABASE_ERROR);
        //////////////////////////
        ToErrorCode.put(DebitReversal.REVERSAL_MAIN_TRN_NOT_APPROVED, CARD_EXPIRED);
        //////////////////////////
        ToErrorCode.put(Authorization.CARD_SERVICE_VOLUME_LIMITED, TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE);
        //////////////////////////
        ToErrorCode.put(Authorization.CARD_SERVICE_COUNT_LIMITED, CASH_TRANSACTION_NOT_ALLOWED);
        //////////////////////////
        ToErrorCode.put(Authorization.CARD_SERVICE_LIMITED, BANK_LINK_DOWN);
        //////////////////////////
        ToErrorCode.put(DebitAdd.INACTIVE_ACCOUNT, ISOResponseCodes.INVALID_ACCOUNT);
        //////////////////////////

//        ToErrorCode.put(Authorization.UNKNOWN_TRANSACTION_SOURCE, UNKNOWN_TRANSACTION_SOURCE);
        ToErrorCode.put(Authorization.EXCEEDED_PASSWORD_RETRY, TRANSACTION_CODE_MISMATCH);

        //////////////////////////
        ToErrorCode.put(Authorization.DISABLED_CARD, INVALID_MERCHANT);
        //////////////////////////
        ToErrorCode.put(ChangePinBlock.MIN_LENGTH_PIN_INCORRECT, ISOResponseCodes.INVALID_CARD_STATUS);
        //////////////////////////
        ToErrorCode.put(ChangePinBlock.MAX_LENGTH_PIN_INCOORECT, ISOResponseCodes.INVALID_CARD_STATUS);
        //////////////////////////
        ToErrorCode.put(ChangePinBlock.OLD_PIN_INCORRECT, ISOResponseCodes.TRANSACTION_REJECTED);
        //////////////////////////
        ToErrorCode.put(ChangePinBlock.MANDATORY_CHANGE_PIN, ISOResponseCodes.MANDATORY_CHANGE_PIN);	//Mirkamali(Task174)
	}


	public static Hashtable<String, String> FromErrorCode = new Hashtable<String, String>();
	static{
		FromErrorCode.put(APPROVED, Global.SUCCESS);
//		FromErrorCode.put(APPROVED, Global.APPROVED_VIP);
		//////////////////////////
		FromErrorCode.put(ISOResponseCodes.MESSAGE_FORMAT_ERROR, Global.GENERAL_ERROR);
//		FromErrorCode.put(MESSAGE_FORMAT_ERROR, Global.SYSTEM_NOT_AVAILABLE);
		FromErrorCode.put(FIELD_ERROR, Global.SYSTEM_BUSY);
//		FromErrorCode.put(CUSTOMER_NOT_FOUND, CUSTOMER_NOT_FOUND);
		//////////////////////////
		FromErrorCode.put(INVALID_CARD_STATUS, Global.GENERAL_DATA_ERROR);
//		FromErrorCode.put(INVALID_CARD_STATUS, Global.FUNCTION_NOT_AVAILABLE);
//		FromErrorCode.put(INVALID_CARD_STATUS, Global.UNSUPPORTED_SERVICE);
//		FromErrorCode.put(INVALID_CARD_STATUS, Global.UNSUPPORTED_MESSAGE);
//		FromErrorCode.put(INVALID_CARD_STATUS, Authorization.AUTHORIZATION_FAILURE);
//		FromErrorCode.put(INVALID_CARD_STATUS, DebitAdd.AMOUNT_TOO_SMALL);
//		FromErrorCode.put(INVALID_CARD_STATUS, DebitAdd.INVALID_CURRENCY_CODE);
//		FromErrorCode.put(INVALID_CARD_STATUS, DebitReversal.MESSAGE_CAN_NOT_BE_REVERSED);
		//////////////////////////
		FromErrorCode.put(Global.CAPTURE_CARD, INVALID_ACCOUNT_STATUS);
		//////////////////////////
		FromErrorCode.put(Authorization.UNRECOGNIZED_OR_INVALID_CARD_ISSUER, ACCOUNT_LOCKED);
		//////////////////////////
		FromErrorCode.put(Authorization.UNRECOGNIZED_CARD_NUMBER, WARM_CARD);
		//////////////////////////
		FromErrorCode.put(Authorization.STOLEN_CARD, ACQUIRER_NACK);
		//////////////////////////
		FromErrorCode.put(Authorization.LOST_CARD, EXPIRY_DATE_MISMATCH);
		//////////////////////////
		FromErrorCode.put(Authorization.EXPIRED_CARD, DUPLICATE_LINKED_ACCOUNT);
		//////////////////////////
		FromErrorCode.put(Authorization.CARD_ACCOUNT_ID_MATCHES_MULTIPLE_ACCOUNTS, BAD_TRANSACTION_TYPE);
		FromErrorCode.put(BalanceInquiry.ACCOUNT_BALANCE_INFORMATION_NOT_AVAILABLE, BAD_TRANSACTION_TYPE);
		FromErrorCode.put(BalanceInquiry.SINGLE_OR_SOURCE_ACCOUNT_INVALID, BAD_TRANSACTION_TYPE);
		//////////////////////////
		FromErrorCode.put(Authorization.INVALID_PIN_BLOCK, HOST_LINK_DOWN);
		FromErrorCode.put(Authorization.PIN_DATA_REQUIRED, HOST_LINK_DOWN);
		//////////////////////////
		FromErrorCode.put(DebitAdd.AMOUNT_TOO_LARGE, HOST_NOT_PROCESSING);
		FromErrorCode.put(DebitAdd.INSUFFICIENT_FUNDS, HOST_NOT_PROCESSING);
		//////////////////////////
		FromErrorCode.put(Authorization.INVALID_ACQUIRER_DATA, WALLET_IN_PROVISIONAL_STATE);
		//////////////////////////
		FromErrorCode.put(DebitReversal.MESSAGE_BEFORE_REVERSED, ISOResponseCodes.INVALID_ACCOUNT);
		//////////////////////////
		FromErrorCode.put(DebitReversal.REVERSAL_INVALID_AMOUNT, INTERNAL_DATABASE_ERROR);
		//////////////////////////
		FromErrorCode.put(DebitReversal.REVERSAL_MAIN_TRN_NOT_APPROVED, CARD_EXPIRED);
		//////////////////////////
		FromErrorCode.put(Authorization.CARD_SERVICE_VOLUME_LIMITED, TRANSACTION_REJECTED_SWITCH_TO_CONTACT_INTERFACE);
		//////////////////////////
		FromErrorCode.put(Authorization.CARD_SERVICE_COUNT_LIMITED, CASH_TRANSACTION_NOT_ALLOWED);
		//////////////////////////
		FromErrorCode.put(Authorization.CARD_SERVICE_LIMITED, BANK_LINK_DOWN);
		//////////////////////////
		FromErrorCode.put(DebitAdd.INACTIVE_ACCOUNT, ISOResponseCodes.INVALID_ACCOUNT);
		//////////////////////////
//		FromErrorCode.put(Authorization.UNKNOWN_TRANSACTION_SOURCE, UNKNOWN_TRANSACTION_SOURCE);
		FromErrorCode.put(Authorization.EXCEEDED_PASSWORD_RETRY, TRANSACTION_CODE_MISMATCH);
		//////////////////////////
		FromErrorCode.put(Authorization.DISABLED_CARD, INVALID_MERCHANT);
		//////////////////////////
		FromErrorCode.put(ChangePinBlock.MIN_LENGTH_PIN_INCORRECT, ISOResponseCodes.INVALID_CARD_STATUS);
		//////////////////////////
		FromErrorCode.put(ChangePinBlock.MAX_LENGTH_PIN_INCOORECT, ISOResponseCodes.INVALID_CARD_STATUS);
		//////////////////////////
		FromErrorCode.put(ChangePinBlock.MANDATORY_CHANGE_PIN, ISOResponseCodes.MANDATORY_CHANGE_PIN);	//Mirkamali(Task174)
	}


	 private static class Global {
	        public static String SUCCESS = "0";
	        public static String APPROVED_VIP = "6";
	        public static String SYSTEM_BUSY = "09";
	        public static String GENERAL_ERROR = "100";
	        public static String GENERAL_DATA_ERROR = "200";
	        public static String SYSTEM_NOT_AVAILABLE = "300";
	        public static String FUNCTION_NOT_AVAILABLE = "400";
	        public static String UNSUPPORTED_SERVICE = "500";
	        public static String UNSUPPORTED_MESSAGE = "600";
	        public static String CAPTURE_CARD = "4000";
	    }
	 private static class Authorization {
	        public static String AUTHORIZATION_FAILURE = "1760";
	        public static String UNRECOGNIZED_OR_INVALID_CARD_ISSUER = "2520";
	        public static String UNRECOGNIZED_CARD_NUMBER = "2530";
	        public static String INVALID_ACQUIRER_DATA = "6190";
	        public static String STOLEN_CARD = "2540";
	        public static String LOST_CARD = "6040";
	        public static String EXPIRED_CARD = "3380";
	        public static String CARD_ACCOUNT_ID_MATCHES_MULTIPLE_ACCOUNTS = "3560";
	        public static String INVALID_PIN_BLOCK = "6130";
	        public static String PIN_DATA_REQUIRED = "6080";
	        public static String EXCEEDED_PASSWORD_RETRY = "75"; //shetab: 75
	        public static String DISABLED_CARD = "78"; //shetab: 78

	        //Checkin card service results
	        public static String CARD_SERVICE_VOLUME_LIMITED = "61";
	        public static String CARD_SERVICE_COUNT_LIMITED = "65";
	        public static String CARD_SERVICE_LIMITED = "57";
	    }
	 private static class BalanceInquiry {
	        public static String ACCOUNT_BALANCE_INFORMATION_NOT_AVAILABLE = "2210";
	        public static String SINGLE_OR_SOURCE_ACCOUNT_INVALID = "2300";
	    }
	 private static class DebitAdd {
	        public static String AMOUNT_TOO_SMALL = "2020";
	        public static String AMOUNT_TOO_LARGE = "2030";
	        public static String INVALID_CURRENCY_CODE = "2740";
	        public static String INSUFFICIENT_FUNDS = "2940";
	        public static String INACTIVE_ACCOUNT = "79"; //shetab: 79
	    }
	 private static class DebitReversal {
	        public static String MESSAGE_CAN_NOT_BE_REVERSED = "810";
	        public static String MESSAGE_BEFORE_REVERSED = "811"; //02
	        public static String REVERSAL_INVALID_AMOUNT = "812"; //13
	        public static String REVERSAL_MAIN_TRN_NOT_APPROVED = "813"; //34
	    }

	 private static class ChangePinBlock {
		 public static String MANDATORY_CHANGE_PIN = "996"; //Mirkamali(Task174)
		 public static String OLD_PIN_INCORRECT = "997";
		 public static String MIN_LENGTH_PIN_INCORRECT = "998";
		 public static String MAX_LENGTH_PIN_INCOORECT = "999"; //02
	 }

}
