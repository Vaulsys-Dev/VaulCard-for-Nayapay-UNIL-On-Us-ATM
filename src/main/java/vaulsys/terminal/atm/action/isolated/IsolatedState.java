package vaulsys.terminal.atm.action.isolated;

import vaulsys.message.Message;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;

public abstract class IsolatedState extends AbstractState {

	@Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
        return this;
    }

	@Override
	protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
		return null;
	}
}
