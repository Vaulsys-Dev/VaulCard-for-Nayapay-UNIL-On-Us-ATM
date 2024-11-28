package vaulsys.protocols.PaymentSchemes.MasterCard;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.base.ClearingDate;
import vaulsys.clearing.base.ClearingDateManager;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.customer.Currency;
import vaulsys.entity.impl.Institution;
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
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.text.ParseException;

import org.apache.log4j.Logger;

public class MasterCard87ISOToIFXMapper extends ISOtoIfxMapper {

	public static final MasterCard87ISOToIFXMapper Instance = new MasterCard87ISOToIFXMapper();
	
	protected MasterCard87ISOToIFXMapper(){}
	
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
			ifx.setMti(mti); //Raza setting MTI
			if (Util.hasText(mti)) {
				logger.debug("ISOtoIFX:: MTI [" + mti + "]"); //Raza LOGGING ENHANCED
			}
		} catch (NumberFormatException e) {
			ISOException isoe = new ISOException("Invalid MTI", e);
			ifx.setSeverity(Severity.ERROR);
			ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}
		String str_fld2 = isoMsg.getString(2);
		ifx.setAppPAN(str_fld2);
		if (Util.hasText(str_fld2)) {
			logger.debug("ISOtoIFX::DE-2 PAN [" + str_fld2 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld3 = isoMsg.getString(3).trim(); //Raza using trim value padded with space
		//Integer emvTrnType = null;
		logger.debug("ISOtoIFX::DE-3 ProcCode [" + str_fld3 + "]"); //Raza LOGGING ENHANCED
		String emvTrnType = null;
		if (str_fld3 != null && str_fld3.length() == 6) {
			try {
				emvTrnType = str_fld3.substring(0, 2).trim();
				ifx.setAccTypeFrom(AccType.mapAcctType(str_fld3.substring(2, 4))); //Raza map as per received value
				ifx.setAccTypeTo(AccType.mapAcctType(str_fld3.substring(4, 6))); //Raza map as per received value
			} catch (NumberFormatException e) {
				ISOException isoe = new ISOException("Invalid Process Code: " + str_fld3, e);
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
				}
				logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			//ifx.setAccTypeFrom(AccType.MAIN_ACCOUNT); //Raza MasterCard Commenting
			//ifx.setAccTypeTo(AccType.MAIN_ACCOUNT); //Raza MasterCard commenting
			mapTrnType(ifx, emvTrnType);
		}
		else if(str_fld3 != null && str_fld3.length() == 4)
		{
			try {
				emvTrnType = str_fld3.substring(0, 2).trim();
				ifx.setAccTypeFrom(AccType.mapAcctType(str_fld3.substring(2, 4))); //Raza map as per received value
				ifx.setAccTypeTo(AccType.mapAcctType(str_fld3.substring(2, 4))); //Raza map as per received value
			} catch (NumberFormatException e) {
				ISOException isoe = new ISOException("Invalid Process Code: " + str_fld3, e);
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
				}
				logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			//ifx.setAccTypeFrom(AccType.MAIN_ACCOUNT); //Raza MasterCard Commenting
			//ifx.setAccTypeTo(AccType.MAIN_ACCOUNT); //Raza MasterCard commenting
			mapTrnType(ifx, emvTrnType);
		}
		else
		{
			ISOException isoe = new ISOException("Invalid Process Code: " + str_fld3);
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}

		try {
			String str_fld4 = isoMsg.getString(4).trim();
			if (isoMsg.isRequest()) {
				ifx.setTrx_Amt(Util.longValueOf(str_fld4));
			}
			ifx.setAuth_Amt(Util.longValueOf(str_fld4));
			ifx.setReal_Amt(Util.longValueOf(str_fld4));
			logger.debug("ISOtoIFX::DE-4 Amount-Tran [" + str_fld4 + "]"); //Raza LOGGING ENHANCED
		} catch (Exception e) {
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(e.getClass().getSimpleName() + ": "
						+ e.getMessage());
			}
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		String str_fld5 = isoMsg.getString(5).trim();
		if (Util.hasText(str_fld5)) {
			ifx.setSett_Amt(Util.longValueOf(str_fld5));
			logger.debug("ISOtoIFX::DE-5 Amount-Settlement [" + str_fld5 + "]"); //Raza LOGGING ENHANCED
		}

		try {
			String sec_amt = isoMsg.getString(6).trim();
			if (Util.hasText(sec_amt)) {
				ifx.setSec_Amt(Util.longValueOf(sec_amt));
				logger.debug("ISOtoIFX::DE-6 Amount-CardHolderBilling [" + sec_amt + "]"); //Raza LOGGING ENHANCED
			} else
				ifx.setSec_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
		} catch (Exception e) {
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		String str_fld7 = isoMsg.getString(7);
		if (!str_fld7.equals("")) {
			ifx.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", str_fld7.trim())));
			logger.debug("ISOtoIFX::DE-7 Transmission Date & Time [" + str_fld7 + "]"); //Raza LOGGING ENHANCED
		}

		/*String str_fld8 = isoMsg.getString(8);
		if(Util.hasText(str_fld8)) {
			logger.debug("ISOtoIFX::DE-8 Amount-CardHolderBilling Fee [" + str_fld8 + "]"); //Raza LOGGING ENHANCED
		}*/

		String str_fld9 = isoMsg.getString(9);
		if (Util.hasText(str_fld9)) {
			ifx.setConvRate_Sett(str_fld9);
			logger.debug("ISOtoIFX::DE-9 Conversion Rate Settlement [" + str_fld9 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld10 = isoMsg.getString(10).trim();
		if (Util.hasText(str_fld10)) {
			ifx.setSec_CurRate(str_fld10);
			logger.debug("ISOtoIFX::DE-10 Conversion Rate Settlement [" + str_fld10 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld11 = isoMsg.getString(11);
		if(Util.hasText(str_fld11)) { //Raza check for exception handling for trim if STAN is null
			ifx.setSrc_TrnSeqCntr(str_fld11.trim());
			ifx.setMy_TrnSeqCntr(str_fld11.trim());
			logger.debug("ISOtoIFX::DE-11 STAN [" + str_fld11 + "]"); //Raza LOGGING ENHANCED
		}

		//ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(str_fld11.trim()));
		//ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(str_fld11.trim()));

		String localTime = isoMsg.getString(12).trim();
		if (Util.hasText(localTime)) {
			ifx.setTimeLocalTran(localTime);
			logger.debug("ISOtoIFX::DE-12 Time Loc Tran [" + localTime + "]"); //Raza LOGGING ENHANCED
		}

		String localDate = isoMsg.getString(13).trim();
		if (Util.hasText(localDate)) {
			ifx.setDateLocalTran(localDate);
			logger.debug("ISOtoIFX::DE-13 Date Loc Tran [" + localDate + "]"); //Raza LOGGING ENHANCED
		}
		DateTime now = DateTime.now();

		try {
			DateTime d = new DateTime(MyDateFormatNew.parse("MMddHHmmss", localDate + localTime));
			if (d != null && ProcessContext.get().getMyInstitution().getBin().equals(Long.valueOf(isoMsg.getString(32).trim()))
					&& FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())) {

				if (d.getDayDate().getMonth() == 12 && now.getDayDate().getMonth() == 1) {
					logger.debug("set origDt year to parsal!");
					d.getDayDate().setYear(now.getDayDate().getYear() - 1);

				} else if (d.getDayDate().getMonth() == 1 && now.getDayDate().getMonth() == 12) {
					logger.debug("set origDt year to sale dige!");
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
			logger.debug("ISOtoIFX::DE-14 Date Expiration [" + expDate + "]"); //Raza LOGGING ENHANCED
			if (expDate != null && !expDate.equals("")) {
				expDate = expDate.trim();
				ifx.setExpDt(Long.parseLong(expDate));
			}
		} catch (Exception e) {
			logger.debug("Exception in setting ExpDate(14)!");
		}

		String settleDate = isoMsg.getString(15);
		if(Util.hasText(settleDate))
		{
		settleDate = settleDate.trim();
		logger.debug("ISOtoIFX::DE-15 Date Settlement [" + settleDate + "]"); //Raza LOGGING ENHANCED
		try {
			MonthDayDate d = new MonthDayDate(MyDateFormatNew.parse("MMdd", settleDate));
			if (Util.hasText(settleDate))
				ifx.setSettleDt(d);

			if (d != null && d.getMonth() == 1 && DateTime.now().getDayDate().getMonth() == 12) {
				d.setYear(DateTime.now().getDayDate().getYear() + 1);
				ifx.setSettleDt(d);
			} else if (d != null && d.getMonth() == 12 && DateTime.now().getDayDate().getMonth() == 1) {
				d.setYear(DateTime.now().getDayDate().getYear() - 1);
				ifx.setSettleDt(d);
			}

		} catch (Exception e) {
			logger.debug("Exception in setting settleDate(15)!");
		}
		}
		String str_fld16 = isoMsg.getString(16);
		if(Util.hasText(str_fld16)) //Raza MasterCard for DE-16
		{
			ifx.setSec_CurDate(str_fld16);
			logger.debug("ISOtoIFX::DE-16 Date Conversion [" + str_fld16 + "]"); //Raza LOGGING ENHANCED
		}

		String postedDate,str_fld17 = isoMsg.getString(17);
		if(Util.hasText(str_fld17)) {
			postedDate = str_fld17.trim();
			if(isoMsg.isRequest()){
				MonthDayDate transactionDate = new MonthDayDate(MyDateFormatNew.parse("MMdd", (String) postedDate));
				Institution institution = ProcessContext.get().getInstitution("9012") ; //Raza MasterCard verify this
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
				logger.debug("Exception in setting PostedDate(17)");
			}
			logger.debug("ISOtoIFX::DE-17 Date Capture [" + str_fld17 + "]"); //Raza LOGGING ENHANCED
		}
		else
		{
			postedDate = isoMsg.getString(13); //Raza For MasterCard using Field-13 Date Local Txn (MMDD)
			logger.debug("ISOtoIFX::DE-17 Date Capture [" + postedDate + "]"); //Raza LOGGING ENHANCED
			if(isoMsg.isRequest()){
				MonthDayDate transactionDate = new MonthDayDate(MyDateFormatNew.parse("MMdd", (String) postedDate));
				Institution institution = ProcessContext.get().getInstitution("9012") ; //Raza MasterCard verify this
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
				logger.debug("Exception in setting PostedDate(17)");
			}
		}

		String str_fld18 = isoMsg.getString(18);
		if(Util.hasText(str_fld18)) {
			ifx.setMerchantType(str_fld18);
			logger.debug("ISOtoIFX::DE-18 Merchant Type [" + str_fld18 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld20 = isoMsg.getString(20);
		if(Util.hasText(str_fld20)) {
			ifx.setPanCountryCode(str_fld20);
			logger.debug("ISOtoIFX::DE-20 PAN Country Code [" + str_fld20 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld22 = isoMsg.getString(22);
		if(Util.hasText(str_fld22)) {
			ifx.setPosEntryModeCode(str_fld22);
			logger.debug("ISOtoIFX::DE-22 POS Entry Mode [" + str_fld22 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld23 = isoMsg.getString(23);
		if(Util.hasText(str_fld23)) {
			ifx.setCardSequenceNo(str_fld23);
			logger.debug("ISOtoIFX::DE-23 Card Seq No [" + str_fld23 + "]"); //Raza LOGGING ENHANCED
		}

		/*String str_fld25 = isoMsg.getString(25); //MasterCard does not use this - use Field 61 Instead
		if(Util.hasText(str_fld25)) {
			logger.debug("ISOtoIFX::DE-25 POS Condition Code [" + str_fld25 + "]"); //Raza LOGGING ENHANCED
			mapTerminalType(ifx, str_fld25);
			ifx.setPosConditionCode(isoMsg.getString(25));
		}*/

		String str_fld26 = isoMsg.getString(26);
		if(Util.hasText(str_fld26)) {
			ifx.setPosPinCaptureCode(str_fld26);
			logger.debug("ISOtoIFX::DE-26 POS Pin Capt Code [" + str_fld26 + "]"); //Raza LOGGING ENHANCED
		}

		/*String str_fld27 = isoMsg.getString(27); //Raza MasterCard does not use this
		if(Util.hasText(str_fld27))
			logger.debug("ISOtoIFX::DE-27 Auth ID Respose Length [" + str_fld27 + "]"); //Raza LOGGING ENHANCED*/

		String str_fld28 = isoMsg.getString(28);
		if(Util.hasText(str_fld28)) {
			ifx.setAmountTranFee(str_fld28);
			logger.debug("ISOtoIFX::DE-28 Amount Tran Fee [" + str_fld28 + "]"); //Raza LOGGING ENHANCED
		}

		/*String str_fld29 = isoMsg.getString(29); //Raza MasterCard does not use this
		if(Util.hasText(str_fld29))
			logger.debug("ISOtoIFX::DE-29 Amount Settlement Fee [" + str_fld29 + "]"); //Raza LOGGING ENHANCED*/

		/*String str_fld30 = isoMsg.getString(30); //Raza MasterCard does not use this
		if(Util.hasText(str_fld30))
			logger.debug("ISOtoIFX::DE-30 Amount Tran Processing Fee [" + str_fld30 + "]"); //Raza LOGGING ENHANCED*/

		/*String str_fld31 = isoMsg.getString(31); //Raza MasterCard does not use this
		if(Util.hasText(str_fld31))
			logger.debug("ISOtoIFX::DE-31 Amount Settlement Processing Fee [" + str_fld31 + "]"); //Raza LOGGING ENHANCED*/

		String str_fld32 = isoMsg.getString(32);
		if(Util.hasText(str_fld32)) {
			logger.debug("ISOtoIFX::DE-32 Acquiring Inst Id Code [" + str_fld32 + "]"); //Raza LOGGING ENHANCED
			ifx.setBankId(str_fld32);
		}
		//ifx.setBankId(Long.valueOf(str_fld32));

		String str_fld33 = isoMsg.getString(33);
		if(Util.hasText(str_fld33)) {
			logger.debug("ISOtoIFX::DE-33 Forwarding Inst Id Code [" + str_fld33 + "]"); //Raza LOGGING ENHANCED
			ifx.setFwdBankId(str_fld33);
			//ifx.setDestBankId(Long.valueOf(isoMsg.getString(33).trim()));
		}
		ifx.setDestBankId(isoMsg.getString(2).substring(0,6)); //Raza For Routing (Get Routing from IMD)

		/*String str_fld34 = isoMsg.getString(34); //Raza MasterCard does not use this
		if(Util.hasText(str_fld34))
			logger.debug("ISOtoIFX::DE-34 PAN Extended [" + str_fld34 + "]"); //Raza LOGGING ENHANCED*/

		String str_fld35 = isoMsg.getString(35);
		if(Util.hasText(str_fld35)) {
			ifx.setTrk2EquivData(str_fld35);
			logger.debug("ISOtoIFX::DE-35 Track 2 Data [" + str_fld35 + "]"); //Raza LOGGING ENHANCED
		}

		/*String str_fld36 = isoMsg.getString(36); //Raza Verify this MasterCard does not use this
		if(Util.hasText(str_fld36)) {
			logger.debug("ISOtoIFX::DE-36 Track 3 Data [" + str_fld36 + "]"); //Raza LOGGING ENHANCED
		}*/

		mapFieldANFix(ifx, isoMsg, 37);
		//logger.debug("ISOtoIFX::DE-37 RRN [" + isoMsg.getString(37) + "]"); //Raza LOGGING ENHANCED

		String str_fld38 = isoMsg.getString(38).trim();
		if(Util.hasText(str_fld38)) {
			ifx.setApprovalCode(isoMsg.getString(38).trim());
			logger.debug("ISOtoIFX::DE-38 Auth Id Response [" + str_fld38 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld39 = isoMsg.getString(39).trim();
		if(Util.hasText(str_fld39)) {
			//ifx.setRsCode(mapError(isoMsg.getString(39).trim())); //Raza commenting
			ifx.setRsCode(isoMsg.getString(39).trim());
			logger.debug("ISOtoIFX::DE-39 Response Code [" + str_fld39 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld41 = isoMsg.getString(41);
		if(Util.hasText(str_fld41)) {
			logger.debug("ISOtoIFX::DE-41 Card Acceptor Terminal Id [" + str_fld41 + "]"); //Raza LOGGING ENHANCED
			ifx.setTerminalId(str_fld41); //Raza Verify This
		}

		String str_fld42 = isoMsg.getString(42);
		if(Util.hasText(str_fld42)) {
			ifx.setOrgIdNum(str_fld42);
			logger.debug("ISOtoIFX::DE-42 Card Acceptor Identification Code [" + str_fld42 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld43 = isoMsg.getString(43);
		if(Util.hasText(str_fld43)) {
			ifx.setCardAcceptNameLoc(str_fld43);
			logger.debug("ISOtoIFX::DE-43 Card Acceptor Name Location [" + str_fld43 + "]"); //Raza LOGGING ENHANCED
		}

		//mapField44(ifx, isoMsg.getString(44), convertor); //Raza MASTERCARD commenting
		String str_fld44 = isoMsg.getString(44);
		if(Util.hasText(str_fld44)) {
			ifx.setAddResponseData(str_fld44);
			logger.debug("ISOtoIFX::DE-44 Additional Response Data [" + str_fld44 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld45 = isoMsg.getString(45);
		if(Util.hasText(str_fld45)) {
			ifx.setTrack1Data(str_fld45);
			logger.debug("ISOtoIFX::DE-45 Track 1 Data [" + str_fld45 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld48 = isoMsg.getString(48);;
		if(Util.hasText(str_fld48)) {
			ifx.setAddDataPrivate(str_fld48);
			logger.debug("ISOtoIFX::DE-48 Additional Data [" + str_fld48 + "]"); //Raza LOGGING ENHANCED
		}
		//if (isoMsg.hasField(48)) {
			//field48 = new String((byte[])isoMsg.getValue(48)); //Raza MasterCard commenting
			//str_fld48 = isoMsg.getString(48); //new String((byte[])isoMsg.getValue(48));
			//mapField48(ifx, field48, convertor); //Raza MasterCard commenting
		//}
		try
		{
			String acquire_currency = isoMsg.getString(49);
			Currency currency = null;

			if (Util.hasText(acquire_currency)) {
				logger.debug("ISOtoIFX::DE-49 Currency Code Tran [" + acquire_currency + "]"); //Raza LOGGING ENHANCED
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

		} catch (Exception e) {
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(e.getClass().getSimpleName() + ": "
						+ e.getMessage());
			}
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		String str_fld50 = isoMsg.getString(50);
		Currency currency = null;
		if(Util.hasText(str_fld50)) {
			try {
					currency = ProcessContext.get().getCurrency(Integer.parseInt(str_fld50));
					if (currency == null) {
						throw new ISOException("Invalid Currency Code: " + str_fld50);
					} else {
						ifx.setSett_Currency(str_fld50);
				}
			} catch (Exception e) {
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			logger.debug("ISOtoIFX::DE-50 Currency Code Settlement [" + str_fld50 + "]"); //Raza LOGGING ENHANCED
		}

        try {
			String issuer_currency = isoMsg.getString(51).trim();
			currency = null;
			if (Util.hasText(issuer_currency)) {
				logger.debug("ISOtoIFX::DE-51 Currency Code Card Holder Billing [" + issuer_currency + "]"); //Raza LOGGING ENHANCED
				currency = ProcessContext.get().getCurrency(Integer.parseInt(issuer_currency));
				ifx.setSec_Currency(currency.getCode());
				if (currency == null){
					throw new ISOException("Invalid Currency Code: " + issuer_currency);
				}
			} else{
				ifx.setSec_Currency(ifx.getAuth_Currency());
			}
			/*String sec_amt = isoMsg.getString(6).trim();
			if (Util.hasText(sec_amt)) {
				ifx.setSec_Amt(Util.longValueOf(sec_amt));
			} else
				ifx.setSec_Amt(Util.longValueOf(isoMsg.getString(4).trim()));*/

		} catch (Exception e) {
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
		}

//        String P43 = isoMsg.getString(43);

		String str_fld52 = isoMsg.getString(52).trim();
		if(Util.hasText(str_fld52)) {
			ifx.setPINBlock(str_fld52);
			logger.debug("ISOtoIFX::DE-52 PIN Block [" + str_fld52 + "]"); //Raza LOGGING ENHANCED
		}

        String P54 = isoMsg.getString(54);
		if(Util.hasText(P54))
		{
			logger.debug("ISOtoIFX::DE-54 Additional Amounts [" + P54 + "]"); //Raza LOGGING ENHANCED
		}
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

		/*String str_fld25 =  isoMsg.getString(25);
		if(Util.hasText(str_fld25))
		{
			logger.debug("ISOtoIFX::DE-25 POS CONDITION CODE [" + str_fld25 + "]"); //Raza LOGGING ENHANCED
			ifx.setPosConditionCode(str_fld25);
		}*/

        if (!Util.hasText(isoMsg.getString(25).trim()) && ifx.getTerminalId() != null) { //Raza Update Field 25 with 61
			if (ProcessContext.get().getMyInstitution().getBin().equals(ifx.getBankId()) &&
					FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole()) &&
					ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
				TerminalType terminalType = GlobalContext.getInstance().getTerminalType(ifx.getTerminalId());
				if (ifx.getTerminalId() != null && terminalType != null && TerminalType.isPhisycalDeviceTerminal(terminalType)){
					ifx.setTerminalType(terminalType);
				}
			}
		}

		String str_fld55 = isoMsg.getString(55);
		if(Util.hasText(str_fld55))
		{
			logger.debug("ISOtoIFX::DE-55 ICC System Related Data [" + str_fld55 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld58 = isoMsg.getString(58);
		if(Util.hasText(str_fld58))
		{
			ifx.setAuthAgentInstId(str_fld58);
			logger.debug("ISOtoIFX::DE-58 Authorization Agent Institution Id [" + str_fld58 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld60 = isoMsg.getString(60);
		if(Util.hasText(str_fld60))
		{
			ifx.setSelfDefineData(str_fld60); //Raza using SelfDefineData as Advice Reason Code
			logger.debug("ISOtoIFX::DE-60 Advice Reason Code [" + str_fld60 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld61 = isoMsg.getString(61);
		if(Util.hasText(str_fld61))
		{
			logger.debug("ISOtoIFX::DE-61 POS Data [" + str_fld61 + "]"); //Raza LOGGING ENHANCED
			mapTerminalType(ifx, str_fld61.substring(9,10)); //Raza Using SubField 10 of POS_DATA (CardHolder Activated Terminal Level Indicator)
			//MasterCard does not uses Field 25 POS CONDITION CODE
			//ifx.setPosConditionCode(isoMsg.getString(25)); //Raza MasterCard Verify this.
		}

		String str_fld62 = isoMsg.getString(62);
		if(Util.hasText(str_fld62))
		{
			logger.debug("ISOtoIFX::DE-62 Intermediate Network Facility (INF) Data [" + str_fld62 + "]"); //Raza LOGGING ENHANCED
		}

		//Field-63
		String str_fld63 = isoMsg.getString(63);
		if(Util.hasText(str_fld63)) {
			ifx.setNetworkData(isoMsg.getString(63));
			logger.debug("ISOtoIFX::DE-63 Network Data [" + str_fld63 + "]"); //Raza LOGGING ENHANCED
		}

		/*String str_fld64 = isoMsg.getString(64).trim(); //Raza commenting not used by MasterCard
		if(Util.hasText(str_fld64)) {
			ifx.setMsgAuthCode(str_fld64);
			logger.debug("ISOtoIFX::DE-64 Message Authentication Code [" + str_fld63 + "]"); //Raza LOGGING ENHANCED
			//String P64 = isoMsg.getString(64).trim();
			//String S128 = isoMsg.getString(128).trim(); //Raza MasterCard Commenting
			//if (P64 != null && P64.length() > 0)
			//	ifx.setMsgAuthCode(P64);
		}*/

        /*else if (S128 != null && S128.length() > 0) //Raza MasterCard commenting
            ifx.setMsgAuthCode(S128);*/

		/*String str_fld70 = isoMsg.getString(70);
		if(Util.hasText(str_fld70)) {
			ifx.setNetworkData(isoMsg.getString(70));
			logger.debug("ISOtoIFX::DE-63 Network Management Info Code [" + str_fld70 + "]"); //Raza LOGGING ENHANCED
		}*/

        String S90 = isoMsg.getString(90);
        if (S90 != null && S90.length() >= 20) {
			logger.debug("ISOtoIFX::DE-90 Org Data Elements [" + S90 + "]"); //Raza LOGGING ENHANCED
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
        
        String str_fld91 = isoMsg.getString(91);
		if(Util.hasText(str_fld91))
		{
			logger.debug("ISOtoIFX::DE-91 File Update Code [" + str_fld91 + "]"); //Raza LOGGING ENHANCED
		}

        String S95 = isoMsg.getString(95);
        if (S95 != null && S95.length() >= 24) {
			logger.debug("ISOtoIFX::DE-95 Replacement Amounts [" + S95 + "]"); //Raza LOGGING ENHANCED
            ifx.setNew_AmtAcqCur(S95.substring(0, 12));
            ifx.setNew_AmtIssCur(S95.substring(12, 24));
            Long real_Amt = Util.longValueOf(ifx.getNew_AmtAcqCur());
            real_Amt = (real_Amt!=null && !real_Amt.equals(0L))? real_Amt :Util.longValueOf(ifx.getNew_AmtIssCur());
            if (real_Amt!= null && !real_Amt.equals(0L))
            	ifx.setReal_Amt(real_Amt);
        }

		String str_fld96 = isoMsg.getString(96);
		if(Util.hasText(str_fld96))
		{
			logger.debug("ISOtoIFX::DE-96 Message Security Code [" + str_fld96 + "]"); //Raza LOGGING ENHANCED
		}


		String str_fld100 = isoMsg.getString(100);
		if(Util.hasText(str_fld100)) {
			ifx.setRecvBankId(str_fld100);
			logger.debug("ISOtoIFX::DE-100 Receving Institution Id Code [" + str_fld100 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld101 = isoMsg.getString(101);
		if(Util.hasText(str_fld101)) {
			logger.debug("ISOtoIFX::DE-101 File Name [" + str_fld101 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld102 = isoMsg.getString(102);
		if(Util.hasText(str_fld102)) {
			ifx.setMainAccountNumber(str_fld102); //Raza Verify This
			logger.debug("ISOtoIFX::DE-102 Account Id - 1 [" + str_fld102 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld103 = isoMsg.getString(103);
		if(Util.hasText(str_fld103)) {
			logger.debug("ISOtoIFX::DE-103 Account Id - 2 [" + str_fld103 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld110 = isoMsg.getString(110);
		if(Util.hasText(str_fld110)) {
			logger.debug("ISOtoIFX::DE-110 Additional Data - 2 [" + str_fld110 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld111 = isoMsg.getString(111);
		if(Util.hasText(str_fld111)) {
			logger.debug("ISOtoIFX::DE-111 Amount Currency Conversion Assessment [" + str_fld111 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld112 = isoMsg.getString(112);
		if(Util.hasText(str_fld112)) {
			logger.debug("ISOtoIFX::DE-112 Additional Data National Use [" + str_fld112 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld120 = isoMsg.getString(120);
		if(Util.hasText(str_fld120)) {
			logger.debug("ISOtoIFX::DE-120 Record Data [" + str_fld120 + "]"); //Raza LOGGING ENHANCED
			ifx.setRecordData(str_fld120);
		}

		String str_fld122 = isoMsg.getString(122);
		if(Util.hasText(str_fld122)) {
			logger.debug("ISOtoIFX::DE-122 Additional Record Data [" + str_fld122 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld124 = isoMsg.getString(124);
		if(Util.hasText(str_fld124)) {
			logger.debug("ISOtoIFX::DE-124 Member Defined Data [" + str_fld124 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld125 = isoMsg.getString(125);
		if(Util.hasText(str_fld125)) {
			logger.debug("ISOtoIFX::DE-125 New PIN Data [" + str_fld125 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld126 = isoMsg.getString(126);
		if(Util.hasText(str_fld126)) {
			logger.debug("ISOtoIFX::DE-126 Switch Private Data [" + str_fld126 + "]"); //Raza LOGGING ENHANCED
		}

		String str_fld127 = isoMsg.getString(127);
		if(Util.hasText(str_fld127)) {
			logger.debug("ISOtoIFX::DE-127 Processor Private Data [" + str_fld127 + "]"); //Raza LOGGING ENHANCED
		}
        
        if (ifx.getIfxType()!= null && !ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
        		&& ifx.getTerminalType() ==null ){
        	ISOException isoe = new ISOException("Invalid terminal type code: " + Integer.parseInt("0"+str_fld61.trim()));
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
        }
        
        if (ifx.getIfxType() != null && ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType())){
        	ifx.setMy_TrnSeqCntr(Util.generateTrnSeqCntr(6));
        }
        
        


        return ifx;
    }

}
