package vaulsys.message.exception;

import vaulsys.exception.base.DecisionMakerException;
import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class MessageBindingException extends DecisionMakerExceptionImp {

	
	public MessageBindingException() {
		super();
	}

	public MessageBindingException(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageBindingException(String message) {
		super(message);
	}

	public MessageBindingException(Throwable cause) {
		super(cause);
	}

	@Override
	public void alterIfxByErrorType(Ifx ifx) {
		if (getCause() != null && getCause() instanceof DecisionMakerException) {
            ((DecisionMakerException) getCause()).alterIfxByErrorType(ifx);
        } else {
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
