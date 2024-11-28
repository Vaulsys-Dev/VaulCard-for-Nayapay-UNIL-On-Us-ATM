package vaulsys.authorization.exception.topup;

import vaulsys.authorization.exception.AuthorizationException;

public class UnsupportedTransation extends AuthorizationException {
    public UnsupportedTransation(String s) {
        super(s);
    }

    public UnsupportedTransation() {
    }

    public UnsupportedTransation(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public UnsupportedTransation(Throwable arg0) {
        super(arg0);
    }
}
