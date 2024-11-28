package vaulsys.protocols.epay;

import vaulsys.protocols.base.ProtocolMessageValidator;

public class EpayProtocolMessageValidator implements ProtocolMessageValidator {

	@Override
	public void makeValidate() {
	}

	@Override
	public boolean validateIntegrity() {
		return false;
	}

}
