package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.webservices.ghasedak.GhasedakRsItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RsAuxGhasedak extends RsAuxBase { 
//	public int numOfRecords;
	public List<GhasedakRsItem> records = new ArrayList<GhasedakRsItem>();
	
	
	public RsAuxGhasedak() {
		super("60");
	}

	@Override
	public void fromIfx(Ifx ifx) {
		super.fromIfx(ifx);
//		numOfRecords = ifx.getGhasedakData().getGhasedakRsItems().size();
		records = ifx.getGhasedakData().getGhasedakRsItems();
	}

	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException {
		super.pack(out);
		out.writePadded(records.size(), 3, false);
		for(GhasedakRsItem record : records){
			out.write(GS);
			out.write(record.getItemType().toString(), 40);
			out.write(GS);
			out.write(record.getAmount(), 40);
			out.write(GS);
			out.write(record.getCurrencyCode().toString(), 40);
			out.write(GS);
			out.write(record.getCreditDate() + "-" + record.getCreditTime(), 40);
		}
		
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nNumber of records: ").append(records.size());
	}
}
