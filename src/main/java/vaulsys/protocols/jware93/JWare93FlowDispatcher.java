package vaulsys.protocols.jware93;

import static vaulsys.base.components.MessageTypeFlowDirection.NotSupported;
import vaulsys.message.Message;
import vaulsys.wfe.base.DispatcherException;
import vaulsys.wfe.base.FlowDispatcher;


public class JWare93FlowDispatcher implements FlowDispatcher {

    @Override
    public String dispatch(Message message) throws DispatcherException {
        return NotSupported;
    }

}
