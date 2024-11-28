package vaulsys.message.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class DuplicateMessageException extends DecisionMakerExceptionImp {

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(ISOResponseCodes.INVALID_CURRENCY_CODE);
//        ifx.Status.Severity = SeverityEnum.Error;
//        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();


    }

    @Override
    public boolean returnError() {
        return true;
    }

    public DuplicateMessageException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public DuplicateMessageException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public DuplicateMessageException() {
        super();
    }

    public DuplicateMessageException(String s) {
        super(s);
    }
}
