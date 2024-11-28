package vaulsys.protocols.exception.exception;

import vaulsys.exception.base.DecisionMakerException;
import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class NotMappedProtocolToIfxException extends DecisionMakerExceptionImp {
    public NotMappedProtocolToIfxException() {
        super();
    }

    public NotMappedProtocolToIfxException(String msg) {
        super(msg);
    }

    public NotMappedProtocolToIfxException(Exception ex) {
        super(ex);
    }

    public NotMappedProtocolToIfxException(String string, Exception e) {
        super(string, e);
    }

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        if (getCause() != null && (getCause() instanceof DecisionMakerException))
            ((DecisionMakerException) getCause()).alterIfxByErrorType(ifx);
        else
        	ifx.setRsCode(ISOResponseCodes.INVALID_CARD_STATUS);
    }

    @Override
    public boolean returnError() {
    	
        if (getCause() != null && (getCause() instanceof DecisionMakerException))
            return ((DecisionMakerException) getCause()).returnError();

        return true;
    }
}
