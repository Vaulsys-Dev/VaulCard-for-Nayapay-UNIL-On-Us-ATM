package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class DuplicateEPaymentMessageException extends AuthorizationException {

	public DuplicateEPaymentMessageException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public DuplicateEPaymentMessageException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public DuplicateEPaymentMessageException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void alterIfxByErrorType(Ifx ifx) {
		ifx.setRsCode(ISOResponseCodes.PERMISSION_DENIED);
		// ifx.Status.Severity = SeverityEnum.Error;
		// ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " +
		// getMessage();

	}

	@Override
	public boolean returnError() {
		return true;
	}

}
