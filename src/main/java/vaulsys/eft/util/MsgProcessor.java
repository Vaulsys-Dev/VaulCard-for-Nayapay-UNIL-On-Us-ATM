package vaulsys.eft.util;

import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.imp.Ifx;

public class MsgProcessor {

	public static Ifx processor(Ifx incomingIFX) throws CloneNotSupportedException {
		Ifx outgoingIFX;
		if (incomingIFX == null)
			outgoingIFX = new Ifx();
		else
			outgoingIFX = incomingIFX.clone();

		outgoingIFX.setIfxDirection(IfxDirection.OUTGOING);
		return outgoingIFX;
	}

}
