package vaulsys.authorization.exception.card;

public class CardNotFoundException extends CardAuthorizerException {

    public CardNotFoundException() {
    }

    public CardNotFoundException(String message) {
        super(message);
    }

    public CardNotFoundException(Throwable cause) {
        super(cause);
    }

    public CardNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
