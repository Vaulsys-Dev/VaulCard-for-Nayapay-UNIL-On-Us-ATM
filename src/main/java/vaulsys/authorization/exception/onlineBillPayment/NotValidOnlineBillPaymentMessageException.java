package vaulsys.authorization.exception.onlineBillPayment;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class NotValidOnlineBillPaymentMessageException extends DecisionMakerExceptionImp{
	 @Override
	 public void alterIfxByErrorType(Ifx ifx) {
		 ifx.setRsCode(ISOResponseCodes.INCORRECT_ONLINE_REFNUMBER);
	 }
	 public NotValidOnlineBillPaymentMessageException(String message) {
		 super(message);
	 }
     public NotValidOnlineBillPaymentMessageException() {
     }
     @Override
     public boolean returnError() {
    	 return true;
     }
}
