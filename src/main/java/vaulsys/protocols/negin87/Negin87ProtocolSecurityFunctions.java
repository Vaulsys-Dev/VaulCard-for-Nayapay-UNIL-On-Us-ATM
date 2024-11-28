package vaulsys.protocols.negin87;

import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;

public class Negin87ProtocolSecurityFunctions extends ISOSecurityFunctions {

	@Override
	public Boolean isTranslatePIN(Ifx ifx) {
		IfxType inIfxType = ifx.getIfxType();
		return super.isTranslatePIN(ifx) && 
			(/*ShetabFinalMessageType.isRequestMessage(inIfxType)
				&& */
				!((ISOFinalMessageType.isReturnMessage(inIfxType) || ISOFinalMessageType.isReturnReverseMessage(inIfxType))
//						&& incomingMessage.getIfx().getBankId().equals(639347L) )
				));
	}
}
