package vaulsys.security.component;

import vaulsys.security.SecurityService;
import vaulsys.security.base.SecurityFunction;
import vaulsys.security.base.SecurityProfile;
import vaulsys.security.exception.SMException;
import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.security.jceadapter.JCESecurityModule;
import vaulsys.security.keystore.KeyType;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.securekey.SecureKey;
import vaulsys.security.ssm.base.EncryptedPIN;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.util.SwitchRuntimeException;
import vaulsys.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

/**
 * @author Ali Lahijani
 */
public class SecurityComponent{
    public static final String		FUNC_GENERATEKEY	= "generateKey";
    public static final String		FUNC_IMPORTKEY		= "importKey";
    public static final String		FUNC_EXPORTKEY		= "exportKey";
    public static final String		FUNC_ENCRYPTPIN		= "encryptPIN";
    public static final String		FUNC_DECRYPTPIN		= "decryptPIN";
    public static final String		FUNC_IMPORTPIN		= "importPIN";
    public static final String		FUNC_EXPORTPIN		= "exportPIN";
    public static final String		FUNC_TRANSLATEPIN	= "PIN_TRANS";
    public static final String		FUNC_DECRYPT		= "decrypt";
    public static final String		FUNC_ENCRYPT		= "encrypt";
    public static final String		FUNC_MAC_GEN		= "MAC_GEN";
    public static final String		FUNC_MAC_VER		= "MAC_VER";
    //m.rehman: for cms operations
    public static final String      FUNC_VALIDATEPIN    = "PIN_VALIDATE";
    public static final String      FUNC_CHANGEPIN    = "PIN_CHANGE";
    //Raza adding for NayaPay
    public static final String      FUNC_GENERATEPIN    = "PIN_GENERATE";
    
    private static Cipher cipher = null;
    
    private static Logger logger = Logger.getLogger(SecurityComponent.class);
    
    private static SecurityComponent securityCompinent = new SecurityComponent();
    
    protected static SMAdapter smAdapter;
    //    	private AccessManager			smAdapter;
//    protected SecurityProfile			securityProfile;
//    protected Set<SecureKey> keySet;
    private static SMAdapter defaultSecurityModule;

    static{
    	try {
			smAdapter = getDefaultSecurityModule();
			cipher = Cipher.getInstance("DES");
			
		} catch (SMException e) {
			logger.error("Could not load security module", e);
			throw new SwitchRuntimeException("Could not load security module", e);
		} catch(Exception e){
			logger.error(e);
		}
    }
//    private SecurityComponent() throws SMException {
////        this.keySet = keysByType;
////        this.securityProfile = profile;
//        this.smAdapter = getDefaultSecurityModule();
//
//    }

    private static synchronized SMAdapter getDefaultSecurityModule() throws SMException {
        if (defaultSecurityModule == null) {
            defaultSecurityModule = new JCESecurityModule("src/main/resources/config/LMK.jceks", "$3cureP@$$".toCharArray(), "org.bouncycastle.jce.provider.BouncyCastleProvider");
//            defaultSecurityModule = new EracomSecurityModule();
        }
        return defaultSecurityModule;
    }

    public static SecureDESKey generateKey(short keyLength, String keyType) throws SMException {
		return smAdapter.generateKey(keyLength, keyType);
	}

    public static byte[] generateCBC_MAC(Long securityProfileId, Set<SecureKey> keySet, byte[] data) throws Exception {
    	SecurityFunction mac_gen_func = SecurityService.findSecurityFunction(securityProfileId, FUNC_MAC_GEN);
        // SecurityFunction mac_gen_func = securityProfile.getSecurityFunction(FUNC_MAC_GEN);

        int macLength = Integer.parseInt(mac_gen_func.getParameterValue("MacLength"));
        byte[] mac = smAdapter.generateCBC_MAC(data, SecureDESKey.getKeyByType(KeyType.TYPE_TAK, keySet));
        return Arrays.copyOf(mac, macLength);
    }
    
/*
    public static byte[] generateCBC_MAC(Long securityProfileId, Set<SecureKey> keySet, byte[] data, Integer keyIndex) throws Exception {
    	SecurityFunction mac_gen_func = SecurityService.findSecurityFunction(securityProfileId, FUNC_MAC_GEN);
        // SecurityFunction mac_gen_func = securityProfile.getSecurityFunction(FUNC_MAC_GEN);

        int macLength = Integer.parseInt(mac_gen_func.getParameterValue("MacLength"));
        byte[] mac = smAdapter.generateCBC_MAC(data, SecureDESKey.getKeyByType(KeyType.TYPE_TAK, keySet, keyIndex));
        return Arrays.copyOf(mac, macLength);
    }
  
*/  
    public static SecureDESKey importKey(short keyLength, String keyType, byte[] encryptedKey, SecureDESKey kek, boolean checkParity) throws Exception {
//        SecureDESKey kek = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
        return smAdapter.importKey(keyLength, keyType, encryptedKey, kek, checkParity);
    }

    public static SecureDESKey importKey(Set<SecureKey> keySet, short keyLength, String keyType, byte[] encryptedKey, boolean checkParity) throws Exception {
        SecureDESKey kek = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
        return smAdapter.importKey(keyLength, keyType, encryptedKey, kek, checkParity);
    }
  
/*  
    public static SecureDESKey importKey(Set<SecureKey> keySet, short keyLength, String keyType, byte[] encryptedKey, boolean checkParity, Integer keyIndex) throws Exception {
        SecureDESKey kek = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet, keyIndex);
        SecureDESKey result = smAdapter.importKey(keyLength, keyType, encryptedKey, kek, checkParity);
        result.setKeyIndex(keyIndex.shortValue());
        return result;
    }
*/

    public static byte[] exportKey(SecureDESKey key, SecureDESKey kek) throws Exception {
//        SecureDESKey kek = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
        return smAdapter.exportKey(key, kek);
    }

    public static byte[] exportKey(Set<SecureKey> keySet, SecureDESKey key) throws Exception {
        SecureDESKey kek = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, keySet);
        return smAdapter.exportKey(key, kek);
    }

    public static boolean verifyMac(Long securityProfileId, Set<SecureKey> keySet, byte[] data, String sentMac) throws Exception {
//    	securityProfile = GeneralDao.Instance.getObject(SecurityProfile.class, securityProfileId);
        SecurityFunction mac_ver_func = SecurityService.findSecurityFunction(securityProfileId, FUNC_MAC_VER);  
//        	securityProfile.getSecurityFunction(FUNC_MAC_VER);
        int macLength = Integer.parseInt(mac_ver_func.getParameterValue("MacLength"));
        int skipLength = Integer.parseInt(mac_ver_func.getParameterValue("SkipLength"));

        byte[] pureData = new byte[data.length - (skipLength)];
        System.arraycopy(data, 0, pureData, 0, data.length - (skipLength));
        byte[] msgMAC = new byte[16];
        System.arraycopy(data, data.length - (macLength*2), msgMAC, 0, (macLength*2));
        msgMAC = Hex.decode(msgMAC);
//        msgMAC = Hex.decode(sentMac.getBytes());

        byte[] mac = generateCBC_MAC(securityProfileId, keySet, pureData);
        if (mac == null)
            return false;
//        String ourMac = ISOUtil.hexString(mac); // HSMUtil.hexToString(mac);

        for (int i = 0; i < mac.length && i < macLength; i++)
            if (mac[i] != msgMAC[i])
                return false;

        return true;

        // return sentMac.equalsIgnoreCase(ourMac);
    }
   
/* 
    public static boolean verifyMac(Long securityProfileId, Set<SecureKey> keySet, byte[] data, String sentMac, Integer keyIndex) throws Exception {
//    	securityProfile = GeneralDao.Instance.getObject(SecurityProfile.class, securityProfileId);
        SecurityFunction mac_ver_func = SecurityService.findSecurityFunction(securityProfileId, FUNC_MAC_VER);  
//        	securityProfile.getSecurityFunction(FUNC_MAC_VER);
        int macLength = Integer.parseInt(mac_ver_func.getParameterValue("MacLength"));
        int skipLength = Integer.parseInt(mac_ver_func.getParameterValue("SkipLength"));

        byte[] pureData = new byte[data.length - (skipLength)];
        System.arraycopy(data, 0, pureData, 0, data.length - (skipLength));
        byte[] msgMAC = new byte[16];
        System.arraycopy(data, data.length - (macLength*2), msgMAC, 0, (macLength*2));
        msgMAC = Hex.decode(msgMAC);
//        msgMAC = Hex.decode(sentMac.getBytes());

        byte[] mac = generateCBC_MAC(securityProfileId, keySet, pureData, keyIndex);
        if (mac == null)
            return false;
//        String ourMac = ISOUtil.hexString(mac); // HSMUtil.hexToString(mac);

        for (int i = 0; i < mac.length && i < macLength; i++)
            if (mac[i] != msgMAC[i])
                return false;

        return true;

        // return sentMac.equalsIgnoreCase(ourMac);
    }

*/

    public static byte[] translatePIN(Long securityProfileId, Set<SecureKey> keySet, byte[] inputPinBlock, String anb, 
    		Long inProfileId, Set<SecureKey> inputKeySet) throws Exception {

        SecurityFunction out_pin_trans = SecurityService.findSecurityFunction(securityProfileId, FUNC_TRANSLATEPIN); 
//        	securityProfile.getSecurityFunction(FUNC_TRANSLATEPIN);
        
        SecurityFunction in_pin_trans = SecurityService.findSecurityFunction(inProfileId, FUNC_TRANSLATEPIN); 
//        	inProfile.getSecurityFunction(FUNC_TRANSLATEPIN);

        byte PFi = HSMUtil.stringToHex((in_pin_trans.getParameterValue("PIN Format")))[0];
        byte PFo = HSMUtil.stringToHex((out_pin_trans.getParameterValue("PIN Format")))[0];

        int anb_length = Integer.parseInt((in_pin_trans.getParameterValue("AccountNumber Length")));
        anb = anb.substring(anb.length() - anb_length - 1, anb.length() - 1);

//        SecureDESKey masterKey = getMasterKey();

        SecureDESKey fromKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, inputKeySet);
        SecureDESKey toKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
        EncryptedPIN inputPin = new EncryptedPIN(inputPinBlock, PFi, anb);
        EncryptedPIN outputPin = smAdapter.translatePIN(inputPin, fromKey, toKey, PFo);

        return outputPin.getPINBlock();
    }
    
    public static byte[] translatePIN(Long securityProfileId, Set<SecureKey> keySet, byte[] inputPinBlock, String anb, 
    		Long inProfileId, Set<SecureKey> inputKeySet,String dstAnb) throws Exception {

        SecurityFunction out_pin_trans = SecurityService.findSecurityFunction(securityProfileId, FUNC_TRANSLATEPIN); 
//        	securityProfile.getSecurityFunction(FUNC_TRANSLATEPIN);
        
        SecurityFunction in_pin_trans = SecurityService.findSecurityFunction(inProfileId, FUNC_TRANSLATEPIN); 
//        	inProfile.getSecurityFunction(FUNC_TRANSLATEPIN);

        byte PFi = HSMUtil.stringToHex((in_pin_trans.getParameterValue("PIN Format")))[0];
        byte PFo = HSMUtil.stringToHex((out_pin_trans.getParameterValue("PIN Format")))[0];

        int anb_length = Integer.parseInt((in_pin_trans.getParameterValue("AccountNumber Length")));
        anb = anb.substring(anb.length() - anb_length - 1, anb.length() - 1);

//        SecureDESKey masterKey = getMasterKey();

        SecureDESKey fromKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, inputKeySet);
        SecureDESKey toKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
        EncryptedPIN inputPin = new EncryptedPIN(inputPinBlock, PFi, anb);
        int anb2_length = Integer.parseInt((in_pin_trans.getParameterValue("AccountNumber Length")));
        dstAnb = dstAnb.substring(dstAnb.length() - anb2_length - 1, dstAnb.length() - 1);
        EncryptedPIN outputPin = smAdapter.translatePIN(inputPin, fromKey, toKey, PFo,dstAnb);
        return outputPin.getPINBlock();
    }    

    public static byte[] decrypt(byte[] inputBlock, Set<SecureKey> inputKeySet) throws Exception {
        SecureDESKey fromKey = SecureDESKey.getKeyByType(KeyType.TYPE_TMK, inputKeySet);
        byte[] decryptedData = smAdapter.decrypt(inputBlock, fromKey);
    	return decryptedData;
    }
    
    public static byte[] encrypt(byte[] inputBlock, SecureDESKey key) throws Exception {
        byte[] encryptedData = smAdapter.encrypt(inputBlock, key, "ZeroBytePadding");
    	return encryptedData;
    }
    
    public static byte[] desDecrypt(byte[] inputBlock, byte[] desKey) throws Exception {
        byte[] decryptedData = smAdapter.desDecrypt(inputBlock, desKey);
    	return decryptedData;
    }
    
    public static byte[] desEncrypt(byte[] inputBlock, byte[] desKey) throws Exception {
        byte[] encryptedData = smAdapter.desEncrypt(inputBlock, desKey);
    	return encryptedData;
    }
    
    public static byte[] tripleDesDecrypt(byte[] inputBlock, byte[] key1, byte[] key2, byte[] key3) throws Exception {
        byte[] decryptedData3 = smAdapter.desDecrypt(inputBlock, key3);
        byte[] encryptedData2 = smAdapter.desEncrypt(decryptedData3, key2);
        byte[] decryptedData1 = smAdapter.desDecrypt(encryptedData2, key1);
    	return decryptedData1;
    }
    
    public static byte[] tripleDesEncrypt(byte[] inputBlock, byte[] key1, byte[] key2, byte[] key3) throws Exception {
        byte[] encryptedData1 = smAdapter.desEncrypt(inputBlock, key1);
        byte[] decryptedData2 = smAdapter.desDecrypt(encryptedData1, key2);
        byte[] encryptedData3 = smAdapter.desEncrypt(decryptedData2, key3);
    	return encryptedData3;
    }

    public static byte[] encryptPINByKey(Long securityProfileId, Set<SecureKey> keySet, String inputPIN, String inputPAN) throws Exception {
        SecureDESKey toKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);

        SecurityFunction out_pin_trans = SecurityService.findSecurityFunction(securityProfileId, FUNC_TRANSLATEPIN); 
//    	securityProfile.getSecurityFunction(FUNC_TRANSLATEPIN);
    
	    byte PFi = HSMUtil.stringToHex((out_pin_trans.getParameterValue("PIN Format")))[0];
	
	    int anb_length = Integer.parseInt((out_pin_trans.getParameterValue("AccountNumber Length")));
	    inputPAN = inputPAN.substring(inputPAN.length() - anb_length - 1, inputPAN.length() - 1);
	
        EncryptedPIN pin = smAdapter.encryptPINByKey(inputPIN, inputPAN, PFi, toKey);
    	return pin.getPINBlock();
    }
    
    public static String decryptPINByKey(Long securityProfileId, Set<SecureKey> keySet, byte[] pinBlock, String PAN) throws Exception {
        SecureDESKey inKey = SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet);
        
        SecurityFunction in_pin_trans = SecurityService.findSecurityFunction(securityProfileId, FUNC_TRANSLATEPIN); 
//    	securityProfile.getSecurityFunction(FUNC_TRANSLATEPIN);
    
	    byte PFi = HSMUtil.stringToHex((in_pin_trans.getParameterValue("PIN Format")))[0];
	
	    int anb_length = Integer.parseInt((in_pin_trans.getParameterValue("AccountNumber Length")));
	    PAN = PAN.substring(PAN.length() - anb_length - 1, PAN.length() - 1);
	
	    EncryptedPIN inputPin = new EncryptedPIN(pinBlock, PFi, PAN);

	    String pin = smAdapter.decryptPINByKey(inputPin, inKey);
    	return pin;
    }

    public static byte[] generateCellChargePIN(Set<SecureKey> keySet, String cellChargePin) throws SMException{
    	if (cellChargePin.length() < 16){
    		for (int i = cellChargePin.length(); i < 16; i++)
    			cellChargePin += "F";
    	}
    	
    	return smAdapter.encrypt(Hex.decode(cellChargePin), SecureDESKey.getKeyByType(KeyType.TYPE_TPK, keySet), "NoPadding");
    }    
    
//    public byte[] rsaEncrypt(byte[] plainData) throws Exception {
//	    byte[] cipher = smAdapter.rsaEncrypt(plainData);
//    	return cipher;
//    }
//
    public static byte[] rsaDecrypt(byte[] cipherData) throws Exception {
	    byte[] plain = smAdapter.rsaDecrypt(cipherData);
    	return plain;
    }
    
    public static String base64Encrypt(String data, byte[] byteKey, String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, java.security.InvalidKeyException {
        try {
            // Encode the string into bytes using utf-8
            byte[] utf8 = data.getBytes("UTF8");
            SecretKey key = new SecretKeySpec(byteKey, algorithm/*"DES"*/);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            // Encrypt
            byte[] enc = cipher.doFinal(utf8);

            // Encode bytes to base64 to get a string
            return new sun.misc.BASE64Encoder().encode(enc);
        } catch (javax.crypto.BadPaddingException e) {
        	logger.error(e);
        } catch (IllegalBlockSizeException e) {
        	logger.error(e);
        } catch (UnsupportedEncodingException e) {
        	logger.error(e);
        } catch (java.io.IOException e) {
        	logger.error(e);
        }
        return null;
    }

    public static String base64Decrypt(String data, byte[] byteKey, String algorithm) throws java.security.InvalidKeyException {
        try {
            // Decode base64 to get bytes
            byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(data);
            SecretKey key = new SecretKeySpec(byteKey, algorithm/*"DES"*/);
            cipher.init(Cipher.DECRYPT_MODE, key);
            // Decrypt
            byte[] utf8 = cipher.doFinal(dec);

            // Decode using utf-8
            return new String(utf8, "UTF8");
        } catch (javax.crypto.BadPaddingException e) {
        	logger.error(e);
        } catch (IllegalBlockSizeException e) {
        	logger.error(e);
        } catch (UnsupportedEncodingException e) {
        	logger.equals(e);
        } catch (java.io.IOException e) {
        	logger.error(e);
        }
        return null;
    }
    
    public static void main(String[] args) throws SMException {
        new JCESecurityModule("/config/LMK.jceks", "$3cureP@$$".toCharArray(), "org.bouncycastle.jce.provider.BouncyCastleProvider", true);
    }
}
