package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.FS;

import java.text.ParseException;

import vaulsys.calendar.DateTime;
import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;

public class RqInfoMsg extends RqBaseMsg {
	public Long    cardAcceptorNumber;
	public String  cardDetails;
	public Long    transactionAmount;
	public Integer softwareLevel;
	public String  transactionDateAndTime;
	public Long    terminalCountryCode;
	public Integer transactionCurrencyCode;
	public String  cipherBlock;
	
	@Override
	protected void unpack(ApacsByteArrayReader in) {
		cardAcceptorNumber = in.getLongMaxToSep("cardAcceptorNumber", 15, FS);
		cardDetails = in.getStringMaxToSep("cardDetails", 40, FS);
		transactionAmount = in.getLongMaxToSep("transactionAmount", 11, FS);
		in.skipToSep("descriptive data", 16, FS); // descriptive data, empty
		confirmationCode = in.getIntegerFixed("confirmationCode", 1, 16);
		in.skipFixed(1); // balance Code, must be 49
		String sl = null;
		try {
			//TODO: It should have length 7
			sl = in.getStringMaxToSep("software level", 15, FS);
			softwareLevel = Integer.valueOf(sl);
		} catch (Exception e) {
			logger.error("Bad Software Level Field Format: " + sl);
		}
		in.skipToSep("transaction Amount Other", 11, FS); // transaction Amount Other, empty
		transactionDateAndTime = in.getStringFixedToSep("transactionDateAndTime", 10, FS);
		in.skipToSep("EMV Terminal Type", 2, FS); // EMV Terminal Type, empty
		terminalCountryCode = in.getLongFixedToSep("terminalCountryCode", 3, FS); // must be "001"
		transactionCurrencyCode = in.getIntegerFixedToSep("transactionCurrencyCode", 3, FS); // must be "364"
		in.skipToSep("reason code", 2, FS); // reason code, empty
		in.skipToSep("IC Request Data", 215, FS); // IC Request Data, empty

		ApacsByteArrayReader auxData = in.getBytesMaxToSep("auxData", 480, FS);
		if (auxData.getRemainSize() > 0) {
			this.auxiliaryData = new AuxiliaryDataComponent();
			this.auxiliaryData.unpack(auxData);
		}

		if(in.getRemainSize() > 16)
			this.cipherBlock = in.getStringFixed("cipherBlock", 16);
		
	}

	@Override
	public void toIfx(Ifx ifx) {
		super.toIfx(ifx);

		ifx.setOrgIdNum(String.valueOf(cardAcceptorNumber));
		ifx.setStatusDesc("No card-details!");
		if (transactionAmount == null) {
			if (IfxType.BAL_INQ_RQ.equals(ifx.getIfxType()))
				transactionAmount = 0L;
			else {
				logger.warn("null amount");
				ifx.setStatusDesc("null amount ");
				ifx.setSeverity(Severity.WARN);
			}
		}
		ifx.setAuth_Amt(transactionAmount);
		ifx.setReal_Amt(transactionAmount);
		ifx.setTrx_Amt(transactionAmount);

		if(softwareLevel != null)
			ifx.setApplicationVersion(softwareLevel.toString());

		if (transactionCurrencyCode != 0)
			ifx.setAuth_Currency(transactionCurrencyCode);
		else
			ifx.setAuth_Currency(GlobalContext.RIAL_CURRENCY_CODE);

		ifx.setCountryCode(terminalCountryCode);

		ifx.setAuth_CurRate("1");
		ifx.setSec_Amt(ifx.getAuth_Amt());
		ifx.setSec_Currency(ifx.getAuth_Currency());
		ifx.setSec_CurRate(ifx.getAuth_CurRate());

		ifx.setAccTypeFrom(AccType.MAIN_ACCOUNT);
		ifx.setAccTypeTo(AccType.MAIN_ACCOUNT);

//		try {
//			if(ifx.getAppPAN() != null)
//				ifx.setFwdBankId(Long.parseLong(ifx.getAppPAN().substring(0, 6)));
//		} catch (Exception e) {
//			logger.warn("toIfx: FwdBankId: ", e);
//			ifx.setStatusDesc("Wrong apppan: " + ifx.getAppPAN());
//			ifx.setSeverity(Severity.ERROR);
//		}
		ifx.setDestBankId(ifx.getFwdBankId());
		ifx.setRecvBankId(ifx.getFwdBankId());

		DateTime posTrxDateTime = null;
		try {
			posTrxDateTime = new DateTime(MyDateFormatNew.parse("yyMMddHHmm", transactionDateAndTime));
		} catch (ParseException e) {
			logger.warn("Wrong dateTime format: " + transactionDateAndTime);
		}

		if(posTrxDateTime != null){
			if(DateTime.between(posTrxDateTime, 10, 10)) {
				ifx.setOrigDt(posTrxDateTime);
			} else {
				/**** set origDt even if in not between 10 day before and after ****/
				ifx.setOrigDt(posTrxDateTime);

				logger.warn("Incorrect OrigDate: " + posTrxDateTime);
				ifx.setStatusDesc(String.format("POS has wrong date[%s]!", posTrxDateTime));
				ifx.setSeverity(Severity.ERROR);
			}
		} else {
			logger.warn("Incorrect OrigDate: " + posTrxDateTime);
			ifx.setStatusDesc(String.format("POS has wrong date[%s]!", posTrxDateTime));
			ifx.setSeverity(Severity.ERROR);
		}

		if(auxiliaryData != null)
			auxiliaryData.toIfx(ifx);

		ifx.setPINBlock(cipherBlock);
	}
	
	
	@Override
	protected void msgString(StringBuilder builder) {
		builder.append("\nShop: ").append(cardAcceptorNumber);
		builder.append("\nConfirm: ").append(confirmationCode);
		builder.append("\nTime: ").append(transactionDateAndTime);
		if(auxiliaryData != null)
			builder.append(auxiliaryData.toString());
		
	}

}
