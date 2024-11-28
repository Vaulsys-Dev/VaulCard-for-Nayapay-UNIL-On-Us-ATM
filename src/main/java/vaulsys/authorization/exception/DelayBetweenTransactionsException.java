package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;

public class DelayBetweenTransactionsException extends AuthorizationException {

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        super.alterIfxByErrorType(ifx);
    }

    @Override
    public boolean returnError() {
        return true;
    }


    public DelayBetweenTransactionsException(String s) {
        super(s);
    }

}
