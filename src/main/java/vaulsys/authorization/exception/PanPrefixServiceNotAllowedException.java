package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class PanPrefixServiceNotAllowedException extends AuthorizationException {

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
    	ifx.setRsCode(ISOResponseCodes.BANK_LINK_DOWN);
    }

    @Override
    public boolean returnError() {
        return true;
    }


    public PanPrefixServiceNotAllowedException(String s) {
        super(s);
    }

}
