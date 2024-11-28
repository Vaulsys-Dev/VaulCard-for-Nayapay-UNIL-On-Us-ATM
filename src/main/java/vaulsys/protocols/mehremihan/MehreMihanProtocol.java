package vaulsys.protocols.mehremihan;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import org.apache.log4j.Logger;

public class MehreMihanProtocol extends ISO8583BaseProtocol {
	public MehreMihanProtocol(String name) {
		super(name, new MehreMihanProtocolFunctions(),
				new ISOSecurityFunctions(),
				new MehreMihanProtocolMessageValidator(),
				new MehreMihanProtocolDialog(), new MehreMihanFlowDispatcher());
	}

	@Override
	protected Logger getLogger() {
		if (logger == null)
			logger = Logger.getLogger(MehreMihanProtocol.class);
		return logger;
	}

}
