package vaulsys.user;

import vaulsys.util.SwitchRuntimeException;

public class UserNotFoundException extends SwitchRuntimeException {

    public UserNotFoundException() {
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
