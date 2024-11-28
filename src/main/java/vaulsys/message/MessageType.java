package vaulsys.message;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class MessageType implements IEnum {

    private static final byte INCOMING_VALUE = 0;
    private static final byte OUTGOING_VALUE = 1;
    private static final byte SCHEDULE_VALUE = 2;

    public static MessageType INCOMING = new MessageType(INCOMING_VALUE);
    public static MessageType OUTGOING = new MessageType(OUTGOING_VALUE);
    public static MessageType SCHEDULE = new MessageType(SCHEDULE_VALUE);

    private byte type;

    public MessageType() {
    }

    public MessageType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageType that = (MessageType) o;

        if (type != that.type) return false;

        return true;
    }

    public int hashCode() {
        return (int) type;
    }
}
