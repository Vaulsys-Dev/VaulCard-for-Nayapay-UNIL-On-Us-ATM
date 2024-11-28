package vaulsys.protocols.PaymentSchemes.MasterCard;


import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;

import org.apache.log4j.Logger;

public class MasterCard87Protocol extends ISO8583BaseProtocol {

    public MasterCard87Protocol(String name) {
        super(name, new MasterCard87ProtocolFunctions(),
        		new ISOSecurityFunctions(),
        		new MasterCard87ProtocolMessageValidator(),
                new MasterCard87ProtocolDialog(), new MasterCard87FlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(MasterCard87Protocol.class);
        return logger;
    }
}
