package vaulsys.job;

import vaulsys.util.SwitchRuntimeException;

public class SwitchJobException extends SwitchRuntimeException {

    public SwitchJobException(Throwable cause) {
        super(cause);
    }

    public SwitchJobException(String message) {
        super(message);
    }

    public SwitchJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public SwitchJobException(Throwable cause, int baseCode, int errorCode) {
        super(cause, baseCode, errorCode);
    }

    public SwitchJobException(String msg, int baseCode, int errorCode) {
        super(msg, baseCode, errorCode);
    }

    public SwitchJobException(String msg, Throwable cause, int baseCode, int errorCode) {
        super(msg, cause, baseCode, errorCode);
    }
}
