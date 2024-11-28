package vaulsys.message.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class ReversalOriginatorNotFoundException extends DecisionMakerExceptionImp {

    public ReversalOriginatorNotFoundException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ReversalOriginatorNotFoundException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public ReversalOriginatorNotFoundException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public ReversalOriginatorNotFoundException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        //TODO: JUST FOR POS
    	ifx.setRsCode(ISOResponseCodes.CARD_EXPIRED);
//        ifx.Status.Severity = SeverityEnum.Error;
//        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();


    }

    @Override
    public boolean returnError() {
        return true;
    }

}
