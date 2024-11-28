package vaulsys.protocols.base;

import vaulsys.authentication.exception.MacFailException;
import vaulsys.message.Message;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

import java.util.Set;

public interface ProtocolSecurityFunctions {

	public void verifyMac(Terminal terminal, Long securityProfileId, Set<SecureKey> keySet, String mac, byte[] binaryData, boolean enable) throws MacFailException;
	
    public void setMac(ProcessContext processContext, Terminal t, Long securityProfileId, Set<SecureKey> keySet, Message message, Boolean enables) throws Exception;
    
    public Boolean isTranslatePIN(Ifx ifx);
}
