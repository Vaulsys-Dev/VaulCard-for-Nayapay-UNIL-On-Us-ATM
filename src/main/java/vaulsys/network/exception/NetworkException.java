package vaulsys.network.exception;

import vaulsys.util.SwitchRuntimeException;

//TASK Task108 - (3080) Bug Resalat
public class NetworkException extends SwitchRuntimeException {

    public NetworkException() {
    }

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
