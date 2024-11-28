package vaulsys.protocols.ndc.base.NetworkToTerminal.write;

import vaulsys.protocols.ndc.base.NDCWriteCommandTypes;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.protocols.ndc.constants.NDCConstants;

public class NDCWriteCommandFitTableLoadMsg extends NDCWriteCommandMsg {
    public NDCWriteCommandFitTableLoadMsg(){
    	this.writeIdentifier = NDCConstants.WRITE_IDENTIFIER;
        this.modifier = NDCWriteCommandTypes.FIT_TABLE_LOAD;
    }
}
