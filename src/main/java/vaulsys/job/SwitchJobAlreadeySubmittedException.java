package vaulsys.job;

public class SwitchJobAlreadeySubmittedException extends SwitchJobException {
    
    public SwitchJobAlreadeySubmittedException(Throwable cause) {
        super(cause);
    }

    public SwitchJobAlreadeySubmittedException(String message) {
        super(message);
    }

    public SwitchJobAlreadeySubmittedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SwitchJobAlreadeySubmittedException(Throwable cause, int baseCode, int errorCode) {
        super(cause, baseCode, errorCode);
    }

    public SwitchJobAlreadeySubmittedException(String msg, int baseCode, int errorCode) {
        super(msg, baseCode, errorCode);
    }

    public SwitchJobAlreadeySubmittedException(String msg, Throwable cause, int baseCode, int errorCode) {
        super(msg, cause, baseCode, errorCode);
    }
}
