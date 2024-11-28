package vaulsys.protocols.cms;

import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;

public class CMSProtocolDialog implements ProtocolDialog {

	@Override
	public Ifx refine(Ifx ifx) {
		return null;
	}

	@Override
	public ProtocolMessage refine(ProtocolMessage protocolMessage) throws Exception {
		return null;
	}

	////Raza Adding for Field traslation start
	@Override
	public ProtocolMessage TranslateToFanap(ProtocolMessage protocolMessage) throws Exception
	{
		//logger.info("Translating incoming message from CMS...");
		return protocolMessage;
	}

	@Override
	public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception
	{
		//logger.info("Translating outgoing message for CMS...");
		return protocolMessage;
	}
	////Raza Adding for Field traslation end
}
