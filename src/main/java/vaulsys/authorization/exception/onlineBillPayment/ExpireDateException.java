package vaulsys.authorization.exception.onlineBillPayment;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class ExpireDateException extends DecisionMakerExceptionImp{
	@Override
	public void alterIfxByErrorType(Ifx ifx) {
		ifx.setRsCode(ISOResponseCodes.REFRENCE_NUMBER_IS_EXPIRE);
	}
	public ExpireDateException(String message) {
		super(message);
	}
	public ExpireDateException() {
	}
	@Override
	public boolean returnError() {
		return true;
	}

}
