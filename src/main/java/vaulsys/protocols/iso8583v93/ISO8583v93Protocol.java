package vaulsys.protocols.iso8583v93;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;

import org.apache.log4j.Logger;

public class ISO8583v93Protocol extends ISO8583BaseProtocol {

    public ISO8583v93Protocol(String name) {
        super(name, new ISO8583v93ProtocolFunctions(),
        		new ISOSecurityFunctions(),
        		new ISO8583v93ProtocolMessageValidator(),
                new ISO8583v93ProtocolDialog(), new ISO8583v93FlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(ISO8583v93Protocol.class);
        return logger;
    }
}
