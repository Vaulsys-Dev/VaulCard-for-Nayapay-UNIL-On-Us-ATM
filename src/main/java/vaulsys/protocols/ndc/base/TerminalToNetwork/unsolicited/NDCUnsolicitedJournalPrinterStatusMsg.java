package vaulsys.protocols.ndc.base.TerminalToNetwork.unsolicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCUnsolicitedStatusMsg;
import vaulsys.protocols.ndc.base.deviceStatusInfo.NDCJournalPrinter;
import vaulsys.util.MyInteger;

public class NDCUnsolicitedJournalPrinterStatusMsg extends NDCUnsolicitedStatusMsg<NDCJournalPrinter> {

    public NDCUnsolicitedJournalPrinterStatusMsg() {
    }

    public NDCUnsolicitedJournalPrinterStatusMsg(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        super(rawdata, offset);
        statusInformation = NDCJournalPrinter.fromBinary(rawdata, offset);
    }
}