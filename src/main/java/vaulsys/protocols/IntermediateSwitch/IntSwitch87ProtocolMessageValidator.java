package vaulsys.protocols.IntermediateSwitch;

import vaulsys.protocols.base.ProtocolMessageValidator;

import org.apache.log4j.Logger;

public class IntSwitch87ProtocolMessageValidator implements ProtocolMessageValidator {

    transient Logger logger = Logger.getLogger(IntSwitch87ProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
    	return true;
    }

    @Override
    public void makeValidate() {
    }
}
