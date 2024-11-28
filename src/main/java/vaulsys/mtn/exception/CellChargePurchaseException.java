package vaulsys.mtn.exception;

import vaulsys.exception.impl.DecisionMakerExceptionImp;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class CellChargePurchaseException extends DecisionMakerExceptionImp {

	public CellChargePurchaseException(String message) {
		super(message);
	}

	public CellChargePurchaseException() {
		super();
	}

	public CellChargePurchaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public CellChargePurchaseException(Throwable cause) {
		super(cause);
	}

	@Override
	public boolean returnError() {
		return true;
	}

	@Override
	public void alterIfxByErrorType(Ifx ifx) {
		ifx.setRsCode(ISOResponseCodes.MESSAGE_FORMAT_ERROR);
	}
	
}
