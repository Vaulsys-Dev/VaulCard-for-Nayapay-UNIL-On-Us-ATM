package vaulsys.protocols.PaymentSchemes.UnionPay;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;

import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class UnionPayProtocol extends ISO8583BaseProtocol {

    public UnionPayProtocol(String name) {
        super(name, new UnionPayProtocolFunctions(),
                new ISOSecurityFunctions(),
                new UnionPayProtocolMessageValidator(),
                new UnionPayProtocolDialog(), new UnionPayFlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(UnionPayProtocol.class);
        return logger;
    }
}
