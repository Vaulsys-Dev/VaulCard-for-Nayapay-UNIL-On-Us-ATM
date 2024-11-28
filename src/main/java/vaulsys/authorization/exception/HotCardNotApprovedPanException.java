package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;


//TASK Task015 : HotCard
//AldComment Add for HotCard feature
public class HotCardNotApprovedPanException extends ATMAuthorizationException{

	@Override
	public void alterIfxByErrorType(Ifx ifx) {
		ifx.setRsCode(ISOResponseCodes.HOTCARD_NOT_APPROVED+ "");
	}

	@Override
	public boolean returnError() {
		return true;
	}
	
    public HotCardNotApprovedPanException() {
    }

    public HotCardNotApprovedPanException(String s) {
        super(s);
    }	

}
