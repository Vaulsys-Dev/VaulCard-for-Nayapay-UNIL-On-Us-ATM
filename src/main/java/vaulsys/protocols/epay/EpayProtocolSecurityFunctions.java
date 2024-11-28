package vaulsys.protocols.epay;

import vaulsys.authentication.exception.MacFailException;
import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolSecurityFunctionsImpl;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

import java.util.Set;

public class EpayProtocolSecurityFunctions extends ProtocolSecurityFunctionsImpl {

	@Override
	public void setMac(ProcessContext processContext, Terminal t, Long securityProfileId, Set<SecureKey> keySet, Message message, Boolean enables)
			throws Exception {
	}

	@Override
	public void verifyMac(Terminal terminal, Long securityProfileId, Set<SecureKey> keySet, String mac,
			byte[] binaryData, boolean enable) throws MacFailException {
	}

    public void encrypt(Set<SecureKey> keySet, Message message)  throws Exception{
		byte[] binaryData = message.getBinaryData();
		byte[] b = SecurityComponent.encrypt(binaryData, SecureDESKey.getKeyByType(SMAdapter.TYPE_TMK, keySet));
		message.setBinaryData(b);    	
    }
    
    public void decrypt(Set<SecureKey> keySet, Message message) throws Exception{
		byte[] binaryData = message.getBinaryData();
		byte[] b = SecurityComponent.decrypt(binaryData, keySet);
		message.setBinaryData(b);
    }
    
    public void generatePinBlock(Set<SecureKey> keySet, Message message) throws Exception {
		byte[] binaryData = message.getBinaryData();
		byte[] b = SecurityComponent.decrypt(binaryData, keySet);
		message.setBinaryData(b);    	
    }
}
