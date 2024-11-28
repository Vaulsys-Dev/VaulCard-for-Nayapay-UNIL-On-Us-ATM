package vaulsys.protocols.pos87;

import vaulsys.protocols.base.ProtocolMessageValidator;

import org.apache.log4j.Logger;

public class Pos87ProtocolMessageValidator implements ProtocolMessageValidator {

    transient Logger logger = Logger
            .getLogger(Pos87ProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
    	return true;
    }

    @Override
    public void makeValidate() {
    }
}
