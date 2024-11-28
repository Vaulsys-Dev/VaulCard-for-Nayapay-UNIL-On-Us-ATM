package vaulsys.protocols.PaymentSchemes.base;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.*;

/**
 * @author hp
 */
public class IFB_LHCHAR extends ISOStringFieldPackager {
	public IFB_LHCHAR() {
		super(NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, BinaryPrefixer.B);
	}

	/**
	 * @param len
	 *            - field len
	 * @param description
	 *            symbolic descrption
	 */
	public IFB_LHCHAR(int len, String description) {
		super(len, description, NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, BinaryPrefixer.B);
		checkLength(len, 255);
	}

	public void setLength(int len) {
		checkLength(len, 255);
		super.setLength(len);
	}
}
