package vaulsys.protocols.ndc.base.NetworkToTerminal.write;

import vaulsys.protocols.ndc.base.NDCWriteCommandTypes;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.protocols.ndc.constants.NDCConstants;

public class NDCWriteCommandConfigurationIDLoadMsg extends NDCWriteCommandMsg {

    public NDCWriteCommandConfigurationIDLoadMsg() {
    	this.writeIdentifier = NDCConstants.WRITE_IDENTIFIER;
        this.modifier = NDCWriteCommandTypes.CONFIGURATION_ID_LOAD;
    }
}
