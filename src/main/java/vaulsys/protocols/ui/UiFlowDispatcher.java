package vaulsys.protocols.ui;

import static vaulsys.base.components.MessageTypeFlowDirection.NotSupported;
import vaulsys.base.components.MessageTypeFlowDirection;
import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.wfe.base.DispatcherException;
import vaulsys.wfe.base.FlowDispatcher;

import org.apache.log4j.Logger;

public class UiFlowDispatcher implements FlowDispatcher {
    transient static Logger logger = Logger.getLogger(UiFlowDispatcher.class);
    
	@Override
	public String dispatch(Message message) throws DispatcherException {

		try {
			if (message.isIncomingMessage()) {

				ProtocolMessage protocolMessage = message.getProtocolMessage();

				if (protocolMessage instanceof MessageObject) {
					return MessageTypeFlowDirection.GENERAL_UI;
				}
			}
			return NotSupported;

		} catch (Exception e) {
			logger.error(e);
			throw new DispatcherException(e);
		}
	}

}
