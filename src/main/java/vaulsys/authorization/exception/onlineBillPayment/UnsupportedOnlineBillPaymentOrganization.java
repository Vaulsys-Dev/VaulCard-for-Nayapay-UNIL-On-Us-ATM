package vaulsys.authorization.exception.onlineBillPayment;

import vaulsys.authorization.exception.AuthorizationException;

public class UnsupportedOnlineBillPaymentOrganization extends AuthorizationException {
	public UnsupportedOnlineBillPaymentOrganization(String s) {
		super(s);
	}
	
	public UnsupportedOnlineBillPaymentOrganization() {
	}
	public UnsupportedOnlineBillPaymentOrganization(String arg0, Throwable arg1) {
	        super(arg0, arg1);
	    }

	    public UnsupportedOnlineBillPaymentOrganization(Throwable arg0) {
	        super(arg0);
	    }

}
