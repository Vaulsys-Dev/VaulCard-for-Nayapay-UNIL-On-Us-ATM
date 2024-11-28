package vaulsys.protocols.cms;

import vaulsys.protocols.base.ProtocolMessageValidator;

public class CMSProtocolMessageValidator implements ProtocolMessageValidator {

	@Override
	public void makeValidate() {
	}

	@Override
	public boolean validateIntegrity() {
		return false;
	}

}
