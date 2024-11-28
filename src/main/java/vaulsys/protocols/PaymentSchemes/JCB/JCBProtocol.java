package vaulsys.protocols.PaymentSchemes.JCB;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class JCBProtocol extends ISO8583BaseProtocol {

    public JCBProtocol(String name) {
        super(name, new JCBProtocolFunctions(),
                new ISOSecurityFunctions(),
                new JCBProtocolMessageValidator(),
                new JCBProtocolDialog(), new JCBFlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(JCBProtocol.class);
        return logger;
    }
}
