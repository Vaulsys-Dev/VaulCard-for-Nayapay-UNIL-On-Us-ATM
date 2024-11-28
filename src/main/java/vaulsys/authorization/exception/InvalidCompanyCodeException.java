package vaulsys.authorization.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class InvalidCompanyCodeException extends DecisionMakerExceptionImp{
	 @Override
	 public void alterIfxByErrorType(Ifx ifx) {
		 ifx.setRsCode(ISOResponseCodes.INVALID_COMPANY_CODE);
	 }
	 public InvalidCompanyCodeException(String message) {
		 super(message);
	 }
     public InvalidCompanyCodeException() {
     }
     @Override
     public boolean returnError() {
    	 return true;
     }

}
