package vaulsys.protocols.exception.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class CantAddNecessaryDataToIfxException extends DecisionMakerExceptionImp {

    public CantAddNecessaryDataToIfxException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public CantAddNecessaryDataToIfxException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public CantAddNecessaryDataToIfxException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public CantAddNecessaryDataToIfxException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
    	ifx.setRsCode(ISOResponseCodes.INVALID_CARD_STATUS);

    }

    @Override
    public boolean returnError() {
        return true;
    }

}
