package vaulsys.protocols.mizan;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import org.apache.log4j.Logger;

public class MizanProtocol extends ISO8583BaseProtocol {
	private static final Logger logger = Logger.getLogger(MizanProtocol.class);

	public MizanProtocol(String name) {
		super(name,
				new MizanProtocolFunctions(),
				new MizanProtocolSecurityFunctions(),
				new MizanProtocolMessageValidator(),
				new MizanProtocolDialog(),
				new MizanFlowDispatcher());
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
