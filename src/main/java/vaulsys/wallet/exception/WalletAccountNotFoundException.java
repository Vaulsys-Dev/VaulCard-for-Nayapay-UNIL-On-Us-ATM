package vaulsys.wallet.exception;

import vaulsys.exception.base.DecisionMakerException;
import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;

/**
 * Created by Raza
 */
public class WalletAccountNotFoundException extends DecisionMakerExceptionImp {
    private Boolean returnError;

    public WalletAccountNotFoundException(String s) {
        super(s);
        this.returnError = true;
    }

    public WalletAccountNotFoundException() {
        super();
        this.returnError = true;
    }

    public WalletAccountNotFoundException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        this.returnError = true;
    }

    public WalletAccountNotFoundException(Throwable arg0) {
        super(arg0);
        this.returnError = true;
    }

    public WalletAccountNotFoundException(String s, Boolean returnError) {
        super(s);
        this.returnError = returnError;
    }

    public WalletAccountNotFoundException(Boolean returnError) {
        super();
        this.returnError = returnError;
    }

    public WalletAccountNotFoundException(String arg0, Throwable arg1, Boolean returnError) {
        super(arg0, arg1);
        this.returnError = returnError;
    }

    public WalletAccountNotFoundException(Throwable arg0, Boolean returnError) {
        super(arg0);
        this.returnError = returnError;
    }

    public void alterIfxByErrorType(Ifx ifx) {
        if (getCause()!= null && getCause() instanceof DecisionMakerException){
            DecisionMakerException cause = (DecisionMakerException) getCause();
            cause.alterIfxByErrorType(ifx);
        }
        else if (!Util.hasText(ifx.getRsCode())) {
            ifx.setRsCode(ISOResponseCodes.WARM_CARD); //14-Invalid account number (no such number)
        }
    }

    @Override
    public boolean returnError() {
        return this.returnError ;
    }

}
