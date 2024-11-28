package vaulsys.protocols.ndc.base.NetworkToTerminal;

import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ndc.constants.NDCConstants;
import vaulsys.protocols.ndc.constants.NDCMessageClassNetworkToTerminal;
import vaulsys.protocols.ndc.constants.NDCOperationalTypes;
import vaulsys.protocols.ndc.constants.NDCPrinterFlag;
import vaulsys.protocols.ndc.constants.NDCTerminalCommandModifier;
import vaulsys.protocols.ndc.constants.NDCTerminalCommandModifierConfigurationInfo;
import vaulsys.protocols.ndc.encoding.NDCConvertor;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.atm.Receipt;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.StringFormat;
import vaulsys.util.constants.ASCIIConstants;
import vaulsys.wfe.ProcessContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class NDCOperationalMsg extends NDCNetworkToTerminalMsg {

    public byte responseFlag;
    public String messageSequenceNumber;
    public NDCOperationalTypes commandCode;
    public NDCTerminalCommandModifier commandModifier;
//    public NDCTerminalCommandModifier commandSubmodifier; 
    public String MAC;
    public Boolean doPrintImmediate;

    public NDCOperationalMsg() {
        messageType = NDCMessageClassNetworkToTerminal.TERMINAL_COMMAND;
    }

    public byte[] toBinary() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        if (doPrintImmediate != null && doPrintImmediate) {
            out.write(NDCConstants.FUNCTION_COMMAND);
            out.write(ASCIIConstants.FS);
            out.write(StringFormat.formatNew(9, StringFormat.JUST_RIGHT, this.getLogicalUnitNumber(), '0').getBytes());
            out.write(ASCIIConstants.FS);
            out.write(messageSequenceNumber.getBytes());
            out.write(ASCIIConstants.FS);
            out.write(ASCIIConstants.FS);
            out.write("00000000".getBytes());
            out.write(ASCIIConstants.FS);
            out.write("00004".getBytes());
            out.write(ASCIIConstants.FS);
            out.write("002".getBytes());
            
    		out.write(ASCIIConstants.ESC);
    		out.write(0x3A);
    		out.write("000".getBytes());
    		
    		out.write(ASCIIConstants.ESC);
    		out.write(0x26);
    		out.write("FANAPFA.FNT".getBytes());
    		out.write(ASCIIConstants.ESC);
    		out.write(0x5c);

    		out.write(ASCIIConstants.ESC);
    		out.write(0x25);
    		out.write("000".getBytes());
            
//            NDCConvertor convertor = new NDCConvertor();
//            ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, this.getLogicalUnitNumber());
//            ATMConfiguration atmConfiguration = ProcessContext.get().getATMConfiguration(atm.getOwnOrParentConfigurationId());
//			Integer lineLength = atmConfiguration.getReceiptLineLength();
//			Integer leftMargin = atmConfiguration.getReceiptLeftMargin();
//			NDCConvertor.TextDirection textDir = NDCConvertor.TextDirection.RightToLeft;

			
//			ByteArrayOutputStream table = new ByteArrayOutputStream();
//			for(int j=0; j<16; j++){
//				table.write(0x30+(byte) (j%10));
//			}
//			table.write(ASCIIConstants.CR);
//			table.write(ASCIIConstants.LF);
//
//			for(int i=2; i<16; i++){
//				table.write(0x30+(byte) (i%10));
//				table.write(0x20);
//
//				for(int j=0; j<16; j++){
//					table.write((byte)(i*16+j));
//				}
//				table.write(ASCIIConstants.CR);
//				table.write(ASCIIConstants.LF);
//			}
//			table.write(ASCIIConstants.FF);
			
//			String encoding = atmConfiguration.getEncodingMap().get(NDCConvertor.FARSI_RECIEPT_ENCODING);
//			String extendedEncoding = atmConfiguration.getEncodingMap().get(NDCConvertor.FARSI_EXTENDED_RECIEPT_ENCODING);
//			int indexOfConvertor = str.indexOf("[ESC](");
//			if (indexOfConvertor != -1){
//				encoding = str.substring(indexOfConvertor+6, indexOfConvertor+6 + 1);
//				if(atmConfiguration != null){
//					convertor =  getConvertor(encoding, /*encodings*/atmConfiguration.getEncodingMap(), /*convertors*/ atmConfiguration.getConvertorsMap());
//					if (encoding.equals(/*encodings.get(FARSI_RECIEPT_ENCODING)*/atmConfiguration.getEncodingMap().get(FARSI_RECIEPT_ENCODING))){
//						extendedEncoding = /*encodings.get(FARSI_EXTENDED_RECIEPT_ENCODING*/atmConfiguration.getEncodingMap().get(FARSI_EXTENDED_RECIEPT_ENCODING);
//					} else if (encoding.equals(/*encodings.get(FARSI_SCREEN_ENCODING)*/atmConfiguration.getEncodingMap().get(FARSI_SCREEN_ENCODING))){
//						extendedEncoding = /*encodings.get(FARSI_EXTENDED_SCREEN_ENCODING)*/atmConfiguration.getEncodingMap().get(FARSI_EXTENDED_SCREEN_ENCODING);
//					}
//				}
//			}
			

//			out.write(convertor.finalize(table.toByteArray(), "", ""));
			
//			out.write(ASCIIConstants.ESC);
//			out.write(("("+atmConfiguration.getEncodingMap().get(NDCConvertor.FARSI_RECIEPT_ENCODING)).getBytes());
//			out.write(convertor.center(textDir, lineLength, leftMargin, convertor.convert2Farsi(convertor.bnkFarsiName(atmConfiguration.getBnkFarsiName()))));
//			out.write(ASCIIConstants.LF);
//			out.write(convertor.center(textDir, lineLength, leftMargin, convertor.convert2Farsi("خودپرداز "+atm.getOwner().getName()+" 123456")));
//			out.write(ASCIIConstants.LF);
//			out.write(ASCIIConstants.LF);
			

			out.write(ASCIIConstants.FS);
            out.write("01020304".getBytes());
            
            
        } else {
	        
	        out.write(messageType.getCode());
	//				out.write((byte)opmsg.responseFlag);
	        out.write(ASCIIConstants.FS);
	        out.write("000".getBytes());
	//        out.write(logicalUnitNumber.toString().getBytes());
	        out.write(ASCIIConstants.FS);
	//        messageSequenceNumber = "000";// + Util.generateTrnSeqCntr(4);
	        out.write(messageSequenceNumber.getBytes());
	        out.write(ASCIIConstants.FS);
	        out.write(commandCode.getCode());
	        
	        if(commandModifier != null)
	        	out.write(commandModifier.getCode());
	//       if(commandSubmodifier != null)
	//     	  out.write(commandSubmodifier.getCode());
	//        out.write(ASCIIConstants.FS);
	//        out.write(MAC.getBytes());
        }
        
        return out.toByteArray();
    }

    public String toString() {
    	return super.toString()
                + "respFlag:\t\t" + responseFlag + "\r\n"
                + "msgSeqNum:\t\t" + messageSequenceNumber + "\r\n"
                + "commandCode:\t\t" + commandCode + "\r\n"
                + "commandModfier:\t\t" + commandModifier + "\r\n";
    }

	@Override
	public Boolean isRequest() throws Exception {
		return null;
	}
    
    
}
