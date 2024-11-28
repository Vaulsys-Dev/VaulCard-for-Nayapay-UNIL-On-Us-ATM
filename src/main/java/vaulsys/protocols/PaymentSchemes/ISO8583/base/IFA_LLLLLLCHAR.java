/*
 * Copyright (c) 2007 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package vaulsys.protocols.PaymentSchemes.ISO8583.base;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.*;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.AsciiPrefixer;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.NullPadder;

/**
 * ISOFieldPackager ASCII variable len CHAR
 *
 * @author Alejandro Revilla
 * @see vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOComponent
 */
public class IFA_LLLLLLCHAR extends ISOStringFieldPackager {
    public IFA_LLLLLLCHAR() {
        super(vaulsys.protocols.PaymentSchemes.ISO8583.base.NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, vaulsys.protocols.PaymentSchemes.ISO8583.base.AsciiPrefixer.LLLLLL);
    }

    /**
     * @param len         - field len
     * @param description symbolic descrption
     */
    public IFA_LLLLLLCHAR(int len, String description) {
        super(len, description, NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, AsciiPrefixer.LLLLL);
        checkLength(len, 999999);
    }

    public void setLength(int len) {
        checkLength(len, 999999);
        super.setLength(len);
    }
}
