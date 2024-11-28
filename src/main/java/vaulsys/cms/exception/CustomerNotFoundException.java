package vaulsys.cms.exception;

import vaulsys.exception.base.DecisionMakerException;
import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;

/**
 * Created by HP on 03-May-17.
 */
public class CustomerNotFoundException extends DecisionMakerExceptionImp {
    private Boolean returnError;

    public CustomerNotFoundException(String s) {
        super(s);
        this.returnError = true;
    }

    public CustomerNotFoundException() {
        super();
        this.returnError = true;
    }

    public CustomerNotFoundException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        this.returnError = true;
    }

    public CustomerNotFoundException(Throwable arg0) {
        super(arg0);
        this.returnError = true;
    }

    public CustomerNotFoundException(String s, Boolean returnError) {
        super(s);
        this.returnError = returnError;
    }

    public CustomerNotFoundException(Boolean returnError) {
        super();
        this.returnError = returnError;
    }

    public CustomerNotFoundException(String arg0, Throwable arg1, Boolean returnError) {
        super(arg0, arg1);
        this.returnError = returnError;
    }

    public CustomerNotFoundException(Throwable arg0, Boolean returnError) {
        super(arg0);
        this.returnError = returnError;
    }

    public void alterIfxByErrorType(Ifx ifx) {
        if (getCause()!= null && getCause() instanceof DecisionMakerException){
            DecisionMakerException cause = (DecisionMakerException) getCause();
            cause.alterIfxByErrorType(ifx);
        }
        else if (!Util.hasText(ifx.getRsCode())) {
            ifx.setRsCode(ISOResponseCodes.LIMIT_EXCEEDED); //01
        }
    }

    @Override
    public boolean returnError() {
        return this.returnError ;
    }

}
