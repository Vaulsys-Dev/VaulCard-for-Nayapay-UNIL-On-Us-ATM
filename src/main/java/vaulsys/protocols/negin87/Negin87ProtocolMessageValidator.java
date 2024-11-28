package vaulsys.protocols.negin87;

import vaulsys.protocols.base.ProtocolMessageValidator;

import org.apache.log4j.Logger;

public class Negin87ProtocolMessageValidator implements ProtocolMessageValidator {

    transient Logger logger = Logger.getLogger(Negin87ProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
    	return true;
    }

    @Override
    public void makeValidate() {
    }
}
