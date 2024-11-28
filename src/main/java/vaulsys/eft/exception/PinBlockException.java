package vaulsys.eft.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.exception.base.DecisionMakerException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.util.Util;

public class PinBlockException extends DecisionMakerExceptionImp{
	private Boolean returnError;

    public PinBlockException(String s) {
        super(s);
        this.returnError = false;
    }

    public PinBlockException() {
        super();
        this.returnError = false;
    }

    public PinBlockException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        this.returnError = false;
    }

    public PinBlockException(Throwable arg0) {
        super(arg0);
        this.returnError = false;
    }

    public PinBlockException(String s, Boolean returnError) {
    	super(s);
    	this.returnError = returnError;
    }
    
    public PinBlockException(Boolean returnError) {
    	super();
    	this.returnError = returnError;
    }
    
    public PinBlockException(String arg0, Throwable arg1, Boolean returnError) {
    	super(arg0, arg1);
    	this.returnError = returnError;
    }
    
    public PinBlockException(Throwable arg0, Boolean returnError) {
    	super(arg0);
    	this.returnError = returnError;
    }
    
    public void alterIfxByErrorType(Ifx ifx) {
    	if (getCause()!= null && getCause() instanceof DecisionMakerException){
    		DecisionMakerException cause = (DecisionMakerException) getCause();
    		cause.alterIfxByErrorType(ifx);
    	}
        //m.rehman: if Resp Code is already set, then ignore
        else if (!Util.hasText(ifx.getRsCode())) {
    		ifx.setRsCode(ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
    	}
    }

    @Override
    public boolean returnError() {
        return this.returnError ;
    }

}
