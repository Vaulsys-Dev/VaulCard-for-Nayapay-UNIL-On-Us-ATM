package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;

public class SufficientAmountException extends ATMAuthorizationException {

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode (ATMErrorCodes.ATM_NOT_SUFFICIENT_AMOUNT + "");
//        ifx.Status.Severity = SeverityEnum.Error;
//        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();

    }

    @Override
    public boolean returnError() {
        return true;
    }


    public SufficientAmountException() {
    }

    public SufficientAmountException(String s) {
        super(s);
    }

    public SufficientAmountException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public SufficientAmountException(Throwable arg0) {
        super(arg0);
    }
}