package vaulsys.authorization.exception.topup;

import vaulsys.authorization.exception.AuthorizationException;

public class UnsupportedTopupOrganization extends AuthorizationException {

    public UnsupportedTopupOrganization(String s) {
        super(s);
    }

    public UnsupportedTopupOrganization() {
    }

    public UnsupportedTopupOrganization(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public UnsupportedTopupOrganization(Throwable arg0) {
        super(arg0);
    }
}
