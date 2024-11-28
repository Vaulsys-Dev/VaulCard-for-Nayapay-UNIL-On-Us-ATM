package vaulsys.security.hsm.base.exception;


import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;

public class NotAvailableHSMChannelFoundException extends DecisionMakerExceptionImp {


    public NotAvailableHSMChannelFoundException() {
        super();
    }

    public NotAvailableHSMChannelFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAvailableHSMChannelFoundException(Throwable cause) {
        super(cause);

    }

    public NotAvailableHSMChannelFoundException(String message) {
        super(message);
    }

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
      //TODO: Define error code
    }

    @Override
    public boolean returnError() {
        return true;
    }
}
