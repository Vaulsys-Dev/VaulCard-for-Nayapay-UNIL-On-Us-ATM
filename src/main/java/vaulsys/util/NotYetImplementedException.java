package vaulsys.util;

public class NotYetImplementedException extends RuntimeException {

    static final long serialVersionUID = 84208443L;

    public NotYetImplementedException() {
    }

    public NotYetImplementedException(String message) {
        super(message);
    }

    public NotYetImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotYetImplementedException(Throwable cause) {
        super(cause);
    }
}
