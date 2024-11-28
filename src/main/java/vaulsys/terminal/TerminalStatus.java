package vaulsys.terminal;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class TerminalStatus implements IEnum {

    private static final byte INSTALL_VALUE = 1;
    private static final byte NOT_INSTALL_VALUE = 2;

    public static final TerminalStatus INSTALL = new TerminalStatus(INSTALL_VALUE);
    public static final TerminalStatus NOT_INSTALL = new TerminalStatus(NOT_INSTALL_VALUE);

    private byte status;

    TerminalStatus() {
    }

    TerminalStatus(byte status) {
        this.status = status;
    }

    public byte getStatus() {
		return status;
	}

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof TerminalStatus)) return false;

        TerminalStatus posStatus = (TerminalStatus) o;

        if (status != posStatus.status) return false;

        return true;
    }

    public int hashCode() {
        return (int) status;
    }
    
    //TASK Task074 
	@Override
	public String toString() {
		switch (status) {
			case INSTALL_VALUE:
				return "INSTALL";
			case NOT_INSTALL_VALUE:
				return "NOT_INSTALL";
			default:
				return "UNKNOWN";	
		}
	}     
}
