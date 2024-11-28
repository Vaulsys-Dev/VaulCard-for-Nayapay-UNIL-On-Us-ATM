package vaulsys.authorization.exception.card;

public class DuplicateCardException extends Exception {

    public DuplicateCardException() {
    }

    public DuplicateCardException(String message) {
        super(message);
    }

    public DuplicateCardException(Throwable cause) {
        super(cause);
    }

    public DuplicateCardException(String message, Throwable cause) {
        super(message, cause);
    }

}
