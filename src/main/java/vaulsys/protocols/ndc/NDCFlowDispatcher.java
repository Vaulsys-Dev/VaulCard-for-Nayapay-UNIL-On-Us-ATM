package vaulsys.protocols.ndc;

import static vaulsys.base.components.MessageTypeFlowDirection.Financial;
import static vaulsys.base.components.MessageTypeFlowDirection.Network;
import static vaulsys.base.components.MessageTypeFlowDirection.NotSupported;
import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.constants.NDCMessageClassTerminalToNetwork;
import vaulsys.wfe.base.DispatcherException;
import vaulsys.wfe.base.FlowDispatcher;

public class NDCFlowDispatcher implements FlowDispatcher {

    @Override
    public String dispatch(Message message) throws DispatcherException {
//        if (message instanceof IncomingMessage) {
    	
    	if (message.isIncomingMessage()){
            Message incomingMessage = message;
            ProtocolMessage protocolMessage = incomingMessage.getProtocolMessage();

            if (protocolMessage instanceof NDCMsg) {
                NDCMsg ndcMsg = (NDCMsg) protocolMessage;

                if (NDCMessageClassTerminalToNetwork.CONSUMER_REQUEST_OPERATIONAL_MESSAGE.equals(ndcMsg.messageType))
                        return Financial;
                if (NDCMessageClassTerminalToNetwork.STATUS_MESSAGE.equals(ndcMsg.messageType) || 
                		NDCMessageClassTerminalToNetwork.ENCRYPTOR_INITIALISATION_DATA.equals(ndcMsg.messageType))
                        return Network;
//                if (NDCConstants.NEED_TO_REVERS_MESSAGE == ndcMsg.messageType)
//                        return ATM_REVERSAL;
                }
            }
       
    	return NotSupported;

    }
}
