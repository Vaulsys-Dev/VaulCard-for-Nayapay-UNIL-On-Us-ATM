package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;

public class RsAuxOtherDataRecords extends RsAuxBase {
	
	transient Logger logger = Logger.getLogger(RsAuxOtherDataRecords.class);
	public Map<String, String> rsAuxOtherDataRecords = new HashMap<String, String>();


	public RsAuxOtherDataRecords() {
		super("98");
	}

	@Override
	public void fromIfx(Ifx ifx) {
		rsAuxOtherDataRecords.put("RN", Util.hasText(ifx.getNetworkRefId()) ? ifx.getNetworkRefId() : "");
		String pc = "";
		try{
			pc = "0000000000"; //TASK Task093 : gharar shod 10ta 0 bezarim
//			pc = ((Shop)ifx.getEndPointTerminal().getOwner()).getOwnOrParentContact().getAddress().getPostalCode();
		}catch(Exception e){
        	logger.info("Exception in sending postal code to pos: " + ifx.getId(), e);
			pc = "";
		}
		pc = Util.hasText(pc)?pc:"";
		rsAuxOtherDataRecords.put("PC", pc);
	}
	
	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException {
		out.write("Z6", 2);
		out.write("98", 2);
		out.write(GS);
		out.writePadded(rsAuxOtherDataRecords.size(), 3, false);
//		out.write(GS);
		for(Map.Entry<String, String> entry : rsAuxOtherDataRecords.entrySet()) {
			out.write(GS);
			out.write(entry.getKey()+"="+entry.getValue(), (entry.getKey()+"="+entry.getValue()).length());
		}
	}

	@Override
	protected void auxString(StringBuilder builder) {
		for(Map.Entry<String, String> entry : rsAuxOtherDataRecords.entrySet()) {
			builder.append("\nrsAuxOtherDataRecords: ").append(entry.getKey()+"="+entry.getValue());
		}

	}
}
