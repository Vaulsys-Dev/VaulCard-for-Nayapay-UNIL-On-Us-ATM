package vaulsys.protocols.apacs70.base;

import java.io.IOException;

import vaulsys.modernpayment.onlinebillpayment.OnlineBillPaymentStatus;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.apacs70.encoding.Apacs70FarsiConvertor;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;

public class RsAuxRefPayment extends RsAuxBase {
	public String referenceNumber;
	public String description;
	public OnlineBillPaymentStatus paymentStatus;
	
	public RsAuxRefPayment() {
		super("54");
	}

	@Override
	public void fromIfx(Ifx ifx) {
		super.fromIfx(ifx);

		referenceNumber = ifx.getOnlineBillPaymentRefNum();
		description = ifx.getOnlineBillPaymentDescription();

		if(IfxType.ONLINE_BILLPAYMENT_TRACKING.equals(ifx.getIfxType()) &&
				ifx.getOnlineBillPaymentData() != null &&
				ifx.getOnlineBillPaymentData().getOnlineBillPayment() != null)
			paymentStatus = ifx.getOnlineBillPaymentData().getOnlineBillPayment().getPaymentStatus();
	}

	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException{
		super.pack(out);

		out.write(ApacsConstants.GS);
		out.write(referenceNumber, 20);
		out.write(ApacsConstants.GS);
		byte[] descBArr = Apacs70FarsiConvertor.Instance.encode(description);
		out.writeTruncate(descBArr, 90);
		out.write(ApacsConstants.GS);
		if(paymentStatus != null)
			out.write(String.format("%02d", paymentStatus.getCode()), 2);
		else
			out.write("99", 2);
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nReference Number: ").append(referenceNumber);
		builder.append("\nDescription: ").append(description);
		if(paymentStatus != null)
			builder.append("\nStatus: ").append(paymentStatus);
	}
}
