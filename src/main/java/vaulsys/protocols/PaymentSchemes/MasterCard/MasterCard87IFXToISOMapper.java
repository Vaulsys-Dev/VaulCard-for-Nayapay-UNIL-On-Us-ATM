package vaulsys.protocols.PaymentSchemes.MasterCard;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.migration.MigrationDataService;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.IfxToISOMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryField;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

public class MasterCard87IFXToISOMapper extends IfxToISOMapper{

	transient Logger logger = Logger.getLogger(MasterCard87IFXToISOMapper.class);
	public static final MasterCard87IFXToISOMapper Instance = new MasterCard87IFXToISOMapper();
	
	protected MasterCard87IFXToISOMapper(){}
	
    public ISOMsg map(Ifx ifx, EncodingConvertor convertor) throws ISOException {
		ISOMsg isoMsg = new ISOMsg();
		ISOPackager packager = ((MasterCard87Protocol) ProtocolProvider.Instance
				.getByClass(MasterCard87Protocol.class)).getPackager();
		isoMsg.setPackager(packager);
		if (ISOFinalMessageType.isTransferCardToAccountMessage(ifx.getIfxType())) {
			ifx.setTrnType(TrnType.DECREMENTALTRANSFER);
		}
		//logger.debug("incomingIfx.getMti() Incoming MTI [" + ifx.getMti() + "]"); //Raza TEMP
		if (Util.hasText(ifx.getMti())) //Raza setting MTI
		{
			isoMsg.setMTI(ifx.getMti());
		} else {
			isoMsg.setMTI(String.valueOf(fillMTI(ifx.getIfxType(), ifx.getMti())));
		}
		logger.debug("IFXtoISO:: MTI [" + isoMsg.getMTI() + "]"); //Raza LOGGING ENHANCED

		isoMsg.set(2, ifx.getAppPAN());
		logger.debug("IFXtoISO::DE-2 PAN [" + ifx.getAppPAN() + "]"); //Raza LOGGING ENHANCED

		String processCode = "0000";
		isoMsg.set(3, mapTrnType(ifx.getTrnType()) + processCode);
		logger.debug("IFXtoISO::DE-3 ProcCode [" + isoMsg.getString(3) + "]"); //Raza LOGGING ENHANCED

//        if (!IntSwitchFinalMessageType.isReversalMessage(ifx.getIfxType())) {
//        	if (ifx.getReal_Amt() != null)
//        		isoMsg.set(4, ifx.getReal_Amt().toString());
//        	
//        } else {
//        	
//        }

		String str_fld4 = ifx.getAuth_Amt().toString();
		if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
			if (str_fld4 != null)
				isoMsg.set(4, str_fld4);
			logger.debug("IFXtoISO::DE-4 Amount-Tran [" + str_fld4 + "]"); //Raza LOGGING ENHANCED
		} else {
			Long fld4_trx = ifx.getTrx_Amt();
			if (fld4_trx != null) {
				isoMsg.set(4, fld4_trx.toString());
				logger.debug("IFXtoISO::DE-4 Amount-Tran [" + fld4_trx + "]"); //Raza LOGGING ENHANCED
			}

		}

		Long Sec_Amt = ifx.getSec_Amt();
		if (Sec_Amt == null && str_fld4 != null) {
			isoMsg.set(6, str_fld4);
			logger.debug("IFXtoISO::DE-6 Amount-CardHolderBilling [" + str_fld4 + "]"); //Raza LOGGING ENHANCED
		} else if (Sec_Amt != null && Sec_Amt.toString().equals(str_fld4)) {
			isoMsg.set(6, str_fld4);
			logger.debug("IFXtoISO::DE-6 Amount-CardHolderBilling [" + str_fld4 + "]"); //Raza LOGGING ENHANCED
		} else if (Sec_Amt != null && Sec_Amt.toString().equals(ifx.getTrx_Amt())) {
			isoMsg.set(6, str_fld4);
			logger.debug("IFXtoISO::DE-6 Amount-CardHolderBilling [" + str_fld4 + "]"); //Raza LOGGING ENHANCED
		} else if (Sec_Amt != null) {
			isoMsg.set(6, Sec_Amt);
			logger.debug("IFXtoISO::DE-6 Amount-CardHolderBilling [" + Sec_Amt + "]"); //Raza LOGGING ENHANCED
		}

//        if (ifx.getSent_Amt() != null)
//			isoMsg.set(4, ifx.getSent_Amt().toString());
//
//        if (ifx.getSec_Amt() == null && ifx.getSent_Amt() != null)
//        	isoMsg.set(6, isoMsg.getString(4));
//        
//        else if (ifx.getSec_Amt() != null && ifx.getSec_Amt().equals(ifx.getSent_Amt()))
//        	isoMsg.set(6, isoMsg.getString(4));
//        
//        else if (ifx.getSec_Amt() != null && ifx.getSec_Amt().equals(ifx.getAuth_Amt()))
//        	isoMsg.set(6, isoMsg.getString(4));
//        
//        else if (ifx.getSec_Amt() != null) {
//        	isoMsg.set(6, ifx.getSec_Amt().toString());
//        }

//        if (ifx.getReal_Amt() != null)
//        	isoMsg.set(4, ifx.getReal_Amt().toString());

//        if (ifx.getSec_Amt() == null && ifx.getReal_Amt() != null)
//        	isoMsg.set(6, isoMsg.getString(4));
//        
//        else if (ifx.getSec_Amt() != null && ifx.getSec_Amt().equals(ifx.getAuth_Amt()))
//        	isoMsg.set(6, isoMsg.getString(4));
//        
//        else if (ifx.getSec_Amt() != null) 
//			isoMsg.set(6, ifx.getSec_Amt().toString());

//        if (ifx.getAuth_Amt() != null)
//        	isoMsg.set(4, ifx.getAuth_Amt().toString());
//        
//        if (ifx.getSec_Amt() == null && ifx.getAuth_Amt() != null)
//        	isoMsg.set(6, isoMsg.getString(4));
//        else if (ifx.getSec_Amt() != null)
//        	isoMsg.set(6, ifx.getSec_Amt().toString());
		DateTime fld7 = ifx.getTrnDt();
		if (fld7 != null) {
			isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", fld7.toDate()));
			logger.debug("IFXtoISO::DE-7 Transmission Date & Time [" + isoMsg.getString(7) + "]"); //Raza LOGGING ENHANCED -- Optimize this do'nt use get
		}

		String str_fld10 = ifx.getSec_CurRate();
		if (Util.hasText(str_fld10)) {
			isoMsg.set(10, str_fld10);
			logger.debug("IFXtoISO::DE-10 Conversion Rate Settlement [" + str_fld10 + "]");
		}

		//Mirkamali(Task166): Adapt with IntSwitch's V7
		String str_fld11 = ifx.getMy_TrnSeqCntr();
		if (Util.hasText(str_fld11)) {
			isoMsg.set(11, str_fld11);
			logger.debug("IFXtoISO::DE-11 STAN [" + str_fld11 + "]");
			//isoMsg.set(11, ifx.getSrc_TrnSeqCntr());
		}


		isoMsg.set(12, MyDateFormatNew.format("HHmmss", ifx.getOrigDt().toDate()));
		logger.debug("IFXtoISO::DE-12 Time Loc Tran [" + isoMsg.getString(12) + "]"); //Raza LOGGING ENHANCED
		//logger.debug("Field - 12 [" + isoMsg.getString(12) + "]");

		isoMsg.set(13, MyDateFormatNew.format("MMdd", ifx.getOrigDt().toDate()));
		logger.debug("IFXtoISO::DE-13 Date Loc Tran [" + isoMsg.getString(13) + "]"); //Raza LOGGING ENHANCED

		String str_fld14 = ifx.getExpDt().toString();
		if (str_fld14 != null) {
			isoMsg.set(14, str_fld14);
			logger.debug("IFXtoISO::DE-14 Date Expiration [" + str_fld14 + "]"); //Raza LOGGING ENHANCED
			//logger.debug("Field - 14 [" + isoMsg.getString(14) + "]");
		}

		if (ifx.getSettleDt() != null) {
			isoMsg.set(15, MyDateFormatNew.format("MMdd", ifx.getSettleDt()));
			logger.debug("IFXtoISO::DE-15 Date Settlement [" + isoMsg.getString(15) + "]"); //Raza LOGGING ENHANCED
		}

		if (ifx.getSec_CurDate() != null) //Raza MasterCard for DE-16
		{
			isoMsg.set(16, ifx.getSec_CurDate());
			logger.debug("IFXtoISO::DE-16 Date Conversion [" + isoMsg.getString(16) + "]"); //Raza LOGGING ENHANCED
		}

        /*if (ifx.getPostedDt()!= null) {
			isoMsg.set(17, MyDateFormatNew.format("MMdd", ifx.getPostedDt()));
			logger.debug("IFXtoISO::DE-17 Date Capture [" + isoMsg.getString(17) + "]"); //Raza LOGGING ENHANCED
		}*///Raza MasterCard commenting

		String str_fld18 = "";
		if (Util.hasText(str_fld18)) //Raza MasterCard for DE-16
		{
			isoMsg.set(18, str_fld18);
			logger.debug("IFXtoISO::DE-18 Merchant Type [" + str_fld18 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld20 = "";
		if (Util.hasText(str_fld20)) {
			isoMsg.set(20, str_fld20);
			logger.debug("IFXtoISO::DE-20 PAN Country Code [" + str_fld20 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld22 = "";
		if (Util.hasText(str_fld22)) {
			isoMsg.set(22, str_fld22);
			logger.debug("IFXtoISO::DE-22 POS Entry Mode [" + str_fld22 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld23 = "";
		if (Util.hasText(str_fld23)) {
			isoMsg.set(23, str_fld23);
			logger.debug("IFXtoISO::DE-23 Card Sequence Number [" + str_fld23 + "]"); //Raza LOGGING ENHANCED
		}

		isoMsg.set(25, fillTerminalType(ifx));
		logger.debug("IFXtoISO::DE-25 POS Condition Code [" + isoMsg.getString(25) + "]"); //Raza LOGGING ENHANCED

		String str_fld26 = "";
		if (Util.hasText(str_fld26)) {
			isoMsg.set(26, str_fld26);
			logger.debug("IFXtoISO::DE-26 POS PIN Capture Code [" + str_fld26 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld28 = "";
		if (Util.hasText(str_fld28)) {
			isoMsg.set(28, str_fld28);
			logger.debug("IFXtoISO::DE-28 Amount-Tran Fee [" + str_fld28 + "]"); //Raza LOGGING ENHANCED
		}

//        if(ifx.getTerminalType().equals(TerminalType.MOBILE))
//        	isoMsg.set(25, TerminalType.INTERNET.getCode());
		String str_fld32 = ifx.getBankId();
		if (Util.hasText(str_fld32)) {
			isoMsg.set(32, str_fld32);
			logger.debug("IFXtoISO::DE-25 Acquiring Inst Id Code [" + str_fld32 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld33 = ifx.getFwdBankId();
		if (Util.hasText(str_fld33)) {
			isoMsg.set(33, str_fld33);
			logger.debug("IFXtoISO::DE-33 Forwarding Inst Id Code [" + str_fld33 + "]"); //Raza LOGGING ENHANCED
		}


		if (!ifx.isResponse()) {
			String str_fld35 = ifx.getTrk2EquivData();
			isoMsg.set(35, str_fld35);
			logger.debug("IFXtoISO::DE-35 Track 2 Data [" + str_fld35 + "]"); //Raza LOGGING ENHANCED
		}


		isoMsg.set(37, fillFieldANFix(ifx, 37));
		logger.debug("IFXtoISO::DE-37 RRN [" + isoMsg.getString(37) + "]"); //Raza LOGGING ENHANCED

		String str_fld38 = ifx.getApprovalCode();
		if (Util.hasText(str_fld38)) {
			isoMsg.set(38, str_fld38);
			logger.debug("IFXtoISO::DE-38 Auth Id Response [" + isoMsg.getString(38) + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld39 = ifx.getRsCode();
		if (Util.hasText(str_fld39)) {
			isoMsg.set(39, mapError(ifx.getIfxType(), str_fld39));
			logger.debug("IFXtoISO::DE-39 Response Code [" + str_fld39 + "]"); //Raza LOGGING ENHANCED
		}

		isoMsg.set(41, fillFieldANFix(ifx, 41));
		logger.debug("IFXtoISO::DE-41 Card Acceptor Terminal Id [" + isoMsg.getString(41) + "]"); //Raza LOGGING ENHANCED

		isoMsg.set(42, fillFieldANFix(ifx, 42));
		logger.debug("IFXtoISO::DE-42 Card Acceptor Identification Code [" + isoMsg.getString(42) + "]"); //Raza LOGGING ENHANCED

		String str_fld43 = getcardacceptornamelocation(ifx);
		if (Util.hasText(str_fld43)) {
			isoMsg.set(43, str_fld43); //new ISOBinaryField(43, fillField43(ifx, convertor)));
			logger.debug("IFXtoISO::DE-43 Card Acceptor Name Location [" + str_fld43 + "]"); //Raza LOGGING ENHANCED
		}

        /*isoMsg.set(new ISOBinaryField(44, fillField44(ifx, convertor))); //Update Field 44
		logger.debug("Field - 44 [" + isoMsg.getString(44) + "]");
		logger.debug("IFXtoISO::DE-44 Additional Response Data [" + isoMsg.getString(44) + "]"); //Raza LOGGING ENHANCED*/

		String str_fld45 = "";
		if (Util.hasText(str_fld45)) {
			isoMsg.set(45, str_fld45);
			logger.debug("IFXtoISO::DE-45 Track 1 Data [" + str_fld45 + "]"); //Raza LOGGING ENHANCED*/
		}

		String str_fld48 = "";
		if (Util.hasText(str_fld48)) {
			isoMsg.set(48, str_fld48);
			logger.debug("IFXtoISO::DE-48 Additional Data [" + str_fld48 + "]"); //Raza LOGGING ENHANCED*/
		}

		Integer fld49 = ifx.getAuth_Currency();
		if (fld49 != null) {
			isoMsg.set(49, fld49);
			logger.debug("IFXtoISO::DE-49 Currency Code Tran [" + fld49 + "]");
		}

		String str_fld50 = "";
		if (Util.hasText(str_fld50)) {
			logger.debug("IFXtoISO::DE-50 Currency Code Settlement [" + str_fld50 + "]");
		}

		Integer fld51 = ifx.getSec_Currency(), auth_Cur = ifx.getAuth_Currency();
		if (fld51 != null) {
			isoMsg.set(51, fld51);
			logger.debug("IFXtoISO::DE-51 Currency Code Card Holder Billing [" + fld51 + "]");
		} else //Not Checking Auth Currency is null or not.. we assume it is filled
		{
			isoMsg.set(51, auth_Cur);
			logger.debug("IFXtoISO::DE-51 Currency Code Card Holder Billing [" + auth_Cur + "]");
		}

		String str_fld52 = ifx.getPINBlock();
		if (Util.hasText(str_fld52)) {
			isoMsg.set(52, str_fld52);
			logger.debug("IFXtoISO::DE-52 PIN Block [" + str_fld52 + "]");
		}


        /*if (ifx.getMode()!= null && Util.hasText(ifx.getCheckDigit())) //Raza MASTERCARD commenting
        	isoMsg.set(53, ifx.getMode().getType()+ifx.getCheckDigit()+"00000000000");
		logger.debug("Field - 53 [" + isoMsg.getString(53) + "]");*/

		//Raza MasterCard commenitng TEMP
		String P54 = "";
		String strBal = "";

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
			else if (acctBal.getAcctType().equals(AccType.SAVING))
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
		logger.debug("IFXtoISO::DE-54 Additional Amounts [" + P54 + "]"); //Raza commenting TEMP

		String str_fld55 = ""; //ICC_DATA
		if (Util.hasText(str_fld55)) {
			isoMsg.set(55, str_fld55);
			logger.debug("IFXtoISO::DE-55 ICC System Related Data [" + str_fld55 + "]");
		}

		String str_fld58 = "";
		if (Util.hasText(str_fld58))
		{
			isoMsg.set(58, str_fld58);
			logger.debug("IFXtoISO::DE-58 ICC Auth Agent Inst ID [" + str_fld58 + "]");
		}

		String str_fld60 = "";
		if(Util.hasText(str_fld60))
		{
			isoMsg.set(60, str_fld60);
			logger.debug("IFXtoISO::DE-60 Advice Reason Code [" + str_fld60 + "]"); //Raza LOGGING ENHANCED
		}

		if(isoMsg.isRequest()) {
			String str_fld61 = "" + ifx.getTerminalType();
			if (Util.hasText(str_fld61)) {
				logger.debug("IFXtoISO::DE-61 POS Data [" + str_fld61 + "]"); //Raza LOGGING ENHANCED
				isoMsg.set(61, str_fld61);
				//MasterCard does not uses Field 25 POS CONDITION CODE
				//ifx.setPosConditionCode(isoMsg.getString(25)); //Raza MasterCard Verify this.
			}
		}

		String str_fld62 = "";
		if(Util.hasText(str_fld62))
		{
			logger.debug("IFXtoISO::DE-62 Intermediate Network Facility (INF) Data [" + str_fld62 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld63 = ifx.getNetworkData();
		if(Util.hasText(str_fld63))
		{
			isoMsg.set(63,ifx.getNetworkData());
			logger.debug("IFXtoISO::DE-63 Network Data [" + str_fld62 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld64 = ifx.getMsgAuthCode();
		if(Util.hasText(str_fld64))
		{
			isoMsg.set(64, str_fld64);
			logger.debug("IFXtoISO::DE-64 Message Authentication Code [" + str_fld64 + "]"); //Raza LOGGING ENHANCED
		}

		if (ifx.getNetworkManagementInformationCode()!= null) {
			String str_fld70 = ""+ifx.getNetworkManagementInformationCode().getType();
			isoMsg.set(70, str_fld70);
			logger.debug("IFXtoISO::DE-70 Network Management Info Code [" + str_fld70 + "]"); //Raza LOGGING ENHANCED
		}
        
        StringBuilder S90 = new StringBuilder();
        if(ISOFinalMessageType.isReversalOrRepeatMessage(ifx.getIfxType()) || ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {
            if (ifx.getSafeOriginalDataElements().getMessageType() == null){
            	throw new ISOException("Invalid original data element: No Message Type for field 90");
            }
            
            S90.append(ifx.getSafeOriginalDataElements().getMessageType());
            
            try {
				S90.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getTrnSeqCounter(), '0'));
			} catch (Exception e) {
				S90.append("000000");
			}
			try {
				S90.append(MyDateFormatNew.format("MMddHHmmss", ifx.getSafeOriginalDataElements().getOrigDt().toDate()));
			} catch (Exception e) {
				S90.append("0000000000");
			}
			try {
				S90.append(StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getBankId(), '0'));
			} catch (Exception e) {
				S90.append("00000000000");
			}
			try {
				S90.append(StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getFwdBankId(), '0'));
			} catch (Exception e) {
				S90.append("00000000000");
			}
            isoMsg.set(90, S90.toString());
			logger.debug("IFXtoISO::DE-90 Org Data Elements [" + S90 + "]"); //Raza LOGGING ENHANCED
        }

        StringBuilder S95 = new StringBuilder();
        if (ifx.getNew_AmtAcqCur() != null && ifx.getNew_AmtIssCur() != null) {
            S95.append(ifx.getNew_AmtAcqCur());
            S95.append(ifx.getNew_AmtIssCur());
            S95.append("C00000000");
            S95.append("C00000000");
            isoMsg.set(95, S95);
			logger.debug("IFXtoISO::DE-95 Replacement Amounts [" + S95 + "]");
        }

		if (ifx.getKeyManagement()!= null && Util.hasText(ifx.getKeyManagement().getKey())) {
			isoMsg.set(new ISOBinaryField(96, Hex.decode(ifx.getKeyManagement().getKey())));
			logger.debug("IFXtoISO::DE-96 Field Message Security Code [" + isoMsg.getString(96) + "]");
		}

		String str_fld100 = ifx.getNetworkData();
		if(Util.hasText(str_fld100))
		{
			isoMsg.set(100, str_fld100);
			logger.debug("IFXtoISO::DE-100 Receving Institution Id Code [" + str_fld100 + "]");
		}
		//Raza MasterCard commenting
        /*if (ifx.getRecvBankId() == null || ifx.getRecvBankId().equals(""))
            isoMsg.set(100, ifx.getDestBankId().toString());
        else
            isoMsg.set(100, ifx.getRecvBankId().toString());

		logger.debug("Field - 100 [" + isoMsg.getString(100) + "]");*/

        /*if (isoMsg.getMaxField() > 64) { //Raza MasterCard commenting
            isoMsg.set(128, ifx.getMsgAuthCode());
            isoMsg.unset(64);
        } else {
            isoMsg.set(64, ifx.getMsgAuthCode());
            isoMsg.unset(128);
        }*/


		//logger.debug("Field - 128 [" + isoMsg.getString(128) + "]"); //Raza MasterCard commenting


		String str_fld102 = ifx.getMainAccountNumber();
		if(Util.hasText(str_fld102)) {
			isoMsg.set(102, str_fld102);
			logger.debug("IFXtoISO::DE-102 Account Id - 1 [" + isoMsg.getString(102) + "]");
		}


        
        StringBuilder CVV2 = new StringBuilder();
        StringBuilder secAppPAN = new StringBuilder();
        try {
        	MigrationDataService.setChangedFields(ifx, isoMsg, CVV2, secAppPAN);
        } catch(Exception e) {
        	logger.error("Exception in return changed field, ifx: " + ifx.getId(), e);
        }
        
        /****** Don't move this line, must be haminja! ******/
        //isoMsg.set(new ISOBinaryField(48, fillField48(ifx, CVV2.toString(), secAppPAN.toString(), convertor))); //Not Required by CUP
        
        return isoMsg;
    }

    @Override
    public byte[] fillField43(Ifx ifx, EncodingConvertor convertor) {
    	//TODO: GC Performance
//		byte[] result = new byte[40];
//				
//		String name = (Util.hasText(ifx.getName())) ? ifx.getName() : "";
//		name = StringFormat.formatNew(22, StringFormat.JUST_LEFT, name, ' ');
//		name = name.replaceAll("ی", "ي");
//		name = name.replaceAll("ء", "ئ");
//		name = name.replaceAll("ک", "ك");
//		
//		try {
//			System.arraycopy(name.getBytes("windows-1256"), 0, result, 0, 22);
//		} catch (UnsupportedEncodingException e) {			
//			System.arraycopy(name.getBytes(), 0, result, 0, 22);
//		}
//		
//		String city = " ";
//		city = StringFormat.formatNew(13, StringFormat.JUST_LEFT, city, ' ');
//		
//		try {
//			System.arraycopy(city.getBytes("windows-1256"), 0, result, 22, 13);
//		} catch (UnsupportedEncodingException e) {
//			System.arraycopy(city.getBytes(), 0, result, 22, 13);
//		}
//
//		String state = " ";
//		state = StringFormat.formatNew(3, StringFormat.JUST_LEFT, state, ' ');
//		System.arraycopy(state.getBytes(), 0, result, 35, 3);
//		
//		String country = " ";
//		country = StringFormat.formatNew(2, StringFormat.JUST_LEFT, country, ' ');
//		System.arraycopy(country.getBytes(), 0, result, 38, 2);
//    	
//    	return result;

		ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
		try {
			String ifxName = ifx.getName();
			TerminalType terminalType = ifx.getTerminalType();
			if (terminalType != null) {
				ifxName = terminalType.getName();
				
			} else {
				ifxName = "";
			}
			
			int len = 0;
			if (Util.hasText(ifxName))
				len = ifxName.length();
			
			else {
				ifxName = "";
			}
			
			if (len < 22) {
				for (int i = len; i< 22; i++) {
					ifxName += " "; 
				}
			} else {
				ifxName = ifxName.substring(0, 22);
			}
			
			finalBytes.write(/*convertor.encode(ifxName)*/ifxName.toUpperCase().getBytes());

		} catch (IOException e) {
			logger.error("Exception in writing field 43 " + e, e);
			for (int i = 0; i < 22; i++)
				finalBytes.write(32);
		}
		
		//city
		String city = "";
		for (int i = 0; i < 13; i++)
			city += " ";
		try {
			finalBytes.write(convertor.encode(city));
		} catch (IOException e) {
			logger.error("Exception in writing city in field 43 " + e, e);
			for (int i = 0; i < 13; i++)
				finalBytes.write(32);
		}
		
		//state
		String state = "";
		for (int i = 0; i < 3; i++)
			state += " ";
		try {
			finalBytes.write(convertor.encode(state));
		} catch (IOException e) {
			logger.error("Exception in writing state in field 43 " + e, e);
			for (int i = 0; i < 3; i++)
				finalBytes.write(32);
		}
		
		//country
		String country = "";
		for (int i = 0; i < 2; i++)
			country += " ";
		try {
			finalBytes.write(convertor.encode(country));
		} catch (IOException e) {
			logger.error("Exception in writing country in field 43 " + e, e);
			for (int i = 0; i < 2; i++)
				finalBytes.write(32);

		}
		
		return (finalBytes.size()==0)? null : finalBytes.toByteArray();
    }

	public String getcardacceptornamelocation(Ifx ifx) {

		String CardAcceptorNameLocation = "";
		if(ifx.getOriginatorTerminal() != null) {
			Terminal terminal = ifx.getOriginatorTerminal();

			if (TerminalType.POS.equals(ifx.getTerminalType())) { //CUP cards Acquiring will be done for ATM and POS only.
				POSTerminal pos = (POSTerminal) terminal;
				CardAcceptorNameLocation = pos.getcardacceptornamelocation();
			} else if (TerminalType.ATM.equals(terminal.getTerminalType())) {
				ATMTerminal atm = (ATMTerminal) terminal;
				CardAcceptorNameLocation = atm.getcardacceptornamelocation();
			} else if (TerminalType.PINPAD.equals(terminal.getTerminalType())) {
				PINPADTerminal pp = (PINPADTerminal) terminal;
				CardAcceptorNameLocation = pp.getcardacceptornamelocation();
			}
		}

		return CardAcceptorNameLocation;
	}
    
    @Override
	public byte[] fillField44(Ifx ifx, EncodingConvertor convertor) {
		ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
		if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
				&& (ISOFinalMessageType.isTransferMessage(ifx.getIfxType()) || IfxType.SORUSH_REV_REPEAT_RS.equals(ifx.getIfxType()))) {
				try {
					for (int i = 0; i < 25; i++)
						finalBytes.write(32);
					byte[] name = null;
					byte[] family = null;

					if (UserLanguage.ENGLISH_LANG.equals(ifx.getUserLanguage())) {
						if (ifx.getCardHolderName() != null)
							name = ifx.getCardHolderName().toUpperCase().getBytes();
						if (ifx.getCardHolderFamily() != null)
						family = ifx.getCardHolderFamily().toUpperCase().getBytes();
					} else {
						name = convertor.encode(ifx.getCardHolderName());
						family = convertor.encode(ifx.getCardHolderFamily());
					}

					finalBytes.write(convertor.finalize(name, null, null));
					finalBytes.write(convertor.finalize(family, null, null));

				} catch (IOException e) {
				}
				return (finalBytes.size()==0)? null : finalBytes.toByteArray();
		}
		return null;
	}
    
//    @Override
	public byte[] fillField48(Ifx ifx, String CVV2, String secAppPAN, EncodingConvertor convertor) {
		
		StringBuilder p48 = new StringBuilder();
		
		/****** Mirkamali(Task154): Correct field 48 for billpayment ******/
		//Reserve for IntSwitch
		p48.append("  ");  /*p48.append("00");*/
		
		// CVV2 (It temprary sets with space, it should be correct in future)
		p48.append(StringFormat.formatNew(4, StringFormat.JUST_RIGHT, CVV2, '0'));
		
		if (ISOFinalMessageType.isBillPaymentMessage(ifx.getIfxType()) || ISOFinalMessageType.isBillPaymentReverseMessage(ifx.getIfxType())) {
			
			UserLanguage userLanguage = ifx.getUserLanguage();
			if (UserLanguage.FARSI_LANG.equals(userLanguage))
				p48.append("00");
			else
				p48.append("01");
			
			//Reserve for IntSwitch
			p48.append(StringFormat.formatNew(8, StringFormat.JUST_RIGHT, " "));
			
			if (ifx.getBankId().equals(ProcessContext.get().getMyInstitution().getBin()) || FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(ProcessContext.get().getMyInstitution().getRole())) {

				p48.append(ifx.getBillOrgType().getType());
				p48.append(StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillID(), '0'));
				p48.append(StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillPaymentID(), '0'));
				
			} else {
				p48.append(ifx.getBillUnParsedData());
			}
		} else if (ISOFinalMessageType.isTransferMessage(ifx.getIfxType())) {
			if ( 
					(IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifx.getIfxType())|| IfxType.TRANSFER_REV_REPEAT_RS.equals(ifx.getIfxType()))
					&& !Util.hasText(secAppPAN)
				) {
				p48.append("");
			} else {
				UserLanguage userLanguage = ifx.getUserLanguage();
				if (UserLanguage.FARSI_LANG.equals(userLanguage))
					p48.append("00");
				else
					p48.append("01");
				String appPan = secAppPAN;
				if(Util.isAccount(secAppPAN)){
					appPan = "5022291111111111";
//					ifx.setSecondAppPan("5022291111111111");
					
				}
				if (Util.hasText(secAppPAN)){
					p48.append(appPan.length()+"");
					p48.append(appPan);
				}else{
					logger.error(ifx.getIfxType()+" doesn't have SecAppPan "+ secAppPAN+"!");
				}
			}
		}
		return p48.toString().getBytes();
	}
}
