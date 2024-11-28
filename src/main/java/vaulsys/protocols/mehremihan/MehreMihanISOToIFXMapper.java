package vaulsys.protocols.mehremihan;

import vaulsys.calendar.*;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.customer.Currency;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.*;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MehreMihanISOToIFXMapper extends ISOtoIfxMapper {
	public static final MehreMihanISOToIFXMapper Instance = new MehreMihanISOToIFXMapper();

	protected MehreMihanISOToIFXMapper() {
	}

	Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {

		ISOMsg isoMsg = (ISOMsg) message;
		/** **************** Map ISO to IFX **************** */

		Ifx ifx = new Ifx();

		//Integer mti = null; //Raza MasterCard commenting
		String mti = null;
		try {
			mti = isoMsg.getMTI(); //Integer.parseInt(isoMsg.getMTI()); //Raza MasterCard commenitng
		} catch (NumberFormatException e) {
			ISOException isoe = new ISOException("Invalid MTI", e);
			ifx.setSeverity(Severity.ERROR);
			ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}
		ifx.setAppPAN(isoMsg.getString(2));

		/******/

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
				currency = ProcessContext.get().getCurrency(Integer.parseInt(acquire_currency));//GlobalContext.getInstance().getCurrency(Integer.parseInt(acquire_currency));
				if (currency == null) {
					throw new ISOException("Invalid Currency Code: " + acquire_currency);
				}
			} else {
				currency = ProcessContext.get().getRialCurrency();//GlobalContext.getInstance().getRialCurrency();
			}
			ifx.setAuth_Currency(currency.getCode());
			ifx.setAuth_CurRate("1");
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
			if (Util.hasText(issuer_currency)) {
				currency = ProcessContext.get().getCurrency(Integer.parseInt(issuer_currency));//GlobalContext.getInstance().getCurrency(Integer.parseInt(issuer_currency));
				ifx.setSec_Currency(currency.getCode());
				if (currency == null) {
					throw new ISOException("Invalid Currency Code: " + issuer_currency);
				}
			} else {
				ifx.setSec_Currency(ifx.getAuth_Currency());
			}

			ifx.setSec_CurRate(isoMsg.getString(10).trim());

			String sec_amt = isoMsg.getString(6).trim();
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

		if (!isoMsg.getString(7).equals(""))
			ifx.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", isoMsg.getString(7).trim())));

		ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));
		ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));

		String localTime = isoMsg.getString(12).trim();
		String localDate = isoMsg.getString(13).trim();
		DateTime now = DateTime.now();

		try {

			DateTime d = new DateTime(MyDateFormatNew.parse("MMddHHmmss", localDate + localTime));
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
			if (expDate != null && !expDate.equals("")) {
				expDate = expDate.trim();
				ifx.setExpDt(Long.parseLong(expDate));
			}
		} catch (Exception e) {
			logger.info("Exception in setting ExpDate(14)!");
		}
		String settleDate = isoMsg.getString(15).trim();
		try {
			MonthDayDate d = new MonthDayDate(MyDateFormatNew.parse("MMdd", settleDate));
			if (d != null && d.getMonth() == 1 && DateTime.now().getDayDate().getMonth() == 12) {
				d.setYear(DateTime.now().getDayDate().getYear() + 1);
				ifx.setSettleDt(d);
			} else if (d != null && d.getMonth() == 12 && DateTime.now().getDayDate().getMonth() == 1) {
				d.setYear(DateTime.now().getDayDate().getYear() - 1);
				ifx.setSettleDt(d);
			}

		} catch (Exception e) {
			logger.info("Exception in setting settleDate(15)!");
		}

		String postedDate = isoMsg.getString(17).trim();
		try {
			MonthDayDate d = new MonthDayDate(MyDateFormatNew.parse("MMdd", postedDate));
			if (d != null && d.getMonth() == 1 && DateTime.now().getDayDate().getMonth() == 12) {
				d.setYear(DateTime.now().getDayDate().getYear() + 1);
				ifx.setPostedDt(d);
			} else if (d != null && d.getMonth() == 12 && DateTime.now().getDayDate().getMonth() == 1) {
				d.setYear(DateTime.now().getDayDate().getYear() - 1);
				ifx.setPostedDt(d);
			} else {
				ifx.setPostedDt(d);
			}

		} catch (Exception e) {
			logger.info("Exception in setting PostedDate(17)");
		}

		mapTerminalType(ifx, isoMsg.getString(25));

		ifx.setBankId(isoMsg.getString(32).trim());
//        String checkBin = isoMsg.getString(2).substring(0, 7);
//        if(checkBin == "5029085")
//        	ifx.setBankId(5029085L);


		ifx.setFwdBankId(isoMsg.getString(33).trim());
		ifx.setDestBankId(isoMsg.getString(33).trim());

		if (Long.valueOf(isoMsg.getString(33).trim()).equals(5029085L)) {
			ifx.setDestBankId("502908");
			//ifx.setFwdToBankId(5029085L);
		}


		ifx.setTrk2EquivData(isoMsg.getString(35));
		ifx.setApprovalCode(isoMsg.getString(38).trim());
		ifx.setRsCode(mapError(isoMsg.getString(39).trim()));

		String P43 = isoMsg.getString(43);

		mapField44(ifx, isoMsg.getString(44), convertor);

		ifx.setPINBlock(isoMsg.getString(52).trim());

		String P54 = isoMsg.getString(54);

		if (ISOResponseCodes.APPROVED.equals(ifx.getRsCode()) ||
				ISOResponseCodes.INVALID_ACCOUNT.equals(ifx.getRsCode())) {
			while (P54 != null && P54.length() >= 20) {
				GeneralDao.Instance.flush();
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

				GeneralDao.Instance.flush();
				P54 = P54.substring(20);
			}
		}
		GeneralDao.Instance.flush();
		mapIfxType(ifx, mti, emvTrnType);

		mapFieldANFix(ifx, isoMsg, 37);
		mapFieldANFix(ifx, isoMsg, 41);
		mapFieldANFix(ifx, isoMsg, 42);

		String P64 = isoMsg.getString(64).trim();
		String S128 = isoMsg.getString(128).trim();
		if (P64 != null && P64.length() > 0)
			ifx.setMsgAuthCode(P64);
		else if (S128 != null && S128.length() > 0)
			ifx.setMsgAuthCode(S128);

		String S90 = isoMsg.getString(90);
		if (S90 != null && S90.length() >= 20) {
			ifx.setOriginalDataElements(new MessageReferenceData());
			ifx.getSafeOriginalDataElements().setTrnSeqCounter(ISOUtil.zeroUnPad(S90.substring(4, 10)));

			String msgType = S90.substring(0, 4);
			if (Integer.parseInt(msgType) != 0)
				ifx.getSafeOriginalDataElements().setMessageType(msgType);
			else {
				ISOException isoe = new ISOException("Invalid Format( F_90: " +
						" OriginalData.msgType= NULL, OriginalData.TrnSeqCounter = " + ifx.getSafeOriginalDataElements().getTrnSeqCounter() + ", temrinalId= " + ifx.getTerminalId() + ")");
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
				}
				logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}

			String origDt = S90.substring(10, 20);
			if (Integer.parseInt(origDt) != 0) {
				try {
					ifx.getSafeOriginalDataElements().setOrigDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", origDt)));
				} catch (ParseException e) {
					ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.origDt= NULL, OriginalData.TrnSeqCounter = " +
							ifx.getSafeOriginalDataElements().getTrnSeqCounter()
							+ ", temrinalId= " + ifx.getTerminalId() + ")");
					if (!Util.hasText(ifx.getStatusDesc())) {
						ifx.setSeverity(Severity.ERROR);
						ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
					}
					logger.error(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
				}
			}

			String bankId = S90.substring(20, 31).trim();
			if (Integer.parseInt(bankId) != 0)
				ifx.getSafeOriginalDataElements().setBankId(bankId);
			else {
				ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.bankId= NULL, OriginalData.TrnSeqCounter = " +
						ifx.getSafeOriginalDataElements().getTrnSeqCounter()
						+ ", temrinalId= " + ifx.getTerminalId() + ", OriginalData.origDt= " + ifx.getSafeOriginalDataElements().getOrigDt() +
						")");
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
				}
				logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}

			String fwdBankId = S90.substring(31);
			if (Integer.parseInt(fwdBankId) != 0)
				ifx.getSafeOriginalDataElements().setFwdBankId(fwdBankId);
			else {
				ISOException isoe = new ISOException("Invalid Format( F_90: OriginalData.FwdBankId = NULL, OriginalData.TrnSeqCounter = " +
						ifx.getSafeOriginalDataElements().getTrnSeqCounter()
						+ ", OriginalData.temrinalId= " + ifx.getTerminalId() + ", OriginalData.origDt= " + ifx.getSafeOriginalDataElements().getOrigDt() +
						", OriginalData.bankId =" + ifx.getSafeOriginalDataElements().getBankId() + ")");
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
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
			real_Amt = (real_Amt != null && !real_Amt.equals(0L)) ? real_Amt : Util.longValueOf(ifx.getNew_AmtIssCur());
			if (real_Amt != null && !real_Amt.equals(0L))
				ifx.setReal_Amt(real_Amt);
		}

		//inja lazeme chizi hardcode beshe?
		ifx.setRecvBankId(isoMsg.getString(100));


		if (ifx.getIfxType() != null && !ISOFinalMessageType.isResponseMessage(ifx.getIfxType())
				&& ifx.getTerminalType() == null) {
			ISOException isoe = new ISOException("Invalid terminal type code: " + Integer.parseInt("0" + isoMsg.getString(25).trim()));
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
			}
			logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}

		if (ifx.getIfxType() != null && ISOFinalMessageType.isReversalRqMessage(ifx.getIfxType())) {
			ifx.setMy_TrnSeqCntr(Util.generateTrnSeqCntr(6));
		}


		String field48 = "";
		if (isoMsg.hasField(48)) {
			field48 = new String((byte[]) isoMsg.getValue(48));
			mapField48(ifx, field48, convertor);
		}
		GeneralDao.Instance.flush();
		return ifx;
	}

	@Override
	public void mapField48(Ifx ifx, String f_48, EncodingConvertor convertor) {
		if (!Util.hasText(f_48))
			return;

		if (ISOFinalMessageType.isBankStatementMessage(ifx.getIfxType()) && ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
//			if (f_48.length()<130)
//				return;
			List<BankStatementData> list2 = parseBankStatement(ifx.getEMVRsData(), f_48);
			ifx.setBankStatementData(list2);
		} else if (ISOFinalMessageType.isChangePinBlockMessage(ifx.getIfxType())) {
			if (TrnType.CHANGEINTERNETPINBLOCK.equals(ifx.getTrnType())) {

				Integer cvv2 = Integer.parseInt(f_48.substring(2, 5));
				ifx.setCVV2(cvv2.toString());
				ifx.setExpDt(Long.valueOf(f_48.substring(6, 9)));
			}

		} else if (ISOFinalMessageType.isGetAccountMessage(ifx.getIfxType()) && ISOFinalMessageType.isResponseMessage(ifx.getIfxType())) {
			String field_48 = new String(f_48).toUpperCase();
			if (ISOFinalMessageType.isGetAccountMessage(ifx.getIfxType())) {
				List<CardAccountInformation> list = parseAccountData(ifx.getEMVRsData(), field_48);
				ifx.setCardAccountInformation(list);
			}

		} else
			super.mapField48(ifx, f_48, convertor);
	}

	private List<BankStatementData> parseBankStatement(EMVRsData rsData, String f_48) {
		List<BankStatementData> result = new ArrayList<BankStatementData>();
		StringTokenizer tokenizer;
		StringTokenizer timeTokenizer;
		StringTokenizer dateTokenizer;
		StringTokenizer lineTokenizer;
		lineTokenizer = new StringTokenizer(f_48, "!");


		while (lineTokenizer.hasMoreTokens()) {
			String ans = lineTokenizer.nextToken().trim();
			tokenizer = new StringTokenizer(ans, "|");
			String row = (tokenizer.nextToken().trim());

			String dateStr = tokenizer.nextToken().trim();
			dateTokenizer = new StringTokenizer(dateStr, "/");
			int year = Integer.parseInt(dateTokenizer.nextToken().trim());
			int month = Integer.parseInt(dateTokenizer.nextToken().trim());
			int day = Integer.parseInt(dateTokenizer.nextToken().trim());

			String timeStr = tokenizer.nextToken().trim();
			timeTokenizer = new StringTokenizer(timeStr, "-");
			int hour = Integer.parseInt(timeTokenizer.nextToken().trim());
			int minute = Integer.parseInt(timeTokenizer.nextToken().trim());

			DateTime persianDateTime = new DateTime(new DayDate(year, month, day), new DayTime(hour, minute));

			String trxtyp = tokenizer.nextToken().trim();

			Long amount = Util.longValueOf(tokenizer.nextToken().trim());

			Long balance = Util.longValueOf(tokenizer.nextToken().trim());

			BankStatementData data = new BankStatementData();
			data.setTrxDt(PersianCalendar.toGregorian(persianDateTime));
			data.setTrnType(trxtyp);
			data.setAmount(amount);
			data.setBalance(balance);
			data.setEmvRsData(rsData);
			result.add(data);
		}
		return result;

	}

	private List<CardAccountInformation> parseAccountData(EMVRsData rsData, String accountData) {
		List<CardAccountInformation> result = new ArrayList<CardAccountInformation>();

		int offset = 0;
		int index = 1;
		while (Util.hasText(accountData) && offset < accountData.length()) {
			Integer length = Integer.parseInt(accountData.substring(offset, offset + 2));
			String accNum = accountData.substring(offset + 2, offset + 2 + length);

			CardAccountInformation data = new CardAccountInformation();
			data.setAccountNumber(accNum);
			data.setLength(length);
			data.setIndex(index + "");
			index++;
			data.setEmvRsData(rsData);
			result.add(data);

			offset += (length + 2);
		}

		return result;
	}

}
