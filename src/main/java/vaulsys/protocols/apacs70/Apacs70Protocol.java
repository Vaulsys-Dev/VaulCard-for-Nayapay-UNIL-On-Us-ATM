package vaulsys.protocols.apacs70;

import vaulsys.protocols.base.Protocol;

public class Apacs70Protocol extends Protocol {
	public Apacs70Protocol(String name) {
		super(name, new Apacs70ProtocolFunctions(),
				new Apacs70ProtocolSecurityFunctions(),
				new Apacs70ProtocolMessageValidator(),
				new Apacs70ProtocolDialog(), new Apacs70FlowDispatcher());
	}
}
