package vaulsys.protocols.apacs70;

import static vaulsys.base.components.MessageTypeFlowDirection.Clearing;
import static vaulsys.base.components.MessageTypeFlowDirection.Financial;
import static vaulsys.base.components.MessageTypeFlowDirection.Network;
import static vaulsys.base.components.MessageTypeFlowDirection.NotSupported;
import vaulsys.message.Message;
import vaulsys.protocols.apacs70.base.ApacsMsgType;
import vaulsys.protocols.apacs70.base.RqBaseMsg;
import vaulsys.wfe.base.DispatcherException;
import vaulsys.wfe.base.FlowDispatcher;

public class Apacs70FlowDispatcher implements FlowDispatcher {
	public String dispatch(Message message) throws DispatcherException {
		if (message.isIncomingMessage()) {
			RqBaseMsg rqMsg = (RqBaseMsg) message.getProtocolMessage();
			String type = ApacsMsgType.getRqType(rqMsg.messageType);
			if (ApacsMsgType.FIN_REQ.equals(type) || ApacsMsgType.INFO_REQ.equals(type))
				return Financial;
			else if(ApacsMsgType.NET_REQ.equals(type))
				return Network;
			else if(ApacsMsgType.RECON_REQ.equals(type))
				return Clearing;
			else if(ApacsMsgType.CONF.equals(type))
					return Network;
		}
		return NotSupported;
	}
}
