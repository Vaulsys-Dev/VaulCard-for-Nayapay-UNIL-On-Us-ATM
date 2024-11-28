package vaulsys.message.exception;

import vaulsys.exception.base.DecisionMakerException;
import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class ReversalOriginatorNotApproved extends DecisionMakerExceptionImp {

    public ReversalOriginatorNotApproved() {
        // TODO Auto-generated constructor stub
    }

    public ReversalOriginatorNotApproved(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public ReversalOriginatorNotApproved(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public ReversalOriginatorNotApproved(String message, Throwable cause) {
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

        	if (TerminalType.ATM.equals(ifx.getTerminalType()))
        		ifx.setRsCode(ISOResponseCodes.CARD_EXPIRED);
        		
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
