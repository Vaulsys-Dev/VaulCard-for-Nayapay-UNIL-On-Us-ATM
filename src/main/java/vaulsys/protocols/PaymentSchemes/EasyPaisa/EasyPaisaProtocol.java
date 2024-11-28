package vaulsys.protocols.PaymentSchemes.EasyPaisa;


import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;

import org.apache.log4j.Logger;

public class EasyPaisaProtocol extends ISO8583BaseProtocol {

    public EasyPaisaProtocol(String name) {
        super(name, new EasyPaisaProtocolFunctions(),
        		new ISOSecurityFunctions(),
        		new EasyPaisaProtocolMessageValidator(),
                new EasyPaisaProtocolDialog(), new EasyPaisaFlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(EasyPaisaProtocol.class);
        return logger;
    }
}
