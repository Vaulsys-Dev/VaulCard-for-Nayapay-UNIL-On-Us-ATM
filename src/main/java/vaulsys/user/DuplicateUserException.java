package vaulsys.user;

import vaulsys.util.SwitchRuntimeException;

public class DuplicateUserException extends SwitchRuntimeException {

    public DuplicateUserException() {
    }

    public DuplicateUserException(String message) {
        super(message);
    }

    public DuplicateUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
