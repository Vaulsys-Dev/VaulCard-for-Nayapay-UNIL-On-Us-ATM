package vaulsys.billpayment.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class NotValidBillPaymentMessageException extends DecisionMakerExceptionImp
{
	private static final long serialVersionUID = 1L;

	public NotValidBillPaymentMessageException(String errorMessage) {
		super(errorMessage);
	}
	@Override
	public void alterIfxByErrorType(Ifx ifx) {
		ifx.setRsCode(ISOResponseCodes.ORIGINAL_DATA_ELEMENT_MISMATCH);
		//	        ifx.setRsCode(ErrorCodes.INVALID_CARD_STATUS);
		//	        ifx.Status.Severity = SeverityEnum.Error;
		//	        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();
	}
	@Override
	public boolean returnError() {
		return true;
	}
}
