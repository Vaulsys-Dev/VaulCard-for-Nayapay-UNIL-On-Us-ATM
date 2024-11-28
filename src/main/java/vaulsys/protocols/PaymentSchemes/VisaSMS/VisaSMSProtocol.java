package vaulsys.protocols.PaymentSchemes.VisaSMS;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import org.apache.log4j.Logger;

/**
 * Created by HP on 11/23/2016.
 */
public class VisaSMSProtocol extends ISO8583BaseProtocol {
    public VisaSMSProtocol(String name) {
        super(name, new VisaSMSProtocolFunctions(),
                new ISOSecurityFunctions(),
                new VisaSMSProtocolMessageValidator(),
                new VisaSMSProtocolDialog(), new VisaSMSFlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(VisaSMSProtocol.class);
        return logger;
    }
}
