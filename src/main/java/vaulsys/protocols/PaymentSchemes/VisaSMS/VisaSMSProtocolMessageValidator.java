package vaulsys.protocols.PaymentSchemes.VisaSMS;

import vaulsys.protocols.base.ProtocolMessageValidator;
import org.apache.log4j.Logger;

/**
 * Created by HP on 11/23/2016.
 */
public class VisaSMSProtocolMessageValidator implements ProtocolMessageValidator {
    transient Logger logger = Logger.getLogger(VisaSMSProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
        return false;
    }

    @Override
    public void makeValidate() {

    }
}
