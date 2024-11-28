package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.util.MyInteger;

public class NDCSolicitedStatusSendConfigIDResponseMsg extends NDCSolicitedStatusTerminalStateMsg {

    public String configId;

    public NDCSolicitedStatusSendConfigIDResponseMsg(MyInteger offset, byte[] rawdata) throws NotParsedBinaryToProtocolException {
        super(offset, rawdata);
        configId = new String(rawdata, offset.value, 4);
    }
    
    @Override
    public String toString() {
    	return super.toString() +
    	"Config ID:\t\t" + configId + "\r\n";
    	
    }
}