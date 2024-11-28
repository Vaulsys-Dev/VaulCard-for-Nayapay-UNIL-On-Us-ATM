package vaulsys.authentication.exception;


public class DisableTerminalException extends AuthenticationException{
	 
	    @Override
	    public boolean returnError() {
	        return true;
	    }

	    public DisableTerminalException(String s) {
	        super(s);
	        // TODO Auto-generated constructor stub
	    }

	    public DisableTerminalException() {
	        // TODO Auto-generated constructor stub
	    }

	    public DisableTerminalException(String arg0, Throwable arg1) {
	        super(arg0, arg1);
	        // TODO Auto-generated constructor stub
	    }

	    public DisableTerminalException(Throwable arg0) {
	        super(arg0);
	        // TODO Auto-generated constructor stub
	    }



}
