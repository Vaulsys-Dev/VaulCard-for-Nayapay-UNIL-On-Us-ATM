package vaulsys.protocols.PaymentSchemes.ISO8583.base;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.*;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.AsciiPrefixer;

/**
 * ISOFieldPackager ASCII variable len BINARY
 *
 * @author Alejandro
 * @version $Id: IFA_LLLLLLBINARY.java,v 1.1 2007/02/27 12:46:12 omid Exp $
 * @see vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOComponent
 */
public class IFA_LLLLLLBINARY extends ISOBinaryFieldPackager {
    public IFA_LLLLLLBINARY() {
        super(LiteralBinaryInterpreter.INSTANCE, vaulsys.protocols.PaymentSchemes.ISO8583.base.AsciiPrefixer.LLLLLL);
    }

    /**
     * @param len         - field len
     * @param description symbolic descrption
     */
    public IFA_LLLLLLBINARY(int len, String description) {
        super(len, description, LiteralBinaryInterpreter.INSTANCE, AsciiPrefixer.LLLLLL);
        checkLength(len, 999999);
    }

    public void setLength(int len) {
        checkLength(len, 999999);
        super.setLength(len);
    }
}

