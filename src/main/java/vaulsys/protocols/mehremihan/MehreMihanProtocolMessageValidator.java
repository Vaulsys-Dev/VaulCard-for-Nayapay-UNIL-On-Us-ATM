package vaulsys.protocols.mehremihan;

import vaulsys.protocols.base.ProtocolMessageValidator;
import org.apache.log4j.Logger;

public class MehreMihanProtocolMessageValidator implements ProtocolMessageValidator {
	transient Logger logger = Logger.getLogger(MehreMihanProtocolMessageValidator.class);

	@Override
	public boolean validateIntegrity() {
		return true;
	}

	@Override
	public void makeValidate() {
	}

}
