package vaulsys.protocols.ndc.base.NetworkToTerminal;

import vaulsys.protocols.ndc.constants.NDCActiveKeys;
import vaulsys.protocols.ndc.constants.NDCMessageClassTerminalToNetwork;
import vaulsys.util.constants.ASCIIConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NDCWriteCommandOARMsg extends NDCResponseMsg {

    public byte responseFlag;
    public String messageSequenceNumber;
    public byte writeIdentifier;

    public byte displayFlag;
    public NDCActiveKeys activeKeys;

    public String screenTimer;
    public byte[] screenData;

    public NDCWriteCommandOARMsg() {
        messageType = NDCMessageClassTerminalToNetwork.WRITE_COMMAND;
        writeIdentifier = '2';
    }

    public byte[] toBinary() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(messageType.getCode());
        // out.write((byte) wmsg.responseFlag);
        out.write(ASCIIConstants.FS);
        out.write("000".getBytes());
//        out.write(logicalUnitNumber.toString().getBytes());
        out.write(ASCIIConstants.FS);
        out.write(messageSequenceNumber.getBytes());
        out.write(ASCIIConstants.FS);
        out.write(writeIdentifier);
        out.write(displayFlag);
        out.write(activeKeys.getBytes());
        out.write(ASCIIConstants.FS);
        out.write(screenTimer.getBytes());
        out.write(ASCIIConstants.FS);
        out.write(screenData);
//        out.write(screenData.getBytes());
//        out.write(ASCIIConstants.FS);
//        MAC = MAC == null ? "01020304" : MAC;
//        out.write(MAC.getBytes());
        
        return out.toByteArray();
    }
    
    @Override
    public String toString() {
    	String strResult = super.toString() 
    	+ "responseFlag:\t\t" + responseFlag + "\r\n" 
    	+ "messageSequenceNumber:\t\t" + messageSequenceNumber + "\r\n" 
    	+ "writeIdentifier:\t\t" + writeIdentifier + "\r\n"
		+ "displayFlag:\t\t" + displayFlag + "\r\n" 
		+ "activeKeys:\t\t" + activeKeys.toString() + "\r\n"
		+ "screenTimer:\t\t" + screenTimer + "\r\n"; 
		if(screenData != null)
        	strResult += "screenData:\t\t" + new String(screenData) + "\r\n";
        else
        	strResult += "screenData:\t\t" + "-" + "\r\n";
		return strResult
		;
    }
}
