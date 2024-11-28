package vaulsys.protocols.ndc.base.NetworkToTerminal.operational;

import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCOperationalMsg;
import vaulsys.protocols.ndc.constants.NDCOperationalTypes;

public class NDCOperationalGoOutOfServiceMessage extends NDCOperationalMsg {

    public NDCOperationalGoOutOfServiceMessage() {
        commandCode = NDCOperationalTypes.GO_OUT_OF_SERVICE;
    }
}
