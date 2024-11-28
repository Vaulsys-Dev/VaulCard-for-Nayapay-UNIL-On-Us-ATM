package vaulsys.protocols.PaymentSchemes.ISO8583;

import vaulsys.message.Message;
import vaulsys.protocols.base.ProtocolSecurityFunctionsImpl;
import vaulsys.protocols.exception.exception.MacGenerationException;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.impl.Terminal;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;

import java.util.Set;

import org.apache.log4j.Logger;

public class ISOSecurityFunctions extends ProtocolSecurityFunctionsImpl {

	transient Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void setMac(ProcessContext processContext, Terminal terminal, Long securityProfileId, Set<SecureKey> keySet, Message message,
			Boolean enabled) throws Exception {

		if (enabled) {
			if (securityProfileId != null && keySet != null && !keySet.isEmpty()) {
				if (processContext != null ) {
					SecureKey lastAcqMacKey = processContext.getLastAcqMacKey();
					if (lastAcqMacKey != null) {
						SecureDESKey keyByType = SecureDESKey.getKeyByType(KeyType.TYPE_TAK, keySet);
						keySet.remove(keyByType);
						keySet.add(lastAcqMacKey);
					}
				}
			}else {
				logger.error("MacGenerationException: Failed: No profile or no keyset (MAC Generation)" );
				throw new MacGenerationException("Failed: No profile or no keyset (MAC Generation)");
			}

			byte[] outgoingMessageWithoutMac = null;

			if (message.getIfx() != null) {
				if ("".equals(message.getIfx().getMsgAuthCode())) {
					outgoingMessageWithoutMac = message.getBinaryData();
				} else {
					outgoingMessageWithoutMac = new byte[message.getBinaryData().length - 16];
					System.arraycopy(message.getBinaryData(), 0, outgoingMessageWithoutMac, 0,
							outgoingMessageWithoutMac.length);
				}
			} else {
				// Note: The outgoing message must be one of the network
				// management message!
				// Since all received message in this phase has MAC, we only
				// overwrite it with proper MAC
				outgoingMessageWithoutMac = new byte[message.getBinaryData().length - 16];
				System.arraycopy(message.getBinaryData(), 0, outgoingMessageWithoutMac, 0,
						outgoingMessageWithoutMac.length);
			}

			byte[] mac = SecurityComponent.generateCBC_MAC(securityProfileId, keySet, outgoingMessageWithoutMac);
			String MAC = new String(Hex.encode(mac)).toUpperCase();
//			logger.debug("SENT MAC: " + MAC);

			byte[] binaryMsg = new byte[message.getBinaryData().length];
			
			System.arraycopy(message.getBinaryData(), 0, binaryMsg, 0, binaryMsg.length - MAC.length());
			System.arraycopy(MAC.getBytes(), 0, binaryMsg, binaryMsg.length - MAC.length(), MAC.length());

			message.setBinaryData(binaryMsg);
		} 
	}
}
