package vaulsys.authorization.exception;

public class ATMAuthorizationException extends AuthorizationException {

    public ATMAuthorizationException(String s) {
        super(s);
    }

    public ATMAuthorizationException() {
    }

    public ATMAuthorizationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public ATMAuthorizationException(Throwable arg0) {
        super(arg0);
    }
}
