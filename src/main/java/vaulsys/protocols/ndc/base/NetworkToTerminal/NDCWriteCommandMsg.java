package vaulsys.protocols.ndc.base.NetworkToTerminal;

import vaulsys.protocols.ndc.base.NDCWriteCommandTypes;
import vaulsys.protocols.ndc.constants.NDCMessageClassTerminalToNetwork;
import vaulsys.terminal.atm.constants.NDCUtil;
import vaulsys.util.constants.ASCIIConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class NDCWriteCommandMsg extends NDCNetworkToTerminalMsg {

	public byte responseFlag;
	public String messageSequenceNumber;
	public byte writeIdentifier;
	public NDCWriteCommandTypes modifier;
	// Not Used
	// public String stateNumber;
	// public String stateData;
	// -
	// public String optionalParameters;
	public byte[] allConfigData;

	public String MAC;

	public NDCWriteCommandMsg() {
		messageType = NDCMessageClassTerminalToNetwork.WRITE_COMMAND;
//		messageType = NDCMessageClassNetworkToTerminal.CUSTOMIZATION_DATA_COMMAND;
	}

	public byte[] toBinary() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(messageType.getCode());
		// out.write((byte) wmsg.responseFlag);
		out.write(ASCIIConstants.FS);
		out.write("000".getBytes());
//		out.write(logicalUnitNumber.toString().getBytes());
		out.write(ASCIIConstants.FS);
		out.write(messageSequenceNumber.getBytes());
		out.write(ASCIIConstants.FS);
		out.write(writeIdentifier);
		out.write(modifier.getCode());
		out.write(ASCIIConstants.FS);
		if(allConfigData != null)
			out.write(allConfigData);
		
//		if (modifier == NDCWriteCommandTypes.PARAMETER_TABLE_LOAD)

		if(NDCUtil.isNeedSetMac(this)){
			out.write(ASCIIConstants.FS);
			out.write(MAC.getBytes());
		}
		return out.toByteArray();
	}

	public String toString()
	{
		return super.toString() + 
			"writeCommandIdentifier:\t\t" + messageType + "\r\n" + 
			"responseFlag:\t\t"	+ responseFlag + "\r\n" + 
			"messageSequenceNumber:\t\t" + messageSequenceNumber + "\r\n" + 
			"writeIdentifier:\t\t" + writeIdentifier + "\r\n" + 
			"modifier:\t\t" + modifier + "\r\n" + 
			"allStatesData:\t\t" + toString(allConfigData) + "\r\n" + 
			"MAC:\t\t" + MAC + "\r\n";
	}

	@Override
	public Boolean isRequest() {
		return null;
	}
	
	private String toString(byte[] arr) {
		StringBuilder builder = new StringBuilder();
		if(arr != null)
			for(byte b : arr)
				builder.append((char)b);
		return builder.toString();
	}
}
