package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;
import vaulsys.protocols.ProtocolType;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.apacs70.encoding.Apacs70FarsiConvertor;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.IOException;

public class RsAuxTransferFundAuth extends RsAuxBase {
	public String destIssuerCode;
	public byte[] destCardHolderName;
	public byte[] destCardHolderFamily;

	public RsAuxTransferFundAuth() {
		super("71");
	}

	@Override
	public void fromIfx(Ifx ifx) {
		super.fromIfx(ifx);

//		destIssuerCode = GlobalContext.getInstance().getProtocolConfig(ProtocolType.APACS70, ifx.getDestBankId().toString());

		 String dest = "";
		 if (ifx.getDestBankId() != null) {
			dest = ProcessContext.get().getProtocolConfig(ProtocolType.APACS70, ifx.getDestBankId().toString());
		 }
		 destIssuerCode = Apacs70Utils.changeIssuerCode(ifx, dest);

		Apacs70FarsiConvertor convertor = Apacs70FarsiConvertor.Instance;
		if(Util.hasText(ifx.getCardHolderName()))
			destCardHolderName = convertor.encode(ifx.getCardHolderName());
		if(Util.hasText(ifx.getCardHolderFamily()))
			destCardHolderFamily = convertor.encode(ifx.getCardHolderFamily());
	}

	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException {
		super.pack(out);

		out.write(GS);
		out.writePadded(destIssuerCode, 3, false);
		out.write(GS);
		if(destCardHolderName != null && destCardHolderName.length > 33)
			logger.error("Dest cardholder name is beyond 33: " + destCardHolderName.length);
		out.writeTruncate(destCardHolderName, 33);
		out.write(GS);
		if(destCardHolderFamily != null && destCardHolderFamily.length > 33)
			logger.error("Dest cardholder family is beyond 33: " + destCardHolderFamily.length);
		out.writeTruncate(destCardHolderFamily, 33);
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nDest Issuer Code: ").append(destIssuerCode);
	}
}
