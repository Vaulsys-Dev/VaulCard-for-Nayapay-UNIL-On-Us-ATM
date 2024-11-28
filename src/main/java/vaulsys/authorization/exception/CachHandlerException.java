package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;

public class CachHandlerException extends ATMAuthorizationException {

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(ATMErrorCodes.ATM_CACH_HANDLER + "");
//        ifx.setRsCode(ShetabErrorCodes.SHETAB_SUCCESS);
//        ifx.Status.Severity = SeverityEnum.Error;
//        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();

    }

    @Override
    public boolean returnError() {
        return true;
    }


    public CachHandlerException() {
    }

    public CachHandlerException(String s) {
        super(s);
    }

}