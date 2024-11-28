package vaulsys.netmgmt.exceptions;

public class InvalidNetworkMessageException extends Exception {
    public InvalidNetworkMessageException(String string) {
        super(string);
    }

    public InvalidNetworkMessageException() {
        super();
    }
}
