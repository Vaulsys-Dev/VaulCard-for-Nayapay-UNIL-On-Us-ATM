package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.imp.Ifx;

import java.io.IOException;

public class RsAuxPinChange extends RsAuxBase {
	public String cvv2;
	public String expireDate;

	public RsAuxPinChange() {
		super("70");
	}

	@Override
	public void fromIfx(Ifx ifx) {
		cvv2 = ifx.getCVV2();
		expireDate = ifx.getExpDt().toString();
	}

	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException {
		out.write("Z6", 2);
		out.write("70", 2);
		out.write(GS);
		out.write(cvv2, 4);
		out.write(GS);
		out.write(expireDate, 4);
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nCVV2: ").append(cvv2);
		builder.append("\nExpire Date: ").append(expireDate);
	}
}
