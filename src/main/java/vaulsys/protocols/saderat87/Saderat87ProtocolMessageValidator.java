package vaulsys.protocols.saderat87;

import vaulsys.protocols.base.ProtocolMessageValidator;
import vaulsys.protocols.saderat87.Saderat87ProtocolMessageValidator;

import org.apache.log4j.Logger;

public class Saderat87ProtocolMessageValidator implements ProtocolMessageValidator {
	transient Logger logger = Logger.getLogger(Saderat87ProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
    	return true;
    }

    @Override
    public void makeValidate() {
    }

}
