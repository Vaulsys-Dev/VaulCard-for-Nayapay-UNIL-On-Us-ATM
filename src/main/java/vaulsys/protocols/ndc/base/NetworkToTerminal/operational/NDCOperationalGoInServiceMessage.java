package vaulsys.protocols.ndc.base.NetworkToTerminal.operational;

import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCOperationalMsg;
import vaulsys.protocols.ndc.constants.NDCOperationalTypes;

public class NDCOperationalGoInServiceMessage extends NDCOperationalMsg {

    public NDCOperationalGoInServiceMessage() {
        commandCode = NDCOperationalTypes.GO_IN_SERVICE;
    }
}
