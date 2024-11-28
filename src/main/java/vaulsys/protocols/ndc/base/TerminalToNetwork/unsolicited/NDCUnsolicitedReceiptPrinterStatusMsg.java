package vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCReceiptPrinter;
import vaulsys.util.MyInteger;

public class NDCUnsolicitedReceiptPrinterStatusMsg extends NDCUnsolicitedStatusMsg<NDCReceiptPrinter> {

    public NDCUnsolicitedReceiptPrinterStatusMsg() {
    }

    public NDCUnsolicitedReceiptPrinterStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
        statusInformation = NDCReceiptPrinter.fromBinary(rawdata, offset);
    }
}