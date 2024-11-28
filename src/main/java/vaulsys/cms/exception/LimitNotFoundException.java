package vaulsys.cms.exception;

import vaulsys.exception.base.DecisionMakerException;
import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.imp.Ifx;

public class LimitNotFoundException extends DecisionMakerExceptionImp {

	private Boolean returnError;

    public LimitNotFoundException(String s) {
        super(s);
        this.returnError = true;
    }

    public LimitNotFoundException() {
        super();
        this.returnError = true;
    }

    public LimitNotFoundException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        this.returnError = true;
    }

    public LimitNotFoundException(Throwable arg0) {
        super(arg0);
        this.returnError = true;
    }

    public LimitNotFoundException(String s, Boolean returnError) {
    	super(s);
    	this.returnError = returnError;
    }

    public LimitNotFoundException(Boolean returnError) {
    	super();
    	this.returnError = returnError;
    }

    public LimitNotFoundException(String arg0, Throwable arg1, Boolean returnError) {
    	super(arg0, arg1);
    	this.returnError = returnError;
    }

    public LimitNotFoundException(Throwable arg0, Boolean returnError) {
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
        return this.returnError ;
    }

}
