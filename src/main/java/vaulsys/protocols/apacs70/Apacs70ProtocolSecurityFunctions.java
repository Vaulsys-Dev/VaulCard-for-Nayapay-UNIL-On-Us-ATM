package vaulsys.protocols.apacs70;

import vaulsys.authentication.exception.MacFailException;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.base.ProtocolSecurityFunctionsImpl;
import vaulsys.protocols.exception.exception.MacGenerationException;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.security.component.SecurityComponent;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.POSTerminalService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.TerminalGroup;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;

import java.util.Set;

import org.apache.log4j.Logger;

public class Apacs70ProtocolSecurityFunctions extends ProtocolSecurityFunctionsImpl {
	private static final Logger logger = Logger.getLogger(Apacs70ProtocolSecurityFunctions.class);

	public static int MAC_LEN = 4;
	
	@Override
	public void verifyMac(Terminal terminal, Long securityProfileId, Set<SecureKey> keySet, String mac,
			byte[] binariData, boolean enable) throws MacFailException {

		try {
			if (keySet == null || keySet.isEmpty()) {
				//addDefaultKeySet((POSTerminal) terminal);
				POSTerminalService.addDefaultKeySetForTerminal(/*(POSTerminal)*/ terminal);
				keySet = terminal.getKeySet();
			}
		} catch (Exception e) {
			logger.error("Default keyset generation: ", e);
			throw new MacFailException("Default keyset generation", e);
		}

		
		/*** set manually Apacs SecurityProfile to avoid human error ***/
		Long newSecurityProfileId = securityProfileId;
//		SecurityProfile apacsSecurityProfile = SecurityService.findSecurityProfileLikeName("PACS");
//		if (apacsSecurityProfile != null) {
//			newSecurityProfileId = apacsSecurityProfile.getId();
//			logger.debug("Apacs securityProfile manually set,  id:" + newSecurityProfileId);
//		}
		try {
			if(terminal.getParentGroupId() != 380129801L && terminal.getParentGroupId() != 386825501L){
				TerminalGroup terminalGroup = null;
				if (TerminalType.POS.equals(terminal.getTerminalType())) {
	//				terminalGroup = TerminalService.findTerminalGroup("ترمینال های پروتکل apacs"); //id = 383583201
					terminalGroup = TerminalService.findTerminalGroup(380129801L); //id = 383583201
				} else if (TerminalType.PINPAD.equals(terminal.getTerminalType())) {
	//				terminalGroup = TerminalService.findTerminalGroup("PinPad-Apacs"); //id = 386825501
					terminalGroup = TerminalService.findTerminalGroup(386825501L); //id = 386825501
				}
				if (terminalGroup != null) {
					terminal.setParentGroup(terminalGroup);
					terminal.setSharedFeature(terminalGroup.getSafeSharedFeature());
					newSecurityProfileId = terminalGroup.getSafeSharedFeature().getSecurityProfileId();
					GeneralDao.Instance.saveOrUpdate(terminal);
				}
			}
			
		} catch(Exception e) {
			logger.warn("Exception in getting apacs security profile...");
//			newSecurityProfileId = securityProfileId;
		}
		super.verifyMac(terminal, newSecurityProfileId, /*securityProfileId,*/ keySet, mac, binariData, enable);
	}

	@Override
	public void setMac(ProcessContext processContext, Terminal terminal, Long securityProfileId,
			Set<SecureKey> keySet, Message message, Boolean enabled) throws Exception {

		if (enabled) {
			
			/*** set manually Apacs SecurityProfile to avoid human error ***/
			Long newSecurityProfileId = securityProfileId;
					
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
//			try {
//				SecurityProfile apacsSecurityProfile = SecurityService.findSecurityProfileLikeName("PACS");		
//				newSecurityProfileId = apacsSecurityProfile.getId();
//			} catch(Exception e) {
//				logger.warn("Exception in getting apacs security profile...");
//				newSecurityProfileId = securityProfileId;
//			}
			
//			if (newSecurityProfileId != null) {
//				if(keySet == null || keySet.isEmpty()) {
//					//addDefaultKeySet((POSTerminal) terminal);
//					POSTerminalService.addDefaultKeySetForTerminal(/*(POSTerminal)*/ terminal);
//					keySet = terminal.getKeySet();
//				}
////				if (processContext != null) {
////					SecureKey lastAcqMacKey = processContext.getLastAcqMacKey();
////					if (lastAcqMacKey != null) {
////						SecureDESKey keyByType = SecureDESKey.getKeyByType(KeyType.TYPE_TAK, keySet);
////						keySet.remove(keyByType);
////						keySet.add(lastAcqMacKey);
////					}
////				}
//			} else {
//				logger.error("MacGenerationException: Failed: No profile (MAC Generation)");
//				throw new MacGenerationException("Failed: No profile (MAC Generation)");
//			}

			byte[] outgoingMessageWithoutMac = null;

			if (message.getIfx() != null) {
				if ("".equals(message.getIfx().getMsgAuthCode())) {
					outgoingMessageWithoutMac = message.getBinaryData();
				} else {
					outgoingMessageWithoutMac = new byte[message.getBinaryData().length - MAC_LEN * 2];
					System.arraycopy(message.getBinaryData(), 
							0, outgoingMessageWithoutMac, 
							0, outgoingMessageWithoutMac.length);
				}
			}

			byte[] mac = SecurityComponent.generateCBC_MAC(newSecurityProfileId, keySet, outgoingMessageWithoutMac);
			String MAC = new String(Hex.encode(mac)).toUpperCase();
//			logger.debug("SENT MAC: " + MAC);

			byte[] binaryMsg = new byte[message.getBinaryData().length];

			System.arraycopy(message.getBinaryData(), 0, binaryMsg, 0, binaryMsg.length - MAC.length());
			System.arraycopy(MAC.getBytes(), 0, binaryMsg, binaryMsg.length - MAC.length(), MAC.length());

			message.setBinaryData(binaryMsg);
		}
	}
 
/*	public static void addDefaultKeySet(POSTerminal terminal) throws Exception {
        byte[] defult = new byte[]{0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};
        SecureDESKey masterKey = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TMK, defult, null, false);
        SecureDESKey macKey = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TAK, defult, null, false);
        SecureDESKey pinKey = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TPK, defult, null, false);
        terminal.addSecureKey(masterKey);
        terminal.addSecureKey(macKey);
        terminal.addSecureKey(pinKey);
        
        GeneralDao.Instance.saveOrUpdate(masterKey);
        GeneralDao.Instance.saveOrUpdate(macKey);
        GeneralDao.Instance.saveOrUpdate(pinKey);
        GeneralDao.Instance.saveOrUpdate(terminal);
	}*/
	
/*    public static void addDefaultKeySet(POSTerminal pos) throws Exception {
        byte[] serialNo = pos.getSerialno().getBytes();
        byte[] paddedSerialNo = new byte[16];

        for (int i = 0; i < paddedSerialNo.length; ++i)
            paddedSerialNo[i] = '0';

        int j = serialNo.length-1;
        for (int i = 15; i>=0 && j >= 0; i--,j--)
        	paddedSerialNo[i] = serialNo[j];

        byte[] masterKeyBytes = {1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8};
        byte[] xorResult = XOR(paddedSerialNo, masterKeyBytes);

        byte[] newBigKey = Hex.decode(SHA1(xorResult));
        byte[] evenKey = new byte[8];
        byte[] oddKey = new byte[8];

        for (int i = 0; i < 8; i++) {
            evenKey[i] = newBigKey[2 * i];
            oddKey[i] = newBigKey[2 * i + 1];
        }

        SecureDESKey masterKey = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TMK, oddKey, null, false);
        SecureDESKey macKey = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TAK, evenKey, null, false);
        SecureDESKey pinKey = SecurityComponent.importKey(SMAdapter.LENGTH_DES, KeyType.TYPE_TPK, oddKey, null, false);
        pos.addSecureKey(masterKey);
        logger.debug(String.format("POS[%s], default master key: %s", pos.getCode(), masterKey.getKeyBytes()));
        pos.addSecureKey(macKey);
        logger.debug(String.format("POS[%s], default MAC key: %s", pos.getCode(), macKey.getKeyBytes()));
        pos.addSecureKey(pinKey);
        logger.debug(String.format("POS[%s], default PIN key: %s", pos.getCode(), pinKey.getKeyBytes()));
        
        GeneralDao.Instance.saveOrUpdate(masterKey);
        GeneralDao.Instance.saveOrUpdate(macKey);
        GeneralDao.Instance.saveOrUpdate(pinKey);
        GeneralDao.Instance.saveOrUpdate(pos);
    }

    private static byte[] XOR(byte[] buffer1, byte[] buffer2) {
        byte[] result = new byte[buffer1.length];

        int i = 0;
        while (i < buffer1.length) {
            result[i] = (byte) (buffer1[i] ^ buffer2[i]);
            i++;
        }
        return result;
    }

    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    private static String SHA1(byte[] src) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(src);
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
*/
}
