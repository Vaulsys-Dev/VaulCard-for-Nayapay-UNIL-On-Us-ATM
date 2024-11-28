package vaulsys.protocols.saderat87;

import vaulsys.protocols.PaymentSchemes.ISO8583.ISO8583BaseProtocol;
import vaulsys.protocols.PaymentSchemes.ISO8583.ISOSecurityFunctions;
import vaulsys.protocols.saderat87.Saderat87FlowDispatcher;
import vaulsys.protocols.saderat87.Saderat87Protocol;
import vaulsys.protocols.saderat87.Saderat87ProtocolDialog;
import vaulsys.protocols.saderat87.Saderat87ProtocolFunctions;
import vaulsys.protocols.saderat87.Saderat87ProtocolMessageValidator;
import vaulsys.wfe.base.FlowDispatcher;

import org.apache.log4j.Logger;


public class Saderat87Protocol extends ISO8583BaseProtocol {
	
public Saderat87Protocol(String name) {
	        super(name, new Saderat87ProtocolFunctions(),
	        		new ISOSecurityFunctions(),
	        		new Saderat87ProtocolMessageValidator(),
	                new Saderat87ProtocolDialog(), new Saderat87FlowDispatcher());
	    }
	

	    @Override
	    protected Logger getLogger() {
	        if (logger == null)
	            logger = Logger.getLogger(Saderat87Protocol.class);
	        return logger;
	    }

}
