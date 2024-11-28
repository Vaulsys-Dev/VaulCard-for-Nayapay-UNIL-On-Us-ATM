package vaulsys.protocols.ndc.base.NetworkToTerminal.write;

import vaulsys.protocols.ndc.base.NDCWriteCommandTypes;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.protocols.ndc.constants.NDCConstants;

public class NDCWriteCommandStateTableLoadMsg extends NDCWriteCommandMsg {

    public NDCWriteCommandStateTableLoadMsg() {
    	this.writeIdentifier = NDCConstants.WRITE_IDENTIFIER;
        this.modifier = NDCWriteCommandTypes.STATE_TABLE_LOAD;
    }
}
