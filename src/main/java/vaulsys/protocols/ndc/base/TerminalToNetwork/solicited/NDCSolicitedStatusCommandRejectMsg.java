package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.base.TerminalToNetwork.NDCSolicitedStatusMsg;
import vaulsys.protocols.ndc.constants.NDCStatusDescriptor;
import vaulsys.util.MyInteger;

public class NDCSolicitedStatusCommandRejectMsg extends NDCSolicitedStatusMsg {
	 public NDCSolicitedStatusCommandRejectMsg(MyInteger offset, byte[] rawdata) throws NotParsedBinaryToProtocolException {
	        super(offset, rawdata);
	        statusDescriptor = NDCStatusDescriptor.COMMAND_REJECT;
	    }

	    public NDCSolicitedStatusCommandRejectMsg() {
	        statusDescriptor = NDCStatusDescriptor.COMMAND_REJECT;
	    }

}
