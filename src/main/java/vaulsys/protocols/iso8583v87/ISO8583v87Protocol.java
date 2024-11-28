package vaulsys.protocols.iso8583v87;


import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;

import org.apache.log4j.Logger;

public class ISO8583v87Protocol extends ISO8583BaseProtocol {

    public ISO8583v87Protocol(String name) {
        super(name, new ISO8583v87ProtocolFunctions(),
        		new ISOSecurityFunctions(),
        		new ISO8583v87ProtocolMessageValidator(),
                new ISO8583v87ProtocolDialog(), new ISO8583v87FlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(ISO8583v87Protocol.class);
        return logger;
    }
}
