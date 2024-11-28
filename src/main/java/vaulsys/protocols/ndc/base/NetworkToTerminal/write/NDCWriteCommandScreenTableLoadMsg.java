package vaulsys.protocols.ndc.base.NetworkToTerminal.write;

import vaulsys.protocols.ndc.base.NDCWriteCommandTypes;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.protocols.ndc.constants.NDCConstants;

public class NDCWriteCommandScreenTableLoadMsg extends NDCWriteCommandMsg {

    public NDCWriteCommandScreenTableLoadMsg() {
    	writeIdentifier = NDCConstants.WRITE_IDENTIFIER;
        modifier = NDCWriteCommandTypes.SCREEN_TABLE_LOAD;
    }
}
