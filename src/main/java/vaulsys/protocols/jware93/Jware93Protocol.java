package vaulsys.protocols.jware93;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;

import org.apache.log4j.Logger;


public class Jware93Protocol extends ISO8583BaseProtocol {

    public Jware93Protocol(String name) {
        super(name, new Jware93ProtocolMapper(),
        		new ISOSecurityFunctions(),
        		new Jware93ProtocolMessageValidator(), new Jware93ProtocolDialog(), new JWare93FlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(Jware93Protocol.class);
        return logger;
    }
}
