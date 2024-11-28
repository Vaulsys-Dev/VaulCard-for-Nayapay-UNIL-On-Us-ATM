package vaulsys.network.channel.base;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class CommunicationMethod implements IEnum {

    private static final byte SAME_SOCKET_VALUE = 1;
    private static final byte ANOTHER_SOCKET_VALUE = 2;

    public static final CommunicationMethod SAME_SOCKET = new CommunicationMethod(SAME_SOCKET_VALUE);
    public static final CommunicationMethod ANOTHER_SOCKET = new CommunicationMethod(ANOTHER_SOCKET_VALUE);

    private byte method;

    public CommunicationMethod() {
    }

    public CommunicationMethod(byte methd) {
        this.method = methd;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommunicationMethod that = (CommunicationMethod) o;

        if (method != that.method) return false;

        return true;
    }

    public int hashCode() {
        return (int) method;
    }
}
