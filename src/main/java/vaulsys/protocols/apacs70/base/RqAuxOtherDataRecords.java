package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.apacs70.ApacsByteArrayReader;

import java.util.HashMap;
import java.util.Map;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;

public class RqAuxOtherDataRecords extends RqAuxBase {
	
	public Map<String, String> rqAuxOtherDataRecords = new HashMap<String, String>();
	public int numOfKeys = 0;

	public RqAuxOtherDataRecords(int numOfKeys) {
		this.numOfKeys = numOfKeys;
	}

	@Override
	public void unpack(ApacsByteArrayReader in) {
		for(int i = 0; i < numOfKeys; i++){
			String keyValue = in.getStringMaxToSep("Key=value "+(i+1), 50, GS);
			rqAuxOtherDataRecords.put(keyValue.split("=")[0], keyValue.split("=")[1]);
			in.skipToSep(GS);
		}

	}
	
	@Override
	protected void auxString(StringBuilder builder) {
		for(Map.Entry<String, String> entry : rqAuxOtherDataRecords.entrySet()) {
			builder.append("\nrqAuxOtherDataRecords: ").append(entry.getKey()+"="+entry.getValue());
		}

	}
}
