package vaulsys.mtn.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class NoChargeAvailableException extends CellChargePurchaseException {

	
	
	public NoChargeAvailableException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NoChargeAvailableException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public NoChargeAvailableException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public NoChargeAvailableException(String message) {
		super(message);
	}

	@Override
	public void alterIfxByErrorType(Ifx ifx) {

		ifx.setRsCode(ISOResponseCodes.ORIGINAL_AMOUNT_INCORRECT);

	}

}
