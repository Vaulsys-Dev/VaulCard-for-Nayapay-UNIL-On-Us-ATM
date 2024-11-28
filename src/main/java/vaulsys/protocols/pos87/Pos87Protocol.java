package vaulsys.protocols.pos87;


import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;

import org.apache.log4j.Logger;

public class Pos87Protocol extends ISO8583BaseProtocol {

    public Pos87Protocol(String name) {
        super(name, new Pos87ProtocolFunctions(),
        		new Pos87ProtocolSecurityFunctions(),
        		new Pos87ProtocolMessageValidator(),
                new Pos87ProtocolDialog(), new Pos87FlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(Pos87Protocol.class);
        return logger;
    }
}
