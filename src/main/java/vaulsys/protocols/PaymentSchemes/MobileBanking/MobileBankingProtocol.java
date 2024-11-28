package vaulsys.protocols.PaymentSchemes.MobileBanking;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class MobileBankingProtocol extends ISO8583BaseProtocol {

    public MobileBankingProtocol(String name) {
        super(name, new MobileBankingProtocolFunctions(),
                new ISOSecurityFunctions(),
                new MobileBankingProtocolMessageValidator(),
                new MobileBankingProtocolDialog(), new MobileBankingFlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(MobileBankingProtocol.class);
        return logger;
    }
}
