package vaulsys.protocols.iso8583v93;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.customer.Currency;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.exception.exception.ReferenceTransactionNotFoundException;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MessageReferenceData;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOtoIfxMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.text.ParseException;

import org.apache.log4j.Logger;

public class ISO8583v93ISOToIFXMapper extends ISOtoIfxMapper{

	public static final ISO8583v93ISOToIFXMapper Instance = new ISO8583v93ISOToIFXMapper();
	
	private ISO8583v93ISOToIFXMapper(){}
	
	Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {
		
		ISOMsg isoMsg = (ISOMsg) message;

//        MyDateFormat dateFormatMMDDhhmmss = new MyDateFormat("MMddHHmmss");
//        MyDateFormat dateFormatMMDD = new MyDateFormat("MMdd");

        /** **************** Map ISO to IFX **************** */

        Ifx ifx = new Ifx();

        //Integer mti = null; //Raza MasterCard commenting
		String mti = null;
		//try {
			mti = isoMsg.getMTI(); //Integer.parseInt(isoMsg.getMTI()); //Raza MasterCard
		//} catch (NumberFormatException e) { //Raza MasterCard commenting
		//	ISOException isoe = new ISOException("Invalid MTI", e); //Raza MasterCard commenting
		//	ifx.setSeverity( Severity.ERROR); //Raza MasterCard commenting
		//	ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": "+ isoe.getMessage()); //Raza MasterCard commenting
		//} //Raza MasterCard commenting

//        Integer processCode = null;

        //Integer emvTrnType = null; //Raza MasterCard commenting
		String emvTrnType = null;
        
        ifx.setAppPAN( isoMsg.getString(2));
        String str_fld3 = isoMsg.getString(3);
        
		if (str_fld3 != null && str_fld3.length() == 6) {
            if (str_fld3.substring(2, 4).trim().equals("00"))
            	ifx.setAccTypeFrom ( AccType.MAIN_ACCOUNT);
            else if (str_fld3.substring(2, 4).trim().equals("10"))
            	ifx.setAccTypeFrom (AccType.SUBSIDIARY_ACCOUNT);
            else if (str_fld3.substring(2, 4).trim().equals("20"))
            	ifx.setAccTypeFrom (AccType.CARD);
           
            if (str_fld3.substring(4, 6).trim().equals("00"))
            	ifx.setAccTypeTo (AccType.MAIN_ACCOUNT);
            else if (str_fld3.substring(4, 6).trim().equals("10"))
            	ifx.setAccTypeTo (AccType.SUBSIDIARY_ACCOUNT);
            else if (str_fld3.substring(4, 6).trim().equals("20"))
            	ifx.setAccTypeTo (AccType.CARD);
            
            
            try {
            	  emvTrnType = str_fld3.substring(0, 2).trim(); //Integer.parseInt(str_fld3.substring(0, 2).trim()); //Raza MasterCard commenting
            } catch (NumberFormatException e) {
            	ISOException isoe = new ISOException("Invalid Process Code: "+ str_fld3, e);
            	if (!Util.hasText(ifx.getStatusDesc())) {
            		ifx.setSeverity( Severity.ERROR);
            		ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            	}
            	
            	logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }


           mapTrnType(ifx, emvTrnType);
        }
        
        try{
        	String acquire_currency = isoMsg.getString(49);
        	Currency currency = ProcessContext.get().getCurrency(Integer.parseInt(acquire_currency));//GlobalContext.getInstance().getCurrency(Integer.parseInt(acquire_currency));
        	if (currency == null)
        		throw new ISOException("Ivalid Currency Code: "+ acquire_currency);
			ifx.setAuth_Currency ( currency.getCode());
        }catch (Exception e) {
        	if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity ( Severity.ERROR);
				ifx.setStatusDesc ( e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    	
        ifx.setAuth_CurRate ( "1");
        
        if (ISOFinalMessageType.isRequestMessage(ifx.getIfxType())) {
        	ifx.setTrx_Amt(Long.parseLong(isoMsg.getString(4).trim()));
        }
        
        ifx.setAuth_Amt(Long.parseLong(isoMsg.getString(4).trim()));
        ifx.setReal_Amt(Long.parseLong(isoMsg.getString(4).trim()));
        
        try {
			String issuer_currency = isoMsg.getString(51).trim();
			
			if (Util.hasText(issuer_currency)) {
				Currency currency = ProcessContext.get().getCurrency(Integer.parseInt(issuer_currency));//GlobalContext.getInstance().getCurrency(Integer.parseInt(issuer_currency));
				if (currency == null)
					throw new ISOException("Ivalid Currency Code: " + issuer_currency);
				ifx.setSec_Currency(currency.getCode());
			}
		} catch (Exception e) {
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity ( Severity.ERROR);
				ifx.setStatusDesc ( e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		ifx.setSec_CurRate(isoMsg.getString(10).trim());
		ifx.setSec_Amt(Long.parseLong(isoMsg.getString(6).trim()));
        
        if (!isoMsg.getString(7).equals(""))
            ifx.setTrnDt (new DateTime( MyDateFormatNew.parse("MMddHHmmss", isoMsg.getString(7).trim())));

        ifx.setSrc_TrnSeqCntr ( ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));
        ifx.setMy_TrnSeqCntr ( ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));

        String localTime = isoMsg.getString(12).trim();
        String localDate = isoMsg.getString(13).trim();
//        try {
//			ifx.setOrigDt(new DateTime( MyDateFormatNew.parse("MMddHHmmss", localDate + localTime)));
//		} catch (Exception e) {
//			ISOException isoe = new ISOException("Unparsable Original Date.", e);
//			if (!Util.hasText(ifx.getStatusDesc())) {
//				ifx.setSeverity( Severity.ERROR);
//				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
//			}
//			logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
//		}
        try{
            MonthDayDate d= new MonthDayDate(MyDateFormatNew.parse("MMdd", localDate));
			if (d != null && d.getMonth() == 1 && DateTime.now().getDayDate().getMonth() == 12) {
				d.setYear(DateTime.now().getDayDate().getYear() + 1);
				DateTime dd=new DateTime(MyDateFormatNew.parse("MMddHHmmss", d+localTime));
				ifx.setOrigDt(dd);
			}
			else if( d!= null && d.getMonth() == 12 && DateTime.now().getDayDate().getMonth() ==1){
				d.setYear(DateTime.now().getDayDate().getYear()-1);
				DateTime dd=new DateTime(MyDateFormatNew.parse("MMddHHmmss", d+localTime));
				ifx.setOrigDt(dd);
			}
		}catch (Exception e) {
			// TODO: handle exception
			ISOException isoe = new ISOException("Unparsable Original Date.", e);
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}
		
		
		try {
			String expDate = isoMsg.getString(14).trim();
			ifx.setExpDt(Long.parseLong(expDate));

			String settleDate = isoMsg.getString(15).trim();
			// ifx.setSettleDt ( new MonthDayDate(MyDateFormatNew.parse("MMdd", settleDate)));
			MonthDayDate d = new MonthDayDate(MyDateFormatNew.parse("MMdd", settleDate));
			if (d != null && d.getMonth() == 1 && DateTime.now().getDayDate().getMonth() == 12) {
				d.setYear(DateTime.now().getDayDate().getYear() + 1);
				ifx.setSettleDt(d);
				
			} else if (d != null && d.getMonth() == 12 && DateTime.now().getDayDate().getMonth() == 1) {
				d.setYear(DateTime.now().getDayDate().getYear() - 1);
				ifx.setSettleDt(d);
			}
		} catch (Exception e) {
			logger.info(e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		try {
            String postedDate = isoMsg.getString(17).trim();
//            ifx.setPostedDt ( new MonthDayDate(MyDateFormatNew.parse("MMdd", postedDate)));
            MonthDayDate d= new MonthDayDate(MyDateFormatNew.parse("MMdd", postedDate));
			if (d != null && d.getMonth() == 1 && DateTime.now().getDayDate().getMonth() == 12) {
				d.setYear(DateTime.now().getDayDate().getYear() + 1);
				ifx.setPostedDt(d);
				
			} else if( d!= null && d.getMonth() == 12 && DateTime.now().getDayDate().getMonth() ==1){
				d.setYear(DateTime.now().getDayDate().getYear()-1);
				ifx.setSettleDt(d);
			}else{
				ifx.setPostedDt(d);
			}
            

        } catch (Exception e) {
        	logger.info(e.getClass().getSimpleName() + ": " + e.getMessage());
//        	ISOException isoe = new ISOException("Invalid Date", e);
//			ifx.setSeverity( Severity.ERROR);
//			ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": "+ isoe.getMessage();
        }

//        Integer terminalTypeCode = Integer.parseInt("0" + isoMsg.getString(25).trim());
//        switch (terminalTypeCode) {
//            case (2):
//                ifx.setTerminalType ( TerminalType.ATM);
//                break;
//            case (3):
//                ifx.setTerminalType( TerminalType.PINPAD);
//                break;
//            case (14):
//                ifx.setTerminalType(  TerminalType.POS);
//                break;
//            case (59):
//                ifx.setTerminalType(  TerminalType.INTERNET);
//                break;
//            case (7):
//                ifx.setTerminalType(  TerminalType.VRU);
//                break;
//			default:
//				ISOException isoe = new ISOException("Invalid terminal type code: " + terminalTypeCode);
//				if (!Util.hasText(ifx.getStatusDesc())) {
//					ifx.setSeverity( Severity.ERROR);
//					ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
//				}
//				logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
//				break;
//        }

//        Integer terminalTypeCode = Integer.parseInt("0"+isoMsg.getString(25).trim());
//		ifx.setTerminalType(new TerminalType(terminalTypeCode));
		mapTerminalType(ifx, isoMsg.getString(25));
        
        ifx.setBankId (isoMsg.getString(32).trim());
        ifx.setFwdBankId (isoMsg.getString(33).trim());
        ifx.setDestBankId (isoMsg.getString(33).trim());
        ifx.setTrk2EquivData ( isoMsg.getString(35));
        
        ifx.setNetworkRefId ( ISOUtil.zeroUnPad(isoMsg.getString(37).trim()));
        ifx.setApprovalCode ( isoMsg.getString(38).trim());
        ifx.setRsCode ( mapError(isoMsg.getString(39).trim()));
        
        ifx.setTerminalId ( ISOUtil.zeroUnPad(isoMsg.getString(41).trim()));
        ifx.setOrgIdNum ( ISOUtil.zeroUnPad(isoMsg.getString(42).trim()));

        String P43 = isoMsg.getString(43);
//        if (P43 != null && P43.length() == 40) {
//            try {
//                ITerminal terminal = getTerminalService().findTerminal(Long.valueOf(ifx.getTerminalId()));
//                Contact contact = terminal.getOwner().getContact();
//
//                ifx.setName ( contact.getName());
//                ifx.setCity ( contact.getAddress().getCity());
//                ifx.setStateProv ( contact.getAddress().getState());
//                ifx.setCountry ( contact.getAddress().getCountry());
////                ifx.setCity ( contact.getAddress().getCity().getAbbreviation());
////                ifx.setStateProv ( contact.getAddress().getState().getAbbreviation());
////                ifx.setCountry ( contact.getAddress().getCountry().getAbbreviation());
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

				// acctBal.ifx = ifx;
				// ifx.AcctBals.add(acctBal);

				P54 = P54.substring(20);
			}
		}

        String P64 = isoMsg.getString(64).trim();
        String S128 = isoMsg.getString(128).trim();
        if (P64 != null && P64.length() > 0)
            ifx.setMsgAuthCode ( P64);
        else if (S128 != null && S128.length() > 0)
            ifx.setMsgAuthCode ( S128);

        String S90 = isoMsg.getString(90);
        if (S90 != null && S90.length() >= 20) {
        	
        	ifx.setOriginalDataElements (new MessageReferenceData());
            ifx.getSafeOriginalDataElements().setTrnSeqCounter ( ISOUtil.zeroUnPad(S90.substring(4, 10)));
            
            String msgType = S90.substring(0, 4);
            if (Integer.parseInt(msgType) != 0)
                ifx.getSafeOriginalDataElements().setMessageType ( msgType);
            else{
            	ReferenceTransactionNotFoundException isoe = new ReferenceTransactionNotFoundException("Referenced transaction not found(TrnSeqCntr = "+
    					ifx.getSafeOriginalDataElements().getTrnSeqCounter() 
    					+", temrinalId= "+ ifx.getTerminalId() +", msgType= NULL)");
            	if (!Util.hasText(ifx.getStatusDesc())) {
   					ifx.setSeverity( Severity.ERROR);
   					ifx.setStatusDesc(isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
   				}
               logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }
            
            String origDt = S90.substring(10, 20);
            if (Integer.parseInt(origDt) != 0) {
                try {
                    ifx.getSafeOriginalDataElements().setOrigDt (new DateTime( MyDateFormatNew.parse("MMddHHmmss", origDt)));
                } catch (ParseException e) {
                    ReferenceTransactionNotFoundException isoe = new ReferenceTransactionNotFoundException("Referenced transaction not found(TrnSeqCntr = "+
					ifx.getSafeOriginalDataElements().getTrnSeqCounter() 
					+", temrinalId= "+ ifx.getTerminalId() +", origDt= NULL)");
                    if (!Util.hasText(ifx.getStatusDesc())) {
       					ifx.setSeverity( Severity.ERROR);
       					ifx.setStatusDesc(isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
       				}
                   logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
                }
            }
            String bankId = S90.substring(20, 31).trim();
            if (Integer.parseInt(bankId) != 0)
        		ifx.getSafeOriginalDataElements().setBankId (bankId);
        	    else{
//        	    	ifx.OriginalDataElements.BankId = referenceIfx.BankId;
        	    	ReferenceTransactionNotFoundException isoe = new ReferenceTransactionNotFoundException("Referenced transaction not found(TrnSeqCntr = "+
        					ifx.getSafeOriginalDataElements().getTrnSeqCounter() 
        					+", temrinalId= "+ ifx.getTerminalId() +", origDt= "+ ifx.getSafeOriginalDataElements().getOrigDt()+
        					", bankId = NULL)" );
        	    	if (!Util.hasText(ifx.getStatusDesc())) {
       					ifx.setSeverity( Severity.ERROR);
       					ifx.setStatusDesc(isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
       				}
                   logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        	    }

                
            String fwdBankId = S90.substring(31).trim();
    	    if (Integer.parseInt(fwdBankId) != 0)
    			ifx.getSafeOriginalDataElements().setFwdBankId (fwdBankId);
    		    else{
//    		    	ifx.OriginalDataElements.FwdBankId = referenceIfx.DestBankId;
    		    	ReferenceTransactionNotFoundException isoe = new ReferenceTransactionNotFoundException("Referenced transaction not found(TrnSeqCntr = "+
    						ifx.getSafeOriginalDataElements().getTrnSeqCounter() 
    						+", temrinalId= "+ ifx.getTerminalId() +", origDt= "+ ifx.getSafeOriginalDataElements().getOrigDt()+
    						", FwdBankId = NULL"+
    						", bankId ="+ ifx.getSafeOriginalDataElements().getBankId() +")" );
    		    	if (!Util.hasText(ifx.getStatusDesc())) {
       					ifx.setSeverity( Severity.ERROR);
       					ifx.setStatusDesc(isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
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

        /** ************************** */

        // ifx.ExtISO.P15 = isoMsg.getString(15);
//        ifx.getIFX_Ext().P1 = isoMsg.getMTI();
//        ifx.getIFX_Ext().P22 = isoMsg.getString(22);
//        ifx.getIFX_Ext().P24 = isoMsg.getString(24);
//        ifx.getIFX_Ext().P30 = isoMsg.getString(30);
        // ifx.getIFX_Ext().P39 = isoMsg.getString(39);
//        ifx.getIFX_Ext().P48 = isoMsg.getString(48);
//        ifx.getIFX_Ext().P50 = isoMsg.getString(50);
//        ifx.getIFX_Ext().P53 = isoMsg.getString(53);
//        ifx.getIFX_Ext().P56 = isoMsg.getString(56);
//        ifx.getIFX_Ext().P74 = isoMsg.getString(74);
//        ifx.getIFX_Ext().P75 = isoMsg.getString(75);
//        ifx.getIFX_Ext().P76 = isoMsg.getString(76);
//        ifx.getIFX_Ext().P77 = isoMsg.getString(77);
//        ifx.getIFX_Ext().P78 = isoMsg.getString(78);
//        ifx.getIFX_Ext().P79 = isoMsg.getString(79);
//        ifx.getIFX_Ext().P80 = isoMsg.getString(80);
//        ifx.getIFX_Ext().P81 = isoMsg.getString(81);
//        ifx.getIFX_Ext().P86 = isoMsg.getString(86);
//        ifx.getIFX_Ext().P87 = isoMsg.getString(87);
//        ifx.getIFX_Ext().P88 = isoMsg.getString(88);
//        ifx.getIFX_Ext().P89 = isoMsg.getString(89);
//        ifx.getIFX_Ext().P93 = isoMsg.getString(93);
//        ifx.getIFX_Ext().P94 = isoMsg.getString(94);
//        ifx.getIFX_Ext().P96 = isoMsg.getString(96);
//        ifx.getIFX_Ext().P97 = isoMsg.getString(97);
//        ifx.getIFX_Ext().P102 = isoMsg.getString(102);

        mapField48(ifx, isoMsg.getString(48), convertor);
        /** ************************** */

        /** ************************** */

        mapIfxType(ifx, mti, emvTrnType);
        
        if (ifx.getIfxType()!= null && !ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
        		&& ifx.getTerminalType()==null ){
        	ISOException isoe = new ISOException("Invalid terminal type code: " + Integer.parseInt("0"+isoMsg.getString(25).trim()));
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity( Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        }
        
        return ifx;

    }

}
