package vaulsys.protocols.PaymentSchemes.VisaBaseI;

import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.wfe.base.DispatcherException;
import vaulsys.wfe.base.FlowDispatcher;

import static vaulsys.base.components.MessageTypeFlowDirection.*;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class VisaBaseIFlowDispatcher implements FlowDispatcher {
    @Override
    public String dispatch(Message message) throws DispatcherException {

        ISOMsg isoMsg;
        int mti;
        String messageType;

        try {
            messageType = "";
            if (message.isIncomingMessage()){

                ProtocolMessage protocolMessage = message.getProtocolMessage();

                if (protocolMessage instanceof ISOMsg) {
                    isoMsg = (ISOMsg) protocolMessage;
                    mti = Integer.parseInt(isoMsg.getMTI().substring(1,2));
                    switch (mti) {
                        case 1:
                        case 2:
                        case 4:
                            messageType = Financial;
                            break;
                        case 5:
                            messageType = Clearing;
                            break;
                        case 8:
                            messageType = Network;
                            break;
                        default:
                            messageType = NotSupported;
                            break;
                    }
                }
            }
            return messageType;

        } catch (ISOException e) {
            throw new DispatcherException(e);
        }
    }
}
