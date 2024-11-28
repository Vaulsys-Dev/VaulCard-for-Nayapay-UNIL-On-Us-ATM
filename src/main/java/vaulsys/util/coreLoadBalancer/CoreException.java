package vaulsys.util.coreLoadBalancer;

public abstract class CoreException extends Exception {
    private String exceptionType = null;
    private Object causeObject;

    protected CoreException(Throwable cause, Object causeObject, String exceptionType) {
        super(cause);
        this.causeObject = causeObject;
        this.exceptionType = exceptionType;
    }

    
    

    protected CoreException(String message, Object causeObject, String exceptionType) {
        super(message);
        this.causeObject = causeObject;
        this.exceptionType = exceptionType;
    }

    public CoreException() {
    }

    public CoreException(String message) {
        super(message);
    }

    public CoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public CoreException(Throwable cause) {
        super(cause);
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }


    public Object getCauseObject() {
        return causeObject;
    }

    public void setCauseObject(Object causeObject) {
        this.causeObject = causeObject;
    }

}
