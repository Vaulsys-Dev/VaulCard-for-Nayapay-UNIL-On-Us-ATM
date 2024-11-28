package vaulsys.protocols.iso8583v93;

import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;

import java.util.Date;

public class ISO8583v93IFXToISOMapper extends IfxToISOMapper{

	public static final ISO8583v93IFXToISOMapper Instance = new ISO8583v93IFXToISOMapper();
	
	private ISO8583v93IFXToISOMapper(){}
	
	
    public ISOMsg map(Ifx ifx, EncodingConvertor convertor) throws ISOException {

//        MyDateFormat dateFormatMMDDhhmmss = new MyDateFormat("MMddHHmmss");
//        MyDateFormat dateFormatYYMM = new MyDateFormat("yyMM");
//        MyDateFormat dateFormathhmmss = new MyDateFormat("HHmmss");
//        MyDateFormat dateFormatMMDD = new MyDateFormat("MMdd");

        ISOMsg isoMsg = new ISOMsg();
        ISOPackager packager = ((ISO8583v93Protocol) ProtocolProvider.Instance.
        		getByClass(ISO8583v93Protocol.class)).getPackager();
        isoMsg.setPackager(packager);

        isoMsg.setMTI(String.valueOf(ifx.getIfxType()));
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
		
        isoMsg.set(3, mapTrnType(ifx.getTrnType())+ processCode);

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
        
        isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifx.getTrnDt().toDate()));
        isoMsg.set(10, ifx.getSec_CurRate());
        isoMsg.set(11, ifx.getSrc_TrnSeqCntr());
        isoMsg.set(12, MyDateFormatNew.format("HHmmss", ifx.getOrigDt().toDate()));
        isoMsg.set(13, MyDateFormatNew.format("MMdd", ifx.getOrigDt().toDate()));

        
        if (ifx.getSettleDt() != null)
        	isoMsg.set(15, MyDateFormatNew.format("MMdd", ifx.getSettleDt()));
        if (ifx.getPostedDt()!= null)
        	isoMsg.set(17, MyDateFormatNew.format("MMdd", ifx.getPostedDt()));

        if (ifx.getSafeCardAcctId() != null)
        	isoMsg.set(14, ifx.getExpDt());
        else
        	isoMsg.set(14, MyDateFormatNew.format("yyMM", (Date) null));
        
        
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
        
        
        
//        String field43 = "bpi                   Tehran       7  IR";
//        try {
////            ITerminal terminal = getTerminalService().findTerminal(Long.valueOf(ifx.getTerminalId()));
////            Contact contact = terminal.getOwner().getContact();
////
////            ifx.setName( contact.getName());
////            ifx.setCity( contact.getAddress().getCity());
////            ifx.setStateProv( contact.getAddress().getState());
////            ifx.setCountry( contact.getAddress().getCountry());
////            ifx.setCity( contact.getAddress().getCity().getAbbreviation());
////            ifx.setStateProv( contact.getAddress().getState().getAbbreviation());
////            ifx.setCountry( contact.getAddress().getCountry().getAbbreviation());
//
//            field43 = MyString.valueOf("bpi                   Tehran       7  IR", ' ', 40, true);
//        } catch (Exception e) {
//            field43 = MyString.valueOf("bpi                   Tehran       7  IR", ' ', 40, true);
//        }
        isoMsg.set(44, fillField44(ifx, convertor));
        
        isoMsg.set(49, ifx.getAuth_Currency());
        isoMsg.set(51, ifx.getSec_Currency());
        isoMsg.set(52, ifx.getPINBlock());
        
        String P54 = "";
        String strBal = "";

        //for( AcctBal acctBal:ifx.AcctBals) {
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

        isoMsg.set(48, fillField48(ifx, convertor));

        return isoMsg;
    }

    @Override
	public byte[] fillField48(Ifx ifx, EncodingConvertor convertor) {
		String p48 = "";
		if (ISOFinalMessageType.isBillPaymentMessage(ifx.getIfxType())) {
//			StringFormat format = new StringFormat(13, StringFormat.JUST_RIGHT);
//			StringFormat format10 = new StringFormat(10, StringFormat.JUST_RIGHT);
			p48 += "00";
			p48 += ifx.getCVV2();
			p48 += StringFormat.formatNew(10, StringFormat.JUST_RIGHT, "0", '0');
			p48 += ifx.getBillOrgType().getType();
			p48 += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillID(), '0');
			p48 += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillPaymentID(), '0');
		}
		return p48.getBytes();
	}


}
