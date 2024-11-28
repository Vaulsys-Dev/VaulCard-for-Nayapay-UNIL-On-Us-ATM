package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;
import vaulsys.entity.OrganizationService;
import vaulsys.protocols.ProtocolType;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.apacs70.encoding.Apacs70FarsiConvertor;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.StringFormat;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.io.IOException;

public class RsAuxThirdPartyPurchase extends RsAuxBase {
public byte[] companyName;
	
	public RsAuxThirdPartyPurchase() {
		super("56");
	}

	@Override
	public void fromIfx(Ifx ifx) {
		super.fromIfx(ifx);
		
//		String billTypeSrc = ProcessContext.get().getProtocolConfig(ProtocolType.APACS70, ifx.getBillOrgType().getType());
//		companyName = StringFormat.formatNew(3, StringFormat.JUST_RIGHT, billTypeSrc, '0');
		Apacs70FarsiConvertor convertor = Apacs70FarsiConvertor.Instance;
		if(Util.hasText(ifx.getThirdPartyName()))
			companyName = convertor.encode(ifx.getThirdPartyName());		

	}

	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException {
//		super.pack(out);
		
		out.write("Z6", 2);
		out.write("56", 2);
		out.write(GS);
//		out.writePadded(issuerCode, 3, false);
		
//		out.write(GS);
		if(companyName != null && companyName.length > 80)
			logger.error("Company name is beyond 80: " + companyName.length);
		out.writeTruncate(companyName, 80);
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nCompany name: ").append(companyName);
	}

}
