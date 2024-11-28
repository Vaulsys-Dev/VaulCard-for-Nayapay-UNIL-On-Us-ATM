package vaulsys.protocols.maskantest;

import vaulsys.protocols.base.Protocol;

public class MaskanProtocol extends Protocol {

	public MaskanProtocol(String name) {
		super(name, new MaskanProtocolFunctions(), null, null, null, new MaskanFlowDispatcher());
	}
}