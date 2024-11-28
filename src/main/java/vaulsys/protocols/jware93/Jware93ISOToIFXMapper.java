package vaulsys.protocols.jware93;

import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOtoIfxMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;

public class Jware93ISOToIFXMapper extends ISOtoIfxMapper{

    public static Ifx map(ISOMsg isoMsg/* , int protocolVersion */)
            throws ISOException {
        return null;
//		
//		Ifx ifx = new Ifx();
//		int mti = 0;
//		try {
//			mti = Integer.parseInt(isoMsg.getMTI());
//		} catch (NumberFormatException e) {
//			e.printStackTrace();
//		} catch (ISOException e) {
//			e.printStackTrace();
//		}
//
//		/** **************** Map ISO to IFX **************** */
//		/*
//		 * FieldNo. NAME MAPPING ADDRESS
//		 * -----------------------------------------------------------------------------------------------
//		 * 1 MTI IFX[@MTI] 2 PAN IFX.MsgRqHdr.EMVRqData.AppPAN 3
//		 * PROCCESING_CODE[0..2] IFX.MsgRqHdr.EMVRqData.EMVTrnType 4
//		 * AMOUNT,TRANSACTION IFX.MsgRqHdr.EMVRqData.AuthAmt.Amt 6
//		 * AMOUNT,CARDHOLDER_BILLING IFX.MsgRqHdr.EMVRqData.SecAmt.Amt 7
//		 * DATE_TIME,TRANSMISSION 10 CONVERSION_RATE,CARDHOLDER_BILLING
//		 * IFX.MsgRqHdr.EMVRqData.SecAmt.CurRate 11 SYSTEMS_TRACE_AUDIT_NO.
//		 * IFX.MsgRqHdr.EMVRqData.TrnSeqCntr 12 DATE_TIME,LOCAL_TRANSACTION
//		 * //"difference in iso & shetab" 13 DATE_EFFECTIVE
//		 * IFX.MsgRqHdr.EMVRqData.TrnDt //"difference in iso & shetab" 14
//		 * DATE,EXPIRATION IFX.MsgRqHdr.EMVRqData.AppExpDt 15 DATE,SATTLEMENT 17
//		 * DATE,CAPTURE 18 MERCHANT_TYPE
//		 * IFX.MsgRqHdr.NetworkTrnInfo.OriginatorType 22
//		 * POINT_OF_SERCIVE_DATA_CODE 24 FUNCTION_CODE 25
//		 * MESSAGE_REASON_CODE(TERMINAL_TYPE)
//		 * IFX.MsgRqHdr.NetworkTrnInfo.TerminalType ???? 30 AMOUNTS,ORIGINAL 32
//		 * ACQUIRER_INSTITUTION_ID IFX.MsgRqHdr.NetworkTrnInfo.BankId 33
//		 * FORWARDING_INSTITUTION_ID IFX.MsgRqHdr.NetworkTrnInfo.FwdBankId ????
//		 * 35 TRACK2_DATA IFX.MsgRqHdr.EMVRqData.Trk2EquivData 37
//		 * RETRIEVAL_REF_NO. IFX.MsgRqHdr.NetworkTrnInfo.NetworkRefId 39
//		 * ACTION_CODE ?????????? ///???? 41 CARD_ACCEPTOR_TERMINAL_ID
//		 * IFX.MsgRqHdr.NetworkTrnInfo.TerminalId 42 CARD_ACCEPTOR_ID_CODE 43
//		 * CARD_ACCEPTOR_NAME/LOCATION[0..22] IFX.MsgRqHdr.NetworkTrnInfo.Name
//		 * 43 CARD_ACCEPTOR_NAME/LOCATION[22..35]
//		 * IFX.MsgRqHdr.NetworkTrnInfo.City 43
//		 * CARD_ACCEPTOR_NAME/LOCATION[35..38]
//		 * IFX.MsgRqHdr.NetworkTrnInfo.StateProv 43
//		 * CARD_ACCEPTOR_NAME/LOCATION[38..40]
//		 * IFX.MsgRqHdr.NetworkTrnInfo.Country 48 ADDITIONAL_DATA_PRIVATE 49
//		 * CURRENCY_CODE,TRANSACTION IFX.MsgRqHdr.EMVRqData.AuthAmt.CurCode 50
//		 * CURRENCY_CODE,RECONCILIATION 51 CURRENCY_CODE,CARDHOLDER_BILLING
//		 * IFX.MsgRqHdr.EMVRqData.SecAmt.CurCode 52 PIN_DATA
//		 * IFX.SignOnRq.PINBlock 53 SECURITY_RELATED_CONTROL_INFO 54
//		 * AMOUNTS,ADDITIONAL 56 OROGINAL_DATA_ELEMENTS // * 64 MAC
//		 * IFX.MsgRqHdr.MsgAuthCode.MacValue 74 CREDITS,NUMBER 75
//		 * CREDITS,REVERSAL_NUMBER 76 DEBITS,NUMBER 77 DEBITS,REVERSAL_NUMBER 78
//		 * TRANSFER,NUMBER 79 TRANSFER,REVERSAL_NUMBER 80 INQUIRIES,NUMBER 81
//		 * AUTHORIZATIONS,NUMBER 86 CREDITS,AMOUNT 87 CREDITS,REVERSAL_AMOUNT 88
//		 * DEBITS,AMOUNT 89 DEBITS,REVERSAL_AMOUNT 93 TRN_ 94 96 97 124 128
//		 * 
//		 */
//		ifx.MsgRqHdr = new MsgRqHdr();
//		ifx.MsgRqHdr.NetworkTrnInfo = new NetworkTrnInfo();
//		
//		ifx.MsgRqHdr.NetworkTrnInfo.BankId = isoMsg.getString(32);
//		ifx.MsgRqHdr.NetworkTrnInfo.FwdBankId = isoMsg.getString(33);
//		ifx.MsgRqHdr.NetworkTrnInfo.OriginatorType = isoMsg.getString(18);
//		ifx.MsgRqHdr.NetworkTrnInfo.NetworkOwner = "SHETAB"; // ??????
//		ifx.MsgRqHdr.NetworkTrnInfo.NetworkRefId = isoMsg.getString(37);
//		String P43 = isoMsg.getString(43);
//		if (P43 != null && P43.length() > 0) {
//			ifx.MsgRqHdr.NetworkTrnInfo.Name = P43.substring(0, 22);
//			ifx.MsgRqHdr.NetworkTrnInfo.City = P43.substring(22, 35);
//			ifx.MsgRqHdr.NetworkTrnInfo.StateProv = P43.substring(35, 38);
//			ifx.MsgRqHdr.NetworkTrnInfo.Country = P43.substring(38, 40); // ?????
//		}
//		ifx.MsgRqHdr.NetworkTrnInfo.TerminalId = isoMsg.getString(41);
//		ifx.MsgRqHdr.NetworkTrnInfo.TerminalType = isoMsg.getString(25);
//
//		ifx.MsgRqHdr.EMVRqData = new EMVRqData();
//		ifx.MsgRqHdr.EMVRqData.AppExpDt = isoMsg.getString(14);
//		ifx.MsgRqHdr.EMVRqData.AppPAN = isoMsg.getString(2);
//		ifx.MsgRqHdr.EMVRqData.Auth_Amt = isoMsg.getString(4);
//		ifx.MsgRqHdr.EMVRqData.Auth_CurCode = isoMsg.getString(49);
//		ifx.MsgRqHdr.EMVRqData.Auth_CurRate = "1";
//		
//		ifx.MsgRqHdr.EMVRqData.Sec_Amt = isoMsg.getString(6);
//		ifx.MsgRqHdr.EMVRqData.Sec_CurCode = isoMsg.getString(51);
//		ifx.MsgRqHdr.EMVRqData.Sec_CurRate = isoMsg.getString(10);
//
//		if (isoMsg.getString(3) != null && isoMsg.getString(3).length() > 0) {
//			ifx.MsgRqHdr.EMVRqData.EMVTrnType = isoMsg.getString(3).substring(0, 2);
//		}
//		if (isoMsg.getString(43) != null && isoMsg.getString(43).length() > 0) {
//			ifx.MsgRqHdr.EMVRqData.TerminalCountryCode = isoMsg.getString(43).substring(38, 40); // ??????^
//		}
//		ifx.MsgRqHdr.EMVRqData.Trk2EquivData = isoMsg.getString(35);
//		ifx.MsgRqHdr.EMVRqData.TrnDt = isoMsg.getString(13);
//		ifx.MsgRqHdr.EMVRqData.TrnSeqCntr = isoMsg.getString(11);
//		
//
//		ifx.MsgRqHdr.PointOfServiceData = new PointOfServiceData();
//		ifx.MsgRqHdr.PointOfServiceData.Environment = isoMsg.getString(25); // //???^
//
//		/** ************************** */
//		// BusElement messageAuthCode = new BusElement("MsgAuthCode");
//		// messageAuthCode.setAttribute("MacValue", isoMsg.getString(64));
//		// messageRequestHeader.addBusElement(messageAuthCode);
//
//		/** ************************** */
//		ifx.SignonRq = new SignonRq();
//		ifx.SignonRq.PINBlock = isoMsg.getString(52);
//
//		/** ************************** */
//		ifx.ExtISO = new ExtISO();
//		ifx.ExtISO.P7 = isoMsg.getString(7);
//		ifx.ExtISO.P12= isoMsg.getString(12);
//		ifx.ExtISO.P15= isoMsg.getString(15);
//		ifx.ExtISO.P17= isoMsg.getString(17);
//		ifx.ExtISO.P22= isoMsg.getString(22);
//		ifx.ExtISO.P24= isoMsg.getString(24);
//		ifx.ExtISO.P30= isoMsg.getString(30);
//		ifx.ExtISO.P39= isoMsg.getString(39);
//		ifx.ExtISO.P42= isoMsg.getString(42);
//		ifx.ExtISO.P48= isoMsg.getString(48);
//		ifx.ExtISO.P50= isoMsg.getString(50);
//		ifx.ExtISO.P53= isoMsg.getString(53);
//		ifx.ExtISO.P54= isoMsg.getString(54);
//		ifx.ExtISO.P56= isoMsg.getString(56);
//		ifx.ExtISO.P74= isoMsg.getString(74);
//		ifx.ExtISO.P75= isoMsg.getString(75);
//		ifx.ExtISO.P76= isoMsg.getString(76);
//		ifx.ExtISO.P77= isoMsg.getString(77);
//		ifx.ExtISO.P78= isoMsg.getString(78);
//		ifx.ExtISO.P79= isoMsg.getString(79);
//		ifx.ExtISO.P80= isoMsg.getString(80);
//		ifx.ExtISO.P81= isoMsg.getString(81);
//		ifx.ExtISO.P86= isoMsg.getString(86);
//		ifx.ExtISO.P87= isoMsg.getString(87);
//		ifx.ExtISO.P88= isoMsg.getString(88);
//		ifx.ExtISO.P89= isoMsg.getString(89);
//		ifx.ExtISO.P93= isoMsg.getString(93);
//		ifx.ExtISO.P94= isoMsg.getString(94);
//		ifx.ExtISO.P96= isoMsg.getString(96);
//		ifx.ExtISO.P97= isoMsg.getString(97);
//		ifx.ExtISO.P124= isoMsg.getString(124);
//
//
//		/** ************************** */
//		int processCode = Integer.parseInt(isoMsg.getString(3));
//		ifx.TrnType = String.valueOf(processCode);
//
//		/** ************************** */
//
//		String finalMessageType = "";
//		switch (mti) {
//		case ShetabMessageTypes.FINANCIAL_RQ_93:
//		case ShetabMessageTypes.FINANCIAL_RQ_87:
//			switch (processCode) {
//			case ShetabTransactionTypes.BALANCE_INQUERY:
//				finalMessageType = ShetabFinalMessageType.BAL_INQ_RQ;
//				break;
//			case ShetabTransactionTypes.BILL_PAYMENT_93:
//			case ShetabTransactionTypes.BILL_PAYMENT_87:
//				finalMessageType = ShetabFinalMessageType.PMT_ADD_RQ;
//				break;
//			case ShetabTransactionTypes.PURCHASE:
//			case ShetabTransactionTypes.WITHDRAWAL:
//			case ShetabTransactionTypes.CHECK_ACCOUNT:
//				finalMessageType = ShetabFinalMessageType.DEBIT_ADD_RQ;
//				break;
//			}
//			break;
//
//		case ShetabMessageTypes.FINANCIAL_RS_93:
//		case ShetabMessageTypes.FINANCIAL_RS_87:
//			switch (processCode) {
//			case ShetabTransactionTypes.BALANCE_INQUERY:
//				finalMessageType = ShetabFinalMessageType.BAL_INQ_RS;
//				break;
//			case ShetabTransactionTypes.BILL_PAYMENT_93:
//			case ShetabTransactionTypes.BILL_PAYMENT_87:
//				finalMessageType = ShetabFinalMessageType.PMT_ADD_RS;
//				break;
//			case ShetabTransactionTypes.PURCHASE:
//			case ShetabTransactionTypes.WITHDRAWAL:
//				finalMessageType = ShetabFinalMessageType.DEBIT_ADD_RS;
//				break;
//			}
//			break;
//
//		case ShetabMessageTypes.REVERSAL_ADVICE_93:
//		case ShetabMessageTypes.REVERSAL_ADVICE_87:
//			switch (processCode) {
//			case ShetabTransactionTypes.BALANCE_INQUERY:
//				finalMessageType = ShetabFinalMessageType.BAL_REV_RQ;
//				break;
//			case ShetabTransactionTypes.BILL_PAYMENT_93:
//			case ShetabTransactionTypes.BILL_PAYMENT_87:
//				finalMessageType = ShetabFinalMessageType.PMT_REV_RQ;
//				break;
//			case ShetabTransactionTypes.PURCHASE:
//			case ShetabTransactionTypes.WITHDRAWAL:
//				finalMessageType = ShetabFinalMessageType.DEBIT_REV_RQ;
//				break;
//			}
//			break;
//
//		case ShetabMessageTypes.REVERSAL_ADVICE_RS_93:
//		case ShetabMessageTypes.REVERSAL_ADVICE_RS_87:
//			switch (processCode) {
//			case ShetabTransactionTypes.BALANCE_INQUERY:
//				finalMessageType = ShetabFinalMessageType.BAL_REV_RS;
//				break;
//			case ShetabTransactionTypes.BILL_PAYMENT_93:
//			case ShetabTransactionTypes.BILL_PAYMENT_87:
//				finalMessageType = ShetabFinalMessageType.PMT_REV_RS;
//				break;
//			case ShetabTransactionTypes.PURCHASE:
//			case ShetabTransactionTypes.WITHDRAWAL:
//				finalMessageType = ShetabFinalMessageType.DEBIT_REV_RS;
//				break;
//			}
//			break;
//
//		case ShetabMessageTypes.ACQUIRER_RECONCILIATION_RQ_93:
//		case ShetabMessageTypes.ACQUIRER_RECONCILIATION_RQ_87:
//			finalMessageType = ShetabFinalMessageType.ACQUIRER_REC_RQ;
//			break;
//		case ShetabMessageTypes.ACQUIRER_RECONCILIATION_RS_93:
//		case ShetabMessageTypes.ACQUIRER_RECONCILIATION_RS_87:
//			finalMessageType = ShetabFinalMessageType.ACQUIRER_REC_RS;
//			break;
//		case ShetabMessageTypes.ACQUIRER_RECONCILIATION_RQ_REPEAT_93:
//		case ShetabMessageTypes.ACQUIRER_RECONCILIATION_RQ_REPEAT_87:
//			finalMessageType = ShetabFinalMessageType.ACQUIRER_REC_RQ_REPEAT;
//			break;
//		case ShetabMessageTypes.ACQUIRER_RECONCILIATION_RQ_REPEAT_RS_93:
//		case ShetabMessageTypes.ACQUIRER_RECONCILIATION_RQ_REPEAT_RS_87:
//			finalMessageType = ShetabFinalMessageType.ACQUIRER_REC_RQ_REPEAT_RS;
//			break;
//
//		case ShetabMessageTypes.ISSUER_RECONCILIATION_RQ_93:
//		case ShetabMessageTypes.ISSUER_RECONCILIATION_RQ_87:
//			finalMessageType = ShetabFinalMessageType.CARD_ISSUER_REC_RQ;
//			break;
//		case ShetabMessageTypes.ISSUER_RECONCILIATION_RS_93:
//		case ShetabMessageTypes.ISSUER_RECONCILIATION_RS_87:
//			finalMessageType = ShetabFinalMessageType.CARD_ISSUER_REC_RS;
//			break;
//		case ShetabMessageTypes.ISSUER_RECONCILIATION_RQ_REPEAT_93:
//		case ShetabMessageTypes.ISSUER_RECONCILIATION_RQ_REPEAT_87:
//			finalMessageType = ShetabFinalMessageType.CARD_ISSUER_REC_RQ_REPEAT;
//			break;
//		case ShetabMessageTypes.ISSUER_RECONCILIATION_RQ_REPEAT_RS_93:
//		case ShetabMessageTypes.ISSUER_RECONCILIATION_RQ_REPEAT_RS_87:
//			finalMessageType = ShetabFinalMessageType.CARD_ISSUER_REC_RQ_REPEAT_RS;
//			break;
//
//		case ShetabMessageTypes.NETWORK_MANAGEMENT_RQ_93:
//		case ShetabMessageTypes.NETWORK_MANAGEMENT_RQ_87:
//			finalMessageType = ShetabFinalMessageType.NETWORK_MGR_RQ;
//			break;
//		case ShetabMessageTypes.NETWORK_MANAGEMENT_RS_93:
//		case ShetabMessageTypes.NETWORK_MANAGEMENT_RS_87:
//			finalMessageType = ShetabFinalMessageType.NETWORK_MGR_RS;
//			break;
//		case ShetabMessageTypes.NETWORK_MANAGEMENT_RQ_REPEAT_93:
//		case ShetabMessageTypes.NETWORK_MANAGEMENT_RQ_REPEAT_87:
//			finalMessageType = ShetabFinalMessageType.NETWORK_MGR_RQ_REPEATS;
//			break;
//		case ShetabMessageTypes.NETWORK_MANAGEMENT_RQ_REPEAT_RS_93:
//		case ShetabMessageTypes.NETWORK_MANAGEMENT_RQ_REPEAT_RS_87:
//			finalMessageType = ShetabFinalMessageType.NETWORK_MGR_RQ_REPEATS;
//			break;
//		}
//
//		ifx.IfxType = finalMessageType;
//
//		// message.getComponent(0)
//		// message.getString(fldno)
//		// message.getString(fldno)
//		return ifx;
    }

    public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
