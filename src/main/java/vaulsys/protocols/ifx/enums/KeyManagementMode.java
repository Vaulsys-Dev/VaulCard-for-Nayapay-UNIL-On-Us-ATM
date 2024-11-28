package vaulsys.protocols.ifx.enums;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class KeyManagementMode implements Cloneable, Serializable {

	private static final int ISSUER_PIN_VALUE = 0;
	private static final int ACQUIER_PIN_VALUE = 1;
	private static final int ISSUER_MAC_VALUE = 2;
	private static final int ACQUIER_MAC_VALUE = 3;
	private static final int ACQ_ISS_PIN_VALUE = 4; //Raza MASTERCARD KEY EXCHANGE

	public static final KeyManagementMode ISSUER_PIN = new KeyManagementMode(ISSUER_PIN_VALUE);
	public static final KeyManagementMode ACQUIER_PIN = new KeyManagementMode(ACQUIER_PIN_VALUE);
	public static final KeyManagementMode ISSUER_MAC = new KeyManagementMode(ISSUER_MAC_VALUE);
	public static final KeyManagementMode ACQUIER_MAC = new KeyManagementMode(ACQUIER_MAC_VALUE);
	public static final KeyManagementMode ACQ_ISS_PIN = new KeyManagementMode(ACQ_ISS_PIN_VALUE); //Raza MASTERCARD KEY EXCHANGE

	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public KeyManagementMode() {
		super();
	}

	public KeyManagementMode(int type) {
		super();
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		KeyManagementMode that = (KeyManagementMode) obj;
		return this.type == that.type;
	}

	@Override
	public int hashCode() {
		return type;
	}

	@Override
	protected Object clone() {
		return new KeyManagementMode(this.type);
	}

	public KeyManagementMode copy() {
		return (KeyManagementMode) clone();
	}

	public static KeyManagementMode getMode(int type) {
		switch (type) {
		case ISSUER_PIN_VALUE:
			return ISSUER_PIN;
		case ISSUER_MAC_VALUE:
			return ISSUER_MAC;
		case ACQUIER_MAC_VALUE:
			return ACQUIER_MAC;
		case ACQUIER_PIN_VALUE:
			return ACQUIER_PIN;
		case ACQ_ISS_PIN_VALUE: //Raza MASTERCARD KEY EXCHANGE
			return ACQ_ISS_PIN;
		}
		
		return new KeyManagementMode(type);
	}
	
	@Override
	public String toString() {
		switch (type) {
		case ISSUER_PIN_VALUE:
			return "ISSUER_PIN";
		case ISSUER_MAC_VALUE:
			return "ISSUER_MAC";
		case ACQUIER_MAC_VALUE:
			return "ACQUIER_MAC";
		case ACQUIER_PIN_VALUE:
			return "ACQUIER_PIN";
		case ACQ_ISS_PIN_VALUE: //Raza MASTERCARD KEY EXCHANGE
			return "ACQ_ISS_PIN";
		}
		return super.toString();
	}

}
