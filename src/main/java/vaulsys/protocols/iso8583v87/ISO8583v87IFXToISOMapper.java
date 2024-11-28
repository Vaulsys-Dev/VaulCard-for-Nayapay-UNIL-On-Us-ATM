package vaulsys.protocols.iso8583v87;

import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryField;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;

import java.util.Date;

public class ISO8583v87IFXToISOMapper extends IfxToISOMapper{

	public static final ISO8583v87IFXToISOMapper Instance = new ISO8583v87IFXToISOMapper();
	
	private ISO8583v87IFXToISOMapper(){}
	
	
	
    public ISOMsg map(Ifx ifx, EncodingConvertor convertor) throws ISOException {

//        MyDateFormat dateFormatMMDDhhmmss = new MyDateFormat("MMddHHmmss");
//        MyDateFormat dateFormatYYMM = new MyDateFormat("yyMM");
//        MyDateFormat dateFormathhmmss = new MyDateFormat("HHmmss");
//        MyDateFormat dateFormatMMDD = new MyDateFormat("MMdd");

        ISOMsg isoMsg = new ISOMsg();
        ISOPackager packager = ((ISO8583v87Protocol) ProtocolProvider.Instance.getByClass(ISO8583v87Protocol.class)).getPackager();
        isoMsg.setPackager(packager);

        isoMsg.setMTI(String.valueOf(fillMTI(ifx.getIfxType(), ifx.getMti())));
        isoMsg.set(2, ifx.getAppPAN());

        String processCode = "";
		if (AccType.MAIN_ACCOUNT.equals(ifx.getAccTypeFrom())) {
			processCode = "00";
		} else if (AccType.SUBSIDIARY_ACCOUNT.equals(ifx.getAccTypeFrom())) {
			processCode = "10";
		} else if (AccType.CARD.equals(ifx.getAccTypeFrom())) {
			processCode = "20";
		}
		if (AccType.MAIN_ACCOUNT.equals(ifx.getAccTypeTo())) {
			processCode += "00";
		} else if (AccType.SUBSIDIARY_ACCOUNT.equals(ifx.getAccTypeTo())) {
			processCode += "10";
		} else if (AccType.CARD.equals(ifx.getAccTypeTo())) {
			processCode += "20";
		}

        isoMsg.set(3, mapTrnType(ifx.getTrnType()) + processCode);

        Long amt = ifx.getAuth_Amt();
        if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
        	isoMsg.set(4, ifx.getAuth_Amt().toString());
        	        	
        } else {
        	isoMsg.set(4, ifx.getTrx_Amt().toString());
        	amt = ifx.getTrx_Amt();
        	
        }
        
        if (ifx.getSec_Amt() == null) {
        	isoMsg.set(6, isoMsg.getString(4));
        	
        } else if (ifx.getSec_Amt() != null && ifx.getSec_Amt().equals(amt)){
        	isoMsg.set(6, amt.toString());
        	
        } else if (ifx.getSec_Amt() != null && ifx.getSec_Amt().equals(ifx.getTrx_Amt())) {
        	isoMsg.set(6, amt.toString());
        	
        } else if (ifx.getSec_Amt() != null) {
        	isoMsg.set(6, ifx.getSec_Amt().toString());
        	
        }
        
//        if (ifx.getAuth_Amt() != null)
//			isoMsg.set(4, ifx.getAuth_Amt().toString());
//        
//        if (ifx.getSec_Amt() == null && ifx.getAuth_Amt() != null)
//        	isoMsg.set(6, isoMsg.getString(4));
//        else if (ifx.getSec_Amt() != null)
//			isoMsg.set(6, ifx.getSec_Amt().toString());
        
        if (ifx.getTrnDt() != null)
        	isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifx.getTrnDt().toDate()));
        isoMsg.set(10, ifx.getSec_CurRate());
        isoMsg.set(11, ifx.getSrc_TrnSeqCntr());
        isoMsg.set(12, MyDateFormatNew.format("HHmmss", ifx.getOrigDt().toDate()));
        isoMsg.set(13, MyDateFormatNew.format("MMdd", ifx.getOrigDt().toDate()));

        if (ifx.getSafeCardAcctId() != null)
            isoMsg.set(14, ifx.getExpDt());
        else
            isoMsg.set(14, MyDateFormatNew.format("yyMM", (Date) null));

        if (ifx.getSettleDt() != null)
        	isoMsg.set(15, MyDateFormatNew.format("MMdd", ifx.getSettleDt()));
        if (ifx.getPostedDt()!= null)
        	isoMsg.set(17, MyDateFormatNew.format("MMdd", ifx.getPostedDt()));

//        String terminalType = "";
//        if (TerminalType.ATM.equals(ifx.getTerminalType()))
//            terminalType = "02";
//        else if (TerminalType.PINPAD.equals(ifx.getTerminalType()))
//            terminalType = "03";
//        else if (TerminalType.POS.equals(ifx.getTerminalType()))
//            terminalType = "14";
//        else if (TerminalType.INTERNET.equals(ifx.getTerminalType()))
//            terminalType = "59";
//        else if (TerminalType.VRU.equals(ifx.getTerminalType()))
//            terminalType = "07";
//
//        isoMsg.set(25, terminalType);
        isoMsg.set(25, fillTerminalType(ifx));
        isoMsg.set(32, ifx.getBankId().toString());

//		isoMsg.set(33, ifx.MsgRqHdr.NetworkTrnInfo.FwdBankId);
        isoMsg.set(33, ifx.getDestBankId().toString());
        isoMsg.set(35, ifx.getTrk2EquivData());
//        StringFormat format37 = new StringFormat(12, StringFormat.JUST_RIGHT);
        isoMsg.set(37, StringFormat.formatNew(12, StringFormat.JUST_RIGHT, ifx.getNetworkRefId(), '0'));
        isoMsg.set(38, ifx.getApprovalCode());
        
        isoMsg.set(39, mapError(ifx.getIfxType(), ifx.getRsCode()));
        
//        StringFormat format41 = new StringFormat(8, StringFormat.JUST_RIGHT);
//        StringFormat format42 = new StringFormat(15, StringFormat.JUST_RIGHT);
        isoMsg.set(41, StringFormat.formatNew(8, StringFormat.JUST_RIGHT, ifx.getTerminalId(), '0'));
        isoMsg.set(42, StringFormat.formatNew(15, StringFormat.JUST_RIGHT, ifx.getOrgIdNum().toString(), '0'));

        String field43 = "bpi                   Tehran       7  IR";
//        try {
////            ITerminal terminal = getTerminalService().findTerminal(Long.valueOf(ifx.getTerminalId()));
////            Contact contact = terminal.getOwner().getContact();
//
////            ifx.setName( contact.getName());
////            ifx.setCity( contact.getAddress().getCity());
////            ifx.setStateProv( contact.getAddress().getState());
////            ifx.setCountry( contact.getAddress().getCountry());
////            ifx.setCity( contact.getAddress().getCity().getAbbreviation());
////            ifx.setStateProv( contact.getAddress().getState().getAbbreviation());
////            ifx.setCountry( contact.getAddress().getCountry().getAbbreviation());
//
////			field43 = MyString.valueOf(ifx.getName, ' ', 22, true)
////					+ MyString.valueOf(ifx.getCity, ' ', 13, true)
////					+ MyString.valueOf(ifx.getStateProv, ' ', 3, true)
////					+ MyString.valueOf(ifx.getCountry, ' ', 2, true);
////			field43 = MyString.valueOf(contact.getName(), ' ', 22, true)
////			+ MyString.valueOf(contact.getCity(), ' ', 13, true)
////			+ MyString.valueOf(contact.getStateProv(), ' ', 3, true)
////			+ MyString.valueOf(contact.getCountry(), ' ', 2, true);
//
//            //field43 = MyString.valueOf("NA", ' ', 40, true);
//
//            field43 = MyString.valueOf("bpi                   Tehran       7  IR", ' ', 40, true);
//        } catch (Exception e) {
//            field43 = MyString.valueOf("bpi                   Tehran       7  IR", ' ', 40, true);
//        }

        isoMsg.set(new ISOBinaryField(43, fillField43(ifx, convertor)));

        isoMsg.set(new ISOBinaryField(44, fillField44(ifx, convertor)));
        
        isoMsg.set(49, ifx.getAuth_Currency());
        isoMsg.set(51, (ifx.getSec_Currency() != null /*&& Util.hasText(ifx.getSec_Currency())*/? ifx.getSec_Currency() : ifx.getAuth_Currency() ));
        
        isoMsg.set(52, ifx.getPINBlock());

        if (ifx.getMode()!= null && Util.hasText(ifx.getCheckDigit()))
        	isoMsg.set(53, ifx.getMode().getType()+ifx.getCheckDigit()+"00000000000");
        
        String P54 = "";
        String strBal = "";

        //for( AcctBal acctBal:ifx.getAcctBals) {
        for (int i = 0; i < 2; ++i) {
            AcctBal acctBal = null;
            if (i == 0)
                acctBal = ifx.getAcctBalAvailable();
            else if (i == 1)
                acctBal = ifx.getAcctBalLedger();

            if (acctBal == null)
                continue;

            strBal = "";
            if (acctBal.getAcctType().equals(AccType.CURRENT))
                strBal += "01";
            else if (acctBal.getAcctType().equals( AccType.SAVING))
                strBal += "02";
            else
                strBal += "00";

            if (acctBal.getBalType().equals(BalType.LEDGER))
                strBal += "01";
            else if (acctBal.getBalType().equals(BalType.AVAIL))
                strBal += "02";
            else
                strBal += "00";

            strBal += acctBal.getCurCode();
            strBal += acctBal.getAmt();
            P54 += strBal;
        }

        isoMsg.set(54, P54);

        if (ifx.getNetworkManagementInformationCode()!= null)
        	isoMsg.set(70, ifx.getNetworkManagementInformationCode().getType());
        
        String S90 = "";

        if(ISOFinalMessageType.isReversalOrRepeatMessage(ifx.getIfxType()) || ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {
//            StringFormat binFormat = new StringFormat(11, StringFormat.JUST_RIGHT);
//            StringFormat trnSeqCntrFormat = new StringFormat(6, StringFormat.JUST_RIGHT);

            
            if (ifx.getSafeOriginalDataElements().getMessageType() == null)
            	throw new ISOException("Invalid original data element: No Message Type for field 90");
            
            S90 += ifx.getSafeOriginalDataElements().getMessageType();
            S90 += StringFormat.formatNew(6, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getTrnSeqCounter(), '0');
            S90 += MyDateFormatNew.format("MMddHHmmss", ifx.getSafeOriginalDataElements().getOrigDt().toDate());
            S90 += StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getBankId(), '0');
            S90 += StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getFwdBankId(), '0');
            isoMsg.set(90, S90);
        }


        String S95 = "";
        if (ifx.getNew_AmtAcqCur() != null && ifx.getNew_AmtIssCur() != null) {
            S95 += ifx.getNew_AmtAcqCur();
            S95 += ifx.getNew_AmtIssCur();
            S95 += "C00000000";
            S95 += "C00000000";
            isoMsg.set(95, S95);
        }


        if (ifx.getRecvBankId() == null || ifx.getRecvBankId().equals(""))
            isoMsg.set(100, ifx.getDestBankId().toString());
        else
            isoMsg.set(100, ifx.getRecvBankId().toString());

//		isoMsg.set(15, ifx.getExtISO.P15);//

        isoMsg.set(18, "0743");

//        isoMsg.set(22, ((ExtISO)ifx.getIFX_Ext()).P22);
//        isoMsg.set(24, ((ExtISO)ifx.getIFX_Ext()).P24);
//        isoMsg.set(30, ((ExtISO)ifx.getIFX_Ext()).P30);
//		isoMsg.set(39, ((ExtISO)ifx.getIFX_Ext()).P39);//
//        isoMsg.set(50, ((ExtISO)ifx.getIFX_Ext()).P50);
//        isoMsg.set(53, ((ExtISO)ifx.getIFX_Ext()).P53);
//        isoMsg.set(56, ((ExtISO)ifx.getIFX_Ext()).P56);
//        isoMsg.set(74, ((ExtISO)ifx.getIFX_Ext()).P74);
//        isoMsg.set(75, ((ExtISO)ifx.getIFX_Ext()).P75);
//        isoMsg.set(76, ((ExtISO)ifx.getIFX_Ext()).P76);
//        isoMsg.set(77, ((ExtISO)ifx.getIFX_Ext()).P77);
//        isoMsg.set(78, ((ExtISO)ifx.getIFX_Ext()).P78);
//        isoMsg.set(79, ((ExtISO)ifx.getIFX_Ext()).P79);
//        isoMsg.set(81, ((ExtISO)ifx.getIFX_Ext()).P80);
//        isoMsg.set(86, ((ExtISO)ifx.getIFX_Ext()).P86);
//        isoMsg.set(87, ((ExtISO)ifx.getIFX_Ext()).P87);
//        isoMsg.set(88, ((ExtISO)ifx.getIFX_Ext()).P88);
//        isoMsg.set(89, ((ExtISO)ifx.getIFX_Ext()).P89);
//        isoMsg.set(93, ((ExtISO)ifx.getIFX_Ext()).P93);
//        isoMsg.set(94, ((ExtISO)ifx.getIFX_Ext()).P94);
//        isoMsg.set(96, ((ExtISO)ifx.getIFX_Ext()).P96);
//        isoMsg.set(97, ((ExtISO)ifx.getIFX_Ext()).P97);
//        isoMsg.set(102, ((ExtISO)ifx.getIFX_Ext()).P102);
//		isoMsg.set(124, ((ExtISO)ifx.getIFX_Ext()).P124);

        if (isoMsg.getMaxField() > 64) {
            isoMsg.set(128, ifx.getMsgAuthCode());
            isoMsg.unset(64);
        } else {
            isoMsg.set(64, ifx.getMsgAuthCode());
            isoMsg.unset(128);
        }
//        isoMsg.set(48, fillField48(ifx) );
        isoMsg.set(new ISOBinaryField(48, fillField48(ifx, convertor)));
        
        return isoMsg;
    }

//    private static TerminalService getTerminalService() {
//        return SwitchApplication.get().getTerminalService();
//    }
    
    @Override
	public byte[] fillField48(Ifx ifx, EncodingConvertor convertor) {
		String p48 = "";
//		if (TerminalType.INTERNET.equals(ifx.getTerminalType())) {
			p48 += "00";
			p48 += (Util.hasText(ifx.getCVV2()) ? ifx.getCVV2() : "0000");
//		}
		if (ISOFinalMessageType.isBillPaymentMessage(ifx.getIfxType())) {
//			if (!TerminalType.INTERNET.equals(ifx.getTerminalType()))
//				p48 += "000000";
//			StringFormat format = new StringFormat(13, StringFormat.JUST_RIGHT);
//			StringFormat format10 = new StringFormat(10, StringFormat.JUST_RIGHT);
			p48 += StringFormat.formatNew(10, StringFormat.JUST_RIGHT, "0", '0');
			p48 += ifx.getBillOrgType().getType();
			p48 += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillID(), '0');
			p48 += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillPaymentID(), '0');
		} else if (ISOFinalMessageType.isTransferMessage(ifx.getIfxType())) {
//			if (ShetabFinalMessageType.isTransferCheckAccountMessage(ifx.getIfxType()))
//				p48 = "00" + "0000";
			UserLanguage userLanguage = ifx.getUserLanguage();
			if (UserLanguage.FARSI_LANG.equals(userLanguage))
				p48 += "00";
			else 
				p48 += "01";
			String appPan = ifx.getSecondAppPan();
			p48 += appPan.length();
			p48 += appPan;
		}
		return p48.getBytes();
	}
}
