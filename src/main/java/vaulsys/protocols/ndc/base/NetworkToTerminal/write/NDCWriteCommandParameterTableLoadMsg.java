package vaulsys.protocols.ndc.base.NetworkToTerminal.write;

import vaulsys.protocols.ndc.base.NDCWriteCommandTypes;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.protocols.ndc.constants.NDCConstants;
import vaulsys.protocols.ndc.constants.NDCMessageClassTerminalToNetwork;

public class NDCWriteCommandParameterTableLoadMsg extends NDCWriteCommandMsg {

    public NDCWriteCommandParameterTableLoadMsg() {
    	writeIdentifier = NDCConstants.WRITE_IDENTIFIER;
        modifier = NDCWriteCommandTypes.PARAMETER_TABLE_LOAD;
        messageType = NDCMessageClassTerminalToNetwork.WRITE_COMMAND;
    }

    public NDCWriteCommandParameterTableLoadMsg(Long luno, String optionalParameter, String msgSeqCntr,
                                                    byte[] configData) {
        this.modifier = NDCWriteCommandTypes.PARAMETER_TABLE_LOAD;
        messageType = NDCMessageClassTerminalToNetwork.WRITE_COMMAND;
        logicalUnitNumber = luno;
//        logicalUnitNumber = luno;
        messageSequenceNumber = msgSeqCntr;
        writeIdentifier = NDCConstants.WRITE_IDENTIFIER;
//        optionalParameters = optionalParameter;// NDCOptionalParameter.AUTOMATIC_RETURN_PREVIOUS_MODE;
        allConfigData = configData;
        MAC = "01020304";
    }

//    public byte[] toBinary() throws IOException {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        out.write(messageType);
//        // out.write((byte) wmsg.responseFlag);
//        out.write(ASCIIConstants.FS);
//        out.write(logicalUnitNumber.byteValue());
//        out.write(ASCIIConstants.FS);
//        out.write(messageSequenceNumber.getBytes());
//        out.write(ASCIIConstants.FS);
//        out.write(writeIdentifier);
//        out.write(modifier);
//        out.write(ASCIIConstants.FS);
//        out.write("000".getBytes());
//        out.write("000".getBytes());
////        out.write(optionalParameters.getBytes());
//        out.write("000".getBytes());
//        out.write("000".getBytes());
//        out.write("000".getBytes());
//        out.write(ASCIIConstants.FS);
//        out.write(allConfigData);
//        out.write(ASCIIConstants.FS);
//
//        out.write(MAC.getBytes());
//        return out.toByteArray();
//    }
}
