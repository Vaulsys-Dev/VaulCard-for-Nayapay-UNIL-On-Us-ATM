package vaulsys.terminal.atm.action.consumer;

import vaulsys.message.Message;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.impl.ATMTerminal;

public abstract class ConsumerState extends AbstractState {

    @Override
    protected Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm) {
    	return null;
    }
}
