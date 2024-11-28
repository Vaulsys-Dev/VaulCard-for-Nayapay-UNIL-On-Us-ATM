package vaulsys.protocols.ndc.base.NetworkToTerminal.operational;

import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCOperationalMsg;
import vaulsys.protocols.ndc.constants.NDCOperationalTypes;

public class NDCOperationalSendConfigIDMessage extends NDCOperationalMsg {

    public NDCOperationalSendConfigIDMessage() {
        commandCode = NDCOperationalTypes.SEND_CONFIG_ID;
    }
}
