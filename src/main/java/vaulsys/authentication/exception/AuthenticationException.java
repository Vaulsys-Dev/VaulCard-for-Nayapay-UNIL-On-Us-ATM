package vaulsys.authentication.exception;

import vaulsys.exception.base.DecisionMakerException;
import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class AuthenticationException extends DecisionMakerExceptionImp {
	
	private Boolean returnError;
	

    public AuthenticationException(String s) {
        super(s);
        returnError = false;
    }

    public AuthenticationException() {
        super();
        returnError = false;
    }

    public AuthenticationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        returnError = false;
    }

    public AuthenticationException(Throwable arg0) {
        super(arg0);
        returnError = false;
    }

    public AuthenticationException(String s, Boolean returnError) {
    	super(s);
    	this.returnError = returnError;
    }
    
    public AuthenticationException(Boolean returnError) {
    	super();
    	this.returnError = returnError;
    }
    
    public AuthenticationException(String arg0, Throwable arg1,  Boolean returnError) {
    	super(arg0, arg1);
    	this.returnError = returnError;
    }
    
    public AuthenticationException(Throwable arg0, Boolean returnError) {
    	super(arg0);
    	this.returnError = returnError;
    }
    
    public void alterIfxByErrorType(Ifx ifx) {
    	if (getCause()!= null && getCause() instanceof DecisionMakerException){
    		DecisionMakerException cause = (DecisionMakerException) getCause();
    		cause.alterIfxByErrorType(ifx);
    	}else{
    		ifx.setRsCode(ISOResponseCodes.INVALID_CARD_STATUS);
    	}
    }

    @Override
    public boolean returnError() {
        return this.returnError;
    }


}
