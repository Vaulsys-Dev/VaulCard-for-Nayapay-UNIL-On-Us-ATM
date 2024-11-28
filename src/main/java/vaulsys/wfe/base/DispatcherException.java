package vaulsys.wfe.base;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;

public class DispatcherException extends Exception {

    public DispatcherException() {
        super();
    }
    
    public DispatcherException(ISOException e) {
    	super(e);
    }

    public DispatcherException(Throwable cause) {
        super(cause);
    }
}
