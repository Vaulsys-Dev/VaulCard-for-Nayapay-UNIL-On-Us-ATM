package vaulsys.protocols.jware93;

import vaulsys.protocols.base.ProtocolMessageValidator;

import org.apache.log4j.Logger;

public class Jware93ProtocolMessageValidator implements ProtocolMessageValidator {

    transient Logger logger = Logger.getLogger(Jware93ProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
    	return true;
    }

    @Override
    public void makeValidate() {
    }

}
