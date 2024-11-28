package vaulsys.protocols.ndc.base.NetworkToTerminal.write;

import vaulsys.protocols.ndc.base.NDCWriteCommandTypes;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCWriteCommandMsg;
import vaulsys.protocols.ndc.constants.NDCConstants;
import vaulsys.protocols.ndc.constants.NDCMessageClassTerminalToNetwork;
import vaulsys.util.constants.ASCIIConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NDCWriteCommandEnhancedParameterTableLoadMsg extends NDCWriteCommandMsg {

    public NDCWriteCommandEnhancedParameterTableLoadMsg() {
    	writeIdentifier = NDCConstants.WRITE_IDENTIFIER;
        modifier = NDCWriteCommandTypes.ENHANCED_PARAMETER_TABLE_LOAD;
        messageType = NDCMessageClassTerminalToNetwork.WRITE_COMMAND;
    }

//    public NDCWriteCommandEnhancedParameterTableLoadMsg(Long luno, String optionalParameter, String msgSeqCntr,
//                                                            byte[] configData) {
//        this.modifier = NDCConstants.MODIFIER_ENHANCED_CUSTOMIZATION;
//        messageType = NDCConstants.WRITE_COMMAND;
//        logicalUnitNumber = luno;
//        messageSequenceNumber = msgSeqCntr;
//        writeIdentifier = NDCConstants.WRITE_IDENTIFIER;
////        optionalParameters = optionalParameter;// NDCOptionalParameter.AUTOMATIC_RETURN_PREVIOUS_MODE;
//        allConfigData = configData;
//        MAC = "01020304";
//    }

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
        out.write(modifier.getCode());
        out.write(ASCIIConstants.FS);
        out.write("000".getBytes());
//        out.write(logicalUnitNumber.toString().getBytes());
        out.write(ASCIIConstants.FS);
        out.write(allConfigData);
//        out.write(ASCIIConstants.FS);
//        out.write(MAC.getBytes());

        return out.toByteArray();
    }
}