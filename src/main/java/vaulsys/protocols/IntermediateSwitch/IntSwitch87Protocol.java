package vaulsys.protocols.IntermediateSwitch;


import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;

import org.apache.log4j.Logger;

public class IntSwitch87Protocol extends ISO8583BaseProtocol {

    public IntSwitch87Protocol(String name) {
        super(name, new IntSwitch87ProtocolFunctions(),
        		new ISOSecurityFunctions(),
        		new IntSwitch87ProtocolMessageValidator(),
                new IntSwitch87ProtocolDialog(), new IntSwitch87FlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(IntSwitch87Protocol.class);
        return logger;
    }
}
