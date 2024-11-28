package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.ESC;
import static vaulsys.protocols.apacs70.base.ApacsConstants.FS;
import static vaulsys.protocols.apacs70.base.ApacsConstants.US;
import vaulsys.calendar.DateTime;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.MyDateFormatNew;

import java.io.IOException;

public class RsFinNetMsg extends RsBaseMsg {
	public String ledgerBalance;
	public String availableBalance;
	public String dateAndTime;
	public Long realAmount; // used in discount of Saderaat

	@Override
	public void fromIfx(Ifx ifx) {
		super.fromIfx(ifx);

		dateAndTime = MyDateFormatNew.format("yyMMddHHmmss", DateTime.now().toDate());
		ledgerBalance = Apacs70Utils.convertAcctBalAmt(ifx.getAcctBalLedgerAmt());
		availableBalance = Apacs70Utils.convertAcctBalAmt(ifx.getAcctBalAvailableAmt());
		realAmount = ifx.getAuth_Amt();
//		realAmount = ifx.getSent_Amt();
//		realAmount = ifx.getReal_Amt();
		
		auxiliaryData = new AuxiliaryDataComponent();
		auxiliaryData.fromIfx(ifx);
	}

	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException {
		super.pack(out);
		
		// Authorization Code
		out.write(FS);
		// Amount(Total) /Session Number
		out.write(FS);

		/* M E S S A G E */
			// Card Acceptor Display Data
			out.write(US);
			out.write(ledgerBalance, 15);
			out.write(ESC);
			out.write(availableBalance, 15);
			out.write(US);
			// PINPAD Data
			out.write(US);
			out.write(realAmount, 11); // Transaction Amount
		out.write(FS);
		// Referral Telephone Number
		out.write(FS);
		// Floor Limits (Pre and Post Communication)
		out.write(FS);
		out.write(dateAndTime, 12);
		out.write(FS);
		// IC response Data
		out.write(FS);
		// Response Additional Data
		out.write(FS);
		if(auxiliaryData != null)
			auxiliaryData.pack(out);
		out.write(FS);
		out.write(MAC, 8);
	}
	
	@Override
	protected void msgString(StringBuilder builder) {
		super.msgString(builder);

		builder.append("\r\nLedger Balance: ").append(ledgerBalance);
		builder.append("\r\nAvailable Balance: ").append(availableBalance);
		builder.append("\r\nDateTime: ").append(dateAndTime);
		if(auxiliaryData != null)
			builder.append(auxiliaryData);
	}
}
