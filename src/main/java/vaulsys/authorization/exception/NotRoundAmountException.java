package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;

public class NotRoundAmountException extends ATMAuthorizationException {

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(ATMErrorCodes.ATM_NOT_ROUND_AMOUNT + "");
//        ifx.setRsCode(ShetabErrorCodes.SHETAB_SUCCESS);
//        ifx.Status.Severity = SeverityEnum.Error;
//        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();

    }

    @Override
    public boolean returnError() {
        return true;
    }


    public NotRoundAmountException() {
    }

    public NotRoundAmountException(String s) {
        super(s);
    }

}