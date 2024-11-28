package vaulsys.protocols.saderat87;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.customer.Currency;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOtoIfxMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class Saderat87ISOToIFXMapper extends ISOtoIfxMapper{
	public static final Saderat87ISOToIFXMapper Instance = new Saderat87ISOToIFXMapper();
	
	protected Saderat87ISOToIFXMapper(){}
	
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
		try {
			mti = isoMsg.getMTI(); //Integer.parseInt(isoMsg.getMTI()); //Raza MasterCard commenting
		} catch (NumberFormatException e) {
			ISOException isoe = new ISOException("Invalid MTI", e);
			ifx.setSeverity( Severity.ERROR);
			ifx.setStatusDesc ( isoe.getClass().getSimpleName() + ": "+ isoe.getMessage());
		}
        ifx.setAppPAN(isoMsg.getString(2));//check

		String str_fld3 = isoMsg.getString(3);//check
		//Integer emvTrnType = null; //Raza MasterCard commenting
		String emvTrnType = null;
		if (str_fld3 != null && str_fld3.length() == 6) {
			try {
				emvTrnType = str_fld3.substring(0, 2).trim() ; //Integer.parseInt(str_fld3.substring(0, 2).trim()); //Raza MasterCard commenting
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
			String acquire_currency = isoMsg.getString(49);//check

			Currency currency = null;
			
			if (Util.hasText(acquire_currency)) {
				currency = ProcessContext.get().getCurrency(Integer.parseInt(acquire_currency));//GlobalContext.getInstance().getCurrency(Integer.parseInt(acquire_currency));
				if (currency == null){
					throw new ISOException("Invalid Currency Code: "+ acquire_currency);
				}
			}
			else{
				currency = ProcessContext.get().getRialCurrency();//GlobalContext.getInstance().getRialCurrency();
			}
			ifx.setAuth_Currency(currency.getCode());
			ifx.setAuth_CurRate("1");
			ifx.setAuth_Amt(Util.longValueOf(isoMsg.getString(4).trim()));//check
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
			if (Util.hasText(issuer_currency)) {
				currency = ProcessContext.get().getCurrency(Integer.parseInt(issuer_currency));//GlobalContext.getInstance().getCurrency(Integer.parseInt(issuer_currency));
				ifx.setSec_Currency(currency.getCode());
				if (currency == null){
					throw new ISOException("Invalid Currency Code: " + issuer_currency);
				}
			} else{
				ifx.setSec_Currency(ifx.getAuth_Currency());
			}

			ifx.setSec_CurRate(isoMsg.getString(10).trim());

			String sec_amt = isoMsg.getString(6).trim();//check
			if (Util.hasText(sec_amt)) {
				ifx.setSec_Amt(Long.parseLong(sec_amt));
			} else
				ifx.setSec_Amt(ifx.getAuth_Amt());

		} catch (Exception e) {
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
		
		if (!isoMsg.getString(7).equals(""))//check
            ifx.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", isoMsg.getString(7).trim())));

		/*** check this ***/
        ifx.setSrc_TrnSeqCntr( ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));//check
        ifx.setMy_TrnSeqCntr( ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));

        String localTime = isoMsg.getString(12).trim();//check
        String localDate = isoMsg.getString(13).trim();//check
        try {
			ifx.setOrigDt( new DateTime(MyDateFormatNew.parse("MMddHHmmss", localDate + localTime)));
		} catch (Exception e) {
			ISOException isoe = new ISOException("Unparsable Original Date.", e);
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}
		
		
		try {
            
			String expDate = isoMsg.getString(14);//saderat nemikhad vali mizarim bashe
			if(expDate != null && !expDate.equals("")){
				expDate = expDate.trim();
				ifx.setExpDt( Long.parseLong(expDate) );
			}
		}catch (Exception e) {
			logger.info("Exception in setting ExpDate(14)!");
		}
		try{
            String settleDate = isoMsg.getString(15).trim();//check
            if (Util.hasText(settleDate))
            	ifx.setSettleDt(new MonthDayDate(MyDateFormatNew.parse("MMdd", settleDate)));
		}catch (Exception e) {
			logger.info("Exception in setting settleDate(15)!");
		}
            
		try{
            String postedDate = isoMsg.getString(17).trim();//check
            if (Util.hasText(postedDate))
            	ifx.setPostedDt(new MonthDayDate(MyDateFormatNew.parse("MMdd", postedDate)));

        } catch (Exception e) {
        }
        
		mapTerminalType(ifx, isoMsg.getString(25));//baraie saderat faghat pos mitone bashe
		
//        ifx.setBankId(Long.valueOf(isoMsg.getString(32).trim()));//check
		ifx.setBankId(ProcessContext.get().getMyInstitution().getBin().toString());
//		ifx.setBankId(502229L);
        ifx.setFwdBankId(isoMsg.getString(33).trim());//check
        ifx.setDestBankId(isoMsg.getString(33).trim());

        ifx.setTrk2EquivData(isoMsg.getString(35));//saderat nemikhad
        
        ifx.setApprovalCode(isoMsg.getString(38).trim());//ino nemikhad saderat
        ifx.setRsCode(mapError(isoMsg.getString(39).trim()));//check

        mapField44(ifx, isoMsg.getString(44), convertor);//saderat ino nemikhad
		
        ifx.setPINBlock ( isoMsg.getString(52).trim());

        String P54 = isoMsg.getString(54);//check
        
        if (ISOResponseCodes.APPROVED.equals(ifx.getRsCode()) ||
        		ISOResponseCodes.INVALID_ACCOUNT.equals(ifx.getRsCode())) {
			while (P54 != null && P54.length() >= 20) {
				AcctBal acctBal = new AcctBal();

				Integer acctType = Integer.parseInt(P54.substring(0, 2));

				switch (acctType) {
				case 0:
					acctBal.setAcctType(AccType.TARJIHI);
					break;
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
					if(!ISOFinalMessageType.isTransferMessage(ifx.getIfxType()))
						ifx.setTransientAcctBalLedger(acctBal);
					break;
				case 2:
					acctBal.setBalType(BalType.AVAIL);
					if(ISOFinalMessageType.isTransferMessage(ifx.getIfxType()))
						ifx.setAcctBalAvailable(acctBal);
					if(! ISOFinalMessageType.isTransferMessage(ifx.getIfxType()))
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

        String P64 = isoMsg.getString(64).trim();//ino saderat nemikhad
        String S128 = isoMsg.getString(128).trim();//check
        if (P64 != null && P64.length() > 0)
            ifx.setMsgAuthCode ( P64);
        else if (S128 != null && S128.length() > 0)
            ifx.setMsgAuthCode ( S128);

        ifx.setRecvBankId(isoMsg.getString(100));//ino saderat nemikhad

        mapIfxType(ifx, mti, emvTrnType);
        
        if (ifx.getIfxType()!= null && !ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
        		&& ifx.getTerminalType() ==null ){
        	ISOException isoe = new ISOException("Invalid terminal type code: " + Integer.parseInt("0"+isoMsg.getString(25).trim()));
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity ( Severity.ERROR);
				ifx.setStatusDesc ( isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        }
        
        mapFieldANFix(ifx, isoMsg, 37);
        mapFieldANFix(ifx, isoMsg, 41);
        mapFieldANFix(ifx, isoMsg, 42);
        
        String field48 = "";
        if (isoMsg.hasField(48)) {//check
        	field48 = new String((byte[])isoMsg.getValue(48));
        	mapField48(ifx, field48, convertor);
        }
        //TODO: fielde 103:shomare hesabe dovom va 121:etelaate ezafie ekhtesasi saderat mikhad ma to iso nadarim.
        
        return ifx;
    }
	
	@Override
	public void mapField48(Ifx ifx, String f_48, EncodingConvertor convertor) {
		
	}

}
