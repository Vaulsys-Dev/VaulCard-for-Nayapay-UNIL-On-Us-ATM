package vaulsys.util;

public class SwitchRuntimeException extends RuntimeException {

    final private int errorCode;

    public SwitchRuntimeException() {
        this.errorCode = 0;
    }

    public SwitchRuntimeException(Throwable cause) {
        super(cause);
        this.errorCode = 0;
    }

    public SwitchRuntimeException(String message) {
        super(message);
        this.errorCode = 0;
    }

    public SwitchRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 0;
    }

    public SwitchRuntimeException(Throwable cause, int baseCode, int errorCode) {
        super(cause);
        this.errorCode = errorCode + baseCode;
    }

    public SwitchRuntimeException(String msg, int baseCode, int errorCode) {
        super(msg);
        this.errorCode = errorCode + baseCode;
    }

    public SwitchRuntimeException(String msg, Throwable cause, int baseCode, int errorCode) {
        super(msg, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + getMessage() + "("+errorCode+")";
    }

    public String getErrorKey() {
        return "exception."+getErrorCode();
    }
}
