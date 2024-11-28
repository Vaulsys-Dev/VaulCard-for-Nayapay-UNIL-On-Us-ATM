package vaulsys.protocols.cms;

import vaulsys.protocols.base.Protocol;

public class CMSProtocol extends Protocol {

	public CMSProtocol(String name) {
		super(name, new CMSHttpProtocolFunctions(), new CMSHttpProtocolSecurityFunctions(),
				new CMSProtocolMessageValidator(), new CMSProtocolDialog(), new CMSFlowDispatcher());
	}
}
