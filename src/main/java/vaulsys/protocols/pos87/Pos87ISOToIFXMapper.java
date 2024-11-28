package vaulsys.protocols.pos87;

import vaulsys.billpayment.BillPaymentUtil;
import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.customer.Currency;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.InvalidBusinessDateException;
import vaulsys.protocols.exception.exception.NotMappedProtocolToIfxException;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOtoIfxMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.text.ParseException;

import org.apache.log4j.Logger;

public class Pos87ISOToIFXMapper extends ISOtoIfxMapper {
	
	public static final Pos87ISOToIFXMapper Instance = new Pos87ISOToIFXMapper();
	
	private static Long MCIAppVer = 55L;
	
	private Pos87ISOToIFXMapper(){}
	
    transient static Logger logger = Logger.getLogger(Pos87ISOToIFXMapper.class);

    public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {
    	ISOMsg isoMsg = (ISOMsg) message;
    	
//        MyDateFormat dateFormatMMDDhhmmss = new MyDateFormat("MMddHHmmss");
//        MyDateFormat dateFormatMMDD = new MyDateFormat("MMdd");

        /** **************** Map ISO to IFX **************** */

        Ifx ifx = new Ifx();

        //Integer mti = null; //Raza MasterCard commenting
		String mti = null;

        try {
			mti = isoMsg.getMTI(); //Integer.parseInt(isoMsg.getMTI()); //Raza MasterCard commenting
		} catch (NumberFormatException e) {
			ISOException isoe = new ISOException("Invalid MTI", e);
			ifx.setSeverity(Severity.ERROR);
			ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": "+ isoe.getMessage());
		}

        ifx.setAppPAN(isoMsg.getString(2));
        if (Util.hasText(ifx.getAppPAN()) && !Util.isValidAppPan(ifx.getAppPAN())) {
        	ISOException isoe = new ISOException("Invalid AppPan: "+ ifx.getAppPAN());
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.WARN);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}
        
        //Integer emvTrnType= null; //Raza MasterCard commenting
		String emvTrnType= null;
        
        String str_fld3 = isoMsg.getString(3);
        
		if (str_fld3 != null && str_fld3.length() == 6) {
            try {
            	emvTrnType = str_fld3.substring(0, 2).trim(); //Integer.parseInt(str_fld3.substring(0, 2).trim()); //Raza MasterCard commenting
//				processCode = Integer.parseInt(str_fld3);
			} catch (NumberFormatException e) {
				ISOException isoe = new ISOException("Invalid Process Code: "+ str_fld3, e);
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(  Severity.ERROR);
					ifx.setStatusDesc(  isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
				}
				logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}

			
		 if (str_fld3.substring(2, 4).trim().equals("00"))
            	ifx.setAccTypeFrom ( AccType.MAIN_ACCOUNT);
            else if (str_fld3.substring(2, 4).trim().equals("10"))
            	ifx.setAccTypeFrom ( AccType.SUBSIDIARY_ACCOUNT);
            else if (str_fld3.substring(2, 4).trim().equals("20"))
            	ifx.setAccTypeFrom ( AccType.CARD);
            else 
            	ifx.setAccTypeFrom ( AccType.MAIN_ACCOUNT);
           
            if (str_fld3.substring(4, 6).trim().equals("00"))
            	ifx.setAccTypeTo ( AccType.MAIN_ACCOUNT);
            else if (str_fld3.substring(4, 6).trim().equals("10"))
            	ifx.setAccTypeTo ( AccType.SUBSIDIARY_ACCOUNT);
            else if (str_fld3.substring(4, 6).trim().equals("20"))
            	ifx.setAccTypeTo (AccType.CARD);
            else 
            	ifx.setAccTypeTo ( AccType.MAIN_ACCOUNT);
			
			
			mapTrnType(ifx, emvTrnType);
        }

        try{
        	String acquire_currency = isoMsg.getString(49);

			Currency currency = null;
			
			if (Util.hasText(acquire_currency)) {
				currency = ProcessContext.get().getCurrency(Integer.parseInt(acquire_currency));//GlobalContext.getInstance().getCurrency(Integer.parseInt(acquire_currency));
				if (currency == null){
					throw new ISOException("Ivalid Currency Code: "+ acquire_currency);
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
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc("Encouter with an exception in setting currency/amount("+ e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
			}
			logger.error("Encouter with an exception in setting currency/amount(" + e.getClass().getSimpleName() + ": " + e.getMessage()+")");
        }
    	
        
        try {
			String issuer_currency = isoMsg.getString(51).trim();
			Currency currency = null;
			
			if (Util.hasText(issuer_currency) ) {
				currency = ProcessContext.get().getCurrency(Integer.parseInt(issuer_currency));//GlobalContext.getInstance().getCurrency(Integer.parseInt(issuer_currency));
				if (currency == null){
					throw new ISOException("Ivalid Currency Code: " + issuer_currency);
				}
				ifx.setSec_Currency(currency.getCode());
				
			}else
				ifx.setSec_Currency(ifx.getAuth_Currency());
				
				ifx.setSec_CurRate(isoMsg.getString(10).trim());
				
				
				String sec_amt = isoMsg.getString(6).trim();
				if (Util.hasText(sec_amt))
					ifx.setSec_Amt(Util.longValueOf(sec_amt));
				else
					ifx.setSec_Amt(ifx.getAuth_Amt());
			
		} catch (Exception e) {
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
		}

        if (!isoMsg.getString(7).equals("")){
        	String P7 = isoMsg.getString(7).trim();
        	if(MyDateFormatNew.checkSimpleValidity("MMddHHmmss", P7)){
        		ifx.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", P7)));
        	}
        }

        
        ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));
		ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));

        String localTime = isoMsg.getString(12).trim();
        String localDate = isoMsg.getString(13).trim();

//        ifx.OrigDt = dateFormatMMDDhhmmss.parse(localDate + localTime);

        try {
        	if(MyDateFormatNew.checkSimpleValidity("MMddHHmmss", localDate + localTime)){
        		DateTime tenDayBefore = DateTime.toDateTime(DateTime.now().getTime() - 10 * DateTime.ONE_DAY_MILLIS);
        		DateTime tenDayAfter = DateTime.toDateTime(DateTime.now().getTime() + 10 * DateTime.ONE_DAY_MILLIS);
        		DateTime origDt = new DateTime( MyDateFormatNew.parse("MMddHHmmss", localDate + localTime));
        		ifx.setOrigDt (origDt);
        		if (tenDayBefore.getDateTimeLong() < origDt.getDateTimeLong() &&
        				origDt.getDateTimeLong() < tenDayAfter.getDateTimeLong()) {
				} else {
					logger.warn("Incorrect OrigDate: " + origDt);
					throw new InvalidBusinessDateException("incorret OrigDate: " + origDt);
				}
        		
        	}else{
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

			try {
	            String expDate = isoMsg.getString(14).trim();
	            ifx.setExpDt( Long.parseLong(expDate) );
			} catch (Exception e1) {
//				logger.info("expDate set manually from track2");
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
			}

            String settleDate = isoMsg.getString(15).trim();
            

//            ifx.setSettleDt(new MonthDayDate(MyDateFormatNew.parse("MMdd", settleDate)));
            try{    			
            	MonthDayDate d= new MonthDayDate(MyDateFormatNew.parse("MMdd", settleDate));

            	if (Util.hasText(settleDate))
            		ifx.setSettleDt(d);
    			
            	if (d != null && d.getMonth() == 1 && DateTime.now().getDayDate().getMonth() == 12) {
    				d.setYear(DateTime.now().getDayDate().getYear() + 1);
    				ifx.setSettleDt(d);
    			}
    			else if( d!= null && d.getMonth() == 12 && DateTime.now().getDayDate().getMonth() ==1){
    				d.setYear(DateTime.now().getDayDate().getYear()-1);
    				ifx.setSettleDt(d);
    			}
  		}catch (Exception e) {
  			// TODO: handle exception
//  			logger.info("Exception in setting SettleDate(15)");
  		}

  		String postedDate = isoMsg.getString(17).trim();
//            ifx.setPostedDt ( new MonthDayDate(MyDateFormatNew.parse("MMdd", postedDate)));
            try{
            	
            	MonthDayDate d= new MonthDayDate(MyDateFormatNew.parse("MMdd", postedDate));

            	if (Util.hasText(postedDate))
            		ifx.setPostedDt(d);
            	
				if (d != null && d.getMonth() == 1 && DateTime.now().getDayDate().getMonth() == 12) {
					d.setYear(DateTime.now().getDayDate().getYear() + 1);
					ifx.setPostedDt(d);
				}
				else if( d!= null && d.getMonth() == 12 && DateTime.now().getDayDate().getMonth() ==1){
					d.setYear(DateTime.now().getDayDate().getYear()-1);
					ifx.setPostedDt(d);
				}

          } catch (Exception e) {
//          	logger.info("Exception in setting PostedDate(17)");
          }


        } catch (Exception e) {
//        	ISOException isoe = new ISOException("Invalid Date", e);
//			ifx.setSeverity(  Severity.ERROR);
//			ifx.setStatusDesc(  isoe.getClass().getSimpleName() + ": "+ isoe.getMessage());
        }

        mapTerminalType(ifx, isoMsg.getString(25));

        //TODO
//        Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
        String myBin = ""+ProcessContext.get().getMyInstitution().getBin();
		ifx.setBankId(myBin);
//        ifx.setBankId (Util.longValueOf(isoMsg.getString(32).trim()));
        
        try {
			ifx.setDestBankId(isoMsg.getString(33).trim());
		} catch (Exception e) {
			ISOException isoe = new ISOException("Invalid issuer bank: "+ ifx.getDestBankId());
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}
		
		//		ifx.setFwdBankId (Util.longValueOf(isoMsg.getString(33).trim()));
        ifx.setFwdBankId(ifx.getDestBankId());
        ifx.setTrk2EquivData(isoMsg.getString(35));
		ifx.setNetworkRefId(ISOUtil.zeroUnPad(isoMsg.getString(37).trim()));
		ifx.setApprovalCode(isoMsg.getString(38).trim());
		ifx.setRsCode(mapError(isoMsg.getString(39).trim()));

		ifx.setTerminalId(ISOUtil.zeroUnPad(isoMsg.getString(41).trim()));
		ifx.setOrgIdNum(ISOUtil.zeroUnPad((isoMsg.getString(42).trim())));

		String P43 = isoMsg.getString(43);

		String f_44 = isoMsg.getString(44);
        mapField44(ifx, f_44, convertor);
       
        ifx.setPINBlock (isoMsg.getString(52).trim());

        String P54 = isoMsg.getString(54);
        
        if (ISOResponseCodes.APPROVED.equals(ifx.getRsCode()) || ISOResponseCodes.INVALID_ACCOUNT.equals(ifx.getRsCode())) {
			while (P54 != null && P54.length() >= 20) {
				AcctBal acctBal = new AcctBal();
				Integer acctType = null;
				try {
					Integer.parseInt(P54.substring(0, 2));
				} catch (NumberFormatException e) {
					ISOException isoe = new ISOException("Bad Format: Account Type [field 54]", e);
					if (!Util.hasText(ifx.getStatusDesc())) {
						ifx.setSeverity(Severity.ERROR);
						ifx.setStatusDesc((isoe.getClass().getSimpleName()+ ": " + isoe.getMessage()));
					}
					logger.error(isoe.getClass().getSimpleName() + ": "+ isoe.getMessage());
				}

				switch (acctType) {
				case 1:
					acctBal.setAcctType(AccType.CURRENT);
					break;
				case 2:
					acctBal.setAcctType(AccType.SAVING);
					break;
				default:
					acctBal.setAcctType(AccType.UNKNOWN);
					break;
				}

				Integer amtType = null;

				try {
					Integer.parseInt(P54.substring(2, 4));
				} catch (NumberFormatException e) {
					ISOException isoe = new ISOException("Bad Format: Amount Type [field 54]", e);
					if (!Util.hasText(ifx.getStatusDesc())) {
						ifx.setSeverity(Severity.ERROR);
						ifx.setStatusDesc((isoe.getClass().getSimpleName() + ": " + isoe.getMessage()));
					}
					logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
				}

				switch (amtType) {
				case 1:
					acctBal.setBalType(BalType.LEDGER);
					ifx.setAcctBalLedger(acctBal);
					break;
				case 2:
					acctBal.setBalType(BalType.AVAIL);
					ifx.setAcctBalAvailable(acctBal);
					break;
				default:
					acctBal.setBalType(BalType.UNKNOWN);
					break;
				}

				acctBal.setCurCode(P54.substring(4, 7));
				acctBal.setAmt(P54.substring(7, 20));

				P54 = P54.substring(20);
			}
		}
        
        String P64 = isoMsg.getString(64).trim();
        String S128 = isoMsg.getString(128).trim();

        if (P64 != null && P64.length() > 0)
            ifx.setMsgAuthCode (P64);
        else if (S128 != null && S128.length() > 0)
            ifx.setMsgAuthCode ( S128);

        String field48 = "";
        if (isoMsg.hasField(48))
        	field48 = new String((byte[])isoMsg.getValue(48));
        
        // TODO: Review what is this line?!!!
        
        //new application version
        /***P48 POS format: 
         * 15 byte reserved, 
         * 6 byte last sequence counter,
         * 1 length of application version,
         * application version,
         * Other data: 
         * -) return data
         * -) %billID%paymentID%
         * ***/ 
        
        String field48Data = field48;
        if (field48.length() > 15 && field48.substring(0, 15).equals("000000000000000")) {
        	int startSeqCntr = 15;
        	int endSeqCntr = 15 + 6;
        	int startVerLen = endSeqCntr;
        	int endVerLen = 15 + 6 + 1;
        	int startVer = endVerLen;
        	int endVer = startVer + Integer.parseInt(field48.substring(startVerLen, endVerLen));
        	int startOtherData = endVer;
        	ifx.setLast_TrnSeqCntr((Integer.parseInt(field48.substring(startSeqCntr, endSeqCntr))) + "");
        	ifx.setApplicationVersion(field48.substring(startVer, endVer));
        	field48Data = field48.substring(startOtherData);
        }
        
        if (emvTrnType.equals(ISOTransactionCodes.RETURN) && mti.equals("0200") && !isoMsg.getString(90).equals("bad") && isoMsg.hasField(48) && Util.hasText(field48Data)) {
        	/*** field48Data = return data ***/
        	isoMsg.set(90, "0000" + field48Data + "00000000000000000000000000000000");
        	isoMsg.unset(48);
        }
        
        else if (emvTrnType.equals(ISOTransactionCodes.BILL_PAYMENT_87) && mti.equals("0200") && isoMsg.hasField(48) && Util.hasText(field48Data)) {
        	/*** field48Data = %billID%paymentID% ***/
        	/***
        	 * split[0] = ""
        	 * split[1] = billID
        	 * split[2] = paymentID
        	 ***/
        	
        	String[] split = field48Data.split("%");
        	String billID = split[1];
        	String paymentID = split[2];
        	
        	String billIdLong = "";
        	String payIdLong = "";
        	try {
	        	billIdLong = String.valueOf(Long.parseLong(billID));
	        	payIdLong = String.valueOf(Long.parseLong(paymentID));
        	} catch(Exception e) {
        		logger.warn("bad Bill or Payment ID!");
        	}
        	
        	ifx.setBillID(billIdLong);
        	ifx.setBillPaymentID(payIdLong);
        	ifx.setBillCompanyCode(BillPaymentUtil.extractCompanyCode(billIdLong));
        	ifx.setThirdPartyTerminalId(BillPaymentUtil.getThirdPartyTerminalId(billIdLong));
        	ifx.setBillOrgType(BillPaymentUtil.extractBillOrgType(billIdLong));
        }
        
        else if (emvTrnType.equals(ISOTransactionCodes.PURCHASECHARGE)
//        		&& 
//        		!ShetabFinalMessageType.isReversalMessage(ifx.getIfxType())
        		) {
        	/*** field48Data(after ApplicationVersion:55) = %extraData(comanyCode)% ***/
        	/***
        	 * split[0] = ""
        	 * split[1] = companyCode
        	 * 
        	 * field48Data(before ApplicationVersion:55) = empty
        	 ***/
        	String[] split = field48Data.split("%");
        	
        	if (!Util.hasText(ifx.getApplicationVersion()) || Long.parseLong(ifx.getApplicationVersion()) < MCIAppVer) {
        		if (split.length <= 1)
        			ifx.setThirdPartyCode(9936L);
        		else {
    	        	NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("company code is null...");
    				if (!Util.hasText(ifx.getStatusDesc())) {
    					ifx.setSeverity(Severity.ERROR);
    					ifx.setStatusDesc((ep.getClass().getSimpleName() + ": " + ep.getMessage()));
    				}
    				logger.error(ep.getClass().getSimpleName() + ": " + ep.getMessage());
    	        }
        	} else {
	        	String companyCode = split[1];
	        	if (Util.hasText(companyCode))
	        		ifx.setThirdPartyCode(Long.parseLong(companyCode));
	        	else{
		        	NotMappedProtocolToIfxException ep = new NotMappedProtocolToIfxException("company code is null...");
					if (!Util.hasText(ifx.getStatusDesc())) {
						ifx.setSeverity(Severity.ERROR);
						ifx.setStatusDesc((ep.getClass().getSimpleName() + ": " + ep.getMessage()));
					}
					logger.error(ep.getClass().getSimpleName() + ": " + ep.getMessage());
		        }
	        }
        }
        
        String S90 = isoMsg.getString(90);
        if (S90 != null && S90.length() >= 20) {
            ifx.setOriginalDataElements(new MessageReferenceData());

			String msgType = S90.substring(0, 4);
			if (Integer.parseInt(msgType) != 0)
				ifx.getSafeOriginalDataElements().setMessageType(msgType);
			
			ifx.getOriginalDataElements().setTrnSeqCounter(ISOUtil.zeroUnPad(S90.substring(4, 10)));
			
			String referenceOrigDt = S90.substring(10, 20);
			if (Integer.parseInt(referenceOrigDt) != 0) {
				try {
					if(MyDateFormatNew.checkSimpleValidity("MMddHHmmss", referenceOrigDt)){
						ifx.getSafeOriginalDataElements().setOrigDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", referenceOrigDt)));
					}else{
		        		throw new ParseException("checkSimpleValidity of referenceOrigDt", -1);
		        	}
				} catch (ParseException e) {
					if (!Util.hasText(ifx.getStatusDesc())) {
    					ifx.setSeverity(Severity.ERROR);
    					ifx.setStatusDesc((e.getClass().getSimpleName()+ ": " + e.getMessage()));
    				} 
				}
			} 
//			else
//				ifx.getSafeOriginalDataElements().setOrigDt(referenceIfx.getOrigDt());

			String bankId = S90.substring(20, 31);
			if (Integer.parseInt(bankId) != 0)
				ifx.getSafeOriginalDataElements().setBankId((bankId));
			else 
//					if (referenceIfx != null)
				ifx.getOriginalDataElements().setBankId(myBin);

			String fwdBankId = S90.substring(31);
			if (Integer.parseInt(fwdBankId) != 0)
				ifx.getSafeOriginalDataElements().setFwdBankId(fwdBankId);
//				else if (referenceIfx != null)
//					ifx.getSafeOriginalDataElements().setFwdBankId(referenceIfx.getDestBankId());
//			}
			
			ifx.getOriginalDataElements().setTerminalId(ifx.getTerminalId());
    	    ifx.getOriginalDataElements().setAppPAN(ifx.getAppPAN());
	}


        String S95 = isoMsg.getString(95);
        if (S95 != null && S95.length() >= 24) {
            ifx.setNew_AmtAcqCur(S95.substring(0, 12));
			ifx.setNew_AmtIssCur(S95.substring(12, 24));
            Long real_Amt = Util.longValueOf(ifx.getNew_AmtAcqCur());
            real_Amt = (real_Amt!=null && !real_Amt.equals(0L))? real_Amt :Util.longValueOf(ifx.getNew_AmtIssCur());
            if (real_Amt!= null && !real_Amt.equals(0L))
            	ifx.setReal_Amt(real_Amt);
        }

        ifx.setRecvBankId(isoMsg.getString(100));
      
        mapIfxType(ifx, mti, emvTrnType);
        
        
        return ifx;
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
            		ifx.setSeverity(Severity.ERROR);
            		ifx.setStatusDesc((isoe.getClass().getSimpleName() + ": " + isoe.getMessage()));
            	}
            	logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            	break;
            case (14):
                ifx.setTerminalType(TerminalType.POS);
                break;
            default:
                ifx.setTerminalType(TerminalType.POS);
                // TODO: Fetch transaction and determine the terminal type
        }

    }

}
