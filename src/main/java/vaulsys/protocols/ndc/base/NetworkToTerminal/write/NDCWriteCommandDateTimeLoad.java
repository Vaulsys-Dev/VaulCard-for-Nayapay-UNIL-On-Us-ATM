package vaulsys.protocols.ndc.base.NetworkToTerminal.write;

import vaulsys.protocols.ndc.base.NDCWriteCommandTypes;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.protocols.ndc.constants.NDCConstants;

public class NDCWriteCommandDateTimeLoad extends NDCWriteCommandMsg {

	public NDCWriteCommandDateTimeLoad(String sequence, byte[] data, Long luno) {
		this.messageSequenceNumber = sequence;
		this.writeIdentifier = NDCConstants.WRITE_IDENTIFIER;
		this.modifier = NDCWriteCommandTypes.DATE_TIME_LOAD;
		this.allConfigData = data;
		
		this.logicalUnitNumber = luno;
	}

}
