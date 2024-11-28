package vaulsys.authorization.exception.card;

public class DuplicateCardGroupException extends CardAuthorizerException {

    public DuplicateCardGroupException() {
    }

    public DuplicateCardGroupException(String message) {
        super(message);
    }

    public DuplicateCardGroupException(Throwable cause) {
        super(cause);
    }

    public DuplicateCardGroupException(String message, Throwable cause) {
        super(message, cause);
    }

}
