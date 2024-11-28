package vaulsys.terminal.atm.action.consumer;

import vaulsys.message.Message;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.action.EndState;
import vaulsys.terminal.impl.ATMTerminal;

public class ConsumerEndState extends EndState {
	public static final ConsumerEndState Instance = new ConsumerEndState();

	private ConsumerEndState(){}

    @Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
        return ConsumerEndState.Instance;
    }

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
//    	setDebugTag(inputMessage.getTransaction());
        return null;
    }

}