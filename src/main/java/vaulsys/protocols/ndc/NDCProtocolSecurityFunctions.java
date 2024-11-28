package vaulsys.protocols.ndc;

import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolSecurityFunctionsImpl;
import vaulsys.protocols.exception.exception.MacGenerationException;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.impl.Terminal;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;

import java.util.Set;

import org.apache.log4j.Logger;

public class NDCProtocolSecurityFunctions extends ProtocolSecurityFunctionsImpl {

	transient Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void setMac(ProcessContext processContext, Terminal terminal, Long securityProfileId, Set<SecureKey> keySet, Message message,
			Boolean enabled) throws Exception {

		if (enabled) {
			if (securityProfileId != null && keySet != null && !keySet.isEmpty()) {
				byte[] binaryData = message.getBinaryData();
		        byte[] data = new byte[binaryData.length - 9];
				System.arraycopy(binaryData, 0, data, 0, data.length);
				byte[] mac = SecurityComponent.generateCBC_MAC(securityProfileId, keySet, data);
				String MAC = new String(Hex.encode(mac)).toUpperCase();
//				logger.debug("SENT MAC: " + MAC);
				byte[] binaryMsg = new byte[message.getBinaryData().length];
				System.arraycopy(binaryData, 0, binaryMsg, 0, binaryMsg.length - MAC.length());
				System.arraycopy(MAC.getBytes(), 0, binaryMsg, binaryMsg.length - MAC.length(), MAC.length());
				message.setBinaryData(binaryMsg);
			} else {
				throw new MacGenerationException("Failed: No profile or no keyset (MAC Generation)");
			}
		}
	}

}
