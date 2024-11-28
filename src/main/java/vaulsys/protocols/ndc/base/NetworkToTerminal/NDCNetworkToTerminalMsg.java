package vaulsys.protocols.ndc.base.NetworkToTerminal;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.ndc.base.NDCMsg;

public abstract class NDCNetworkToTerminalMsg extends NDCMsg {

    public Ifx toIfx() throws Exception {
        throw new UnsupportedOperationException("Cannot convert to Ifx message");
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
