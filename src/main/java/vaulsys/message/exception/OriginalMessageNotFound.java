package vaulsys.message.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class OriginalMessageNotFound extends 
        DecisionMakerExceptionImp {

    private boolean returnError;

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
    	ifx.setRsCode(ISOResponseCodes.CARD_EXPIRED);
//        ifx.Status.Severity = SeverityEnum.Error;
//        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();
    }

    @Override
    public boolean returnError() {
        return returnError;
    }

    public OriginalMessageNotFound(String message, Throwable cause, boolean returnError) {
        super(message, cause);
        this.returnError = returnError;
    }

    public OriginalMessageNotFound(String message, boolean returnError) {
        super(message);
        this.returnError = returnError;
    }

    public OriginalMessageNotFound(Throwable cause, boolean returnError) {
        super(cause);
        this.returnError = returnError;
    }

    public OriginalMessageNotFound(boolean returnError) {
        super();
        this.returnError = returnError;
    }

    public OriginalMessageNotFound() {
        super();
        returnError = true;
    }

    public OriginalMessageNotFound(String message, Throwable cause) {
        super(message, cause);
        returnError = true;
    }

    public OriginalMessageNotFound(String message) {
        super(message);
        returnError = true;
    }

    public OriginalMessageNotFound(Throwable cause) {
        super(cause);
        returnError = true;
    }


}
