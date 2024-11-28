package vaulsys.terminal.atm.action.isolated;

import vaulsys.message.Message;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.terminal.impl.ATMTerminal;

public class DeviceLocationState extends IsolatedState {
	public static final DeviceLocationState Instance = new DeviceLocationState();

	private DeviceLocationState(){}

    @Override
    protected Message process(Message inputMessage, ATMTerminal atm) {
    	setDebugTag(inputMessage.getTransaction());
    	NDCMsg ndcMsg = (NDCMsg) inputMessage.getProtocolMessage();
        ((NDCUnsolicitedStatusMsg) ndcMsg).updateStatus(atm);
        return null; 
    }
}