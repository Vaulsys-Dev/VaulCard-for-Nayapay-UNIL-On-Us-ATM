package vaulsys.message.exception;

import vaulsys.exception.base.DecisionMakerException;
import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class ReturnOfTransactionNotAllowed extends DecisionMakerExceptionImp {

    public ReturnOfTransactionNotAllowed() {
        // TODO Auto-generated constructor stub
    }

    public ReturnOfTransactionNotAllowed(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public ReturnOfTransactionNotAllowed(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public ReturnOfTransactionNotAllowed(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        //For hasin pos

        if (getCause() != null && getCause() instanceof DecisionMakerException) {
            ((DecisionMakerException) getCause()).alterIfxByErrorType(ifx);
        } else {
        	ifx.setRsCode(ISOResponseCodes.ORIGINAL_NOT_AUTHORIZED);
//            ifx.Status.Severity = SeverityEnum.Error;
//            ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();
        }


    }

    @Override
    public boolean returnError() {
        if (getCause() != null && getCause() instanceof DecisionMakerException) {
            DecisionMakerException cause = (DecisionMakerException) getCause();
            return cause.returnError();
        }
        return true;
    }

}
