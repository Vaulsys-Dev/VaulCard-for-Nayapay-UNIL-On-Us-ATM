package vaulsys.authorization.exception.card;

public class CardNotAllowedException extends CardAuthorizerException {

    public CardNotAllowedException() {
    }

    public CardNotAllowedException(String message) {
        super(message);
    }

    public CardNotAllowedException(Throwable cause) {
        super(cause);
    }

    public CardNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

}
