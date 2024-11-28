package vaulsys.mtn.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class AllChargeSoldException extends CellChargePurchaseException {

	public AllChargeSoldException(String message) {
		super(message);
	}

	@Override
	public void alterIfxByErrorType(Ifx ifx) {

		ifx.setRsCode(ISOResponseCodes.BAD_EXPIRY_DATE);
	}

}
