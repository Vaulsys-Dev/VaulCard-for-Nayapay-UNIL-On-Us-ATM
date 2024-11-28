package vaulsys.protocols.ifx.enums;

import java.util.ArrayList;
import java.util.List;

import vaulsys.persistence.IEnum;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;

import javax.persistence.Embeddable;

@Embeddable
public class TerminalType implements IEnum{

	private static final int UNKNOWN_VALUE = -1;
	private static final int SWITCH_VALUE = 0;
	private static final int THIRDPARTY_VALUE = 1;
	private static final int ATM_VALUE = 2;
	private static final int PINPAD_VALUE = 3;
	private static final int MOBILE_VALUE = 5;
	private static final int ADMIN_TERM_VALUE = 6;
	private static final int VRU_VALUE = 7;
	private static final int INFOKIOSK_VALUE = 13;
    private static final int POS_VALUE = 14;
    private static final int INTERNET_VALUE = 59;
    private static final int PAYPAL_VALUE = 60;
    private static final int MOBILEWAP_VALUE = 61;
    private static final int USSD_VALUE = 62;
    private static final int INTERNETBANK_VALUE =  63;
    private static final int EPAYPAYPAL_VALUE = 64;
    private static final int MOBILESMS_VALUE = 65;
    private static final int MOBILEBANK_VALUE = 66;
    private static final int PAYPALSMS_VALUE = 67;
    private static final int PAYPALUSSD_VALUE = 68;
    private static final int PAYPALTV_VALUE = 69;
    private static final int MOBILEBANKSMS_VALUE  = 70;
    private static final int MOBILEBANKGPRS_VALUE = 71;
    private static final int MOBILEGPRS_VALUE = 72;
    /**
     * @author k.khodadi
     */
    private static final int PAYPALTEL_VALUE = 73;
    private static final int TV_VALUE = 74;
    

    private static final int KIOSK_CARD_PRESENT_VALUE = 43;
    private static final int SHAPARAK_KIOSK_VALUE = 43;

    public static final TerminalType ATM = new TerminalType(ATM_VALUE);
    public static final TerminalType POS = new TerminalType(POS_VALUE);
    public static final TerminalType VRU = new TerminalType(VRU_VALUE);
    public static final TerminalType PINPAD = new TerminalType(PINPAD_VALUE);
    public static final TerminalType MOBILE = new TerminalType(MOBILE_VALUE);
    public static final TerminalType INTERNET = new TerminalType(INTERNET_VALUE);
    public static final TerminalType ADMIN_TERM = new TerminalType(ADMIN_TERM_VALUE);
    public static final TerminalType UNKNOWN = new TerminalType(UNKNOWN_VALUE);
    public static final TerminalType SWITCH = new TerminalType(SWITCH_VALUE);
    public static final TerminalType INFOKIOSK = new TerminalType(INFOKIOSK_VALUE);
    public static final TerminalType THIRDPARTY = new TerminalType(THIRDPARTY_VALUE);
    public static final TerminalType PAYPAL = new TerminalType(PAYPAL_VALUE);
    public static final TerminalType MOBILEWAP = new TerminalType(MOBILEWAP_VALUE);
    public static final TerminalType USSD = new TerminalType(USSD_VALUE);
    public static final TerminalType INTERNETBANK = new TerminalType(INTERNETBANK_VALUE);
    public static final TerminalType EPAYPAYPAL = new TerminalType(EPAYPAYPAL_VALUE);
    public static final TerminalType MOBILESMS = new TerminalType(MOBILESMS_VALUE);
    public static final TerminalType MOBILEBANK = new  TerminalType(MOBILEBANK_VALUE);
    public static final TerminalType PAYPALSMS = new  TerminalType(PAYPALSMS_VALUE);
    public static final TerminalType PAYPALUSSD = new  TerminalType(PAYPALUSSD_VALUE);
    public static final TerminalType PAYPALTV = new  TerminalType(PAYPALTV_VALUE);
    public static final TerminalType MOBILEBANKSMS = new  TerminalType(MOBILEBANKSMS_VALUE);
    public static final TerminalType MOBILEGPRS = new  TerminalType(MOBILEGPRS_VALUE);
    public static final TerminalType MOBILEBANKGPRS = new  TerminalType(MOBILEBANKGPRS_VALUE);
    /**
     * @author k.khodadi
     */
    public static final TerminalType PAYPALTEL = new  TerminalType(PAYPALTEL_VALUE);
    public static final TerminalType TV = new  TerminalType(TV_VALUE);

    public static final TerminalType KIOSK_CARD_PRESENT = new TerminalType(KIOSK_CARD_PRESENT_VALUE);

    private int code;

    public TerminalType() {
    }

    public TerminalType(int code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof TerminalType)) return false;
        TerminalType that = (TerminalType) o;
        return code == that.code;
    }

    @Override
    public int hashCode() {
        return code;
    }

	public int getCode() {
		return code;
	}

	@Override
	public String toString() {
		return code+"";
	}


	public Class<? extends Terminal> getClassType() {
		if (SWITCH.equals(this)) {
			return SwitchTerminal.class;
		} else if (POS.equals(this)) {
			return POSTerminal.class;
		} else if (ATM.equals(this)) {
			return ATMTerminal.class;
		} else if (PINPAD.equals(this)) {
			return PINPADTerminal.class;
		} else if (INTERNET.equals(this)) {
			return SwitchTerminal.class;
		} else if (THIRDPARTY.equals(this)) {
			return ThirdPartyVirtualTerminal.class;
		}

		return Terminal.class;
	}

	 public static boolean isPhisycalDeviceTerminal(TerminalType terminalType) {
		return  POS.equals(terminalType) ||
				ATM.equals(terminalType) ||
				PINPAD.equals(terminalType) ||
				KIOSK_CARD_PRESENT.equals(terminalType);
	}

	 public static List<TerminalType> convertType(String[] list) {
	        List<TerminalType> terminalType = new ArrayList<TerminalType>();
	        for (int i = 0; i < list.length ; i++) {
	            if (Integer.valueOf(list[i]).equals(POS.getCode())) {
	                terminalType.add(new TerminalType(POS.getCode()));
	            }
	            if (Integer.valueOf(list[i]).equals(ATM.getCode())) {
	                terminalType.add(new TerminalType(ATM.getCode()));
	            }
	            if (Integer.valueOf(list[i]).equals(VRU.getCode()))
	                terminalType.add(new TerminalType(VRU.getCode()));

	            if (Integer.valueOf(list[i]).equals(PINPAD.getCode()))
	                terminalType.add(new TerminalType(PINPAD.getCode()));

	            if (Integer.valueOf(list[i]).equals(MOBILE.getCode()))
	                terminalType.add(new TerminalType(MOBILE.getCode()));

	            if (Integer.valueOf(list[i]).equals(INTERNET.getCode()))
	                terminalType.add(new TerminalType(INTERNET.getCode()));

	            if (Integer.valueOf(list[i]).equals(ADMIN_TERM.getCode()))
	                terminalType.add(new TerminalType(ADMIN_TERM.getCode()));

	            if (Integer.valueOf(list[i]).equals(UNKNOWN.getCode()))
	                terminalType.add(new TerminalType(UNKNOWN.getCode()));

	            if (Integer.valueOf(list[i]).equals(UNKNOWN.getCode()))
	                terminalType.add(new TerminalType(UNKNOWN.getCode()));

	            if (Integer.valueOf(list[i]).equals(INFOKIOSK.getCode()))
	                terminalType.add(new TerminalType(INFOKIOSK.getCode()));

	            if (Integer.valueOf(list[i]).equals(THIRDPARTY.getCode()))
	                terminalType.add(new TerminalType(THIRDPARTY.getCode()));

	            if (Integer.valueOf(list[i]).equals(KIOSK_CARD_PRESENT.getCode()))
	                terminalType.add(new TerminalType(KIOSK_CARD_PRESENT.getCode()));
	        }
//	        System.out.println(terminalType);
	        return terminalType;
	    } 
	
	 public String getName() {

        if(MOBILEBANKGPRS.equals(this))
            return "MOBILEBANKGPRS";

        if(MOBILEGPRS.equals(this))
            return "MOBILEGPRS";

        if(MOBILEBANKSMS.equals(this))
            return "MOBILEBANKSMS";

        if(PAYPALTV.equals(this))
            return "PAYPALTV";

        if (PAYPALUSSD.equals(this))
            return "PAYPALUSSD";

        if (PAYPALSMS.equals(this))
            return "PAYPALSMS";

        if(MOBILEBANK.equals(this))
            return "MOBILEBANK";

        if(EPAYPAYPAL.equals(this))
            return "EPAYPAYPAL";

        if(MOBILESMS.equals(this))
            return "MOBILESMS";

        if(INTERNETBANK.equals(this))
            return "INTERNETBANK";

        if(USSD.equals(this))
            return "USSD";

        if(MOBILEWAP.equals(this))
            return "MOBILEWAP";

        if(PAYPAL.equals(this))
            return "PAYPAL";

		 if (POS.equals(this))
			 return "POS";

		 if (ATM.equals(this))
			 return "ATM";

		 if (PINPAD.equals(this))
			 return "PINPAD";

		 if (INTERNET.equals(this))
			 return "INTERNET";

		 if (THIRDPARTY.equals(this))
			 return "THIRDPARTY";

		 if (MOBILE.equals(this))
				 return "MOBILE";

		 if (INFOKIOSK.equals(this))
			 return "INFOKIOSK";

		 if (ADMIN_TERM.equals(this))
			 return "ADMINTERM";

		 if (VRU.equals(this))
			 return "VRU";

		 if (KIOSK_CARD_PRESENT.equals(this))
			 return "KIOSKCARDPRESENT";

		 return "UNKNOWN";

	 }
	 
	 
	 public String getFarsiName() {

	        if(MOBILEBANKGPRS.equals(this))
	            return "MOBILEBANKGPRS";

	        if(MOBILEGPRS.equals(this))
	            return "MOBILEGPRS";

	        if(MOBILEBANKSMS.equals(this))
	            return "MOBILEBANKSMS";

	        if(PAYPALTV.equals(this))
	            return "PAYPALTV";

	        if (PAYPALUSSD.equals(this))
	            return "PAYPALUSSD";

	        if (PAYPALSMS.equals(this))
	            return "PAYPALSMS";

	        if(MOBILEBANK.equals(this))
	            return "MOBILEBANK";

	        if(EPAYPAYPAL.equals(this))
	            return "EPAYPAYPAL";

	        if(MOBILESMS.equals(this))
	            return "MOBILESMS";

	        if(INTERNETBANK.equals(this))
	            return "INTERNETBANK";

	        if(USSD.equals(this))
	            return "USSD";

	        if(MOBILEWAP.equals(this))
	            return "MOBILEWAP";

	        if(PAYPAL.equals(this))
	            return "PAYPAL";

			 if (POS.equals(this))
				 return "پایانه فروش";

			 if (ATM.equals(this))
				 return "خودپرداز";

			 if (PINPAD.equals(this))
				 return "کارت خوان شعبه";

			 if (INTERNET.equals(this))
				 return "INTERNET";

			 if (THIRDPARTY.equals(this))
				 return "THIRDPARTY";

			 if (MOBILE.equals(this))
					 return "MOBILE";

			 if (INFOKIOSK.equals(this))
				 return "INFOKIOSK";

			 if (ADMIN_TERM.equals(this))
				 return "ADMINTERM";

			 if (VRU.equals(this))
				 return "VRU";

			 if (KIOSK_CARD_PRESENT.equals(this))
				 return "KIOSKCARDPRESENT";

			 return "UNKNOWN";

		 }
}
