package vaulsys.protocols.infotech;

import vaulsys.base.components.BaseComponent;
import vaulsys.protocols.base.ProtocolMessageValidator;

import org.apache.log4j.Logger;

public class InfotechProtocolMessageValidator extends BaseComponent implements
        ProtocolMessageValidator {

    transient Logger logger = Logger.getLogger(InfotechProtocolMessageValidator.class);

    @Override
    public boolean validateIntegrity() {
    	return true;
    }

    @Override
    public void makeValidate() {
    }
}
