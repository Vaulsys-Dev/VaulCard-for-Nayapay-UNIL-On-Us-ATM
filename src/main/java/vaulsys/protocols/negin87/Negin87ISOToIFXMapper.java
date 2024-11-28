package vaulsys.protocols.negin87;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.calendar.PersianCalendar;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.customer.Currency;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.BankStatementData;
import vaulsys.protocols.ifx.imp.CardAccountInformation;
import vaulsys.protocols.ifx.imp.EMVRsData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOtoIfxMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.negin87.util.ChargeFlag;
import vaulsys.protocols.negin87.util.TLVTag;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.MyInteger;
import vaulsys.util.Util;
import vaulsys.util.encoders.Hex;
import vaulsys.util.littleendian.LittleEndian;
import vaulsys.util.littleendian.LittleEndianConsts;
import vaulsys.wfe.ProcessContext;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class Negin87ISOToIFXMapper extends ISOtoIfxMapper {
	

	public static final Negin87ISOToIFXMapper Instance = new Negin87ISOToIFXMapper();
	
	private Negin87ISOToIFXMapper(){}
	
	
	Logger logger = Logger.getLogger(this.getClass());
	@Override
    public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {
		
		ISOMsg isoMsg = (ISOMsg) message;
//        MyDateFormat dateFormatMMDDhhmmss = new MyDateFormat("MMddHHmmss");
//        MyDateFormat dateFormatMMDD = new MyDateFormat("MMdd");

        /** **************** Map ISO to IFX **************** */

        Ifx ifx = new Ifx();

        //Integer mti = null; //Raza MasterCard commeting
		String mti = null;
		try {
			mti = isoMsg.getMTI(); //Integer.parseInt(isoMsg.getMTI()); //Raza MasterCard commenting
		} catch (NumberFormatException e) {
			ISOException isoe = new ISOException("Invalid MTI", e);
			ifx.setSeverity( Severity.ERROR);
			ifx.setStatusDesc ( isoe.getClass().getSimpleName() + ": "+ isoe.getMessage());
		}

        
		//Integer emvTrnType = null; //Raza MasterCard commenting
		String emvTrnType = null;

		ifx.setAppPAN(isoMsg.getString(2));

		String str_fld3 = isoMsg.getString(3);
		if (str_fld3 != null && str_fld3.length() == 6) {
			try {
				emvTrnType = str_fld3.substring(0, 2).trim(); //Integer.parseInt(str_fld3.substring(0, 2).trim()); //Raza MasterCard commenting

			} catch (NumberFormatException e) {
				ISOException isoe = new ISOException("Invalid Process Code: " + str_fld3, e);
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
				}

				logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}

//			if (str_fld3.substring(2, 4).trim().equals("00"))
				ifx.setAccTypeFrom(AccType.MAIN_ACCOUNT);
//			else if (str_fld3.substring(2, 4).trim().equals("10"))
//				ifx.setAccTypeFrom(AccType.SUBSIDIARY_ACCOUNT);
//			else if (str_fld3.substring(2, 4).trim().equals("20"))
//				ifx.setAccTypeFrom(AccType.CARD);
//			else
//				ifx.setAccTypeFrom(AccType.MAIN_ACCOUNT);

//			if (str_fld3.substring(4, 6).trim().equals("00"))
				ifx.setAccTypeTo(AccType.MAIN_ACCOUNT);
//			else if (str_fld3.substring(4, 6).trim().equals("10"))
//				ifx.setAccTypeTo(AccType.SUBSIDIARY_ACCOUNT);
//			else if (str_fld3.substring(4, 6).trim().equals("20"))
//				ifx.setAccTypeTo(AccType.CARD);
//			else
//				ifx.setAccTypeTo(AccType.MAIN_ACCOUNT);

			mapTrnType(ifx, emvTrnType);
		}

            try {
    			String acquire_currency = isoMsg.getString(49);

    			Currency currency = null;
    			
    			if (Util.hasText(acquire_currency)) {
    				currency = ProcessContext.get().getCurrency(Integer.parseInt(acquire_currency));//GlobalContext.getInstance().getCurrency(Integer.parseInt(acquire_currency));
    				if (currency == null){
    					throw new ISOException("Ivalid Currency Code: "+ acquire_currency);
    				}
    			}
    			else{
    				currency = ProcessContext.get().getRialCurrency();//GlobalContext.getInstance().getRialCurrency();
    			}
    			ifx.setAuth_Currency(currency.getCode());
    			ifx.setAuth_CurRate("1");
    			
    			if (isoMsg.isRequest()) {
    				ifx.setTrx_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
    			}
    			
    			ifx.setAuth_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
    			ifx.setReal_Amt(ifx.getAuth_Amt());
    			
    		} catch (Exception e) {
    			if (!Util.hasText(ifx.getStatusDesc())) {
    				ifx.setSeverity(Severity.ERROR);
    				ifx.setStatusDesc(e.getClass().getSimpleName() + ": "
    						+ e.getMessage());
    			}
    			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
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
    				if (Util.hasText(sec_amt)) {
    					ifx.setSec_Amt(Util.longValueOf(sec_amt));
    					
    				} else
    					ifx.setSec_Amt(ifx.getAuth_Amt());
    				
    		} catch (Exception e) {
    			if (!Util.hasText(ifx.getStatusDesc())) {
    				ifx.setSeverity ( Severity.ERROR);
    				ifx.setStatusDesc ( e.getClass().getSimpleName() + ": " + e.getMessage());
    			}
    			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
    		}

		
		
		if (!isoMsg.getString(7).equals(""))
            ifx.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", isoMsg.getString(7).trim())));

        ifx.setSrc_TrnSeqCntr( ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));
        ifx.setMy_TrnSeqCntr( ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));

        String localTime = isoMsg.getString(12).trim();
        String localDate = isoMsg.getString(13).trim();
        DateTime now = DateTime.now();
		
        try {

			DateTime d= new DateTime(MyDateFormatNew.parse("MMddHHmmss", localDate + localTime));
//			if (d != null && GlobalContext.getInstance().getMyInstitution().getBin().equals(Long.valueOf(isoMsg.getString(32).trim()))
			if (d != null && ProcessContext.get().getMyInstitution().getBin().equals(Long.valueOf(isoMsg.getString(32).trim()))
					&& FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())) {
				
				if (d.getDayDate().getMonth() == 12 && now.getDayDate().getMonth() == 1) {
					logger.info("set origDt year to parsal!");
					d.getDayDate().setYear(now.getDayDate().getYear() - 1);
					
				} else if (d.getDayDate().getMonth() == 1 && now.getDayDate().getMonth() == 12) {
					logger.info("set origDt year to sale dige!");
					d.getDayDate().setYear(now.getDayDate().getYear() + 1);
				}				
        	}
			
			ifx.setOrigDt(d);
			
			
		} catch (Exception e) {
			ISOException isoe = new ISOException("Unparsable Original Date.", e);
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity ( Severity.ERROR);
				ifx.setStatusDesc ( isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}
		
		
		try {
            String expDate = isoMsg.getString(14).trim();
            ifx.setExpDt( Long.parseLong(expDate) );
		}catch (Exception e) {
//			if (GlobalContext.getInstance().getMyInstitution().getBin().toString().equals(isoMsg.getString(33))) {
			if (ProcessContext.get().getMyInstitution().getBin().toString().equals(isoMsg.getString(33))) {
				String track2Data = isoMsg.getString(35);
				if (Util.hasText(track2Data)) {
					int PANEndIndex = track2Data .indexOf('=');
					try{
						String cardExpDt = track2Data.substring(PANEndIndex + 1, PANEndIndex + 5);
						ifx.setExpDt(Long.parseLong(cardExpDt)); //ISO 14
					}catch(Exception e1){
						logger.error("Could not set ExpDt from track2");
					}
				}
			}
		}
		String settleDate = isoMsg.getString(15).trim();
		try{
			
//            String settleDate = isoMsg.getString(15).trim();
			MonthDayDate d= new MonthDayDate(MyDateFormatNew.parse("MMdd", settleDate));
            
			if (Util.hasText(settleDate))
            	ifx.setSettleDt(d);		

			if (d != null && d.getMonth() == 1 && now.getDayDate().getMonth() == 12) {
				d.setYear(now.getDayDate().getYear() + 1);
				ifx.setSettleDt(d);
			}
			else if( d!= null && d.getMonth() == 12 && now.getDayDate().getMonth() ==1){
				d.setYear(now.getDayDate().getYear()-1);
				ifx.setSettleDt(d);
			}
		}catch (Exception e) {
			logger.info("Exception in setting SettleDate(15)");
		}
		String postedDate = isoMsg.getString(17).trim();
		try{
//            String postedDate = isoMsg.getString(17).trim();
			MonthDayDate d= new MonthDayDate(MyDateFormatNew.parse("MMdd", postedDate));

			if (Util.hasText(postedDate))
            	ifx.setPostedDt(d);
			
			if (d != null && d.getMonth() == 1 && now.getDayDate().getMonth() == 12) {
				d.setYear(now.getDayDate().getYear() + 1);
				ifx.setPostedDt(d);
			}
			else if( d!= null && d.getMonth() == 12 && now.getDayDate().getMonth() ==1){
				d.setYear(now.getDayDate().getYear()-1);
				ifx.setPostedDt(d);
			}

        } catch (Exception e) {
        	logger.info("Exception in setting PostedDate(17)");
//        	logger.warn(e.getClass().getSimpleName() + ": " + e.getMessage());
//        	ISOException isoe = new ISOException("Invalid Date", e);
//			ifx.setSeverity ( Severity.ERROR);
//			ifx.setStatusDesc ( isoe.getClass().getSimpleName() + ": "+ isoe.getMessage());
        }

        mapTerminalType(ifx, isoMsg.getString(25));
        
        ifx.setBankId (isoMsg.getString(32).trim());
        ifx.setFwdBankId (isoMsg.getString(33).trim());
        ifx.setDestBankId (isoMsg.getString(33).trim());

        ifx.setTrk2EquivData ( isoMsg.getString(35));
        // TODO If trxRef is null
        // then if field 37 != "" then RRN = field 37
        // else if field 37 = null then RRN = newRRN().
        // else if trxRef != null then RRN = trxRef.RRN
        //
//        ifx.setNetworkRefId ( ISOUtil.zeroUnPad(isoMsg.getString(37).trim()));

        ifx.setApprovalCode ( isoMsg.getString(38).trim());
        ifx.setRsCode (mapError(isoMsg.getString(39).trim()));

//        ifx.setTerminalId ( ISOUtil.zeroUnPad(isoMsg.getString(41).trim()));
//        ifx.setOrgIdNum (ISOUtil.zeroUnPad(isoMsg.getString(42).trim()));

        String P43 = isoMsg.getString(43);
//        if (P43 != null && P43.length() == 40) {
//
//            try {
//                ITerminal terminal = getTerminalService().findTerminal(Long.valueOf(ifx.getTerminalId()));
//                Contact contact = terminal.getOwner().getContact();
//
//                if (contact != null) {
//                ifx.setName ( contact.getName());
//                ifx.setCity ( contact.getAddress().getCity());
//                ifx.setStateProv ( contact.getAddress().getState());
//                ifx.setCountry ( contact.getAddress().getCountry());
////                ifx.setCity ( contact.getAddress().getCity().getAbbreviation());
////                ifx.setStateProv ( contact.getAddress().getState().getAbbreviation());
////                ifx.setCountry ( contact.getAddress().getCountry().getAbbreviation());
//                }
//            } catch (Exception e) {
//                ifx.setName ( P43.substring(0, 22).trim());
//                ifx.setCity ( P43.substring(22, 35).trim());
//                ifx.setStateProv ( P43.substring(35, 38).trim());
//                ifx.setCountry ( P43.substring(38, 40).trim());
//            }
//        }

        String f_44 = isoMsg.getString(44);
        mapField44(ifx, f_44, convertor);
		
        ifx.setPINBlock ( isoMsg.getString(52).trim());

        String P54 = isoMsg.getString(54);
        
        if (ISOResponseCodes.APPROVED.equals(ifx.getRsCode())
				|| ISOResponseCodes.INVALID_ACCOUNT.equals(ifx
						.getRsCode())) {
			while (P54 != null && P54.length() >= 20) {
				AcctBal acctBal = new AcctBal();

				Integer acctType = Integer.parseInt(P54.substring(0, 2));

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
					amtType = Integer.parseInt(P54.substring(2, 4));
				} catch (NumberFormatException e) {
					ISOException isoe = new ISOException(
							"Bad Format: Amount Type [field 54]", e);
					if (!Util.hasText(ifx.getStatusDesc())) {
						ifx.setSeverity(Severity.ERROR);
						ifx.setStatusDesc(isoe.getClass().getSimpleName()
								+ ": " + isoe.getMessage());
					}
					logger.error(isoe.getClass().getSimpleName() + ": "
							+ isoe.getMessage());
				}

				switch (amtType) {
				case 1:
					acctBal.setBalType(BalType.LEDGER);
					if(ISOFinalMessageType.isTransferMessage(ifx.getIfxType()))
						ifx.setAcctBalLedger(acctBal);
					if(! ISOFinalMessageType.isTransferMessage(ifx.getIfxType()))
						ifx.setTransientAcctBalLedger(acctBal);
					break;
				case 2:
					acctBal.setBalType(BalType.AVAIL);
					if(ISOFinalMessageType.isTransferMessage(ifx.getIfxType()))
						ifx.setAcctBalAvailable(acctBal);
					if(!ISOFinalMessageType.isTransferMessage(ifx.getIfxType()))
						ifx.setTransientAcctBalAvailable(acctBal);
					break;
				default:
					acctBal.setBalType(BalType.UNKNOWN);
					break;
				}

				acctBal.setCurCode(P54.substring(4, 7));
				acctBal.setAmt(P54.substring(7, 20));

				// acctBal.ifx = ifx;
				// ifx.AcctBals.add(acctBal);

				P54 = P54.substring(20);
			}
		}
        
        mapIfxType(ifx, mti, emvTrnType);
        
        
        ifx.setRecvBankId(isoMsg.getString(100));
//        Long bin = GlobalContext.getInstance().getMyInstitution().getBin();
        String bin = ""+ProcessContext.get().getMyInstitution().getBin();
        if (IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifx.getIfxType()) &&
        		ifx.getRecvBankId().equals(639347L)){
        	ifx.setBankId(bin);
        }
        
        if (IfxType.TRANSFER_RS.equals(ifx.getIfxType()) 
        		|| IfxType.TRANSFER_REV_REPEAT_RS.equals(ifx.getIfxType())
				|| IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifx.getIfxType())) {
			if (!bin.equals(ifx.getRecvBankId()) && !bin.equals(ifx.getDestBankId())) {
				ifx.setBankId(bin);
				if (ifx.getOriginalDataElements() != null)
					ifx.getOriginalDataElements().setBankId(bin);
			}
		}
        
        Boolean trimLefZero = false;
        if (IfxType.TRANSFER_RQ.equals(ifx.getIfxType()) 
        		|| IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifx.getIfxType())
        		|| IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType())) {
        	ifx.setBankId("639347");
        	if (!ifx.getBankId().equals(ifx.getRecvBankId()) && !ifx.getBankId().equals(ifx.getDestBankId())) {
        		ifx.setBankId(bin);
        		trimLefZero = true;
        		if (ifx.getOriginalDataElements() != null)
        			ifx.getOriginalDataElements().setBankId(bin);
        	}
        }

		mapFieldANFix(ifx, isoMsg, 37);
		if (trimLefZero){
			ifx.setNetworkRefId(Util.trimLeftZeros(ifx.getNetworkRefId()));
		}
        
        
        mapFieldANFix(ifx, isoMsg, 41);
        mapFieldANFix(ifx, isoMsg, 42);

        String P64 = isoMsg.getString(64).trim();
        String S128 = isoMsg.getString(128).trim();
        if (P64 != null && P64.length() > 0)
            ifx.setMsgAuthCode ( P64);
        else if (S128 != null && S128.length() > 0)
            ifx.setMsgAuthCode ( S128);

        String S90 = isoMsg.getString(90);
        if (S90 != null && S90.length() >= 20) {
            ifx.setOriginalDataElements ( new MessageReferenceData());
            ifx.getOriginalDataElements().setTrnSeqCounter( ISOUtil.zeroUnPad(S90.substring(4, 10)));
            
            String msgType = S90.substring(0, 4);
            if (Integer.parseInt(msgType) != 0)
                ifx.getOriginalDataElements().setMessageType ( msgType);
            else{
            	ISOException isoe = new ISOException("Invalid Format( F_90: "+
    					" OriginalData.msgType= NULL, OriginalData.TrnSeqCounter = "+ ifx.getOriginalDataElements().getTrnSeqCounter()+", temrinalId= "+ ifx.getTerminalId() +")");
            	if (!Util.hasText(ifx.getStatusDesc())) {
   					ifx.setSeverity ( Severity.ERROR);
   					ifx.setStatusDesc ( isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
   				}
               logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }
            
            String origDt = S90.substring(10, 20);
            if (Integer.parseInt(origDt) != 0) {
                try {
                    ifx.getOriginalDataElements().setOrigDt(new DateTime( MyDateFormatNew.parse("MMddHHmmss", origDt)));
                } catch (ParseException e) {
                	ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.origDt= NULL, OriginalData.TrnSeqCounter = "+
					ifx.getOriginalDataElements().getTrnSeqCounter() 
					+", temrinalId= "+ ifx.getTerminalId() +")");
                    if (!Util.hasText(ifx.getStatusDesc())) {
       					ifx.setSeverity ( Severity.ERROR);
       					ifx.setStatusDesc ( isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
       				}
                   logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
                }
            }
            
            String bankId = S90.substring(20, 31).trim();
            if (Integer.parseInt(bankId) != 0)
        		ifx.getOriginalDataElements().setBankId(bankId);
        	    else{
//        	    	ifx.getOriginalDataElements().setBankId = referenceIfx.setBankId;
        	    	ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.bankId= NULL, OriginalData.TrnSeqCounter = "+
        					ifx.getOriginalDataElements().getTrnSeqCounter() 
        					+", temrinalId= "+ ifx.getTerminalId() +", OriginalData.origDt= "+ ifx.getOriginalDataElements().getOrigDt()+
        					")" );
        	    	if (!Util.hasText(ifx.getStatusDesc())) {
       					ifx.setSeverity ( Severity.ERROR);
       					ifx.setStatusDesc ( isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
       				}
                   logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        	    }

                
            String fwdBankId = S90.substring(31).trim();
    	    if (Integer.parseInt(fwdBankId) != 0)
    			ifx.getOriginalDataElements().setFwdBankId(fwdBankId);
    		else{
    		    	ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.FwdBankId = NULL, OriginalData.TrnSeqCounter = "+
    						ifx.getOriginalDataElements().getTrnSeqCounter() 
    						+", OriginalData.temrinalId= "+ ifx.getTerminalId() +", OriginalData.origDt= "+ ifx.getSafeOriginalDataElements().getOrigDt()+
    						", OriginalData.bankId ="+ ifx.getOriginalDataElements().getBankId() +")" );
    		    	if (!Util.hasText(ifx.getStatusDesc())) {
       					ifx.setSeverity ( Severity.ERROR);
       					ifx.setStatusDesc ( isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
       				}
                   logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
    		    }
    	    
    	    ifx.getOriginalDataElements().setTerminalId(ifx.getTerminalId());
    	    ifx.getOriginalDataElements().setAppPAN(ifx.getAppPAN());
        }
        String S95 = isoMsg.getString(95);
        if (S95 != null && S95.length() >= 24) {
            ifx.setNew_AmtAcqCur ( S95.substring(0, 12));
            ifx.setNew_AmtIssCur ( S95.substring(12, 24));
            
            Long real_Amt = Util.longValueOf(ifx.getNew_AmtAcqCur());
            real_Amt = (real_Amt!=null && !real_Amt.equals(0L))? real_Amt :Util.longValueOf(ifx.getNew_AmtIssCur());
            if (real_Amt!= null && !real_Amt.equals(0L))
            	ifx.setReal_Amt(real_Amt);
        }

        
        
        if (ifx.getIfxType()!= null && !ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
        		&& ifx.getTerminalType() ==null ){
        	ISOException isoe = new ISOException("Invalid terminal type code: " + Integer.parseInt("0"+isoMsg.getString(25).trim()));
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity ( Severity.ERROR);
				ifx.setStatusDesc ( isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        }
        
       /* ifx.setRecvBankId(Util.longValueOf(isoMsg.getString(100)));
        Long bin = GlobalContext.getInstance().getMyInstitution().getBin();
        if (IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifx.getIfxType()) &&
        		ifx.getRecvBankId().equals(639347L)){
        	ifx.setBankId(bin);
        }
        
        if (IfxType.TRANSFER_RS.equals(ifx.getIfxType()) 
        		|| IfxType.TRANSFER_REV_REPEAT_RS.equals(ifx.getIfxType())
				|| IfxType.TRANSFER_CHECK_ACCOUNT_RS.equals(ifx.getIfxType())) {
			if (!bin.equals(ifx.getRecvBankId()) && !bin.equals(ifx.getDestBankId())) {
				ifx.setBankId(bin);
				if (ifx.getOriginalDataElements() != null)
					ifx.getOriginalDataElements().setBankId(bin);
			}
		}
        
        Boolean trimLefZero = false;
        if (IfxType.TRANSFER_RQ.equals(ifx.getIfxType()) 
        		|| IfxType.TRANSFER_REV_REPEAT_RQ.equals(ifx.getIfxType())
        		|| IfxType.TRANSFER_CHECK_ACCOUNT_RQ.equals(ifx.getIfxType())) {
        	ifx.setBankId(639347L);
        	if (!ifx.getBankId().equals(ifx.getRecvBankId()) && !ifx.getBankId().equals(ifx.getDestBankId())) {
        		ifx.setBankId(bin);
        		trimLefZero = true;
        		if (ifx.getOriginalDataElements() != null)
        			ifx.getOriginalDataElements().setBankId(bin);
        	}
        }

		mapFieldANFix(ifx, isoMsg, 37);
		if (trimLefZero){
			ifx.setNetworkRefId(Util.trimLeftZeros(ifx.getNetworkRefId()));
		}*/
		
        byte[] field48 ;
        if (isoMsg.hasField(48)) {
        	field48 = (byte[])isoMsg.getValue(48);
        	mapField48(ifx, field48, convertor);
        }
        
        
        if (ISOFinalMessageType.isReturnMessage(ifx.getIfxType())
        	||	ISOFinalMessageType.isReturnReverseMessage(ifx.getIfxType())) {
        	ifx.getSafeOriginalDataElements().setNetworkTrnInfo(ifx.getNetworkRefId());
        	ifx.getSafeOriginalDataElements().setBankId(ifx.getBankId());
        	ifx.getSafeOriginalDataElements().setTerminalId(ifx.getTerminalId());
        	ifx.getSafeOriginalDataElements().setAppPAN(ifx.getAppPAN());
        }
        
        if (Util.hasText(isoMsg.getString(102))) {
			ifx.setSubsidiaryAccFrom(isoMsg.getString(102));
			ifx.setAccTypeFrom(AccType.SUBSIDIARY_ACCOUNT);

		} else if (Util.hasText(isoMsg.getString(103))) {
			ifx.setSubsidiaryAccTo(isoMsg.getString(103));
			ifx.setAccTypeTo(AccType.SUBSIDIARY_ACCOUNT);
		}
        
/*        if (Util.hasText(isoMsg.getString(102))) {
        	ifx.setSubsidiaryAcct(isoMsg.getString(102));
        	if (ShetabFinalMessageType.isTransferToMessage(ifx.getIfxType()))
        		ifx.setAccTypeTo(AccType.SUBSIDIARY_ACCOUNT);
        	else
        		ifx.setAccTypeFrom(AccType.SUBSIDIARY_ACCOUNT);
        }
        else if (Util.hasText(isoMsg.getString(103))){
        	ifx.setSubsidiaryAcct(isoMsg.getString(103));
        	if (ShetabFinalMessageType.isTransferToMessage(ifx.getIfxType()))
        		ifx.setAccTypeTo(AccType.SUBSIDIARY_ACCOUNT);
        	else
        		ifx.setAccTypeFrom(AccType.SUBSIDIARY_ACCOUNT);
        }
        
*/        
        return ifx;
    }


	@Override
	public void mapField48(Ifx ifx, String f_48, EncodingConvertor convertor) {
		if (!Util.hasText(f_48))
			return;

		if (ISOFinalMessageType.isBankStatementMessage(ifx.getIfxType())&& ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
			if (f_48.length()<130)
				return;
			byte[] data = Hex.decode(f_48);
			List<BankStatementData> list = parseBankStatementData(data,ifx.getEMVRsData());
			ifx.setBankStatementData(list);
			BankStatementData bankStatementData = list.get(0);
			Long amount=bankStatementData.getAmount();
			amount = (bankStatementData.getTrnType().equalsIgnoreCase("D")? -1*amount: amount);
			// Show real amount on the receipt
			ifx.setAcctBalLedgerAmt(new Long(bankStatementData.getBalance()+ amount).toString());
		} else if (TrnType.DEPOSIT_CHECK_ACCOUNT.equals(ifx.getTrnType()))
			return;
		else
			super.mapField48(ifx, f_48, convertor);
	}
	
	public void mapField48(Ifx ifx, byte[] f_48, EncodingConvertor convertor) {
		if (f_48 == null || f_48.length == 0)
			return;
		
		if (ISOFinalMessageType.isChangePinBlockMessage(ifx.getIfxType()) && ISOFinalMessageType.isRequestMessage(ifx.getIfxType())){
			String newPINBlock = new String(f_48).toUpperCase();
			
			if (TerminalType.INTERNET.equals(ifx.getTerminalType())){
				newPINBlock =  newPINBlock.substring(9);
			}
			ifx.setNewPINBlock(newPINBlock);
		}else if (ISOFinalMessageType.isChangePinBlockMessage(ifx.getIfxType()) && ISOFinalMessageType.isResponseMessage(ifx.getIfxType())){
			String field_48 = new String (Hex.encode(Hex.decode(f_48))).toUpperCase();
			if (field_48.length() < 28)
				return;
			String exp_date_tag = new String (Hex.encode(TLVTag.EXP_DATE_TAG)).toUpperCase();
	    	String inquiry_date_tag = new String (Hex.encode(TLVTag.INQUIRY_DATE_TAG)).toUpperCase();
	    	String cvv2_tag = new String (Hex.encode(TLVTag.CVV2_TAG)).toUpperCase();
	    	field_48 = field_48.substring(field_48.indexOf(inquiry_date_tag));
	    	int index = field_48.indexOf(cvv2_tag)+cvv2_tag.length();
	    	String tmp = field_48.substring(index, index+2);
	    	index +=2;
	    	int length = Integer.parseInt(tmp);
	    	String value = new String(Hex.decode(field_48.substring(index, index+(2*length)))); 
//	    	System.out.println("CVV2:"+ value);
	    	ifx.setCVV2(value);
	    	
	    	index = field_48.indexOf(exp_date_tag)+exp_date_tag.length();
	    	tmp = field_48.substring(index, index+2);
	    	index +=2;
//	    	length = Integer.parseInt(new String(Hex.encode(Hex.decode(tmp))));
	    	length = 10;
	    	value = new String(Hex.decode(field_48.substring(index, index+(2*length))));
	    	String[] split = value.split("/");
	    	ifx.setExpDt(Long.parseLong(split[0].substring(2)+split[1]));
//	    	DateTime dateTime = PersianCalendar.toGregorian(new DateTime(new DayDate(Integer.parseInt(split[0]), Integer.parseInt(split[0]), Integer.parseInt(split[0])), DayTime.MAX_DAY_TIME));
//	    	ifx.setExpDt(dateTime.getTime());
		}else if (ISOFinalMessageType.isGetAccountMessage(ifx.getIfxType())&& ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
			String field_48 = new String(f_48).toUpperCase();
			String inquiry_date_tag = new String (Hex.encode(TLVTag.INQUIRY_DATE_TAG)).toUpperCase();
			String customer_account_tag = new String (Hex.encode(TLVTag.CUSTOMER_ACCOUNT_TAG)).toUpperCase();
			int indexOfInqueryDate = field_48.indexOf(inquiry_date_tag);
			
			if (indexOfInqueryDate == -1 )
				return;
			
			field_48 = field_48.substring(indexOfInqueryDate);
			int index = -1;
			int accountIndex = 1;
			List<CardAccountInformation> list = new ArrayList<CardAccountInformation>();
			try {
				while ((index = field_48.indexOf(customer_account_tag)) != -1) {
					index += customer_account_tag.length() + 2;
					String account = field_48.substring(index, index + 34*2);
					byte[] data = Hex.decode(account.substring(0, 29 * 2));
					String c_account = retrieveData(data);
					data = Hex.decode(account.substring(58,58+6 ));
					String c_type = retrieveData(data);
					data = Hex.decode(account.substring(account.length() - 4));
					short c_hostID = LittleEndian.getShort(data);
					CardAccountInformation accountInformation = new CardAccountInformation();
					accountInformation.setAccountNumber(c_account);
					accountInformation.setIndex(new Integer(accountIndex++).toString());
					accountInformation.setLength(c_account.length());
					accountInformation.setEmvRsData(ifx.getEMVRsData());
					list.add(accountInformation);
					field_48 = field_48.substring(index + 34*2);
				}
			} catch (Exception e) {
				logger.error("Encounter with an exception in retrieving Card Accounts. "+ e.getClass().getSimpleName()+": "+ e.getMessage());
				return;
			}
			ifx.setCardAccountInformation(list);
			
		} else
			mapField48(ifx, new String(f_48), convertor);
	}
	
	public List<BankStatementData> parseBankStatementData(byte[] data, EMVRsData rsData) {
//		int numberOfRecords =	LittleEndian.getShort(Hex.decode(new String(data)));
		int numberOfRecords =	LittleEndian.getShort(data);
		List<BankStatementData> result = new ArrayList<BankStatementData>();
		MyInteger offset = new MyInteger(0);
		offset.value += LittleEndianConsts.SHORT_SIZE;
		for (int i =0; i<numberOfRecords; i++){
//			offset += 192;
			BankStatementData parseStatement = parseStatement(data, offset);
			parseStatement.setEmvRsData(rsData);
			result.add(parseStatement);
		}
		return result;
	}

	private BankStatementData parseStatement(byte[] data, MyInteger offset){
		BankStatementData statement = new BankStatementData();
		int brnCode = LittleEndian.getShort(data, offset.value);
		offset.value += LittleEndianConsts.SHORT_SIZE;
		
		DateTime docDate = parseDate(data, offset);
		long docSrl = LittleEndian.getInt(data, offset.value);
		offset.value += LittleEndianConsts.INT_SIZE;
		
		long amount = new Double(LittleEndian.getDouble(data, offset.value)).longValue();
		offset.value += LittleEndianConsts.DOUBLE_SIZE;
 
		long balance = new Double(LittleEndian.getDouble(data, offset.value)).longValue();
		offset.value += LittleEndianConsts.DOUBLE_SIZE;
		
		ChargeFlag m_byChargFlg = ChargeFlag.getCFlag(data[offset.value]);
		if (amount <0){
			m_byChargFlg = ChargeFlag.CHARGED; //m_byChargFlg.reverse();
			amount *= -1;
		}else {
			m_byChargFlg = ChargeFlag.NORMAL;
		}
		
		offset.value++;
		
		String m_szDocDesc = "";
		try {
			m_szDocDesc = new String(data, offset.value, 60, "windows-1256");
		} catch (UnsupportedEncodingException e) {
			logger.error("Could not convert NeginStatement description to windows-1256"+e);
		}
		offset.value += 61;
		
		statement.setAmount(amount);
		statement.setBalance(balance);
		statement.setTrxDt(docDate);
		statement.setTrnType(m_byChargFlg.toString());
		statement.setDescription(m_szDocDesc);
		return statement;
	}
	
	private DateTime parseDate(byte[] data, MyInteger offset) {
		int year = LittleEndian.getShort(data, offset.value);
		offset.value += LittleEndianConsts.SHORT_SIZE;
		
		int month = LittleEndian.getShort(data, offset.value);
		offset.value += LittleEndianConsts.SHORT_SIZE;
		
		int day = LittleEndian.getShort(data, offset.value);
		offset.value += LittleEndianConsts.SHORT_SIZE;
		
		int hour = LittleEndian.getShort(data, offset.value);
		offset.value += LittleEndianConsts.SHORT_SIZE;
		
		int minute = LittleEndian.getShort(data, offset.value);
		offset.value += LittleEndianConsts.SHORT_SIZE;
		
		int second = LittleEndian.getShort(data, offset.value);
		offset.value += LittleEndianConsts.SHORT_SIZE;
		
		DateTime dateTime = new DateTime(new DayDate(year, month, day), new DayTime(hour, minute, second));
		return PersianCalendar.toGregorian(dateTime);
	}

	private String retrieveData(byte[] data) throws UnsupportedEncodingException {
		int i =0;
		while(data[i] != 0)
			i++;
		byte[] newData = new byte[i];
		System.arraycopy(data, 0, newData, 0, i);
		return new String(newData, "windows-1256");
	}
}
