package vaulsys.protocols.jware93;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;

public class Jware93IFXToISOMapper {

    public static ISOMsg map(Ifx ifx) throws ISOException {

        return null;
//		
//		ISOMsg isoMsg = new ISOMsg();
//		/*
//		 * IFX[@MTI, @ProcessCode] MsgReqHdr netTrnInfo [@BankId, @FwdBankId,
//		 * @OriginatorType, @NetworkRefId, @Name, @City, @StateProv, @Country,
//		 * @TerminalId,@TerminalType)] EMVReqData
//		 */
//		//TODO: Dialog
////		if( ifx.MessageType == "BallInqRq")
//			isoMsg.setMTI( "1200" );
////		isoMsg.setMTI(String.valueOf(ifx.MTI));
//		isoMsg.set(3, ifx.TrnType);
//
//		if (ifx.MsgRqHdr != null) {
//			if (ifx.MsgRqHdr.NetworkTrnInfo != null) {
//				isoMsg.set(32, ifx.MsgRqHdr.NetworkTrnInfo.BankId);
//				isoMsg.set(33, ifx.MsgRqHdr.NetworkTrnInfo.FwdBankId);
//				isoMsg.set(18, ifx.MsgRqHdr.NetworkTrnInfo.OriginatorType);
//				/*
//				 * networkTransactionInformation.setAttribute("NetworkOwner",
//				 * "SHETAB"); // ??????
//				 */
//				isoMsg.set(37, ifx.MsgRqHdr.NetworkTrnInfo.NetworkRefId);
//
//				String field43 = String.valueOf(ifx.MsgRqHdr.NetworkTrnInfo.Name)
//						+ String.valueOf(ifx.MsgRqHdr.NetworkTrnInfo.City)
//						+ String.valueOf(ifx.MsgRqHdr.NetworkTrnInfo.StateProv)
//						+ String.valueOf(ifx.MsgRqHdr.NetworkTrnInfo.Country);
//				isoMsg.set(43, field43);
//
//				isoMsg.set(41, ifx.MsgRqHdr.NetworkTrnInfo.TerminalId);
//				isoMsg.set(25, ifx.MsgRqHdr.NetworkTrnInfo.TerminalType);
//			}
//
//			if (ifx.MsgRqHdr.EMVRqData != null) {
//				isoMsg.set(14, ifx.MsgRqHdr.EMVRqData.AppExpDt);
//				isoMsg.set(2, ifx.MsgRqHdr.EMVRqData.AppPAN);
//
//				isoMsg.set(4, ifx.MsgRqHdr.EMVRqData.Auth_Amt);
//				isoMsg.set(49, ifx.MsgRqHdr.EMVRqData.Auth_CurCode);
//				// isoMsg.set(1, authAMT.getAttributeValue(IFXFields.CurRate));
//
//				isoMsg.set(6, ifx.MsgRqHdr.EMVRqData.Sec_Amt);
//				isoMsg.set(51, ifx.MsgRqHdr.EMVRqData.Sec_CurCode);
//				isoMsg.set(10, ifx.MsgRqHdr.EMVRqData.Sec_CurRate);
//
//				isoMsg.set(35, ifx.MsgRqHdr.EMVRqData.Trk2EquivData);
//				isoMsg.set(13, ifx.MsgRqHdr.EMVRqData.TrnDt);
//				isoMsg.set(11, ifx.MsgRqHdr.EMVRqData.TrnSeqCntr);
//			}
//
//			if (ifx.MsgRqHdr.PointOfServiceData != null)
//				isoMsg.set(25, ifx.MsgRqHdr.PointOfServiceData.Environment);
//
//			// BusElement messageAuthCode =
//			// msgReqHdr.getSon(IFXFields.MsgAuthCode);
//			// isoMsg.set(64,
//			// messageAuthCode.getAttributeValue(IFXFields.MacValue));
//		}
//
//		isoMsg.set(52, ifx.SignonRq.PINBlock);
//
//		isoMsg.set(7, ifx.ExtISO.P7);
//		isoMsg.set(12, ifx.ExtISO.P12);
//		isoMsg.set(15, ifx.ExtISO.P15);
//		isoMsg.set(17, ifx.ExtISO.P17);
//		isoMsg.set(22, ifx.ExtISO.P22);
//		isoMsg.set(24, ifx.ExtISO.P24);
//		isoMsg.set(30, ifx.ExtISO.P30);
//		isoMsg.set(39, ifx.ExtISO.P39);
//		isoMsg.set(42, ifx.ExtISO.P42);
//		isoMsg.set(48, ifx.ExtISO.P48);
//		isoMsg.set(50, ifx.ExtISO.P50);
//		isoMsg.set(53, ifx.ExtISO.P53);
//		isoMsg.set(54, ifx.ExtISO.P54);
//		isoMsg.set(56, ifx.ExtISO.P56);
//		isoMsg.set(74, ifx.ExtISO.P74);
//		isoMsg.set(75, ifx.ExtISO.P75);
//		isoMsg.set(76, ifx.ExtISO.P76);
//		isoMsg.set(77, ifx.ExtISO.P77);
//		isoMsg.set(78, ifx.ExtISO.P78);
//		isoMsg.set(79, ifx.ExtISO.P79);
//		isoMsg.set(81, ifx.ExtISO.P80);
//		isoMsg.set(86, ifx.ExtISO.P86);
//		isoMsg.set(87, ifx.ExtISO.P87);
//		isoMsg.set(88, ifx.ExtISO.P88);
//		isoMsg.set(89, ifx.ExtISO.P89);
//		isoMsg.set(93, ifx.ExtISO.P93);
//		isoMsg.set(94, ifx.ExtISO.P94);
//		isoMsg.set(96, ifx.ExtISO.P96);
//		isoMsg.set(97, ifx.ExtISO.P97);
//		isoMsg.set(124, ifx.ExtISO.P124);
//
//		return isoMsg;
//	}
//
//	public static ISOMsg jwareMap(Ifx ifx) throws ISOException {
//
//		ISOMsg isoMsg = new ISOMsg();
//		/*
//		 * IFX[@MTI, @ProcessCode] MsgReqHdr netTrnInfo [@BankId, @FwdBankId,
//		 * @OriginatorType, @NetworkRefId, @Name, @City, @StateProv, @Country,
//		 * @TerminalId,@TerminalType)] EMVReqData
//		 */
//
//		//TODO: Dialog
////		if( ifx.MessageType == "BallInqRq")
//			isoMsg.setMTI( "1200" );
////		isoMsg.setMTI(String.valueOf(ifx.MTI));
//		isoMsg.set(3, ifx.TrnType);
//
//		if (ifx.MsgRqHdr != null) {
//			if (ifx.MsgRqHdr.NetworkTrnInfo != null) {
//				isoMsg.set(32, ifx.MsgRqHdr.NetworkTrnInfo.BankId);
//				isoMsg.set(33, ifx.MsgRqHdr.NetworkTrnInfo.FwdBankId);
//				// isoMsg.set(18,
//				// netTrnInfo.getAttributeValue(IFXFields.OriginatorType));
//				/*
//				 * networkTransactionInformation.setAttribute("NetworkOwner",
//				 * "SHETAB"); // ??????
//				 */
//				isoMsg.set(37, ifx.MsgRqHdr.NetworkTrnInfo.NetworkRefId);
//
//				String field43 = String.valueOf(ifx.MsgRqHdr.NetworkTrnInfo.Name)
//						+ String.valueOf(ifx.MsgRqHdr.NetworkTrnInfo.City)
//						+ String.valueOf(ifx.MsgRqHdr.NetworkTrnInfo.StateProv)
//						+ String.valueOf(ifx.MsgRqHdr.NetworkTrnInfo.Country);
//				isoMsg.set(43, field43);
//
//				isoMsg.set(41, ifx.MsgRqHdr.NetworkTrnInfo.TerminalId);
//				isoMsg.set(26, ifx.MsgRqHdr.NetworkTrnInfo.TerminalType);
//			}
//
//			if (ifx.MsgRqHdr.EMVRqData != null) {
//				isoMsg.set(14, ifx.MsgRqHdr.EMVRqData.AppExpDt);
//				isoMsg.set(2, ifx.MsgRqHdr.EMVRqData.AppPAN);
//
//				isoMsg.set(4, ifx.MsgRqHdr.EMVRqData.Auth_Amt);// ??
//				isoMsg.set(49, ifx.MsgRqHdr.EMVRqData.Auth_CurCode);
//				// isoMsg.set(1, ifx.MsgRqHdr.EMVRqData.AuthAmt.CurRate);
//
//				// ?? isoMsg.set(6, ifx.MsgRqHdr.EMVRqData.SecAmt.Amt);
//				// ?? isoMsg.set(51, ifx.MsgRqHdr.EMVRqData.SecAmt.CurCode);
//				isoMsg.set(10, ifx.MsgRqHdr.EMVRqData.Sec_CurRate);
//
//				isoMsg.set(35, ifx.MsgRqHdr.EMVRqData.Trk2EquivData);
//				// ?? isoMsg.set(13, ifx.MsgRqHdr.EMVRqData.TrnDt);
//				isoMsg.set(11, ifx.MsgRqHdr.EMVRqData.TrnSeqCntr);
//			}
//
//			if (ifx.MsgRqHdr.PointOfServiceData != null) {
//				// ?? isoMsg.set(25,
//				// ifx.MsgRqHdr.PointOfServiceData.Environment);
//			}
//
//			// isoMsg.set(64, ifx.MsgRqHdr.MacValue);
//		}
//
//		// ?? isoMsg.set(52, ifx.SignonRq.PINBlock);
//
//		// ?? isoMsg.set(7, ifx.Son.P7));
//		isoMsg.set(12, ifx.ExtISO.P12);// ??
//		// isoMsg.set(15, son.getAttributeValue(IFXFields.P15));
//		// isoMsg.set(17, son.getAttributeValue(IFXFields.P17));//??
//		// ?? isoMsg.set(22, son.getAttributeValue(IFXFields.P22));
//		// ?? isoMsg.set(24, son.getAttributeValue(IFXFields.P24));
//		isoMsg.set(22, "210101214120");
//		isoMsg.set(24, "200");
//		isoMsg.set(30, ifx.ExtISO.P30);// ??
//		isoMsg.set(39, ifx.ExtISO.P39);
//		isoMsg.set(42, ifx.ExtISO.P42);// ??
//		// ?? isoMsg.set(48, son.getAttributeValue(IFXFields.P48));
//		isoMsg.set(50, ifx.ExtISO.P50);// ??
//		// isoMsg.set(53, son.getAttributeValue(IFXFields.P53));
//		// isoMsg.set(54, son.getAttributeValue(IFXFields.P54));
//		// isoMsg.set(56, son.getAttributeValue(IFXFields.P56));
//		// isoMsg.set(74, son.getAttributeValue(IFXFields.P74));
//		// isoMsg.set(75, son.getAttributeValue(IFXFields.P75));
//		// isoMsg.set(76, son.getAttributeValue(IFXFields.P76));
//		// isoMsg.set(77, son.getAttributeValue(IFXFields.P77));
//		// isoMsg.set(78, son.getAttributeValue(IFXFields.P78));
//		// isoMsg.set(79, son.getAttributeValue(IFXFields.P79));
//		// isoMsg.set(81, son.getAttributeValue(IFXFields.P80));
//		// isoMsg.set(86, son.getAttributeValue(IFXFields.P86));
//		// isoMsg.set(87, son.getAttributeValue(IFXFields.P87));
//		// isoMsg.set(88, son.getAttributeValue(IFXFields.P88));
//		// isoMsg.set(89, son.getAttributeValue(IFXFields.P89));
//		// isoMsg.set(93, son.getAttributeValue(IFXFields.P93));
//		// isoMsg.set(94, son.getAttributeValue(IFXFields.P94));
//		// isoMsg.set(96, son.getAttributeValue(IFXFields.P96));
//		// isoMsg.set(97, son.getAttributeValue(IFXFields.P97));
//		// isoMsg.set(124, son.getAttributeValue(IFXFields.P124));
//
//		return isoMsg;
    }
}
