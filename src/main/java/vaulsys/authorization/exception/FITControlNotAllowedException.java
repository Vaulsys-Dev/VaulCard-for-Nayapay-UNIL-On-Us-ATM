package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class FITControlNotAllowedException extends AuthorizationException {

	private String rsCode;
	
    @Override
    public void alterIfxByErrorType(Ifx ifx) {
    	if (rsCode == null || rsCode.isEmpty()){
    		ifx.setRsCode(ISOResponseCodes.ACCOUNT_LOCKED);
    	}else{
    		ifx.setRsCode(rsCode);
    	}
    }

    @Override
    public boolean returnError() {
        return true;
    }


    public FITControlNotAllowedException(String s, String rsCode) {
        super(s);
        this.rsCode = rsCode;
    }

}
