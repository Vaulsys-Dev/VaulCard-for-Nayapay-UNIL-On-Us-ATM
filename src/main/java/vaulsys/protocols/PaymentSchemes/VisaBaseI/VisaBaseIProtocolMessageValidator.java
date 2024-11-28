package vaulsys.protocols.PaymentSchemes.VisaBaseI;

import vaulsys.protocols.base.ProtocolMessageValidator;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class VisaBaseIProtocolMessageValidator implements ProtocolMessageValidator {

    transient Logger logger = Logger.getLogger(VisaBaseIProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
        return false;
    }

    @Override
    public void makeValidate() {

    }
}
