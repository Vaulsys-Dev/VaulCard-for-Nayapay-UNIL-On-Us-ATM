package vaulsys.authentication.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class MacFailException extends AuthenticationException {

	public MacFailException(String message, Throwable cause) {
		super(message, cause);
	}

	public MacFailException(String message) {
		super(message);
	}

	public MacFailException(Throwable cause) {
		super(cause);
	}

	@Override
	public void alterIfxByErrorType(Ifx ifx) {
		ifx.setRsCode(ISOResponseCodes.INVALID_CARD_STATUS);
	}

	@Override
	public boolean returnError() {
		return true;
	}

}
