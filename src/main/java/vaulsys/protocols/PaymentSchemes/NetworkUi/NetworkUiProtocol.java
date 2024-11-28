package vaulsys.protocols.PaymentSchemes.NetworkUi;

import vaulsys.protocols.base.Protocol;

public class NetworkUiProtocol extends Protocol {

	public NetworkUiProtocol(String name) {
		super(name, new NetworkUiProtocolFunctions(), null,
				null, null, new NetworkUiFlowDispatcher());
	}
}
