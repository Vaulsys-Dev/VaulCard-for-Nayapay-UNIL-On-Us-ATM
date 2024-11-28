package vaulsys.terminal.atm.action;

import vaulsys.message.Message;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.transaction.Transaction;

public abstract class AbstractState{

    protected AbstractState() {}

    protected abstract Ifx createOutgoingIfx(Message outputMsg, ATMTerminal atm);
    
    protected abstract Message process(Message inputMessage, ATMTerminal atm);

    protected abstract AbstractState nextState(Message inputMessage, ATMTerminal atm);
    
    protected void setDebugTag(Transaction t) {
		t.setDebugTag("ATM_"+this.getClass().getSimpleName().toUpperCase());
	}
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}
	
    public AbstractState getNextState(Message inputMessage, ATMTerminal atm){
    	return nextState(inputMessage, atm);
    }

	public Message proceed(Message inputMessage, ATMTerminal atm) {
//    	NDCMsg msg = (NDCMsg) inputMessage.getProtocolMessage();
//        stateClass = nextState(inputMessage).getClass();
//        return nextState(inputMessage).process(inputMessage);
      return process(inputMessage, atm);
	}
}