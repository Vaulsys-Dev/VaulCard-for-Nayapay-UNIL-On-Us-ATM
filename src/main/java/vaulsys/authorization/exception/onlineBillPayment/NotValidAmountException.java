package vaulsys.authorization.exception.onlineBillPayment;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class NotValidAmountException extends DecisionMakerExceptionImp {
	 @Override
	 public void alterIfxByErrorType(Ifx ifx) {
		 ifx.setRsCode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
	 }
	 public NotValidAmountException(String message) {
		 super(message);
	 }
     public NotValidAmountException() {
     }
     @Override
	 public boolean returnError() {
    	 return true;
     }
}
