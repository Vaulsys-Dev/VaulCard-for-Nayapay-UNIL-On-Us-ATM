package vaulsys.terminal.atm.action.supervisor;

import vaulsys.message.Message;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.solicited.NDCSolicitedStatusSupplyCounterMsg;
import vaulsys.terminal.atm.action.AbstractState;
import vaulsys.terminal.atm.action.EndState;
import vaulsys.terminal.impl.ATMTerminal;

public class SupervisorEndingState extends EndState {
	public static final SupervisorEndingState Instance = new SupervisorEndingState();

	private SupervisorEndingState(){}

	@Override
    protected AbstractState nextState(Message inputMessage, ATMTerminal atm) {
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
        if (ndcMsg instanceof NDCSolicitedStatusSupplyCounterMsg) {
        	inputMessage.getIfx().setIfxType(IfxType.ATM_SUPPLY_COUNTER_REQUEST);
            return this.Instance;
        }
        return SupervisorSupplyCounterState.Instance;
    }

	@Override
	protected Message process(Message inputMessage, ATMTerminal atm) {
    	setDebugTag(inputMessage.getTransaction());
        return null;
    }
}