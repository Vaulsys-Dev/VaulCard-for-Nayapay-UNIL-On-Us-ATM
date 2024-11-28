package vaulsys.protocols.epay;

import static vaulsys.base.components.MessageTypeFlowDirection.*;
import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.epay.base.EpayMsg;
import vaulsys.wfe.base.DispatcherException;
import vaulsys.wfe.base.FlowDispatcher;

public class EpayFlowDispatcher implements FlowDispatcher {

	@Override
	public String dispatch(Message message) throws DispatcherException {
    	if (message.isIncomingMessage()){
            Message incomingMessage = message;
            ProtocolMessage protocolMessage = incomingMessage.getProtocolMessage();

            if (protocolMessage instanceof EpayMsg) {
            	EpayMsg epayMsg = (EpayMsg) protocolMessage;

            	if(epayMsg.commandID > 1000 && epayMsg.commandID < 3000)
            		return Financial;
            }
        }
        return NotSupported;
	}

}
