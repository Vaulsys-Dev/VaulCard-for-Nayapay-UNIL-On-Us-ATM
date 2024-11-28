package vaulsys.auditlog;

public class AuditLogException extends Exception{
	 private static final long serialVersionUID = 64194334358561889L;
	    Exception nested = null;

	    public AuditLogException() {
	        super();
	    }

	    public AuditLogException(String s) {
	        super(s);
	    }

	    public AuditLogException(Exception e) {
	        super(e);
	    }

	    public AuditLogException(String s, Exception e) {
	        super(s, e);
	    }

	   
}
