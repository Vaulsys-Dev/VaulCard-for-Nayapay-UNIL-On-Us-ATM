package vaulsys.clearing.reconcile;

import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolMessage;

public interface ICutover {

    ProtocolMessage buildResponse(Message incommingMessage) throws Exception;

    ProtocolMessage buildRequset(Institution institution) throws Exception;

}
