package vaulsys.protocols.infotech;

import static vaulsys.base.components.MessageTypeFlowDirection.Clearing;
import static vaulsys.base.components.MessageTypeFlowDirection.Financial;
import static vaulsys.base.components.MessageTypeFlowDirection.Network;
import static vaulsys.base.components.MessageTypeFlowDirection.NotSupported;
import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.wfe.base.DispatcherException;
import vaulsys.wfe.base.FlowDispatcher;

public class InfotechFlowDispatcher implements FlowDispatcher {

    @Override
    public String dispatch(Message message) throws DispatcherException {
        try {
        	if (message.isIncomingMessage()){
        		 ProtocolMessage protocolMessage = message.getProtocolMessage();

                if (protocolMessage instanceof ISOMsg) {
                    ISOMsg isoMsg = (ISOMsg) protocolMessage;
                    String mtiStr = isoMsg.getMTI();
                    Integer mtiType = Integer.parseInt(mtiStr.substring(1, 2));
                    switch (mtiType) {
                        case 1:
                        case 2:
                        case 4:
                            return Financial;
                        case 5:
                            return Clearing;
                        case 8:
                            return Network;
                    }
                }
            }
            return NotSupported;

        } catch (ISOException e) {
            throw new DispatcherException(e);
        }

    }

}
