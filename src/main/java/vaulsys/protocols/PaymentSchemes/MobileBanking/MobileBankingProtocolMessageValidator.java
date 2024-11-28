package vaulsys.protocols.PaymentSchemes.MobileBanking;

import vaulsys.protocols.base.ProtocolMessageValidator;
import org.apache.log4j.Logger;

/**
 * Created by m.rehman on 4/9/2016.
 */

public class MobileBankingProtocolMessageValidator implements ProtocolMessageValidator {

    transient Logger logger = Logger.getLogger(MobileBankingProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
        return false;
    }

    @Override
    public void makeValidate() {

    }
}
