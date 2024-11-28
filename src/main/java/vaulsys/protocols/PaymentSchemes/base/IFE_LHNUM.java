package vaulsys.protocols.PaymentSchemes.base;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.*;

/**
 * Created by HP on 12/17/2016.
 */
public class IFE_LHNUM extends ISOStringFieldPackager {
    public IFE_LHNUM() {
        super(NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, BinaryPrefixer.B);
    }

    /**
     * @param len         - field len
     * @param description symbolic descrption
     */
    public IFE_LHNUM(int len, String description) {
        super(len, description, NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, BinaryPrefixer.B);
        checkLength(len, 255);
    }

    public void setLength(int len) {
        checkLength(len, 255);
        super.setLength(len);
    }
}
