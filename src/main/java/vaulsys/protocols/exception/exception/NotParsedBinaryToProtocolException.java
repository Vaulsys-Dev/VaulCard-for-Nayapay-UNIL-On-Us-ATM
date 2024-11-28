package vaulsys.protocols.exception.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;

public class NotParsedBinaryToProtocolException extends DecisionMakerExceptionImp {
    public NotParsedBinaryToProtocolException() {
        super();
    }

    public NotParsedBinaryToProtocolException(String msg) {
        super(msg);
    }

    public NotParsedBinaryToProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotParsedBinaryToProtocolException(Throwable cause) {
        super(cause);
    }

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        //TODO
//        ifx.Status.Severity = SeverityEnum.Error;
//        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();

    }

    @Override
    public boolean returnError() {
        return false;
    }
}
