package vaulsys.protocols.PaymentSchemes.base;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.*;

/**
 * @author hp
 */
public class IFB_LHNUM extends ISOStringFieldPackager {
	public IFB_LHNUM() {
		super(NullPadder.INSTANCE, BCDInterpreter.RIGHT_PADDED, BinaryPrefixer.B);
	}

	/**
	 * @param len
	 *            - field len
	 * @param description
	 *            symbolic descrption
	 */
	public IFB_LHNUM(int len, String description, boolean isLeftPadded) {
		super(len, description, NullPadder.INSTANCE,
				isLeftPadded ? BCDInterpreter.LEFT_PADDED : BCDInterpreter.RIGHT_PADDED,
				BinaryPrefixer.B);
		checkLength(len, 255);
	}

	public IFB_LHNUM(int len, String description, boolean isLeftPadded, boolean fPadded) {
		super(len, description, NullPadder.INSTANCE,
				isLeftPadded ? BCDInterpreter.LEFT_PADDED :
						(fPadded ? BCDInterpreter.RIGHT_PADDED_F : BCDInterpreter.RIGHT_PADDED),
				BinaryPrefixer.B);
		checkLength(len, 255);
	}

	public void setLength(int len) {
		checkLength(len, 255);
		super.setLength(len);
	}

	/**
	 * Must override ISOFieldPackager method to set the Interpreter correctly
	 */
	public void setPad(boolean pad) {
		setInterpreter(pad ? BCDInterpreter.LEFT_PADDED : BCDInterpreter.RIGHT_PADDED);
		this.pad = pad;
	}
}
