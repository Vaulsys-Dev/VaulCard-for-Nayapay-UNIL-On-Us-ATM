package vaulsys.protocols.PaymentSchemes.base;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.AsciiPrefixer;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryFieldPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.LiteralBinaryInterpreter;

/**
 * Created by m.rehman on 5/25/2016.
 * ISOFieldPackager Binary variable Ascii length
 */
public class IFB_LLLACHAR extends ISOBinaryFieldPackager {
    public IFB_LLLACHAR() {
        super(LiteralBinaryInterpreter.INSTANCE, AsciiPrefixer.LLL);
    }

    /**
     * @param len         - field len
     * @param description symbolic descrption
     */
    public IFB_LLLACHAR(int len, String description) {
        super(len, description, LiteralBinaryInterpreter.INSTANCE, AsciiPrefixer.LLL);
        checkLength(len, 999);
    }

    public void setLength(int len) {
        checkLength(len, 999);
        super.setLength(len);
    }
}
