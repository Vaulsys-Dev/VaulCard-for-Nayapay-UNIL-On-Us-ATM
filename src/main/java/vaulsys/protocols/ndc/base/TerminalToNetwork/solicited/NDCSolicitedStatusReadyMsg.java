package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCSolicitedStatusMsg;
import vaulsys.protocols.ndc.constants.NDCStatusDescriptor;
import vaulsys.util.MyInteger;

public class NDCSolicitedStatusReadyMsg extends NDCSolicitedStatusMsg {

    public NDCSolicitedStatusReadyMsg(MyInteger offset, byte[] rawdata) throws NotParsedBinaryToProtocolException {
        super(offset, rawdata);
        statusDescriptor = NDCStatusDescriptor.READY;
    }

    public NDCSolicitedStatusReadyMsg() {
        statusDescriptor = NDCStatusDescriptor.READY;
    }


}
