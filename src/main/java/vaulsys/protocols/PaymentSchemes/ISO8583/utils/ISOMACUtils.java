package vaulsys.protocols.PaymentSchemes.ISO8583.utils;

import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.exception.exception.MacGenerationException;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.POSTerminalService;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.util.encoders.Hex;

import java.util.Set;

import org.apache.log4j.Logger;

public class ISOMACUtils {

    transient static Logger logger = Logger.getLogger(ISOMACUtils.class);

    public static void findProfilesAndSetMac(/*Outgoing*/Message outgoingMessage) throws Exception {
        //NOTE: put MAC at the end of the message
        Channel channel = outgoingMessage.getChannel();

        if (channel.getMacEnable()) {
            Long profile = null;
            Set<SecureKey> keySet = null;

            Terminal t = outgoingMessage.getEndPointTerminal();
            if(t == null)
            	return;
            
            keySet = t.getKeySet();
            profile = t.getOwnOrParentSecurityProfileId();

            if ((keySet == null || keySet.isEmpty()) && t instanceof POSTerminal) {
              POSTerminalService.addDefaultKeySetForTerminal((POSTerminal) t);
              keySet = t.getKeySet();
          }
            setMac(profile, keySet, outgoingMessage);
        }
    }

    private static void setMac(Long securityProfileId, Set<SecureKey> keySet, /*Outgoing*/Message message) throws Exception {
        if (securityProfileId != null && keySet != null && !keySet.isEmpty()) {
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
                //Note: The outgoing message must be one of the network management message!
                // Since all received message in this phase has MAC, we only overwrite it with proper MAC
                outgoingMessageWithoutMac = new byte[message.getBinaryData().length - 16];
                System.arraycopy(message.getBinaryData(), 0, outgoingMessageWithoutMac, 0,
                        outgoingMessageWithoutMac.length);
            }

            byte[] mac = SecurityComponent.generateCBC_MAC(securityProfileId, keySet, outgoingMessageWithoutMac);
            //mac = Hex.encode(mac);
            String MAC = new String(Hex.encode(mac)).toUpperCase();
//            logger.debug("ISO UTILS SENT MAC: " + MAC);

            // NOTE: our outgoingMessage has MAC by default. ( To generate
            // outPut_ifx, we clone input_ifx! )
            byte[] binaryMsg = new byte[message.getBinaryData().length /*
																		 * +
																		 * mac.length
																		 */];
            System.arraycopy(message.getBinaryData(), 0, binaryMsg, 0, binaryMsg.length - MAC.length());
            System.arraycopy(MAC.getBytes(), 0, binaryMsg, binaryMsg.length - MAC.length(), MAC.length());
            
            
            message.setBinaryData(binaryMsg);
        } else {
        	logger.error("Failed: No profile or no keyset (MAC Generation)");
            throw new MacGenerationException("Failed: No profile or no keyset (MAC Generation)");
        }
    }
}
