package vaulsys.authorization.exception;

import vaulsys.exception.base.DecisionMakerException;
import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.util.Util;

public class AuthorizationException extends DecisionMakerExceptionImp {
	
	private Boolean returnError;

    public AuthorizationException(String s) {
        super(s);
        this.returnError = true; //false; //Raza TXNPERMISSION changing to true
    }

    public AuthorizationException() {
        super();
        this.returnError = true; //false; //Raza TXNPERMISSION changing to true
    }

    public AuthorizationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        this.returnError = true; //false; //Raza TXNPERMISSION changing to true
    }

    public AuthorizationException(Throwable arg0) {
        super(arg0);
        this.returnError = true; //false; //Raza TXNPERMISSION changing to true
    }

    public AuthorizationException(String s, Boolean returnError) {
    	super(s);
    	this.returnError = returnError;
    }
    
    public AuthorizationException(Boolean returnError) {
    	super();
    	this.returnError = returnError;
    }
    
    public AuthorizationException(String arg0, Throwable arg1, Boolean returnError) {
    	super(arg0, arg1);
    	this.returnError = returnError;
    }
    
    public AuthorizationException(Throwable arg0, Boolean returnError) {
    	super(arg0);
    	this.returnError = returnError;
    }
    
    public void alterIfxByErrorType(Ifx ifx) {
    	if (getCause()!= null && getCause() instanceof DecisionMakerException){
    		DecisionMakerException cause = (DecisionMakerException) getCause();
    		cause.alterIfxByErrorType(ifx);
    	}else{
            if(Util.hasText(ifx.getRsCode()) && ifx.getRsCode().equals(ISOResponseCodes.APPROVED)) //Raza adding for TXN RULE
    		ifx.setRsCode(ISOResponseCodes.INVALID_CARD_STATUS);
    	}
    }

    @Override
    public boolean returnError() {
        return this.returnError ;
    }

}
