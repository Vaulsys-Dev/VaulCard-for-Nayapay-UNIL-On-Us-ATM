package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;

public class TransactionAmountNotAcceptableException extends AuthorizationException {

	private String rsCode;
	
    public TransactionAmountNotAcceptableException(String s, String rsCode) {
        super(s);
        this.rsCode = rsCode;
    }

    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(rsCode);
    }

    @Override
    public boolean returnError() {
        return true;
    }

}
