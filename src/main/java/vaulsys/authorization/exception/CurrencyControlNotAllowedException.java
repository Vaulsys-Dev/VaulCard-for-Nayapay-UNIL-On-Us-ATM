package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;

public class CurrencyControlNotAllowedException extends AuthorizationException {

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        super.alterIfxByErrorType(ifx);
//        ifx.Status.Severity = SeverityEnum.Error;
//        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();

    }

    @Override
    public boolean returnError() {
        return true;
    }


    public CurrencyControlNotAllowedException(String s) {
        super(s);
    }

}
