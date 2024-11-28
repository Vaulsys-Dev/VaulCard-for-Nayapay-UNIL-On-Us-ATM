package vaulsys.authorization.exception.card;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class CardAuthorizerException extends AuthorizationException  {

    public CardAuthorizerException() {
        super();
    }

    public CardAuthorizerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CardAuthorizerException(String message) {
        super(message);
    }

    public CardAuthorizerException(Throwable cause) {
        super(cause);
    }

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(ISOResponseCodes.TRANSACTION_TIMEOUT);
        // ifx.Status.Severity = SeverityEnum.Error;
        // ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " +
        // getMessage();

    }

    @Override
    public boolean returnError() {
        return true;
    }

}
