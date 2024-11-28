package vaulsys.message.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.util.Util;

public class MessageAlreadyReversedException extends DecisionMakerExceptionImp {
    private boolean returnError = true;
    private String rsCode;

    public MessageAlreadyReversedException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public MessageAlreadyReversedException(String message, boolean b) {
        super(message);
        returnError = b;
    }

    public MessageAlreadyReversedException(String message, String rsCode, boolean b) {
        super(message);
        returnError = b;
        this.rsCode = rsCode;
    }
    
    public MessageAlreadyReversedException(String message) {
        super(message);
    }

    public MessageAlreadyReversedException(String message, String rsCode) {
        super(message);
        this.rsCode = rsCode;
    }

    
    public MessageAlreadyReversedException(Throwable cause) {
        super(cause);
    }

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        //TODO:For Hasin POS: INVALID_ACCOUNT
//    	if (rsCode != null && !"".equals(rsCode))
    	if (Util.hasText(rsCode))
    		ifx.setRsCode(rsCode);
    	else
    		ifx.setRsCode(ISOResponseCodes.INVALID_ACCOUNT);

    }

    @Override
    public boolean returnError() {
        return returnError;
    }

}
