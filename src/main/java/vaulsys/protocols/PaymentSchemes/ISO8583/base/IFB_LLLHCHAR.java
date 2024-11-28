/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package vaulsys.protocols.PaymentSchemes.ISO8583.base;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.*;

/**
 * ISOFieldPackager Binary Hex LLLCHAR
 *
 * @author apr@cs.com.uy
 * @version $Id: IFB_LLLHCHAR.java,v 1.1 2007/02/27 12:46:12 omid Exp $
 * @see ISOComponent
 */
public class IFB_LLLHCHAR extends ISOStringFieldPackager {
    public IFB_LLLHCHAR() {
        super(vaulsys.protocols.PaymentSchemes.ISO8583.base.NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, BinaryPrefixer.BBB);
    }

    /**
     * @param len         - field len
     * @param description symbolic descrption
     */
    public IFB_LLLHCHAR(int len, String description) {
        super(len, description, vaulsys.protocols.PaymentSchemes.ISO8583.base.NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, BinaryPrefixer.BBB);
        checkLength(len, 65535);
    }

    public void setLength(int len) {
        checkLength(len, 65535);
        super.setLength(len);
    }
}

