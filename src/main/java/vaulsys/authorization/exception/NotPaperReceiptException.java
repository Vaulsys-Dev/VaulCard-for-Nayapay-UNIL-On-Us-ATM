package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;

public class NotPaperReceiptException extends ATMAuthorizationException {

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT+ "");

    }

    @Override
    public boolean returnError() {
        return true;
    }


    public NotPaperReceiptException() {
    }

    public NotPaperReceiptException(String s) {
        super(s);
    }

}