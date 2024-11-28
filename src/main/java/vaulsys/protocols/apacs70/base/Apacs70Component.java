package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.imp.Ifx;

import java.io.IOException;

public abstract class Apacs70Component {
	public abstract void pack(ApacsByteArrayWriter writer) throws IOException;
	
	public abstract void unpack(ApacsByteArrayReader reader);
	
	public void toIfx(Ifx ifx) {
		throw new RuntimeException("Not Implemented!");
	}
	
	public void fromIfx(Ifx ifx) {
		throw new RuntimeException("Not Implemented!");
	}
}
