package vaulsys.protocols.PaymentSchemes.EasyPaisa;

import vaulsys.protocols.base.ProtocolMessageValidator;

import org.apache.log4j.Logger;

public class  EasyPaisaProtocolMessageValidator implements ProtocolMessageValidator {

    transient Logger logger = Logger.getLogger( EasyPaisaProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
    	return true;
    }

    @Override
    public void makeValidate() {
    }
}
