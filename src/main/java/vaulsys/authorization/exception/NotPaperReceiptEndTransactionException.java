package vaulsys.authorization.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.constants.ATMErrorCodes;

public class NotPaperReceiptEndTransactionException extends ATMAuthorizationException {

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(ATMErrorCodes.ATM_NOT_PAPER_RECEIPT_END_TRANSACTION + "");
//        ifx.setRsCode(ShetabErrorCodes.SHETAB_SUCCESS);
//        ifx.Status.Severity = SeverityEnum.Error;
//        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();

    }

    @Override
    public boolean returnError() {
        return true;
    }


    public NotPaperReceiptEndTransactionException() {
    }

    public NotPaperReceiptEndTransactionException(String s) {
        super(s);
    }

}