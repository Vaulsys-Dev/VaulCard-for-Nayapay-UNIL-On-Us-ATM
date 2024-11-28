package vaulsys.protocols.apacs70.base;

import org.apache.log4j.Logger;

import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.imp.GhasedakData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;
import vaulsys.webservices.ghasedak.GhasedakItemType;

public class RqAuxGhasedak extends RqAuxBase {
	private static final Logger logger = Logger.getLogger(RqAuxGhasedak.class);
	
	public String type;
	
	@Override
	public void unpack(ApacsByteArrayReader in) {
		type = in.getStringMaxToSep("type", 4, ApacsConstants.GS);

		super.unpack(in);
	}

	@Override
	public void toIfx(Ifx ifx) {
		super.toIfx(ifx);
		
		if(Util.hasText(type)){
			GhasedakData data = new GhasedakData();
			data.setItemType(new GhasedakItemType(Integer.valueOf(type)));
//			data.setRqRsCode(GhasedakRqOrRs.GHASEDAK_RQ);
			ifx.setGhasedakData(data);	
		}
		
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nRequested Type: ").append(type);
	}
}
