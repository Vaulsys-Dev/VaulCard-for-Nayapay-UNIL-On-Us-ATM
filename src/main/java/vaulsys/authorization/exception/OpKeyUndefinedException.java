package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;

public class OpKeyUndefinedException extends AuthorizationException {
    
	@Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(ATMErrorCodes.ATM_UNDEFINED_OPKEY+"");
    }

    @Override
    public boolean returnError() {
        return true;
    }

    public OpKeyUndefinedException(String s) {
        super(s);
        // TODO Auto-generated constructor stub
    }

    public OpKeyUndefinedException() {
        // TODO Auto-generated constructor stub
    }

    public OpKeyUndefinedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public OpKeyUndefinedException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }


}
