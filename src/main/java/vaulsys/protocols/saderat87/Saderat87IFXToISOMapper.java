package vaulsys.protocols.saderat87;

import vaulsys.calendar.DayDate;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.UserLanguage;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

public class Saderat87IFXToISOMapper extends IfxToISOMapper{
	transient Logger logger = Logger.getLogger(Saderat87IFXToISOMapper.class);
	public static final Saderat87IFXToISOMapper Instance = new Saderat87IFXToISOMapper();
	
	protected Saderat87IFXToISOMapper(){}
	
    public ISOMsg map(Ifx ifx, EncodingConvertor convertor) throws ISOException {

        ISOMsg isoMsg = new ISOMsg();
        ISOPackager packager = ((Saderat87Protocol) ProtocolProvider.Instance
                .getByClass(Saderat87Protocol.class)).getPackager();
        isoMsg.setPackager(packager);
        
        isoMsg.setMTI(String.valueOf(fillMTI(ifx.getIfxType(), ifx.getMti())));
        
        isoMsg.set(2, ifx.getAppPAN());

        String processCode = "0000";

        isoMsg.set(3, mapTrnType(ifx.getTrnType())+ processCode);

        if (ifx.getAuth_Amt() != null)
			isoMsg.set(4, ifx.getAuth_Amt().toString());
        /*
         * saderat filed 6 nadarad vali ma felan mizarim bashe:D*/
        if (ifx.getSec_Amt() == null && ifx.getAuth_Amt() != null)
        	isoMsg.set(6, isoMsg.getString(4));
        else if (ifx.getSec_Amt() != null)
			isoMsg.set(6, ifx.getSec_Amt().toString());
        
        if (ifx.getTrnDt() != null)
        	isoMsg.set(7, MyDateFormatNew.format("MMddHHmmss", ifx.getTrnDt().toDate()));//sal bayad ezafe shavad
        
        /*** check this ***/
        isoMsg.set(11, ifx.getSrc_TrnSeqCntr());
		isoMsg.set(12, MyDateFormatNew.format("HHmmss", ifx.getOrigDt().toDate()));
		isoMsg.set(13, MyDateFormatNew.format("MMdd", ifx.getOrigDt().toDate()));

    	if (ifx.getExpDt() != null)
    		isoMsg.set(14, ifx.getExpDt());//bar asase salo mah bashad

       
//        if (ifx.getPostedDt()!= null)
//        	isoMsg.set(17, MyDateFormatNew.format("MMdd", ifx.getPostedDt()));
        
       if(TerminalType.POS.equals(ifx.getTerminalType())||TerminalType.ATM.equals(ifx.getTerminalType())||
    		   TerminalType.PINPAD.equals(ifx.getTerminalType())){
    	   isoMsg.set(22, SaderatConst.SADERAT_TERMINALS_WITH_CARD);
       }else if(TerminalType.INTERNET.equals(ifx.getTerminalType())||TerminalType.MOBILE.equals(ifx.getTerminalType())||
    		   TerminalType.VRU.equals(ifx.getTerminalType())){
    	   isoMsg.set(22,SaderatConst.SADERAT_TERMINALS_WITHOUT_CARD);
       }

        isoMsg.set(25, fillTerminalType(ifx));//faghat 14 mitone bashe
//        isoMsg.set(32, ifx.getBankId().toString());
        isoMsg.set(32, "60376992");
        isoMsg.set(17, MyDateFormatNew.format("MMdd",DayDate.now()));
        isoMsg.set(98, "60376992");
        isoMsg.set(103, "0103345288003"+StringFormat.formatNew(11, StringFormat.JUST_LEFT, "603769", ' ')+"0239");

        isoMsg.set(33, ifx.getDestBankId().toString());
        isoMsg.set(35, ifx.getTrk2EquivData());
        
        isoMsg.set(37, fillFieldANFix(ifx, 37));
        
        isoMsg.set(39, mapError(ifx.getIfxType(), ifx.getRsCode()));
        
        isoMsg.set(41, fillFieldANFix(ifx, 41));
        
        isoMsg.set(42, fillFieldANFix(ifx, 42));
        
        isoMsg.set(new ISOBinaryField(43, fillField43(ifx, convertor)));

        isoMsg.set(49, ifx.getAuth_Currency());
        isoMsg.set(51, (ifx.getSec_Currency() != null? ifx.getSec_Currency() : ifx.getAuth_Currency() ));
        
        if(ifx.getPINBlock() != null && !ifx.getPINBlock().equals("") )
        	isoMsg.set(52, ifx.getPINBlock());
        
        
        String S90 = "";

        if(ISOFinalMessageType.isReversalOrRepeatMessage(ifx.getIfxType()) || ISOFinalMessageType.isReturnMessage(ifx.getIfxType())) {
        	//Override it if this is a reversal transaction
//        	isoMsg.set(11, ifx.getSafeOriginalDataElements().getTrnSeqCounter());

            if (ifx.getSafeOriginalDataElements().getMessageType() == null){
            	throw new ISOException("Invalid original data element: No Message Type for field 90");
            }
            S90 += ifx.getSafeOriginalDataElements().getMessageType();
            try {
				S90 += StringFormat.formatNew(6, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getTrnSeqCounter(), '0');
			} catch (Exception e) {
				S90 += "000000";
			}
			try {
				S90 += MyDateFormatNew.format("MMddHHmmss", ifx.getSafeOriginalDataElements().getOrigDt().toDate());
			} catch (Exception e) {
				S90 += "0000000000";
			}
			try {
				S90 += StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getBankId(), '0');
			} catch (Exception e) {
				S90 += "00000000000";
			}
			try {
				S90 += StringFormat.formatNew(11, StringFormat.JUST_RIGHT, ifx.getSafeOriginalDataElements().getFwdBankId(), '0');
			} catch (Exception e) {
				S90 += "00000000000";
			}
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
       
        //TODO: saderat 103 mimhad shomare hesabe dovom
        
        //TODO: saderat 121 mikhad
        if (isoMsg.getMaxField() > 64) {
            isoMsg.set(128, ifx.getMsgAuthCode());
            isoMsg.unset(64);
        } else {
            isoMsg.set(64, ifx.getMsgAuthCode());
            isoMsg.unset(128);
        }
        
//        StringBuilder CVV2 = new StringBuilder();
//        StringBuilder secAppPAN = new StringBuilder();
//        try {
//        	MigrationDataService.setChangedFields(ifx, isoMsg, CVV2, secAppPAN);
//        	
//        } catch(Exception e) {
//        	logger.error("Exception in return changed field, ifx: " + ifx.getId(), e);
//        }
        
        /****** Don't move this line, must be haminja! ******/
      //check shvad ke saderad ba iso fargh dare ya na
//        isoMsg.set(new ISOBinaryField(48, fillField48(ifx, CVV2.toString(), secAppPAN.toString(), convertor)));
//        isoMsg.set(new ISOBinaryField(48, fillField48(ifx, ifx.getCVV2(), ifx.getSecondAppPan(), convertor)));
        
        return isoMsg;
    }

    @Override
    public byte[] fillField43(Ifx ifx, EncodingConvertor convertor) {
		byte[] result = new byte[40];
				
		String name = (Util.hasText(ifx.getName())) ? ifx.getName() : "";
		name = StringFormat.formatNew(22, StringFormat.JUST_LEFT, name, ' ');
		name = name.replaceAll("ی", "ي");
		name = name.replaceAll("ء", "ئ");
		name = name.replaceAll("ک", "ك");
		
		try {
			System.arraycopy(name.getBytes("windows-1256"), 0, result, 0, 22);
		} catch (UnsupportedEncodingException e) {			
			System.arraycopy(name.getBytes(), 0, result, 0, 22);
		}
		
		String city = " ";
//		if (ifx.getCity() != null)
//			city = ifx.getCity().getName();
//		if (Util.hasText(city))
//			city = city.substring(0, Math.min(13, city.length()));
//		else 
//			city = " ";
		city = StringFormat.formatNew(13, StringFormat.JUST_LEFT, city, ' ');
//		city = city.replaceAll("ی", "ي");
//		city = city.replaceAll("ء", "ئ");
//		city = city.replaceAll("ک", "ك");
		
		try {
			System.arraycopy(city.getBytes("windows-1256"), 0, result, 22, 13);
		} catch (UnsupportedEncodingException e) {
			System.arraycopy(city.getBytes(), 0, result, 22, 13);
		}
		
		String state = " ";
//		if (ifx.getStateProv() != null) {
//			state = (Util.hasText(ifx.getStateProv().getAbbreviation())) ? ifx.getStateProv().getAbbreviation() : "";
//		}
			state = StringFormat.formatNew(3, StringFormat.JUST_LEFT, state, ' ');
//			state = state.replaceAll("ی", "ي");
//			state = state.replaceAll("ء", "ئ");
//			state = state.replaceAll("ک", "ك");
			System.arraycopy(state.getBytes(), 0, result, 35, 3);
		
		String country = " ";
//		if (ifx.getCountry() != null) {
//			country = (Util.hasText(ifx.getCountry().getAbbreviation())) ? ifx.getCountry().getAbbreviation() : "";
//		}
			country = StringFormat.formatNew(2, StringFormat.JUST_LEFT, country, ' ');
//			country = country.replaceAll("ی", "ي");
//			country = country.replaceAll("ء", "ئ");
//			country = country.replaceAll("ک", "ك");
			System.arraycopy(country.getBytes(), 0, result, 38, 2);
		
		return result;
    }
    
    @Override
	public byte[] fillField44(Ifx ifx, EncodingConvertor convertor) {
		ByteArrayOutputStream finalBytes = new ByteArrayOutputStream();
		if (ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
				&& ISOFinalMessageType.isTransferMessage(ifx.getIfxType())) {
//			if (IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifx.getIfxType())) {
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
//		else if (IfxType.TRANSFER_RS.equals(ifx.getIfxType())) {
//			return new byte[0];
//		}
		return null;
	}
    
//    @Override
//	public byte[] fillField48(Ifx ifx, String CVV2, String secAppPAN, EncodingConvertor convertor) {
//		String p48 = "";
////			p48 += "00";
//		p48 +=StringFormat.formatNew(14, StringFormat.JUST_RIGHT, "00");
////			p48 += StringFormat.formatNew(4, StringFormat.JUST_RIGHT, CVV2, '0');
//		UserLanguage userlang = ifx.getUserLanguage();
//		if(UserLanguage.FARSI_LANG.equals(userlang))
//			p48 += "01";
//		else if(UserLanguage.ENGLISH_LANG.equals(userlang))
//			p48 += "02";
//		if(ShetabFinalMessageType.isBillPaymentMessage(ifx.getIfxType())){
//			if(OrganizationType.WATER.equals(ifx.getBillOrgType()))
//				p48 += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "WA");
//			else if(OrganizationType.ELECTRONIC.equals(ifx.getBillOrgType()))
//				p48 += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "EL");
//			else if(OrganizationType.GAZ.equals(ifx.getBillOrgType()))
//				p48 += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "GA");
//			else if(OrganizationType.TEL.equals(ifx.getBillOrgType()))
//				p48 += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "TC");
//			else if(OrganizationType.MOBILE.equals(ifx.getBillOrgType()))
//				p48 += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "MC");
//			else if(OrganizationType.MANAGE_NET.equals(ifx.getBillOrgType()))//ino motmaen sho ke male shhardareie
//				p48 += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "MN");
//			else if(OrganizationType.UNDEFINED.equals(ifx.getBillOrgType()))
//				p48 += StringFormat.formatNew(2, StringFormat.JUST_RIGHT, "UD");
//			
//			p48 += StringFormat.formatNew(30, StringFormat.JUST_RIGHT, "00");
//			p48 += StringFormat.formatNew(24, StringFormat.JUST_RIGHT, "00");
//			p48 += StringFormat.formatNew(18, StringFormat.JUST_RIGHT, ifx.getBillID(),'0');
//			p48 += StringFormat.formatNew(18, StringFormat.JUST_RIGHT, ifx.getBillPaymentID(),'0');
//		}else if(ShetabFinalMessageType.isPurchaseChargeMessage(ifx.getIfxType())){
//			p48 += StringFormat.formatNew(14, StringFormat.JUST_RIGHT, "00");
//			 if(UserLanguage.FARSI_LANG.equals(userlang))
//				 p48 += "01";
//			 else if(UserLanguage.ENGLISH_LANG.equals(userlang))
//				 p48 += "02";
//			 p48 += StringFormat.formatNew(4, StringFormat.JUST_RIGHT, "00");//dade sabet
//			 if(OrganizationType.MTNIRANCELL.equals(ifx.getChargeCompanyCode()))
//				 if(mtn)
//			 p48 += StringFormat.formatNew(4, Strng, str)
//			 
//		}
//		if (ShetabFinalMessageType.isBillPaymentMessage(ifx.getIfxType())) {
//			if (ifx.getBankId().equals(GlobalContext.getInstance().getMyInstitution().getBin()) || 
//				FinancialEntityRole.MY_SELF_INTERMEDIATE.equals(GlobalContext.getInstance().getMyInstitution().getRole())) {
//				p48 += StringFormat.formatNew(10, StringFormat.JUST_RIGHT, "0", '0');
//				p48 += ifx.getBillOrgType().getType();
//				p48 += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillID(), '0');
//				p48 += StringFormat.formatNew(13, StringFormat.JUST_RIGHT, ifx.getBillPaymentID(), '0');
//			} else {
//				p48 += ifx.getBillUnParsedData();
//			}
//		} else if (ShetabFinalMessageType.isTransferMessage(ifx.getIfxType())) {
//			if ( 
//					(IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifx.getIfxType())|| IfxType.TRANSFER_REV_REPEAT_RS.equals(ifx.getIfxType()))
//					&& !Util.hasText(secAppPAN)
//				) {
//				p48="";
//			} else {
//				UserLanguage userLanguage = ifx.getUserLanguage();
//				if (UserLanguage.FARSI_LANG.equals(userLanguage))
//					p48 += "00";
//				else
//					p48 += "01";
//				String appPan = secAppPAN;
//				if (Util.hasText(secAppPAN)){
//					p48 += appPan.length();
//					p48 += appPan;
//				}else{
//					logger.error(ifx.getIfxType()+" doesn't have SecAppPan "+ secAppPAN+"!");
//				}
//			}
//		}
//		return p48.getBytes();
//	}

}
