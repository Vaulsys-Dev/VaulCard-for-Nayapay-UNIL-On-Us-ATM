package vaulsys.protocols.fnsPep;

import vaulsys.protocols.base.IfxToProtocolMapper;
import vaulsys.protocols.base.ProtocolProvider;
import vaulsys.protocols.base.ProtocolToIfxMapper;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;
import vaulsys.protocols.shetab87.Shetab87ProtocolFunctions;
import vaulsys.util.SwitchApplication;

import org.apache.log4j.Logger;

public class FnsPepProtocolFunctions extends Shetab87ProtocolFunctions {

    transient Logger logger = Logger.getLogger(FnsPepProtocolFunctions.class);

	@Override
	public ISOPackager getPackager() {
		return ((FnsPepProtocol) ProtocolProvider
                .Instance.getByClass(FnsPepProtocol.class))
                .getPackager();
	}

	@Override
	public IfxToProtocolMapper getIfxToProtocolMapper() {
		return FnsPepIFXToISOMapper.Instance; 
	}

	@Override
	public ProtocolToIfxMapper getProtocolToIfxMapper() {
		return FnsPepISOToIFXMapper.Instance;
	}
}
