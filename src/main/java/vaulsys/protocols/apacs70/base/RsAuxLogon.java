package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.imp.Ifx;

import java.io.IOException;

public class RsAuxLogon extends RsAuxBase {
	public byte[] encryptedMasterKey;
	public byte[] encryptedMACKey;
	public byte[] encryptedPINKey;
	
	public RsAuxLogon() {
		super("52");
	}

	@Override
	public void fromIfx(Ifx ifx) {
		super.fromIfx(ifx);
	}
	
	@Override
	public void pack(ApacsByteArrayWriter out) throws IOException {
		super.pack(out);
		
		out.write(GS);
		// Updated Terminal Code
		out.write(GS);
		// Updated Merchant Code
		out.write(GS);
		// Updated POS IP Address
		out.write(GS);
		out.write(encryptedMasterKey, 8);
		out.write(GS);
		out.write(encryptedMACKey, 8);
		out.write(GS);
		out.write(encryptedPINKey, 8);
		out.write(GS);
		// encrypted Data Key
		out.write(GS);
		// encrypted ICV
	}

	@Override
	protected void auxString(StringBuilder builder) {
	}
}
