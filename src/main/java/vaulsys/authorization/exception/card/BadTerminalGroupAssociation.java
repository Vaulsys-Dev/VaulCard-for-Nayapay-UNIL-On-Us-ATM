package vaulsys.authorization.exception.card;

public class BadTerminalGroupAssociation extends CardAuthorizerException {

    public BadTerminalGroupAssociation() {
    }

    public BadTerminalGroupAssociation(String message) {
        super(message);
    }

    public BadTerminalGroupAssociation(Throwable cause) {
        super(cause);
    }

    public BadTerminalGroupAssociation(String message, Throwable cause) {
        super(message, cause);
    }

}
