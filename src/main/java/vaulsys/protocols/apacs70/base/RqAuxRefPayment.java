package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.imp.Ifx;

public class RqAuxRefPayment extends RqAuxBase {
	public String referenceNumber;

	@Override
	public void unpack(ApacsByteArrayReader in) {
		referenceNumber = in.getStringMaxToSep("paymentRefrenceNumber", 30, ApacsConstants.GS);

		super.unpack(in);
	}

	@Override
	public void toIfx(Ifx ifx) {
		super.toIfx(ifx);

		ifx.setOnlineBillPaymentRefNum(referenceNumber);
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nReference Number: ").append(referenceNumber);
	}
}
