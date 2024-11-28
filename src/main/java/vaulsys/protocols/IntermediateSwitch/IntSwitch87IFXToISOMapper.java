package vaulsys.protocols.IntermediateSwitch;

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

public class IntSwitch87IFXToISOMapper extends IfxToISOMapper{

	transient Logger logger = Logger.getLogger(IntSwitch87IFXToISOMapper.class);
	public static final IntSwitch87IFXToISOMapper Instance = new IntSwitch87IFXToISOMapper();
	
	protected IntSwitch87IFXToISOMapper(){}
	
    public ISOMsg map(Ifx ifx, EncodingConvertor convertor) throws ISOException {
        ISOMsg isoMsg = new ISOMsg();
        ISOPackager packager = ((IntSwitch87Protocol) ProtocolProvider.Instance
                .getByClass(IntSwitch87Protocol.class)).getPackager();
        isoMsg.setPackager(packager);
        if(ISOFinalMessageType.isTransferCardToAccountMessage(ifx.getIfxType())){
        	ifx.setTrnType(TrnType.DECREMENTALTRANSFER);
        }
		//System.out.println("incomingIfx.getMti() Incoming MTI [" + ifx.getMti() + "]"); //Raza TEMP
        isoMsg.setMTI(String.valueOf(fillMTI(ifx.getIfxType(), ifx.getMti())));
		System.out.println("Field MTI [" + isoMsg.getMTI() + "]");
        
        isoMsg.set(2, ifx.getAppPAN());
		System.out.println("Field - 2 [" + ifx.getAppPAN() + "]");

        String processCode = "0000";
        isoMsg.set(3, mapTrnType(ifx.getTrnType())+ processCode);
		System.out.println("Field - 3 [" + isoMsg.getString(3) + "]");

//        if (!IntSwitchFinalMessageType.isReversalMessage(ifx.getIfxType())) {
//        	if (ifx.getReal_Amt() != null)
//        		isoMsg.set(4, ifx.getReal_Amt().toString());
//        	
//        } else {
//        	
//        }
        
        Long amt = ifx.getAuth_Amt();
        if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
        	if (ifx.getAuth_Amt() != null)
        		isoMsg.set(4, ifx.getAuth_Amt().toString());
			System.out.println("Field - 4 ifx.getAuth_Amt() [" + ifx.getAuth_Amt() + "]"); //Raza TEMP
        } else {
        	if (ifx.getTrx_Amt() != null) {
	        	isoMsg.set(4, ifx.getTrx_Amt().toString());
	        	amt = ifx.getTrx_Amt();
				System.out.println("Field - 4 ifx.getTrx_Amt() [" + ifx.getTrx_Amt() + "]"); //Raza TEMPSystem.out.println("Field - 4 ifx.getAuth_Amt() [" + ifx.getAuth_Amt() + "]"); //Raza TEMP
        	}
        	
        }
		System.out.println("Field - 4 [" + isoMsg.getString(4) + "]");

        if (ifx.getSec_Amt() == null && amt != null) {
        	isoMsg.set(6, amt);
        	
        } else if (ifx.getSec_Amt() != null && ifx.getSec_Amt().equals(amt)){
        	isoMsg.set(6, amt.toString());
        	
        } else if (ifx.getSec_Amt() != null && ifx.getSec_Amt().equals(ifx.getTrx_Amt())) {
        	isoMsg.set(6, amt.toString());
        	
        } else if (ifx.getSec_Amt() != null) {
        	isoMsg.set(6, ifx.getSec_Amt().toString());
        	
        }
		System.out.println("Field - 6 [" + isoMsg.getString(6) + "]");
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
        
        if (ifx.getTrnDt() != null)
        	isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifx.getTrnDt().toDate()));

		System.out.println("Field - 7 [" + isoMsg.getString(7) + "]");

        isoMsg.set(10, ifx.getSec_CurRate());
		System.out.println("Field - 10 [" + isoMsg.getString(10) + "]");
        
        //Mirkamali(Task166): Adapt with IntSwitch's V7
        isoMsg.set(11, ifx.getMy_TrnSeqCntr());
		System.out.println("Field - 11 [" + isoMsg.getString(11) + "]");
//      isoMsg.set(11, ifx.getSrc_TrnSeqCntr());
        
        
		isoMsg.set(12, MyDateFormatNew.format("HHmmss", ifx.getOrigDt().toDate()));
		System.out.println("Field - 12 [" + isoMsg.getString(12) + "]");
		isoMsg.set(13, MyDateFormatNew.format("MMdd", ifx.getOrigDt().toDate()));
		System.out.println("Field - 13 [" + isoMsg.getString(13) + "]");

    	if (ifx.getExpDt() != null)
    		isoMsg.set(14, ifx.getExpDt());
		System.out.println("Field - 14 [" + isoMsg.getString(14) + "]");

        if (ifx.getSettleDt() != null)
        	isoMsg.set(15, MyDateFormatNew.format("MMdd", ifx.getSettleDt()));
		System.out.println("Field - 15 [" + isoMsg.getString(15) + "]");
       
        if (ifx.getPostedDt()!= null)
        	isoMsg.set(17, MyDateFormatNew.format("MMdd", ifx.getPostedDt()));
		System.out.println("Field - 17 [" + isoMsg.getString(17) + "]");

        isoMsg.set(25, fillTerminalType(ifx));
		System.out.println("Field - 25 [" + isoMsg.getString(25) + "]");
//        if(ifx.getTerminalType().equals(TerminalType.MOBILE))
//        	isoMsg.set(25, TerminalType.INTERNET.getCode());
        isoMsg.set(32, ifx.getBankId().toString());
		System.out.println("Field - 32 [" + isoMsg.getString(32) + "]");

        isoMsg.set(33, ifx.getDestBankId().toString());
		System.out.println("Field - 33 [" + isoMsg.getString(33) + "]");

        isoMsg.set(35, ifx.getTrk2EquivData());
		System.out.println("Field - 35 [" + isoMsg.getString(35) + "]");
        
        isoMsg.set(37, fillFieldANFix(ifx, 37));
		System.out.println("Field - 37 [" + isoMsg.getString(37) + "]");
        
        isoMsg.set(38, ifx.getApprovalCode());
		System.out.println("Field - 38 [" + isoMsg.getString(38) + "]");
        
        isoMsg.set(39, mapError(ifx.getIfxType(), ifx.getRsCode()));
		System.out.println("Field - 39 [" + isoMsg.getString(39) + "]");
        
        isoMsg.set(41, fillFieldANFix(ifx, 41));
		System.out.println("Field - 41 [" + isoMsg.getString(41) + "]");
        
        isoMsg.set(42, fillFieldANFix(ifx, 42));
		System.out.println("Field - 42 [" + isoMsg.getString(42) + "]");
        
        isoMsg.set(43, getcardacceptornamelocation(ifx)); //new ISOBinaryField(43, fillField43(ifx, convertor)));
		System.out.println("Field - 43 [" + isoMsg.getString(43) + "]");

        isoMsg.set(new ISOBinaryField(44, fillField44(ifx, convertor)));
		System.out.println("Field - 44 [" + isoMsg.getString(44) + "]");
        
        isoMsg.set(49, ifx.getAuth_Currency());
		System.out.println("Field - 49 [" + isoMsg.getString(49) + "]");
        isoMsg.set(51, (ifx.getSec_Currency() != null /*&& StringUtils.hasText(ifx.getSec_Currency())*/? ifx.getSec_Currency() : ifx.getAuth_Currency() ));
		System.out.println("Field - 51 [" + isoMsg.getString(51) + "]");
        
        if(ifx.getPINBlock() != null && !ifx.getPINBlock().equals("") )
        	isoMsg.set(52, ifx.getPINBlock());
		System.out.println("Field - 52 [" + isoMsg.getString(52) + "]");

        if (ifx.getMode()!= null && Util.hasText(ifx.getCheckDigit()))
        	isoMsg.set(53, ifx.getMode().getType()+ifx.getCheckDigit()+"00000000000");

		System.out.println("Field - 53 [" + isoMsg.getString(53) + "]");

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
		System.out.println("Field - 54 [" + isoMsg.getString(54) + "]");

        if (ifx.getNetworkManagementInformationCode()!= null)
        	isoMsg.set(70, ifx.getNetworkManagementInformationCode().getType());

		System.out.println("Field - 70 [" + isoMsg.getString(70) + "]");
        
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
			System.out.println("Field - 90 [" + isoMsg.getString(90) + "]");
        }


        StringBuilder S95 = new StringBuilder();
        if (ifx.getNew_AmtAcqCur() != null && ifx.getNew_AmtIssCur() != null) {
            S95.append(ifx.getNew_AmtAcqCur());
            S95.append(ifx.getNew_AmtIssCur());
            S95.append("C00000000");
            S95.append("C00000000");
            isoMsg.set(95, S95);
			System.out.println("Field - 95 [" + isoMsg.getString(95) + "]");
        }


        if (ifx.getRecvBankId() == null || ifx.getRecvBankId().equals(""))
            isoMsg.set(100, ifx.getDestBankId().toString());
        else
            isoMsg.set(100, ifx.getRecvBankId().toString());

		System.out.println("Field - 100 [" + isoMsg.getString(100) + "]");

        if (isoMsg.getMaxField() > 64) {
            isoMsg.set(128, ifx.getMsgAuthCode());
            isoMsg.unset(64);
        } else {
            isoMsg.set(64, ifx.getMsgAuthCode());
            isoMsg.unset(128);
        }

		System.out.println("Field - 64 [" + isoMsg.getString(64) + "]");
		System.out.println("Field - 128 [" + isoMsg.getString(128) + "]");

        if (ifx.getKeyManagement()!= null && Util.hasText(ifx.getKeyManagement().getKey()))
        	isoMsg.set(new ISOBinaryField(96, Hex.decode(ifx.getKeyManagement().getKey())));

		System.out.println("Field - 96 [" + isoMsg.getString(96) + "]");

        isoMsg.set(102, ifx.getMainAccountNumber());

		System.out.println("Field - 102 [" + isoMsg.getString(102) + "]");
        
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
