package vaulsys.terminal.atm.constants;

import vaulsys.protocols.ndc.base.NDCMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.NDCOperationalMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandConfigurationIDLoadMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandDateTimeLoad;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandEnhancedParameterTableLoadMsg;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandExtEncKeyChange;
import vaulsys.protocols.ndc.base.NetworkToTerminal.write.NDCWriteCommandScreenTableLoadMsg;

public class NDCUtil {
	public static boolean isNeedSetMac(NDCMsg ndcMsg) {
		if (ndcMsg instanceof NDCWriteCommandScreenTableLoadMsg || 
			ndcMsg instanceof NDCWriteCommandConfigurationIDLoadMsg ||
			ndcMsg instanceof NDCWriteCommandEnhancedParameterTableLoadMsg ||
			ndcMsg instanceof NDCWriteCommandDateTimeLoad ||
			(ndcMsg instanceof NDCOperationalMsg && (((NDCOperationalMsg)ndcMsg).doPrintImmediate == null || !((NDCOperationalMsg)ndcMsg).doPrintImmediate)) ||
			ndcMsg instanceof NDCWriteCommandExtEncKeyChange)
			return false;
		return true;
	}
}
