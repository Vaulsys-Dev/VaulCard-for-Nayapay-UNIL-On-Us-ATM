package vaulsys.protocols.saderat87;

import vaulsys.persistence.IEnum;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.PINPADTerminal;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;

public class SaderatConst implements IEnum{
	  private static final int SADERAT_TERMINALS_WITH_CARD_VALUE=21;
	  private static final int SADERAT_TERMINALS_WITHOUT_CARD_VALUE=11;
	  
	  public static final TerminalType SADERAT_TERMINALS_WITH_CARD = new TerminalType(SADERAT_TERMINALS_WITH_CARD_VALUE);
	  public static final TerminalType SADERAT_TERMINALS_WITHOUT_CARD = new TerminalType(SADERAT_TERMINALS_WITHOUT_CARD_VALUE);
	  
	  private int code;

	    public SaderatConst() {
	    }

	    public SaderatConst(int code) {
	        this.code = code;
	    }
	    
	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || !(o instanceof SaderatConst)) return false;
	        SaderatConst that = (SaderatConst) o;
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

}
