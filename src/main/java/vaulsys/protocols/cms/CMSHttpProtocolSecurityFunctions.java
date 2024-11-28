package vaulsys.protocols.cms;

import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolSecurityFunctionsImpl;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

import java.util.Set;

import org.apache.log4j.Logger;

public class CMSHttpProtocolSecurityFunctions extends ProtocolSecurityFunctionsImpl {

	transient private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void setMac(ProcessContext processContext, Terminal terminal, Long securityProfileId, Set<SecureKey> keySet, Message message,
			Boolean enabled) throws Exception {
	}
}
