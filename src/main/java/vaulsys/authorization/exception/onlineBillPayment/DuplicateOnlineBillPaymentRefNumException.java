package vaulsys.authorization.exception.onlineBillPayment;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class DuplicateOnlineBillPaymentRefNumException extends DecisionMakerExceptionImp {
	public DuplicateOnlineBillPaymentRefNumException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateOnlineBillPaymentRefNumException(String message) {
		super(message);
	}

	public DuplicateOnlineBillPaymentRefNumException(Throwable cause) {
		super(cause);
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
