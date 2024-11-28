package vaulsys.protocols.maskantest;

import static vaulsys.base.components.MessageTypeFlowDirection.*;
import vaulsys.message.Message;
import vaulsys.wfe.base.DispatcherException;
import vaulsys.wfe.base.FlowDispatcher;

public class MaskanFlowDispatcher implements FlowDispatcher {

	@Override
	public String dispatch(Message message) throws DispatcherException {
    	if (message.isIncomingMessage()){
       		return Financial;
        }
        return NotSupported;
	}

}
