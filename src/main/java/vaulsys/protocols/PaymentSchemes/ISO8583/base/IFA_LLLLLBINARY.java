package vaulsys.protocols.PaymentSchemes.ISO8583.base;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.*;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.AsciiPrefixer;

/**
 * ISOFieldPackager ASCII variable len BINARY
 *
 * @author Alejandro
 * @version $Id: IFA_LLLLLBINARY.java,v 1.1 2007/02/27 12:46:12 omid Exp $
 * @see vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOComponent
 */
public class IFA_LLLLLBINARY extends ISOBinaryFieldPackager {
    public IFA_LLLLLBINARY() {
        super(LiteralBinaryInterpreter.INSTANCE, vaulsys.protocols.PaymentSchemes.ISO8583.base.AsciiPrefixer.LLLLL);
    }

    /**
     * @param len         - field len
     * @param description symbolic descrption
     */
    public IFA_LLLLLBINARY(int len, String description) {
        super(len, description, LiteralBinaryInterpreter.INSTANCE, AsciiPrefixer.LLLLL);
        checkLength(len, 99999);
    }

    public void setLength(int len) {
        checkLength(len, 99999);
        super.setLength(len);
    }
}