package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;

public class NotSubsidiaryAccountException extends ATMAuthorizationException {

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(ATMErrorCodes.NO_SUBSIDIARY_ACCOUNT+ "");
    }

    @Override
    public boolean returnError() {
        return true;
    }


    public NotSubsidiaryAccountException() {
    }

    public NotSubsidiaryAccountException(String s) {
        super(s);
    }

}