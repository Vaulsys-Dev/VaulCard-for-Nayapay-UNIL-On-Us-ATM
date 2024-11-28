package vaulsys.protocols.epay;

import vaulsys.protocols.base.Protocol;

public class EpayProtocol extends Protocol {

    public EpayProtocol(String name) {
        super(	name, 
        		new EpayProtocolFunctions(), 
        		new EpayProtocolSecurityFunctions(),
        		new EpayProtocolMessageValidator(),
                new EpayProtocolDialog(), new EpayFlowDispatcher());
    }

}
