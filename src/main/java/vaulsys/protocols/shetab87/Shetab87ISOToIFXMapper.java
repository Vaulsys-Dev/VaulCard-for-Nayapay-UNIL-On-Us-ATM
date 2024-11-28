package vaulsys.protocols.shetab87;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.base.ClearingDate;
import vaulsys.clearing.base.ClearingDateManager;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.customer.Currency;
import vaulsys.entity.impl.Institution;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
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
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.text.ParseException;

import org.apache.log4j.Logger;

public class Shetab87ISOToIFXMapper extends ISOtoIfxMapper {

	public static final Shetab87ISOToIFXMapper Instance = new Shetab87ISOToIFXMapper();
	
	protected Shetab87ISOToIFXMapper(){}
	
	Logger logger = Logger.getLogger(this.getClass());
	
	@Override
    public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {
		
		ISOMsg isoMsg = (ISOMsg) message;
        Ifx ifx = new Ifx();

		//System.out.println("Setting Institution of Channel [" + ProcessContext.get().getInputMessage().getChannel().getName() + "]"); //Raza TEMP
		ifx.setInstitutionId(ProcessContext.get().getInputMessage().getChannel().getInstitutionId()); //Raza Set Institution in IFX from channel

        //Integer mti = null; //Raza MasterCard commenting
		String mti = null;
		try {
			mti = isoMsg.getMTI(); //Integer.parseInt(isoMsg.getMTI()); //Raza MasterCard commenting
		} catch (NumberFormatException e) {
			ISOException isoe = new ISOException("Invalid MTI", e);
			ifx.setSeverity( Severity.ERROR);
			ifx.setStatusDesc ( isoe.getClass().getSimpleName() + ": "+ isoe.getMessage());
		}
        ifx.setAppPAN(isoMsg.getString(2));

		String str_fld3 = isoMsg.getString(3);
		//Integer emvTrnType = null; //Raza MasterCard commenting
		String emvTrnType = null;
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

			ifx.setAccTypeFrom(AccType.MAIN_ACCOUNT);
			ifx.setAccTypeTo(AccType.MAIN_ACCOUNT);

			mapTrnType(ifx, emvTrnType);
		}

        try {
			String acquire_currency = isoMsg.getString(49);

			Currency currency = null;
			
			if (Util.hasText(acquire_currency)) {
				currency = ProcessContext.get().getCurrency(Integer.parseInt(acquire_currency));
				if (currency == null){
					throw new ISOException("Invalid Currency Code: "+ acquire_currency);
				}
			}
			else{
				currency = ProcessContext.get().getRialCurrency();
			}
			ifx.setAuth_Currency(currency.getCode());
			ifx.setAuth_CurRate("1");
			
			if (isoMsg.isRequest()) {
				ifx.setTrx_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
			}
			
			ifx.setAuth_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
			ifx.setReal_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
			
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
			if (Util.hasText(issuer_currency)) {
				currency = ProcessContext.get().getCurrency(Integer.parseInt(issuer_currency));
				ifx.setSec_Currency(currency.getCode());
				if (currency == null){
					throw new ISOException("Invalid Currency Code: " + issuer_currency);
				}
			} else{
				ifx.setSec_Currency(ifx.getAuth_Currency());
			}

			ifx.setSec_CurRate(isoMsg.getString(10).trim());

			String sec_amt = isoMsg.getString(6).trim();
			if (Util.hasText(sec_amt)) {
				ifx.setSec_Amt(Util.longValueOf(sec_amt));
			} else
				ifx.setSec_Amt(Util.longValueOf(isoMsg.getString(4).trim()));

		} catch (Exception e) {
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
		
		if (!isoMsg.getString(7).equals(""))
            ifx.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", isoMsg.getString(7).trim())));

		//m.rehman: removing ISOUtil.zeroUnPad to match field 11 on response
        //ifx.setSrc_TrnSeqCntr( ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));
        //ifx.setMy_TrnSeqCntr( ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));
		ifx.setSrc_TrnSeqCntr(isoMsg.getString(11).trim());
		ifx.setMy_TrnSeqCntr(isoMsg.getString(11).trim());

        String localTime = isoMsg.getString(12).trim();
        String localDate = isoMsg.getString(13).trim();
        DateTime now = DateTime.now();
        
        try {
        	
        	DateTime d= new DateTime(MyDateFormatNew.parse("MMddHHmmss", localDate + localTime));
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
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}
		
		
		try {
            
			String expDate = isoMsg.getString(14);
			if(expDate != null && !expDate.equals("")){
				expDate = expDate.trim();
				ifx.setExpDt( Long.parseLong(expDate) );
			}
		}catch (Exception e) {
			logger.info("Exception in setting ExpDate(14)!");
		}
		String settleDate = isoMsg.getString(15).trim();
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
			logger.info("Exception in setting settleDate(15)!");
		}
            
		String postedDate = isoMsg.getString(17).trim();
		if(isoMsg.isRequest()){
			MonthDayDate transactionDate = new MonthDayDate(MyDateFormatNew.parse("MMdd", (String) postedDate));
			Institution institution = ProcessContext.get().getInstitution("9000") ;
//			FinancialEntityService.findEntity(Institution.class, 9000L);
			ClearingDate currentWorkingDay = institution.getCurrentWorkingDay();
			
			DayDate realCurrentWorkingDay = new DayDate(MyDateFormatNew.parse("yyyyMMdd", DayDate.now().getYear()+postedDate));
			
			if(currentWorkingDay.getDate().before(transactionDate) && DayDate.now().getDate().equals(realCurrentWorkingDay.getDate())){
				ClearingDateManager.getInstance().push(transactionDate, DateTime.now(), true, institution);
			}
			
		}
		
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
        	logger.info("Exception in setting PostedDate(17)");
        }
        
		String f_25 = isoMsg.getString(25);
		mapTerminalType(ifx, f_25);
		
        ifx.setBankId(isoMsg.getString(32).trim());
        ifx.setFwdBankId(isoMsg.getString(33).trim());
		ifx.setDestBankId(ifx.getAppPAN().substring(0,6));
		//ifx.setDestBankId(Long.valueOf(isoMsg.getString(33).trim())); //Raza commenting

        ifx.setTrk2EquivData(isoMsg.getString(35));

        ifx.setApprovalCode(isoMsg.getString(38).trim());
        ifx.setRsCode(mapError(isoMsg.getString(39).trim()));

//        String P43 = isoMsg.getString(43);

        mapField44(ifx, isoMsg.getString(44), convertor);
		
        ifx.setPINBlock ( isoMsg.getString(52).trim());

        String P54 = isoMsg.getString(54);
        
        if (ISOResponseCodes.APPROVED.equals(ifx.getRsCode()) ||
        		ISOResponseCodes.INVALID_ACCOUNT.equals(ifx.getRsCode())) {
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
					ISOException isoe = new ISOException("Bad Format: Amount Type [field 54]", e);
					if (!Util.hasText(ifx.getStatusDesc())) {
						ifx.setSeverity(Severity.ERROR);
						ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
					}
					logger.error(isoe.getClass().getSimpleName() + ": "	+ isoe.getMessage());
				}

				switch (amtType) {
				case 1:
					acctBal.setBalType(BalType.LEDGER);
					if(ISOFinalMessageType.isTransferMessage(ifx.getIfxType()))
						ifx.setAcctBalLedger(acctBal);
					else if(!ISOFinalMessageType.isTransferMessage(ifx.getIfxType()))
						ifx.setTransientAcctBalLedger(acctBal);
					break;
				case 2:
					acctBal.setBalType(BalType.AVAIL);
					if(ISOFinalMessageType.isTransferMessage(ifx.getIfxType()))
						ifx.setAcctBalAvailable(acctBal);
					else if(!ISOFinalMessageType.isTransferMessage(ifx.getIfxType()))
						ifx.setTransientAcctBalAvailable(acctBal);
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
        
        mapIfxType(ifx, mti, emvTrnType);
        
        mapFieldANFix(ifx, isoMsg, 37);
        mapFieldANFix(ifx, isoMsg, 41);
        mapFieldANFix(ifx, isoMsg, 42);
        
        if (!Util.hasText(f_25.trim()) && ifx.getTerminalId() != null) {
			if (ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId()) &&
					FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole()) &&
					ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
				TerminalType terminalType = GlobalContext.getInstance().getTerminalType(ifx.getTerminalId());
				if (ifx.getTerminalId() != null && terminalType != null && TerminalType.isPhisycalDeviceTerminal(terminalType)){
					ifx.setTerminalType(terminalType);
				}
			}
		}

        String P64 = isoMsg.getString(64).trim();
        String S128 = isoMsg.getString(128).trim();
        if (P64 != null && P64.length() > 0)
            ifx.setMsgAuthCode(P64);
        else if (S128 != null && S128.length() > 0)
            ifx.setMsgAuthCode(S128);

        String S90 = isoMsg.getString(90);
        if (S90 != null && S90.length() >= 20) {
            ifx.setOriginalDataElements(new MessageReferenceData());
            ifx.getSafeOriginalDataElements().setTrnSeqCounter( ISOUtil.zeroUnPad(S90.substring(4, 10)));
            
            String msgType = S90.substring(0, 4);
            if (Integer.parseInt(msgType) != 0)
                ifx.getSafeOriginalDataElements().setMessageType ( msgType);
            else{
            	ISOException isoe = new ISOException("Invalid Format( F_90: "+
    					" OriginalData.msgType= NULL, OriginalData.TrnSeqCounter = "+ ifx.getSafeOriginalDataElements().getTrnSeqCounter()+", temrinalId= "+ ifx.getTerminalId() +")");
            	if (!Util.hasText(ifx.getStatusDesc())) {
   					ifx.setSeverity(Severity.ERROR);
   					ifx.setStatusDesc(isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
   				}
               logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
            }
            
            String origDt = S90.substring(10, 20);
            if (Integer.parseInt(origDt) != 0) {
                try {
                    ifx.getSafeOriginalDataElements().setOrigDt(new DateTime( MyDateFormatNew.parse("MMddHHmmss", origDt)));
                } catch (ParseException e) {
                	ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.origDt= NULL, OriginalData.TrnSeqCounter = "+
        					ifx.getSafeOriginalDataElements().getTrnSeqCounter() 
        					+", temrinalId= "+ ifx.getTerminalId() +")");
                    if (!Util.hasText(ifx.getStatusDesc())) {
       					ifx.setSeverity(Severity.ERROR);
       					ifx.setStatusDesc(isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
       				}
                   logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
                }
            }
            
            String bankId = S90.substring(20, 31).trim();
            if (Integer.parseInt(bankId) != 0)
        		ifx.getSafeOriginalDataElements().setBankId (bankId);
        	    else {
        	    	ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.bankId= NULL, OriginalData.TrnSeqCounter = "+
        					ifx.getSafeOriginalDataElements().getTrnSeqCounter() 
        					+", temrinalId= "+ ifx.getTerminalId() +", OriginalData.origDt= "+ ifx.getSafeOriginalDataElements().getOrigDt()+ ")");
        	    	if (!Util.hasText(ifx.getStatusDesc())) {
       					ifx.setSeverity(Severity.ERROR);
       					ifx.setStatusDesc(isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
       				}
                   logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        	    }

            String fwdBankId = S90.substring(31);
    	    if (Integer.parseInt(fwdBankId) != 0)
    			ifx.getSafeOriginalDataElements().setFwdBankId (fwdBankId);
    		    else{
    		    	ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.FwdBankId = NULL, OriginalData.TrnSeqCounter = "+
    						ifx.getSafeOriginalDataElements().getTrnSeqCounter() 
    						+", OriginalData.temrinalId= "+ ifx.getTerminalId() +", OriginalData.origDt= "+ ifx.getSafeOriginalDataElements().getOrigDt()+
    						", OriginalData.bankId ="+ ifx.getSafeOriginalDataElements().getBankId() +")" );
    		    	if (!Util.hasText(ifx.getStatusDesc())) {
       					ifx.setSeverity(Severity.ERROR);
       					ifx.setStatusDesc(isoe.getClass().getSimpleName()+ ": " + isoe.getMessage());
       				}
                   logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
    		    }
    	    
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

        
        if (ifx.getIfxType()!= null && !ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
        		&& ifx.getTerminalType() ==null ){
        	ISOException isoe = new ISOException("Invalid terminal type code: " + Integer.parseInt("0"+f_25.trim()));
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        }
        
        if (ifx.getIfxType() != null && ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType())){
        	ifx.setMy_TrnSeqCntr(Util.generateTrnSeqCntr(6));
        }
        
        
        String field48 = "";
        if (isoMsg.hasField(48)) {
        	field48 = new String((byte[])isoMsg.getValue(48));
        	mapField48(ifx, field48, convertor);
        }

        return ifx;
    }

}
