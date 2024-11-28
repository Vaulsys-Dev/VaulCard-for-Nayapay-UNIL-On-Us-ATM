package vaulsys.protocols.ndc;

import vaulsys.protocols.base.Protocol;

public class NDCProtocol extends Protocol {
	public NDCProtocol(String name) {
		super(name, new NDCProtocolFunctions(), new NDCProtocolSecurityFunctions(), new NDCProtocolMessageValidator(), new NDCProtocolDialog(), new NDCFlowDispatcher());
	}
}
