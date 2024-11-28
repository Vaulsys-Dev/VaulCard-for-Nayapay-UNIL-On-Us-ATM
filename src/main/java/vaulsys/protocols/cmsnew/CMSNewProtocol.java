package vaulsys.protocols.cmsnew;

import vaulsys.protocols.base.Protocol;
import vaulsys.protocols.cms.CMSHttpProtocolSecurityFunctions;
import vaulsys.protocols.cms.CMSProtocolDialog;
import vaulsys.protocols.cms.CMSProtocolMessageValidator;

public class CMSNewProtocol extends Protocol {

	public CMSNewProtocol(String name) {
		super(name, new CMSProtocolFunctions(), new CMSHttpProtocolSecurityFunctions(),
				new CMSProtocolMessageValidator(), new CMSProtocolDialog(), new CMSNewFlowDispatcher());
	}
}
