package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class InvalidFieldException extends AuthorizationException {
    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(ISOResponseCodes.INVALID_CARD_STATUS);
    }

    @Override
    public boolean returnError() {
        return true;
    }

    public InvalidFieldException(String s) {
        super(s);
    }

    public InvalidFieldException() {
    }

    public InvalidFieldException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public InvalidFieldException(Throwable arg0) {
        super(arg0);
    }


}
