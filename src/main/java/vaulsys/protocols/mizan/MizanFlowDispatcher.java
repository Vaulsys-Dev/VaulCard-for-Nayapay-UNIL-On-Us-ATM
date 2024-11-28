package vaulsys.protocols.mizan;

import vaulsys.message.Message;
import vaulsys.wfe.base.DispatcherException;
import vaulsys.wfe.base.FlowDispatcher;

import static vaulsys.base.components.MessageTypeFlowDirection.Financial;

public class MizanFlowDispatcher implements FlowDispatcher {

	@Override
	public String dispatch(Message message) throws DispatcherException {
		return Financial;
	}

}
