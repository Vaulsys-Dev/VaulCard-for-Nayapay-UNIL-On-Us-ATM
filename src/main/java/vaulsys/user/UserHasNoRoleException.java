package vaulsys.user;

import vaulsys.util.SwitchRuntimeException;

public class UserHasNoRoleException extends SwitchRuntimeException {

    public UserHasNoRoleException() {
    }

    public UserHasNoRoleException(String message) {
        super(message);
    }

    public UserHasNoRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
