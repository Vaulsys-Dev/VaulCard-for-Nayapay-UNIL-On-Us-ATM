package vaulsys.protocols.infotech;


import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;

import org.apache.log4j.Logger;

public class InfotechProtocol extends ISO8583BaseProtocol {

    public InfotechProtocol(String name) {
        super(name, new InfotechProtocolFunctions(),
        		new InfotechProtocolSecurityFunctions(),
        		new InfotechProtocolMessageValidator(),
                new InfotechProtocolDialog(), new InfotechFlowDispatcher());
    }

    @Override
    protected Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(InfotechProtocol.class);
        return logger;
    }
}
