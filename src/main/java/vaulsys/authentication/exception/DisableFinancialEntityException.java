package vaulsys.authentication.exception;

public class DisableFinancialEntityException extends AuthenticationException{
	 @Override
	 public boolean returnError() {
		 return true;
	 }
	 
	 public DisableFinancialEntityException(String s) {
		 super(s);
		        // TODO Auto-generated constructor stub
	 }
	 
	 public DisableFinancialEntityException() {
		        // TODO Auto-generated constructor stub
	 }
	 
	 public DisableFinancialEntityException(String arg0, Throwable arg1) {
		 super(arg0, arg1);
		        // TODO Auto-generated constructor stub
	 }
	 
	 public DisableFinancialEntityException(Throwable arg0) {
		 super(arg0);
		        // TODO Auto-generated constructor stub
	 }
}
