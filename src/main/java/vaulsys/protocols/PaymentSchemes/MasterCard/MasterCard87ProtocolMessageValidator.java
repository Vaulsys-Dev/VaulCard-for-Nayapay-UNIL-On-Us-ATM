package vaulsys.protocols.PaymentSchemes.MasterCard;

import vaulsys.protocols.base.ProtocolMessageValidator;

import org.apache.log4j.Logger;

public class  MasterCard87ProtocolMessageValidator implements ProtocolMessageValidator {

    transient Logger logger = Logger.getLogger( MasterCard87ProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
    	return true;
    }

    @Override
    public void makeValidate() {
    }
}
