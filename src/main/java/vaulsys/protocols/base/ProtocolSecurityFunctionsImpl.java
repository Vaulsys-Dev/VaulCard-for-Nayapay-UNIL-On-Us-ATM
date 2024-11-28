package vaulsys.protocols.base;

import vaulsys.authentication.exception.MacFailException;
import vaulsys.message.Message;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.security.SecurityService;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

import java.util.Set;

import org.apache.log4j.Logger;

public abstract class ProtocolSecurityFunctionsImpl implements ProtocolSecurityFunctions{

	transient private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void verifyMac(Terminal terminal, Long securityProfileId, Set<SecureKey> keySet, String mac, byte[] binaryData, boolean enable) throws MacFailException{
		try {
			if (!enable)
				return;

			if (mac == null || !SecurityComponent.verifyMac(securityProfileId, keySet, binaryData, mac)) {
				logger.warn("Failed: Mac Verification failed!");
				throw new MacFailException("Failed:Mac verification failed.");
			}
//			logger.debug("Mac is verified");
		} catch (Exception e) {
			throw new MacFailException(e);
		}
	}
	
	
	@Override
	public Boolean isTranslatePIN(Ifx ifx) {
		return SecurityService.isTranslatePIN(ifx);
	}
	
	@Override
    abstract public void setMac(ProcessContext processContext, Terminal terminal, Long securityProfileId, Set<SecureKey> keySet, Message message, Boolean enabled) throws Exception;
}
