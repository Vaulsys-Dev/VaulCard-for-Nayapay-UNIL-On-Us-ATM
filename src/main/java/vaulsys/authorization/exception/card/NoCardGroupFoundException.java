package vaulsys.authorization.exception.card;

public class NoCardGroupFoundException extends CardAuthorizerException {

    public NoCardGroupFoundException() {
        super();
    }

    public NoCardGroupFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoCardGroupFoundException(String message) {
        super(message);
    }

    public NoCardGroupFoundException(Throwable cause) {
        super(cause);
    }

}
