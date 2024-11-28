package vaulsys.thirdparty.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class ThirdPartyPurchaseException extends DecisionMakerExceptionImp {

	public ThirdPartyPurchaseException(String message) {
		super(message);
	}

	public ThirdPartyPurchaseException() {
		super();
	}

	public ThirdPartyPurchaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ThirdPartyPurchaseException(Throwable cause) {
		super(cause);
	}

	@Override
	public boolean returnError() {
		return true;
	}

	@Override
	public void alterIfxByErrorType(Ifx ifx) {
		ifx.setRsCode(ISOResponseCodes.MESSAGE_FORMAT_ERROR);
	}
	
}
