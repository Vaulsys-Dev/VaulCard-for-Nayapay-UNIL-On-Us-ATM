package vaulsys.protocols.shetab87;

import vaulsys.protocols.base.ProtocolMessageValidator;

import org.apache.log4j.Logger;

public class Shetab87ProtocolMessageValidator implements ProtocolMessageValidator {

    transient Logger logger = Logger.getLogger(Shetab87ProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
    	return true;
    }

    @Override
    public void makeValidate() {
    }
}
