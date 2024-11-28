package vaulsys.protocols.ndc.base.NetworkToTerminal.write;

import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.protocols.ndc.constants.NDCExtEncryptionKeyChangeType;

public class NDCWriteCommandExtEncKeyChange extends NDCWriteCommandMsg {
	
	public NDCWriteCommandExtEncKeyChange(NDCExtEncryptionKeyChangeType modifier, Long luno, String seqNo) {
		//this.messageType = 3 Data Command
		
		// Message Sub-class: Extended Encryption Key Information
		this.writeIdentifier = '4';
		
		/**
		 * Modifiers:
		 *	'2': Decipher new communications key with current master key
		 *	'3': Decipher new communications key with current communications key
		 */
		this.modifier = modifier; 

		this.logicalUnitNumber = luno;
		this.messageSequenceNumber = seqNo;
		this.allConfigData = null;
	}

	public NDCWriteCommandExtEncKeyChange(NDCExtEncryptionKeyChangeType modifier, String newKeyData, Long luno, String seqNo) {
		this(modifier, luno, seqNo);
		
		String data = String.format("%03x", newKeyData.length()) + newKeyData;
		this.allConfigData= data.getBytes();
	}
}
