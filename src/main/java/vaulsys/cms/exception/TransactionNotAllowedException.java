package vaulsys.cms.exception;

import vaulsys.exception.base.DecisionMakerException;
import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;

/**
 * Created by Raza
 */
public class TransactionNotAllowedException extends DecisionMakerExceptionImp {
    private Boolean returnError;

    public TransactionNotAllowedException(String s) {
        super(s);
        this.returnError = true;
    }

    public TransactionNotAllowedException() {
        super();
        this.returnError = true;
    }

    public TransactionNotAllowedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        this.returnError = true;
    }

    public TransactionNotAllowedException(Throwable arg0) {
        super(arg0);
        this.returnError = true;
    }

    public TransactionNotAllowedException(String s, Boolean returnError) {
        super(s);
        this.returnError = returnError;
    }

    public TransactionNotAllowedException(Boolean returnError) {
        super();
        this.returnError = returnError;
    }

    public TransactionNotAllowedException(String arg0, Throwable arg1, Boolean returnError) {
        super(arg0, arg1);
        this.returnError = returnError;
    }

    public TransactionNotAllowedException(Throwable arg0, Boolean returnError) {
        super(arg0);
        this.returnError = returnError;
    }

    public void alterIfxByErrorType(Ifx ifx) {
        if (getCause()!= null && getCause() instanceof DecisionMakerException){
            DecisionMakerException cause = (DecisionMakerException) getCause();
            cause.alterIfxByErrorType(ifx);
        }
        else if (!Util.hasText(ifx.getRsCode())) {
            ifx.setRsCode(ISOResponseCodes.BANK_LINK_DOWN); //Update Code for Txn-Not Allowed
        }
    }

    @Override
    public boolean returnError() {
        return this.returnError ;
    }

}
