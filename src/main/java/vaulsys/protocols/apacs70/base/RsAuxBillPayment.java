package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;
import vaulsys.protocols.ProtocolType;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.StringFormat;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.IOException;

public class RsAuxBillPayment extends RsAuxBase {
	public String billType;
	
	public RsAuxBillPayment() {
		super("53");
	}

	@Override
	public void fromIfx(Ifx ifx) {
		super.fromIfx(ifx);
		
		String billTypeSrc = ProcessContext.get().getProtocolConfig(ProtocolType.APACS70, ifx.getBillOrgType().getType());
		billType = StringFormat.formatNew(3, StringFormat.JUST_RIGHT, billTypeSrc, '0');

	}

	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException {
		super.pack(out);
		out.write(GS);
		out.write(billType, 3);
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nBill Type: ").append(billType);
	}
}
