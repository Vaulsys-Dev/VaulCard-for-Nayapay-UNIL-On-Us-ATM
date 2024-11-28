package vaulsys.protocols.infotech;

import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.calendar.DateTime;
import vaulsys.customer.Currency;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.InvalidBusinessDateException;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOtoIfxMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.MyInteger;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.text.ParseException;

import org.apache.log4j.Logger;

public class InfotechISOToIFXMapper extends ISOtoIfxMapper {
	
	public static final InfotechISOToIFXMapper Instance = new InfotechISOToIFXMapper();
	
	private InfotechISOToIFXMapper(){}
	
    transient static Logger logger = Logger.getLogger(InfotechISOToIFXMapper.class);

    public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {
    	ISOMsg isoMsg = (ISOMsg) message;
    	
//        MyDateFormat dateFormatYYYYMMDDhhmmss = new MyDateFormat("yyyyMMddHHmmss");

        /** **************** Map ISO to IFX **************** */

        Ifx ifx = new Ifx();

        //Integer mti = null; //Raza MasterCard commenting
		String mti = null;

        try {
			mti = isoMsg.getMTI(); //Integer.parseInt(isoMsg.getMTI()); //Raza MasterCard commenting
		} catch (NumberFormatException e) {
			ISOException isoe = new ISOException("Invalid MTI", e);
			ifx.setSeverity(  Severity.ERROR);
			ifx.setStatusDesc(  isoe.getClass().getSimpleName() + ": "+ isoe.getMessage());
		}

        ifx.setAppPAN(isoMsg.getString(2));
        if (Util.hasText(ifx.getAppPAN()) && (!Util.isAccount(ifx.getAppPAN()) && !Util.isValidAppPan(ifx.getAppPAN()))){
        	ISOException isoe = new ISOException("Invalid AppPan: "+ ifx.getAppPAN());
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}
        
        //Integer emvTrnType= null; //Raza Mastercard commenting
		String emvTrnType= null;

		String str_fld3 = isoMsg.getString(3);
        
		if (str_fld3 != null && str_fld3.length() == 6) {
            try {
            	emvTrnType = str_fld3.substring(0, 2).trim(); //Integer.parseInt(str_fld3.substring(0, 2).trim());
			} catch (NumberFormatException e) {
				ISOException isoe = new ISOException("Invalid Process Code: "+ str_fld3, e);
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(  Severity.ERROR);
					ifx.setStatusDesc(  isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
				}
				logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}

			
        	ifx.setAccTypeFrom ( AccType.MAIN_ACCOUNT);
        	ifx.setAccTypeTo ( AccType.MAIN_ACCOUNT);
			
			mapTrnType(ifx, emvTrnType);
        }

        try{
        	String acquire_currency = isoMsg.getString(49);

			Currency currency = null;
			
			if (Util.hasText(acquire_currency)) {
				currency = ProcessContext.get().getCurrency(Integer.parseInt(acquire_currency));//GlobalContext.getInstance().getCurrency(Integer.parseInt(acquire_currency));
				if (currency == null){
					throw new ISOException("Invalid Currency Code: "+ acquire_currency);
				}
			} else{
				currency = ProcessContext.get().getRialCurrency();//GlobalContext.getInstance().getRialCurrency();
			}
			
			ifx.setAuth_Currency(currency.getCode());
        	ifx.setAuth_CurRate("1");
        	ifx.setAuth_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
        	ifx.setReal_Amt(ifx.getAuth_Amt());
        	ifx.setTrx_Amt(ifx.getAuth_Amt());
        }catch (Exception e) {
        	if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity ( Severity.ERROR);
				ifx.setStatusDesc ("Encouter with an exception in setting currency/amount(" + e.getClass().getSimpleName() + ": " + e.getMessage()+")");
			}
			logger.error("Encouter with an exception in setting currency/amount(" + e.getClass().getSimpleName() + ": " + e.getMessage()+")");
        }
    	
        ifx.setSec_Currency(ifx.getAuth_Currency());
		ifx.setSec_Amt(ifx.getAuth_Amt());
        
       	ifx.setTrnDt(DateTime.now());

        
        ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));
        ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));

        String localTime = isoMsg.getString(12).trim();
        String localDate = isoMsg.getString(13).trim();

        try {
        	if(MyDateFormatNew.checkSimpleValidity("yyyyMMddHHmmss", localDate + localTime)){
        		DateTime tenDayBefore = DateTime.toDateTime(DateTime.now().getTime() - 10 * DateTime.ONE_DAY_MILLIS);
        		DateTime tenDayAfter = DateTime.toDateTime(DateTime.now().getTime() + 10 * DateTime.ONE_DAY_MILLIS);
        		DateTime origDt = new DateTime( MyDateFormatNew.parse("yyyyMMddHHmmss", localDate + localTime));

        		ifx.setOrigDt (origDt);
        		if (tenDayBefore.getDateTimeLong() < origDt.getDateTimeLong() &&
        				origDt.getDateTimeLong() < tenDayAfter.getDateTimeLong()) {
				} else {
					throw new InvalidBusinessDateException("incorret OrigDate: " + origDt);
				}
        		
        	} else {
        		throw new ParseException("checkSimpleValidity of OrigDate", -1);
        	}
        	
		} catch (Exception e) {
			ISOException isoe = new ISOException("Unparsable Original Date.", e);
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc((isoe.getClass().getSimpleName() + ": " + isoe.getMessage()));
			}
			logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}

		try {
            // TODO check!! set dates manually. It seems there's no need to try
            // $ catch

//			logger.info("expDate set manually from track2");
			String track2Data = isoMsg.getString(35).trim();
			int PANEndIndex = track2Data.indexOf('=');
			if(track2Data.length() > PANEndIndex + 5){
				try {
					String expDate = track2Data.substring(PANEndIndex + 1, PANEndIndex + 5);
					ifx.setExpDt( Long.parseLong(expDate) );
				} catch (Exception e) {
					logger.info("Unusual track2! expDate cannot be retrieved form track2.", e);
				}
			}
        } catch (Exception e) {
        }

        mapTerminalType(ifx, isoMsg.getString(25));
        if(ProcessContext.get().getMyInstitution().getBin().equals(Long.valueOf(isoMsg.getString(32).trim())))
        	ifx.setBankId (isoMsg.getString(32));
        else{
        	ifx.setBankId (isoMsg.getString(32));
        	logger.error("Infotech pos has invalid acquire bank id");
        	NotMappedProtocolToIfxException e=new NotMappedProtocolToIfxException("Infotech pos has invalid acquire bank id");
        	if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(  Severity.ERROR);
				ifx.setStatusDesc((e.getClass().getSimpleName() + ": " + e.getMessage()));
			}
        }
        
        try {
        	if(Util.isAccount(ifx.getAppPAN()))
        		ifx.setDestBankId(ProcessContext.get().getMyInstitution().getBin().toString());
        	else
        		ifx.setDestBankId(ifx.getAppPAN().substring(0,6));
		} catch (Exception e) {
			ISOException isoe = new ISOException("Invalid issuer bank: "+ ifx.getDestBankId());
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(  Severity.ERROR);
				ifx.setStatusDesc(  isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}
		
        ifx.setFwdBankId(ifx.getDestBankId());
        ifx.setTrk2EquivData(isoMsg.getString(35));
        ifx.setNetworkRefId(ISOUtil.zeroUnPad(isoMsg.getString(37)));

        ifx.setTerminalId(ISOUtil.zeroUnPad(isoMsg.getString(41).trim()));
        ifx.setOrgIdNum(ISOUtil.zeroUnPad((isoMsg.getString(42).trim())));

		String f_44 = isoMsg.getString(44);
        mapField44(ifx, f_44, convertor);
       

        ifx.setPINBlock (isoMsg.getString(52).trim());
       
        if (Util.hasText(isoMsg.getString(53)))
        	ifx.setSerialno(isoMsg.getString(53).trim());
        
        String P64 = isoMsg.getString(64).trim();
        String S128 = isoMsg.getString(128).trim();

        if (P64 != null && P64.length() > 0)
            ifx.setMsgAuthCode (P64);
        else if (S128 != null && S128.length() > 0)
            ifx.setMsgAuthCode ( S128);

        byte[] field48 = null;
        if (isoMsg.hasField(48)) {
			field48 = (byte[]) isoMsg.getValue(48);
		}
        
        // TODO: Review what is this line?!!!
        
        //new application version
        /***P48 INGENICO POS format: 
         * 6 byte last sequence counter,
         * application version,
         * Other data: 
         * -) return data : main transaction sequence counter
         * -) bill payment data: billID paymentID
         * -) reset password data: helpdesk password
         * ***/ 
        
        MyInteger offset = new MyInteger(0);
        ifx.setLast_TrnSeqCntr(Integer.parseInt(NDCParserUtils.readUntilFS(field48, offset))+"");
        NDCParserUtils.readFS(field48, offset);
        
        ifx.setApplicationVersion(NDCParserUtils.readUntilFS(field48, offset));
        NDCParserUtils.readFS(field48, offset);
        
        String S90 = isoMsg.getString(90);
        if (emvTrnType.equals(ISOTransactionCodes.RETURN) && mti.equals("0200") && !isoMsg.getString(90).equals("bad") && isoMsg.hasField(48)) {
        	/*** field48Data = return data ***/
        	S90 = NDCParserUtils.readUntilFS(field48, offset) + "00000000000000";
        	NDCParserUtils.readFS(field48, offset);
        	
        } else if (emvTrnType.equals(ISOTransactionCodes.BILL_PAYMENT_87) && mti.equals("0200")) {
        	String shenaseGhabz = NDCParserUtils.readUntilFS(field48, offset);
        	NDCParserUtils.readFS(field48, offset);
        	String shenasePardakht = NDCParserUtils.readUntilFS(field48, offset);
        	NDCParserUtils.readFS(field48, offset);
        	
			String billIdLong = "";
			String payIdLong = "";
			try {
				billIdLong = String.valueOf(Long.parseLong(shenaseGhabz));
				payIdLong = String.valueOf(Long.parseLong(shenasePardakht));
			} catch(Exception e) {
				logger.warn("bad Bill or Payment ID!");
			}
        	
        	ifx.setBillID(billIdLong);
        	ifx.setBillPaymentID(payIdLong);
        	
            ifx.setBillCompanyCode(BillPaymentUtil.extractCompanyCode(billIdLong));
            ifx.setThirdPartyTerminalId(BillPaymentUtil.getThirdPartyTerminalId(billIdLong));
            ifx.setBillOrgType(BillPaymentUtil.extractBillOrgType(billIdLong));
            
        } else if (emvTrnType.equals(ISOTransactionCodes.TRANSFER)
        		|| emvTrnType.equals(ISOTransactionCodes.CHECK_ACCOUNT)
        		|| emvTrnType.equals(ISOTransactionCodes.TRANSFER_CARD_TO_ACCOUNT)
        		|| emvTrnType.equals(ISOTransactionCodes.CHECK_ACCOUNT_CARD_TO_ACCOUNT)){
        	try{
				ifx.setSecondAppPan(NDCParserUtils.readUntilFS(field48, offset));
				NDCParserUtils.readFS(field48, offset);
				
				if (Util.isAccount(ifx.getSecondAppPan()))
					ifx.setRecvBankId(ProcessContext.get().getMyInstitution().getBin().toString());
				else 
					ifx.setRecvBankId(ifx.getSecondAppPan().substring(0, 6));
			} catch (Exception e) {
				logger.error("Could not set p48 fileds...");
			}
        	
        } else if (emvTrnType.equals(ISOTransactionCodes.THIRDPARTY_PAYMENT)) {
        	StringBuilder builder = new StringBuilder();
        	try {
	        	String id = NDCParserUtils.readUntilFS(field48, offset);
	        	NDCParserUtils.readFS(field48, offset);
	        	while (Util.hasText(id)) {
	        		try {
	        			if (Util.hasText(id)) {
	        				builder.append(id + "|");
	        			}
	        			id = "";
			        	id = NDCParserUtils.readUntilFS(field48, offset);
			        	NDCParserUtils.readFS(field48, offset);
	        		} catch(Exception e) {
	        			logger.warn("end of 48 in thirdParty trx!");
	        		}
	        	}
        	} catch(Exception e) {
        		if (Util.hasText(builder.toString())) {
        			ifx.setThirdPartyIds(builder.toString());
        		}
        	}
        } else if (emvTrnType.equals(ISOTransactionCodes.CHANGE_PIN) || emvTrnType.equals(ISOTransactionCodes.CHANGE_PIN2)) {
            ifx.setNewPINBlock(NDCParserUtils.readUntilFS(field48, offset));
            NDCParserUtils.readFS(field48, offset);
        } else if (emvTrnType.equals(ISOTransactionCodes.HOTCARD_INQ)) {
            int PANEndIndex = ifx.getTrk2EquivData().indexOf('=');
            String bufferB = NDCParserUtils.readUntilFS(field48, offset);
            NDCParserUtils.readFS(field48, offset);
            ifx.setAppPAN(bufferB);
            ifx.setSecondAppPan(ifx.getTrk2EquivData().substring(0, PANEndIndex));

            if (!Util.hasText(ifx.getAppPAN()) || !Util.isValidAppPan(ifx.getAppPAN())) {
                Exception e = new Exception("Invalid HOTCARD_RQ AppPan: "+ ifx.getAppPAN());
                if (!Util.hasText(ifx.getStatusDesc())) {
                    ifx.setSeverity(Severity.WARN);
                    ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
                logger.warn(e.getClass().getSimpleName() + ": " + e.getMessage());
            }

            if (!Util.hasText(ifx.getSecondAppPan()) || !Util.isValidAppPan(ifx.getSecondAppPan())) {
                Exception e = new Exception("Invalid Second AppPan: "+ ifx.getSecondAppPan());
                if (!Util.hasText(ifx.getStatusDesc())) {
                    ifx.setSeverity(Severity.WARN);
                    ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
                logger.warn(e.getClass().getSimpleName() + ": " + e.getMessage());
            }

            if (TrnType.HOTCARD.equals(ifx.getTrnType())) {
                ifx.setDestBankId(bufferB.substring(0, 6));
                ifx.setRecvBankId(ifx.getTrk2EquivData().substring(0, 6));
            }
        }
        
        if (S90 != null && S90.length() >= 20) {
            ifx.setOriginalDataElements ( new MessageReferenceData());
				ifx.getOriginalDataElements().setTrnSeqCounter(ISOUtil.zeroUnPad(S90.substring(0, 6)));
				
				String referenceOrigDt = S90.substring(6, 20);
				if (Long.parseLong(referenceOrigDt) != 0) {
					try {
						ifx.getSafeOriginalDataElements().setOrigDt(
								new DateTime(MyDateFormatNew.parse("yyyyMMddHHmmss", referenceOrigDt)));
					} catch (ParseException e) {
						if (!Util.hasText(ifx.getStatusDesc())) {
	    					ifx.setSeverity(Severity.ERROR);
	    					ifx.setStatusDesc((e.getClass().getSimpleName()+ ": " + e.getMessage()));
	    				} 
					}
				} 

				ifx.getOriginalDataElements().setBankId(ifx.getBankId());
				ifx.getOriginalDataElements().setFwdBankId(ifx.getDestBankId());
				ifx.getOriginalDataElements().setTerminalId(ifx.getTerminalId());
				if (Util.hasText(ifx.getAppPAN()))
					ifx.getOriginalDataElements().setAppPAN(ifx.getAppPAN());
				
		}


        String S95 = isoMsg.getString(95);
        if (S95 != null && S95.length() >= 24) {
            ifx.setNew_AmtAcqCur (S95.substring(0, 12));
            ifx.setNew_AmtIssCur ( S95.substring(12, 24));
            Long real_Amt = Util.longValueOf(ifx.getNew_AmtAcqCur());
            real_Amt = (real_Amt!=null && !real_Amt.equals(0L))? real_Amt :Util.longValueOf(ifx.getNew_AmtIssCur());
            if (real_Amt!= null && !real_Amt.equals(0L))
            	ifx.setReal_Amt(real_Amt);
        }

        String S98 = isoMsg.getString(98);
        if (Util.hasText(S98)) {
        	ifx.setThirdPartyCode(Long.valueOf(S98));

if (GlobalContext.getInstance().getMyInstitution().getBin().equals(502229L) 
				&& ifx.getThirdPartyCode().equals(9935L)) {
			ifx.setThirdPartyCode(9936L);
		}
        }
        
        mapIfxType(ifx, mti, emvTrnType);
        
        
        checkLastTransactionStatus(mti);
        
        return ifx;
    }

    private void checkLastTransactionStatus(String mti) { //Raza MasterCard
		
	}

    @Override
    public void mapTerminalType(Ifx ifx, String f_25) {
        Integer terminalTypeCode = Integer.parseInt("0" + f_25.trim());
        	
        switch (terminalTypeCode) {
            case (2):
            case (3):
            case (59):
            case (7):
            	ISOException isoe = new ISOException("Invalid Terminal Type(" + terminalTypeCode + "). POS is expected.");
            	if (!Util.hasText(ifx.getStatusDesc())) {
    				ifx.setSeverity(  Severity.ERROR);
    				ifx.setStatusDesc( (isoe.getClass().getSimpleName() + ": " + isoe.getMessage()));
    			}
            	logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            	break;
            case (14):
                ifx.setTerminalType ( TerminalType.POS);
                break;
            case (43):
                ifx.setTerminalType ( TerminalType.KIOSK_CARD_PRESENT); // KIOSK CARD PRESENT
                break;

            default:
                ifx.setTerminalType ( TerminalType.POS );
        }
    }
}
