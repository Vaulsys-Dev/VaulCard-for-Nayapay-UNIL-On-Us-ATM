package vaulsys.protocols.jware93;

import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;

public class Jware93ProtocolDialog implements ProtocolDialog {

    @Override
    public Ifx refine(Ifx ifx) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProtocolMessage refine(ProtocolMessage protocolMessage) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    ////Raza Adding for Field traslation start
    @Override
    public ProtocolMessage TranslateToFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //logger.info("Translating incoming message from Jware...");
        return protocolMessage;
    }

    @Override
    public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception
    {
        //logger.info("Translating outgoing message for Jware...");
        return protocolMessage;
    }
    ////Raza Adding for Field traslation end
}
