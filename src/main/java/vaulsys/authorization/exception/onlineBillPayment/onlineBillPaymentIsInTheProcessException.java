package vaulsys.authorization.exception.onlineBillPayment;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class onlineBillPaymentIsInTheProcessException extends DecisionMakerExceptionImp{
	@Override
	public void alterIfxByErrorType(Ifx ifx) {
		//baraie in bayad epay bege ke dobare emtehan kone!chun yeki dige dare in ref num ro pardakht mikone
		ifx.setRsCode(ISOResponseCodes.FIELD_ERROR);
	}
	public onlineBillPaymentIsInTheProcessException(String message) {
		super(message);
	}
	public onlineBillPaymentIsInTheProcessException() {
	}
	@Override
	public boolean returnError() {
		return true;
	}

}
