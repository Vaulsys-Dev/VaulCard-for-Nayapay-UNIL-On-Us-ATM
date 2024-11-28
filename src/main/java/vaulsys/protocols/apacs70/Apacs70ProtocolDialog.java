package vaulsys.protocols.apacs70;

import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;

/**
 * Created by IntelliJ IDEA.
 * User: User
 * Date: Oct 8, 2009
 * Time: 5:42:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class Apacs70ProtocolDialog implements ProtocolDialog{
	public Ifx refine(Ifx ifx) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public ProtocolMessage refine(ProtocolMessage protocolMessage) throws Exception {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	////Raza Adding for Field traslation start
	@Override
	public ProtocolMessage TranslateToFanap(ProtocolMessage protocolMessage) throws Exception
	{
		//logger.info("Translating incoming message from Apacs...");
		return protocolMessage;
	}

	@Override
	public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception
	{
		//logger.info("Translating outgoing message for Apacs...");
		return protocolMessage;
	}
	////Raza Adding for Field traslation end
}
