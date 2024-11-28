package vaulsys.protocols.negin87;


import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;

import org.apache.log4j.Logger;

public class Negin87Protocol extends ISO8583BaseProtocol {

    public Negin87Protocol(String name) {
        super(name, new Negin87ProtocolFunctions(),
        		new Negin87ProtocolSecurityFunctions(),
        		new Negin87ProtocolMessageValidator(),
                new Negin87ProtocolDialog(), new Negin87FlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(Negin87Protocol.class);
        return logger;
    }
}
