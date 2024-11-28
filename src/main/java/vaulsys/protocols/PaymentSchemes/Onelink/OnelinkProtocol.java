package vaulsys.protocols.PaymentSchemes.Onelink;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class OnelinkProtocol extends ISO8583BaseProtocol {

    public OnelinkProtocol(String name) {
        super(name, new OnelinkProtocolFunctions(),
                new ISOSecurityFunctions(),
                new OnelinkProtocolMessageValidator(),
                new OnelinkProtocolDialog(), new OnelinkFlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(OnelinkProtocol.class);
        return logger;
    }
}
