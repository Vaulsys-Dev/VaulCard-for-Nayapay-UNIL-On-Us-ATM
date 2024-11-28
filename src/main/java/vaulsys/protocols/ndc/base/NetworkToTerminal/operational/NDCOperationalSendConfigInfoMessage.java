package vaulsys.protocols.ndc.base.NetworkToTerminal.operational;

import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCOperationalMsg;
import vaulsys.protocols.ndc.constants.NDCOperationalTypes;

public class NDCOperationalSendConfigInfoMessage extends NDCOperationalMsg {

    public NDCOperationalSendConfigInfoMessage() {
        commandCode = NDCOperationalTypes.SEND_CONFIG_INFO;
    }

    public NDCOperationalSendConfigInfoMessage(Long logicalUnitNumber) {
        commandCode = NDCOperationalTypes.SEND_CONFIG_INFO;
        this.logicalUnitNumber = logicalUnitNumber;
    }
}