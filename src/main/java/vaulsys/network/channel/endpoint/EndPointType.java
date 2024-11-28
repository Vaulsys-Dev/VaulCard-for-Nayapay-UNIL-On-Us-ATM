package vaulsys.network.channel.endpoint;

import vaulsys.persistence.IEnum;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.KIOSKCardPresentTerminal;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;

import javax.persistence.Embeddable;

@Embeddable
public class EndPointType implements IEnum {

    private static final byte SWITCH_TERMINAL_VALUE = 1;
    private static final byte POS_TERMINAL_VALUE = 2;
    private static final byte ATM_TERMINAL_VALUE = 3;
    private static final byte EPAY_SWITCH_TERMINAL_VALUE = 4;
    private static final byte UI_TERMINAL_VALUE = 5;
    private static final byte PINPAD_TERMINAL_VALUE = 6;
    
    private static final byte DEPENDENT_SWITCH_TERMINAL_VALUE = 7;
    private static final byte KIOSK_CARD_PRESENT_TERMINAL_VALUE = 8;
    

    public static final EndPointType SWITCH_TERMINAL = new EndPointType(SWITCH_TERMINAL_VALUE);
    public static final EndPointType POS_TERMINAL = new EndPointType(POS_TERMINAL_VALUE);
    public static final EndPointType ATM_TERMINAL = new EndPointType(ATM_TERMINAL_VALUE);
    public static final EndPointType EPAY_SWITCH_TERMINAL = new EndPointType(EPAY_SWITCH_TERMINAL_VALUE);
    public static final EndPointType UI_TERMINAL = new EndPointType(UI_TERMINAL_VALUE);
    public static final EndPointType PINPAD_TERMINAL = new EndPointType(PINPAD_TERMINAL_VALUE);
    
    public static final EndPointType DEPENDENT_SWITCH_TERMINAL = new EndPointType(DEPENDENT_SWITCH_TERMINAL_VALUE);
    public static final EndPointType KIOSK_CARD_PRESENT_TERMINAL = new EndPointType(KIOSK_CARD_PRESENT_TERMINAL_VALUE);
    
    private byte type;

    public EndPointType() {
    }

    public EndPointType(byte type) {
        this.type = type;
    }

    public static EndPointType valueOf(String name) {
        if ("SWITCH".equalsIgnoreCase(name)) {
            return SWITCH_TERMINAL;
            
        }else if ("POS".equalsIgnoreCase(name)) {
            return POS_TERMINAL;
            
        }else if ("ATM".equalsIgnoreCase(name)){
        	return ATM_TERMINAL;
        	
        }else if ("EPAY".equalsIgnoreCase(name)){
        	return EPAY_SWITCH_TERMINAL;
        	
    	}else if ("UI".equalsIgnoreCase(name)) {
    		return UI_TERMINAL;
    		
    	}else if ("PINPAD".equalsIgnoreCase(name)) {
    		return PINPAD_TERMINAL;
    	}
        
    	else if ("DEPENDENT_SWITCH".equals(name)){
    		return DEPENDENT_SWITCH_TERMINAL;
    	}
    	 else if ("KIOSK_CARD_PRESENT".equalsIgnoreCase(name)) {
     		return KIOSK_CARD_PRESENT_TERMINAL;
     	}
        
        return null;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndPointType that = (EndPointType) o;

        if (type != that.type) return false;

        return true;
    }

    public int hashCode() {
        return (int) type;
    }

    @Override
    public String toString() {
    	return getClass().getSimpleName();
	}

    public Class getClassType() {
		if (SWITCH_TERMINAL.equals(this) || EPAY_SWITCH_TERMINAL.equals(this)|| DEPENDENT_SWITCH_TERMINAL.equals(this)) {
			return SwitchTerminal.class;
		} else if (POS_TERMINAL.equals(this)) {
			return POSTerminal.class;
		} else if (ATM_TERMINAL.equals(this)) {
			return ATMTerminal.class;
		} else if (PINPAD_TERMINAL.equals(this)) {
			return PINPADTerminal.class;
		} else if (KIOSK_CARD_PRESENT_TERMINAL.equals(this)) {
			return KIOSKCardPresentTerminal.class;
		}

		return Terminal.class;
	}
    
   /* public static EndPointType getEndPointType(Class clazz) {
    	if (SwitchTerminal.class.equals(clazz)){
    		return	SWITCH_TERMINAL;
    	} else if (POSTerminal.class.equals(clazz)) {
    		return POS_TERMINAL;
    	} else if (ATMTerminal.class.equals(clazz))
    		return ATM_TERMINAL;
    	
    	return POS_TERMINAL;
    }*/
    
    public static EndPointType getEndPointType(TerminalType termType) {
    	if (TerminalType.SWITCH.equals(termType)){
    		return	SWITCH_TERMINAL;
    	} else if (TerminalType.POS.equals(termType)) {
    		return POS_TERMINAL;
    	} else if (TerminalType.ATM.equals(termType)) {
    		return ATM_TERMINAL;
    	} else if (TerminalType.PINPAD.equals(termType)) {
    		return PINPAD_TERMINAL;
    	} else if (TerminalType.KIOSK_CARD_PRESENT.equals(termType)) {
    		return KIOSK_CARD_PRESENT_TERMINAL;
    	}
    	
    	return POS_TERMINAL;
    }
    
    public static boolean isSwitchTerminal(EndPointType endPointType) {
		return SWITCH_TERMINAL.equals(endPointType) || EPAY_SWITCH_TERMINAL.equals(endPointType) || DEPENDENT_SWITCH_TERMINAL.equals(endPointType);
	}
    
    public static boolean isPhisycalDeviceTerminal(EndPointType endPointType) {
    	return POS_TERMINAL.equals(endPointType) ||
		    	ATM_TERMINAL.equals(endPointType) ||
		    	PINPAD_TERMINAL.equals(endPointType) ||
		    	KIOSK_CARD_PRESENT_TERMINAL.equals(endPointType);
    }
    
    public static TerminalType getTerminalType(EndPointType endPointType) {
    	if (POS_TERMINAL.equals(endPointType))
    		return TerminalType.POS;
    	
    	if (PINPAD_TERMINAL.equals(endPointType))
    		return TerminalType.PINPAD;
    	
    	if (ATM_TERMINAL.equals(endPointType))
    		return TerminalType.ATM;
    	
    	if (EPAY_SWITCH_TERMINAL.equals(endPointType))
    		return TerminalType.INTERNET;
    	
    	if (KIOSK_CARD_PRESENT_TERMINAL.equals(endPointType))
    		return TerminalType.KIOSK_CARD_PRESENT;
    	
    	if (SWITCH_TERMINAL.equals(endPointType) || DEPENDENT_SWITCH_TERMINAL.equals(endPointType))
    		return TerminalType.SWITCH;
    	
    	return TerminalType.UNKNOWN;
    }
    	
}
