package vaulsys.protocols.ifx.enums;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class NetworkManagementInfo implements Cloneable, Serializable {
	private static final int SIGN_ON_VALUE = 001;
	private static final int SIGN_OFF_VALUE = 002;
	private static final int MAC_CHANGE_VALUE = 164;
	private static final int PIN_CHANGE_VALUE = 165;
	private static final int PIN2_CHANGE_VALUE = 166;
	private static final int CUTOVER_VALUE = 201;
	private static final int CUTOVER_202_VALUE = 202;
	private static final int ECHOTEST_VALUE = 301;
	private static final int KEYEXCHANGE_VALUE = 101; //Raza MasterCard
	private static final int SAF_INIT_VALUE = 060; //Raza MasterCard
	private static final int SIGNON_ISO_VALUE = 061; //Raza MasterCard
	private static final int SIGNOFF_ISO_VALUE = 062; //Raza MasterCard
	private static final int SIGNON_ISS_VALUE = 065; //Raza MasterCard
	private static final int SIGNOFF_ISS_VALUE = 066; //Raza MasterCard
	private static final int KEYEXCHANGE_ISO_VALUE = 161; //Raza MasterCard
	private static final int KEYEXCHANGE_INIT_VALUE = 162; //Raza MasterCard
	private static final int ECHOTEST_ISO_VALUE = 270; //Raza MasterCard
	private static final int SAF_EOF_VALUE = 363; //Raza MasterCard
	private static final int ECHOTEST_1LINK_VALUE = 3;
	private static final int CUTOVER_1LINK_VALUE = 8;
	private static final int SIGNON_ISO_071_VALUE = 71; //Mati Visa
	private static final int SIGNOFF_ISO_072_VALUE = 72; //Mati Visa
	//private static final int ECHOTEST_VISASMS_VALUE = 30;



	
	public static final NetworkManagementInfo SIGN_ON = new NetworkManagementInfo(SIGN_ON_VALUE);
	public static final NetworkManagementInfo SIGN_OFF = new NetworkManagementInfo(SIGN_OFF_VALUE);
	public static final NetworkManagementInfo MAC_CHANGE = new NetworkManagementInfo(MAC_CHANGE_VALUE);
	public static final NetworkManagementInfo PIN_CHANGE = new NetworkManagementInfo(PIN_CHANGE_VALUE);
	public static final NetworkManagementInfo PIN2_CHANGE = new NetworkManagementInfo(PIN2_CHANGE_VALUE);
	public static final NetworkManagementInfo CUTOVER = new NetworkManagementInfo(CUTOVER_VALUE);
	public static final NetworkManagementInfo CUTOVER_202 = new NetworkManagementInfo(CUTOVER_202_VALUE);
	public static final NetworkManagementInfo ECHOTEST = new NetworkManagementInfo(ECHOTEST_VALUE);
	public static final NetworkManagementInfo KEY_EXCHANGE = new NetworkManagementInfo(KEYEXCHANGE_VALUE); //Raza MasterCard
	public static final NetworkManagementInfo SAF_INIT = new NetworkManagementInfo(SAF_INIT_VALUE); //Raza MasterCard
	public static final NetworkManagementInfo SIGNON_ISO = new NetworkManagementInfo(SIGNON_ISO_VALUE); //Raza MasterCard
	public static final NetworkManagementInfo SIGNOFF_ISO = new NetworkManagementInfo(SIGNOFF_ISO_VALUE); //Raza MasterCard
	public static final NetworkManagementInfo SIGNON_ISS = new NetworkManagementInfo(SIGNON_ISS_VALUE); //Raza MasterCard
	public static final NetworkManagementInfo SIGNOFF_ISS = new NetworkManagementInfo(SIGNOFF_ISS_VALUE); //Raza MasterCard
	public static final NetworkManagementInfo KEYEXCHANGE_ISO = new NetworkManagementInfo(KEYEXCHANGE_ISO_VALUE); //Raza MasterCard
	public static final NetworkManagementInfo KEYEXCHANGE_INIT = new NetworkManagementInfo(KEYEXCHANGE_INIT_VALUE); //Raza MasterCard
	public static final NetworkManagementInfo ECHOTEST_ISO = new NetworkManagementInfo(ECHOTEST_ISO_VALUE); //Raza MasterCard
	public static final NetworkManagementInfo SAF_EOF = new NetworkManagementInfo(SAF_EOF_VALUE); //Raza MasterCard
	public static final NetworkManagementInfo ECHOTEST_1LINK = new NetworkManagementInfo(ECHOTEST_1LINK_VALUE);
	public static final NetworkManagementInfo CUTOVER_1LINK = new NetworkManagementInfo(CUTOVER_1LINK_VALUE);
	public static final NetworkManagementInfo SIGNON_ISO_071 = new NetworkManagementInfo(SIGNON_ISO_071_VALUE);
	public static final NetworkManagementInfo SIGNOFF_ISO_072 = new NetworkManagementInfo(SIGNOFF_ISO_072_VALUE);
	//public static final NetworkManagementInfo ECHOTEST_VISASMS = new NetworkManagementInfo(ECHOTEST_VISASMS_VALUE);

	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public NetworkManagementInfo() {
		super();
	}

	public NetworkManagementInfo(int type) {
		super();
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		NetworkManagementInfo that = (NetworkManagementInfo) obj;
		return this.type == that.type;
	}

	@Override
	public int hashCode() {
		return type;
	}

	@Override
	protected Object clone() {
		return new NetworkManagementInfo(this.type);
	}

	public NetworkManagementInfo copy() {
		return (NetworkManagementInfo) clone();
	}

	public static NetworkManagementInfo getMode(int type) {
		switch (type) {
		case CUTOVER_VALUE:
			return CUTOVER;
			//by m.rehman:
			case CUTOVER_202_VALUE:
				return CUTOVER_202;
		case ECHOTEST_VALUE:
			return ECHOTEST;
		case PIN2_CHANGE_VALUE:
			return PIN2_CHANGE;
		case PIN_CHANGE_VALUE:
			return PIN_CHANGE;
		case MAC_CHANGE_VALUE:
			return MAC_CHANGE;
		case SIGN_ON_VALUE:
			return SIGN_ON;
		case SIGN_OFF_VALUE:
			return SIGN_OFF;
//		case ECHOTEST_VISASMS_VALUE:
//			return ECHOTEST_VISASMS;
		}
//		return new NetworkManagementInfo(new String(type));
		return null;
	}
	
	@Override
	public String toString() {
		switch (type) {
		case CUTOVER_VALUE:
			return "CUTOVER";
			case CUTOVER_202_VALUE:
				return "CUTOVER_202";
		case ECHOTEST_VALUE:
			return "ECHOTEST";
		case PIN2_CHANGE_VALUE:
			return "PIN2CHANGE";
		case SIGN_ON_VALUE:
			return "SIGN_ON";
		case SIGN_OFF_VALUE:
			return "SIGN_OFF";
		case PIN_CHANGE_VALUE:
			return "PIN_CHANGE";
		case MAC_CHANGE_VALUE:
			return "MAC_CHANGE";
			case KEYEXCHANGE_VALUE:
				return "KEY_EXCHANGE";
//		case ECHOTEST_VISASMS_VALUE:
//			return "ECHOTESTVISASMS";
		default:
			break;
		}
		return super.toString();
	}
}
