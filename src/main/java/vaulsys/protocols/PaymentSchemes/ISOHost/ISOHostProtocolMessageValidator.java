package vaulsys.protocols.PaymentSchemes.ISOHost;

import vaulsys.protocols.base.ProtocolMessageValidator;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class ISOHostProtocolMessageValidator implements ProtocolMessageValidator {

    transient Logger logger = Logger.getLogger(ISOHostProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
        return false;
    }

    @Override
    public void makeValidate() {

    }
}
