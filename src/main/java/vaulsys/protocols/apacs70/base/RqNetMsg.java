package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.FS;
import vaulsys.calendar.DateTime;
import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.MyDateFormatNew;

import java.text.ParseException;

public class RqNetMsg extends RqBaseMsg {
	public Long    cardAcceptorNumber;
	public Integer softwareLevel;
	public String  transactionDateAndTime;
	public Long    terminalCountryCode;
	public Integer transactionCurrencyCode;
	public String  cipherBlock;

	@Override
	protected void unpack(ApacsByteArrayReader in) {
		cardAcceptorNumber = in.getLongMaxToSep("cardAcceptorNumber", 15, FS);
		in.skipToSep("card details", 40, FS); // card details
		in.skipToSep("transactionAmount", 11, FS); // transactionAmount
		in.skipToSep("descriptive data", 16, FS); // descriptive data, empty
		confirmationCode = in.getIntegerFixed("confirmationCode", 1, 16);
		in.skipFixed(1); // balance Code, must be 49
		String sl = null;
		try {
			sl = in.getStringMaxToSep("Software Level", 15, FS);  //TODO: It should have length 7
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

		if(softwareLevel != null)
			ifx.setApplicationVersion(softwareLevel.toString());

		DateTime now = DateTime.now();
		DateTime posTrxDateTime = null;
		try {
			posTrxDateTime = new DateTime(MyDateFormatNew.parse("yyMMddHHmm", transactionDateAndTime));
		} catch (ParseException e) {
			logger.warn("Wrong dateTime format: " + transactionDateAndTime);
		}
		if (posTrxDateTime == null || now.getDayDate().getYear() != posTrxDateTime.getDayDate().getYear()) {
			/*
			 * In ISO protocol, the year part of date isn't sent, 
			 *  so in response switch encounters problem
			 *  to find the first transaction.
			 *  The orgiDt is the field that cause this problem!
			 */
			logger.warn(String.format("Wrong sent date by POS[%s]: %s", ifx.getTerminalId(), posTrxDateTime));
			ifx.setStatusDesc(String.format("POS date[%s] is and Switch date[%s] has severe difference in year field!", posTrxDateTime, now));
			ifx.setSeverity(Severity.ERROR);
		}
		ifx.setOrigDt(posTrxDateTime);

		ifx.setCountryCode(terminalCountryCode);

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
//		builder.append("\nCipher Block: ").append(cipherBlock);
	}

}
