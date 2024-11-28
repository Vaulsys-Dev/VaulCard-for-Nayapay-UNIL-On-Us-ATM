package vaulsys.protocols.xmlifx;

import static vaulsys.base.components.MessageTypeFlowDirection.*;
import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.wfe.base.DispatcherException;
import vaulsys.wfe.base.FlowDispatcher;

public class XMLIFXFlowDispatcher implements FlowDispatcher {

	@Override
	public String dispatch(Message message) throws DispatcherException {
    	if (message.isIncomingMessage()){
            Message incomingMessage = message;
            ProtocolMessage protocolMessage = incomingMessage.getProtocolMessage();

            if (protocolMessage instanceof Ifx) {
            	XMLIFXMsg ifxMsg = (XMLIFXMsg) protocolMessage;

				//m.rehman: separating BI from financial incase of limit
            	//if(ISOFinalMessageType.isFinancialMessage(ifxMsg.getIfxType()))
				if(ISOFinalMessageType.isFinancialMessage(ifxMsg.getIfxType(),false))
            		return Financial;
            }
        }
        return NotSupported;
	}
}
