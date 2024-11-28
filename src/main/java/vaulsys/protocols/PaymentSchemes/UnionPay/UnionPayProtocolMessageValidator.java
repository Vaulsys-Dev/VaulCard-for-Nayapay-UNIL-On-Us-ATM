package vaulsys.protocols.PaymentSchemes.UnionPay;

import vaulsys.protocols.base.ProtocolMessageValidator;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class UnionPayProtocolMessageValidator implements ProtocolMessageValidator {

    transient Logger logger = Logger.getLogger(UnionPayProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
        return false;
    }

    @Override
    public void makeValidate() {

    }
}
