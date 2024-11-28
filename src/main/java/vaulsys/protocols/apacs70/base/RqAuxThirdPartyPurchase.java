package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.imp.Ifx;

public class RqAuxThirdPartyPurchase extends RqAuxBase{
	public Long ThirdPartyPaymentCompany;
	public String Ids;
	

	@Override
	public void unpack(ApacsByteArrayReader in) {
		ThirdPartyPaymentCompany = in.getLongMaxToSep("ThirdPartyCompany", 20, ApacsConstants.GS);
//		for(; buffer[idx+readLen]!=sep.getByte() && (idx+readLen) < end; readLen++);
//		in.getLongFixedToSep("FirstId", 30, ApacsConstants.FS);//felan endakhtam dor hamintori
//		in.getLongFixedToSep("SecondId", 30, ApacsConstants.FS);//felan endakhtam dor hamintori
//		in.getLongFixedToSep("ThirdId", 30, ApacsConstants.FS);//felan endakhtam dor hamintori\
//		for () {
		Long count = in.getLongFixedToSep("IDCount", 2, ApacsConstants.GS);
		StringBuilder ids = new StringBuilder();
		for(Long i =0L; i< count; i++){
//			Long temp = in.getLongMaxToSep("GS filed",30 ,ApacsConstants.GS);
			String temp = in.getStringMaxToSep("GS filed",30 ,ApacsConstants.GS);
			ids.append(temp + "|");
		}
		Ids = ids.toString();
//		}
//		Long temp1 = in.getLongMaxToSep("GS filed",2 ,ApacsConstants.GS);
		
//		in.skipToSep(GS);
//		in.get
		
		
		super.unpack(in);
	}

	@Override
	public void toIfx(Ifx ifx) {
		super.toIfx(ifx);

		ifx.setThirdPartyCode(this.ThirdPartyPaymentCompany);
		ifx.setThirdPartyIds(this.Ids);
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\n ThirdPartyPaymentCompany Type: ").append(ThirdPartyPaymentCompany);
		builder.append("\n ThPartyIds: ").append(Ids);
	}
}
