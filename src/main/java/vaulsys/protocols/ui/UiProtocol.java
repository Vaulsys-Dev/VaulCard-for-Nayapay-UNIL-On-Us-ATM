package vaulsys.protocols.ui;

import vaulsys.protocols.base.Protocol;

public class UiProtocol extends Protocol {

	public UiProtocol(String name) {
		super(name, new UiProtocolFunctions(), null,
				null, null, new UiFlowDispatcher());
	}
}
