package vaulsys.exception;

public class WebServiceFailException extends Exception {
	private static final long serialVersionUID = 1L;

	public WebServiceFailException(String serviceName, Throwable cause) {
		super("exception occurred using the web service '" + serviceName + "'", cause);
	}
}
