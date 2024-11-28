package vaulsys.protocols.exception.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class ReferenceTransactionNotFoundException extends DecisionMakerExceptionImp {

    public ReferenceTransactionNotFoundException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ReferenceTransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public ReferenceTransactionNotFoundException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public ReferenceTransactionNotFoundException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
    	ifx.setRsCode( ISOResponseCodes.CARD_EXPIRED);
//        ifx.Status.Severity = SeverityEnum.Error;
//        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();


    }

    @Override
    public boolean returnError() {
        return true;
    }

}
