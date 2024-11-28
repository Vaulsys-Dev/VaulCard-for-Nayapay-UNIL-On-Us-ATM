package vaulsys.protocols.fnsPep;


import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import vaulsys.protocols.shetab87.Shetab87ProtocolDialog;
import vaulsys.protocols.shetab87.Shetab87ProtocolMessageValidator;

import org.apache.log4j.Logger;

public class FnsPepProtocol extends ISO8583BaseProtocol {

    public FnsPepProtocol(String name) {
        super(name, new FnsPepProtocolFunctions(),
        		new ISOSecurityFunctions(),
        		new Shetab87ProtocolMessageValidator(),
                new Shetab87ProtocolDialog(), new FnsPepFlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(FnsPepProtocol.class);
        return logger;
    }
}
