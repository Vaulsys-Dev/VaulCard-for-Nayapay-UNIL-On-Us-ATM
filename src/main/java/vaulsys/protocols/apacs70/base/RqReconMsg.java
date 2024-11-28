package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.FS;
import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.imp.Ifx;

public final class RqReconMsg extends RqBaseMsg {
	public Long    cardAcceptorNumber;
	public Integer sessionNumber;
	public Integer lastSuccessfulSequenceNumber;
	public Integer totalValueDebits;
	public Integer totalQuantityDebits;
	public Integer totalValueCredits;
	public Integer totalQuantityCredits;

	@Override
	protected void unpack(ApacsByteArrayReader in) {
		this.cardAcceptorNumber = in.getLongMaxToSep("cardAcceptorNumber", 15, FS);;
		this.sessionNumber = in.getIntegerMaxToSep("sessionNumber", 1, FS);
		//in.skipToSep(FS); // Field 2 : Reserved for APACS
		this.lastSuccessfulSequenceNumber = in.getIntegerMaxToSep("lastSuccessfulSequenceNumber", 4, FS);
		in.skipToSep(FS); // Field 3 : Reserved for APACS
		this.confirmationCode = in.getIntegerFixed("confirmationCode", 1, 16);
		in.skipFixed(1); // balance code, 0x31 is fixed 
		in.skipToSep(FS);
		this.totalValueDebits = in.getIntegerMaxToSep("totalValueDebits", 11, FS);
		this.totalQuantityDebits = in.getIntegerMaxToSep("totalQuantityDebits", 4, FS);
		this.totalValueCredits = in.getIntegerMaxToSep("totalValueCredits", 11, FS);
		this.totalQuantityCredits = in.getIntegerMaxToSep("totalQuantityCredits", 4, FS);
	}

	@Override
	public void toIfx(Ifx ifx) {
		super.toIfx(ifx);

		if(lastSuccessfulSequenceNumber != null)
			ifx.setLast_TrnSeqCntr(lastSuccessfulSequenceNumber.toString());
	}

	@Override
	protected void msgString(StringBuilder builder) {
		builder.append("\r\nMerchant Code: ").append(cardAcceptorNumber);
		builder.append("\r\nSession Number: ").append(sessionNumber);
		builder.append("\r\nLast Successful Sequence Number: ").append(lastSuccessfulSequenceNumber);
		builder.append("\r\nConfirmation Code: ").append(confirmationCode);
		builder.append("\r\nTotal Value Debits: ").append(totalValueDebits);
		builder.append("\r\nTotal Quantity Debits: ").append(totalQuantityDebits);
		builder.append("\r\nTotal Value Credits: ").append(totalValueCredits);
		builder.append("\r\nTotal Quantity Credits: ").append(totalQuantityCredits);
	}
}
