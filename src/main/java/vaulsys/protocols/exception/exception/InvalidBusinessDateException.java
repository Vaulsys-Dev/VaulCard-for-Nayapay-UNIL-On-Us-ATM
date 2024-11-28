package vaulsys.protocols.exception.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class InvalidBusinessDateException extends DecisionMakerExceptionImp {

    public InvalidBusinessDateException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public InvalidBusinessDateException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public InvalidBusinessDateException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public InvalidBusinessDateException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
    	ifx.setRsCode( ISOResponseCodes.REFER_TO_ISSUER);
//        ifx.Status.Severity = SeverityEnum.Error;
//        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();


    }

    @Override
    public boolean returnError() {
        return true;
    }

}
