package vaulsys.protocols.mizan;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.protocols.PaymentSchemes.base.ISOTransactionCodes;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.BalType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.AcctBal;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ifx.imp.MizanSpecificData;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOtoIfxMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

public class MizanISOToIfxMapper extends ISOtoIfxMapper {
	private static final Logger logger = Logger.getLogger(MizanISOToIfxMapper.class);
	public static final MizanISOToIfxMapper Instance = new MizanISOToIfxMapper();

	private MizanISOToIfxMapper() {
	}

	@Override
	public Ifx map(ProtocolMessage message, EncodingConvertor convertor) throws Exception {
		ISOMsg isoMsg = (ISOMsg) message;

		Ifx ifx = new Ifx();

		// MTI
		//Integer mti = null; //Raza MasterCard commenting
		String mti = null;
		try {
			mti = isoMsg.getMTI(); //Integer.parseInt(isoMsg.getMTI()); //Raza MasterCard commenting
		} catch (NumberFormatException e) {
			ISOException isoe = new ISOException("Invalid MTI", e);
			ifx.setSeverity(Severity.ERROR);
			ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
		}

		// P2
		ifx.setAppPAN(isoMsg.getString(2));

		// P3
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

			//mapTrnType(ifx, emvTrnType);
			if (emvTrnType.equals(ISOTransactionCodes.PURCHASE)) //00
				ifx.setTrnType(TrnType.PURCHASE);
			else if (emvTrnType.equals(ISOTransactionCodes.BALANCE_INQUERY)) //31
				ifx.setTrnType(TrnType.BALANCEINQUIRY);
			else {
				ISOException isoe = new ISOException("Invalid Process Code :" + emvTrnType);
				if (!Util.hasText(ifx.getStatusDesc())) {
					ifx.setSeverity(Severity.ERROR);
					ifx.setStatusDesc(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
					logger.warn(isoe.getClass().getSimpleName() + ": " + isoe.getMessage());
				}
			}
		}
		mapIfxType(ifx, mti, emvTrnType);

		// P4 amount
		try {
			ifx.setAuth_Currency(ProcessContext.get().getRialCurrency().getCode());
			ifx.setAuth_CurRate("1");
			ifx.setAuth_Amt(Util.longValueOf(isoMsg.getString(4).trim()));
			ifx.setReal_Amt(ifx.getAuth_Amt());
		} catch (Exception e) {
			if (!Util.hasText(ifx.getStatusDesc())) {
				ifx.setSeverity(Severity.ERROR);
				ifx.setStatusDesc(e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		// P6 ignored

		// P7
		if (!isoMsg.getString(7).equals(""))
			ifx.setTrnDt(new DateTime(MyDateFormatNew.parse("MMddHHmmss", isoMsg.getString(7).trim())));

		// P10 ignored
		//ifx.setSec_CurRate(isoMsg.getString(10).trim());

		// P11
		ifx.setSrc_TrnSeqCntr(ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));
		ifx.setMy_TrnSeqCntr(ISOUtil.zeroUnPad(isoMsg.getString(11).trim()));

		// P12 & P13
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

		// P14
		try {
			String expDate = isoMsg.getString(14);
			if (expDate != null && !expDate.equals("")) {
				expDate = expDate.trim();
				ifx.setExpDt(Long.parseLong(expDate));
			}
		} catch (Exception e) {
			logger.info("Exception in setting ExpDate(14)!");
		}

		// P15: only in acquiry
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

		// P32 & P33
		ifx.setBankId(isoMsg.getString(32).trim());
		ifx.setFwdBankId(isoMsg.getString(33).trim());
		ifx.setDestBankId(isoMsg.getString(33).trim());

		// P35
		ifx.setTrk2EquivData(isoMsg.getString(35));

		// P37
		mapFieldANFix(ifx, isoMsg, 37);

		// P38 & P39
		ifx.setApprovalCode(isoMsg.getString(38).trim());
		ifx.setRsCode(mapError(isoMsg.getString(39).trim()));

		// P41
		mapFieldANFix(ifx, isoMsg, 41);

		// P42
		mapFieldANFix(ifx, isoMsg, 42);

		// P48 In issueri from Mizan Switch
		String p48 = isoMsg.getString(48);
		if (Util.hasText(p48)) {
			MizanSpecificData mizan = new MizanSpecificData();
			// [0-20) is reserved for mizan
			mizan.setRequestCount(Integer.valueOf(p48.substring(20, 22)));
			mizan.setMizanPercent(Integer.valueOf(p48.substring(22, 24)));
			mizan.setBankPercent(Integer.valueOf(p48.substring(24, 26)));
			int count = Integer.valueOf(p48.substring(26, 28));
			mizan.setItemsCount(count);
			StringBuilder builder = new StringBuilder();
			int idx = 28;
			for (int i = 0; i < count; i++) {
				builder.append(Integer.valueOf(p48.substring(idx, idx + 9))).append("|"); // price
				idx += 9;
				builder.append(p48.substring(idx, idx + 26)).append("|"); // prod acc no
				idx += 26;
				builder.append(Integer.valueOf(p48.substring(idx, idx + 2))).append("|"); // prod perc
				idx += 2;
				builder.append(p48.substring(idx, idx + 26)).append("|"); // sell acc no
				idx += 26;
				builder.append(Integer.valueOf(p48.substring(idx, idx + 2))).append("\n"); // sell perc
				idx += 2;
			}
			mizan.setItems(builder.substring(0, builder.length() - 1));
			//ifx.setMizanSpecificData(mizan);
			// TODO: check the sum of prices against the total amount
		}

		// P52 In issueri rq from Mizan
		ifx.setPINBlock(isoMsg.getString(52).trim());

		// P54 In acquieri rs from Mizan
		String P54 = isoMsg.getString(54);
		if (P54 != null && P54.length() >= 20) {
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
		}

		// P64 or S128: MAC
		String P64 = isoMsg.getString(64).trim();
		String S128 = isoMsg.getString(128).trim();
		if (P64 != null && P64.length() > 0)
			ifx.setMsgAuthCode(P64);
		else if (S128 != null && S128.length() > 0)
			ifx.setMsgAuthCode(S128);

		return ifx;
	}

}
