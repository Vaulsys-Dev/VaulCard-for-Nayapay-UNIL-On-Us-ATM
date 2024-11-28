package vaulsys.terminal.atm.action;

import vaulsys.message.Message;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.ATMTerminal;

public abstract class EndState extends AbstractState {

	@Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
        return null;
    }

	@Override
	protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
		return null;
	}

}
