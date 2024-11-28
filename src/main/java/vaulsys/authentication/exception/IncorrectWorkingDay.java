package vaulsys.authentication.exception;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;

public class IncorrectWorkingDay extends AuthenticationException {

    @Override
    public void alterIfxByErrorType(Ifx ifx) {
        ifx.setRsCode(ISOResponseCodes.REFER_TO_ISSUER);
//        ifx.Status.Severity = SeverityEnum.Error;
//        ifx.Status.StatusDesc = this.getClass().getSimpleName() + ": " + getMessage();

    }

    @Override
    public boolean returnError() {
        return true;
    }

    public IncorrectWorkingDay(String s) {
        super(s);
        // TODO Auto-generated constructor stub
    }

    public IncorrectWorkingDay() {
        // TODO Auto-generated constructor stub
    }

    public IncorrectWorkingDay(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    public IncorrectWorkingDay(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

}
