package vaulsys.protocols.apacs70.base;

import java.text.ParseException;

import vaulsys.calendar.DateTime;
import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

public class RqAuxRefund extends RqAuxBase {
	public Integer originalSequenceNumber;
	public String  originalDateAndTime;

	@Override
	public void unpack(ApacsByteArrayReader in) {
		originalSequenceNumber = in.getIntegerFixedToSep("originalSequenceNumber", 4, ApacsConstants.GS);
		originalDateAndTime = in.getStringFixedToSep("originalDateAndTime", 10, ApacsConstants.GS);

		super.unpack(in);
	}
	
	@Override
	public void toIfx(Ifx ifx) {
		super.toIfx(ifx);

		ifx.getSafeOriginalDataElements().setTrnSeqCounter(this.originalSequenceNumber.toString());
		if (this.originalDateAndTime != null && Util.hasText(this.originalDateAndTime))
			try {
				ifx.getSafeOriginalDataElements().setOrigDt(new DateTime(MyDateFormatNew.parse("yyMMddHHmm", this.originalDateAndTime)));
			} catch (ParseException e) {
				e.printStackTrace(); //TODO
			}
//		Long myBin = GlobalContext.getInstance().getMyInstitution().getBin();
			String myBin = "" + ProcessContext.get().getMyInstitution().getBin();
		ifx.getSafeOriginalDataElements().setBankId(myBin);
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nOriginal Sequence Number: ").append(originalSequenceNumber);
		builder.append("\nOriginal DateTime: ").append(originalDateAndTime);
	}
}
