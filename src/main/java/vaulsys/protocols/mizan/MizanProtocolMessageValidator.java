package vaulsys.protocols.mizan;

import vaulsys.base.components.BaseComponent;
import vaulsys.protocols.base.ProtocolMessageValidator;

public class MizanProtocolMessageValidator extends BaseComponent implements ProtocolMessageValidator {

	@Override
	public boolean validateIntegrity() {
		return true;
	}

	@Override
	public void makeValidate() {
	}

}
