/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2016 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package vaulsys.protocols.PaymentSchemes.ISO8583.base;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOBinaryFieldPackager;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.LiteralBinaryInterpreter;

/**
 * ISOBinaryFieldPackager
 *
 * @author @apr
 */
@SuppressWarnings("unused")
public class IFB_LLHEX extends ISOBinaryFieldPackager {
    public IFB_LLHEX() {
        super(LiteralBinaryInterpreter.INSTANCE, vaulsys.protocols.PaymentSchemes.ISO8583.base.HexNibblesPrefixer.LL);
    }

    public IFB_LLHEX (int len, String description) {
        super(len, description, LiteralBinaryInterpreter.INSTANCE, vaulsys.protocols.PaymentSchemes.ISO8583.base.HexNibblesPrefixer.LL);
        checkLength(len, 99);
    }
    public void setLength(int len) {
        checkLength(len, 99);
        super.setLength(len);
    }
}
