package vaulsys.protocols.PaymentSchemes.VisaBaseI;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class VisaBaseIProtocol extends ISO8583BaseProtocol {

    public VisaBaseIProtocol(String name) {
        super(name, new VisaBaseIProtocolFunctions(),
                new ISOSecurityFunctions(),
                new VisaBaseIProtocolMessageValidator(),
                new VisaBaseIProtocolDialog(), new VisaBaseIFlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(VisaBaseIProtocol.class);
        return logger;
    }
}
