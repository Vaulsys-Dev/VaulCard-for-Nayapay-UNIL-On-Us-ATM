package vaulsys.protocols.apacs70.base;

import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.ifx.imp.Ifx;

public class RqAuxPinChange extends RqAuxBase {
	public String cipherBlock;

	@Override
	public void unpack(ApacsByteArrayReader in) {
		cipherBlock = in.getStringFixedToSep("cipherBlock", 16, ApacsConstants.GS);

		super.unpack(in);
	}

	@Override
	public void toIfx(Ifx ifx) {
		super.toIfx(ifx);

		ifx.setNewPINBlock(cipherBlock);
	}

	@Override
	protected void auxString(StringBuilder builder) {
		builder.append("\nNew PIN Cipher Block: ").append(cipherBlock);
	}
}
