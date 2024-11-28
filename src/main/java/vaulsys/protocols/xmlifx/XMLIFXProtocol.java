package vaulsys.protocols.xmlifx;

import vaulsys.protocols.base.Protocol;

public class XMLIFXProtocol extends Protocol {
	public XMLIFXProtocol(String name) {
		super(name, new XMLIFXProtocolFunctions(), null, null, null, new XMLIFXFlowDispatcher());
	}
}
