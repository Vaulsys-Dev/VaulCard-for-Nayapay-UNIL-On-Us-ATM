package vaulsys.protocols.ndc.base.TerminalToNetwork;

import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.constants.NDCMessageClassSolicitedUnSokicited;

public abstract class NDCTerminalToNetworkMsg extends NDCMsg {
    public NDCMessageClassSolicitedUnSokicited solicited;

    public byte[] toBinary() {
        throw new UnsupportedOperationException("Cannot convert a Terminal to Network messages to binary");
    }

    public String toString() {
        return super.toString()
                + "solicited:\t\t" + solicited + "\r\n";

    }

	@Override
	public Boolean isRequest() {
		return null;
	}
    
}
