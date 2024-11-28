package vaulsys.protocols.PaymentSchemes.ISOHost;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class ISOHostProtocol extends ISO8583BaseProtocol {

    public ISOHostProtocol(String name) {
        super(name, new ISOHostProtocolFunctions(),
                new ISOSecurityFunctions(),
                new ISOHostProtocolMessageValidator(),
                new ISOHostProtocolDialog(), new ISOHostFlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(ISOHostProtocol.class);
        return logger;
    }
}
