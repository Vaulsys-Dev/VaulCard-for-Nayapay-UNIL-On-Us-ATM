package vaulsys.protocols.shetab87;


import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;

import org.apache.log4j.Logger;

public class Shetab87Protocol extends ISO8583BaseProtocol {

    public Shetab87Protocol(String name) {
        super(name, new Shetab87ProtocolFunctions(),
        		new ISOSecurityFunctions(),
        		new Shetab87ProtocolMessageValidator(),
                new Shetab87ProtocolDialog(), new Shetab87FlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(Shetab87Protocol.class);
        return logger;
    }
}
