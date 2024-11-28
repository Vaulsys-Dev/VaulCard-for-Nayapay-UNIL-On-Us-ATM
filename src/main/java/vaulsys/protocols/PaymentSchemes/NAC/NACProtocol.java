package vaulsys.protocols.PaymentSchemes.NAC;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class NACProtocol extends ISO8583BaseProtocol {

    public NACProtocol(String name) {
        super(name, new NACProtocolFunctions(),
                new ISOSecurityFunctions(),
                new NACProtocolMessageValidator(),
                new NACProtocolDialog(), new NACFlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(NACProtocol.class);
        return logger;
    }
}
