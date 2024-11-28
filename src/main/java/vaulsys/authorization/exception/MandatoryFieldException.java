package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class MandatoryFieldException extends AuthorizationException {
    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(ISOResponseCodes.ORIGINAL_TRANSACTION_NOT_FOUND);
    }

    @Override
    public boolean returnError() {
        return true;
    }

    public MandatoryFieldException(String s) {
        super(s);
        // TODO Auto-generated constructor stub
    }

    public MandatoryFieldException() {
        // TODO Auto-generated constructor stub
    }

    public MandatoryFieldException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public MandatoryFieldException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }


}
