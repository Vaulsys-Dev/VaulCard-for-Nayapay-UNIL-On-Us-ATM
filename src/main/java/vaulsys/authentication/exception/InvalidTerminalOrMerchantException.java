package vaulsys.authentication.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class InvalidTerminalOrMerchantException extends AuthenticationException{
    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(ISOResponseCodes.ACCOUNT_INACTIVE);
    }

    @Override
    public boolean returnError() {
        return true;
    }

    public InvalidTerminalOrMerchantException(String s) {
        super(s);
        // TODO Auto-generated constructor stub
    }

    public InvalidTerminalOrMerchantException() {
        // TODO Auto-generated constructor stub
    }

    public InvalidTerminalOrMerchantException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public InvalidTerminalOrMerchantException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }


}
