/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package vaulsys.security.jceadapter;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.security.exception.SMException;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.ssm.base.BaseSMAdapter;
import vaulsys.security.ssm.base.EncryptedPIN;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.util.encoders.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Hashtable;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * <p>
 * JCESecurityModule is an implementation of a security module in software.
 * It doesn't require any hardware device to work.<br>
 * JCESecurityModule also implements the SMAdapter, so you can view it: either
 * as a self contained security module adapter that doesn't need a security module
 * or a security module that plugs directly to jpos, so doesn't need
 * a separate adapter.<br>
 * It relies on Java(tm) Cryptography Extension (JCE), hence its name.<br>
 * JCESecurityModule relies on the JCEHandler class to do the low level JCE work.
 * </p>
 * <p>
 * WARNING: This version of JCESecurityModule is meant for testing purposes and
 * NOT for life operation, since the Local Master Keys are stored in CLEAR on
 * the system's disk. Comming versions of JCESecurity Module will rely on
 * java.security.KeyStore for a better protection of the Local Master Keys.
 * </p>
 * @author Hani Samuel Kirollos
 * @version $Revision: 1.2 $ $Date: 2007/04/20 15:36:00 $
 */
@SuppressWarnings({"JavaDoc"})
public class JCESecurityModule extends BaseSMAdapter {

    /**
     * Creates an uninitialized JCE Security Module, you need to setConfiguration to initialize it
     */
    public JCESecurityModule () {
        super();
    }

    /**
     * @param lmkFile Local Master Keys filename of the JCE Security Module
     * @throws SMException
     */
    public JCESecurityModule(String lmkFile, char[] lmkPassword) throws SMException {
        this(lmkFile, lmkPassword, false);
    }

    /**
     * @param lmkFile Local Master Keys filename of the JCE Security Module
     * @param lmkRebuild
     * @throws SMException
     */
    public JCESecurityModule(String lmkFile, char[] lmkPassword, boolean lmkRebuild) throws SMException
    {
        init(null, lmkFile, lmkRebuild, lmkPassword);
    }

    public JCESecurityModule(String lmkFile, char[] lmkPassword, String jceProviderClassName) throws SMException {
        this(lmkFile, lmkPassword, jceProviderClassName, false);
    }

    public JCESecurityModule(String lmkFile, char[] lmkPassword, String jceProviderClassName, boolean lmkRebuild) throws SMException
    {
        init(jceProviderClassName, lmkFile, lmkRebuild, lmkPassword);
    }

    public SecureDESKey generateKeyImpl (short keyLength, String keyType) throws SMException {
        SecureDESKey generatedSecureKey;
        Key generatedClearKey = jceHandler.generateDESKey(keyLength);
        generatedSecureKey = encryptToLMK(keyLength, keyType, generatedClearKey);
        return  generatedSecureKey;
    }

    public SecureDESKey importKeyImpl (short keyLength, String keyType, byte[] encryptedKey,
            SecureDESKey kek, boolean checkParity) throws SMException {
        SecureDESKey importedKey;
        // decrypt encrypted key
        Key encryptingKey = kek == null ? null : decryptFromLMK(kek);
        Key clearKEY = jceHandler.decryptDESKey(keyLength, encryptedKey, encryptingKey,
                checkParity);
        // Encrypt Key under LMK
        importedKey = encryptToLMK(keyLength, keyType, clearKEY);
        return  importedKey;
    }

    public byte[] exportKeyImpl (SecureDESKey key, SecureDESKey kek) throws SMException {
        byte[] exportedKey;
        // get key in clear
        Key clearKey = decryptFromLMK(key);
        // Encrypt key under kek
        exportedKey = jceHandler.encryptDESKey(key.getKeyLength(), clearKey, decryptFromLMK(kek));
        return  exportedKey;
    }

    public EncryptedPIN encryptPINImpl (String pin, String accountNumber) throws SMException {
        EncryptedPIN encryptedPIN;
        byte[] clearPINBlock = calculatePINBlock(pin, FORMAT00, accountNumber);
        // Encrypt
        byte[] translatedPINBlock = jceHandler.encryptData(clearPINBlock, getLMK(PINLMKIndex));
        encryptedPIN = new EncryptedPIN(translatedPINBlock, FORMAT00, accountNumber);
        return  encryptedPIN;
    }

    public String decryptPINImpl (EncryptedPIN pinUnderLmk) throws SMException {
        String pin;
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderLmk.getPINBlock(),
                getLMK(PINLMKIndex));
        pin = calculatePIN(clearPINBlock, pinUnderLmk.getPINBlockFormat(), pinUnderLmk.getAccountNumber());
        return  pin;
    }

    @Override
    public EncryptedPIN encryptPINByKeyImpl(String pin, String accountNumber, byte blockFormat, SecureDESKey toKey) throws SMException {
        EncryptedPIN encryptedPIN = null;
        byte[] clearPINBlock = calculatePINBlock(pin, blockFormat, accountNumber);
        // Encrypt
        byte[] translatedPINBlock = jceHandler.encryptData(clearPINBlock, decryptFromLMK(toKey));
        encryptedPIN = new EncryptedPIN(translatedPINBlock, blockFormat, accountNumber);
        return encryptedPIN;
    }

    @Override
    public String decryptPINByKeyImpl(EncryptedPIN pinUnderLmk, SecureDESKey inKey) throws SMException {
        String pin = null;
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderLmk.getPINBlock(), decryptFromLMK(inKey));
        pin = calculatePIN(clearPINBlock, pinUnderLmk.getPINBlockFormat(), pinUnderLmk.getAccountNumber());
        return pin;
    }

    public EncryptedPIN importPINImpl (EncryptedPIN pinUnderKd1, SecureDESKey kd1) throws SMException {
        EncryptedPIN pinUnderLmk;
        // read inputs
        String accountNumber = pinUnderKd1.getAccountNumber();
        // Use FORMAT00 for encrypting PIN under LMK
        byte destinationPINBlockFormat = FORMAT00;
        // get clear PIN
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderKd1.getPINBlock(),
                decryptFromLMK(kd1));
        // extract clear pin (as entered by card holder)
        String pin = calculatePIN(clearPINBlock, pinUnderKd1.getPINBlockFormat(),
                accountNumber);
        // Format PIN Block using proprietary FORMAT00 to be encrypetd under LMK
        clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, accountNumber);
        // encrypt PIN
        byte[] translatedPINBlock = jceHandler.encryptData(clearPINBlock, getLMK(PINLMKIndex));
        pinUnderLmk = new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat,
                accountNumber);
        return  pinUnderLmk;
    }

    public EncryptedPIN exportPINImpl (EncryptedPIN pinUnderLmk, SecureDESKey kd2,
            byte destinationPINBlockFormat) throws SMException {
        EncryptedPIN exportedPIN;
        String accountNumber = pinUnderLmk.getAccountNumber();
        // process
        // get clear PIN
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderLmk.getPINBlock(),
                getLMK(PINLMKIndex));
        // extract clear pin
        String pin = calculatePIN(clearPINBlock, pinUnderLmk.getPINBlockFormat(),
                accountNumber);
        clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, accountNumber);
        // encrypt PIN
        byte[] translatedPINBlock = jceHandler.encryptData(clearPINBlock, decryptFromLMK(kd2));
        exportedPIN = new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat,
                accountNumber);
        return  exportedPIN;
    }

    public EncryptedPIN translatePINImpl (EncryptedPIN pinUnderKd1, SecureDESKey kd1,
            SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        EncryptedPIN translatedPIN;
        String accountNumber = pinUnderKd1.getAccountNumber();
        // get clear PIN
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderKd1.getPINBlock(),
                decryptFromLMK(kd1));
        String strClearPINBlock = new String(Hex.encode(clearPINBlock));
        String pin = calculatePIN(clearPINBlock, pinUnderKd1.getPINBlockFormat(),
                accountNumber);
        // Reformat PIN Block
        clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, accountNumber);
        // encrypt PIN
        byte[] translatedPINBlock = jceHandler.encryptData(clearPINBlock, decryptFromLMK(kd2));
        translatedPIN = new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat,
                accountNumber);
        return  translatedPIN;
    }
    
    public EncryptedPIN translatePINImpl (EncryptedPIN pinUnderKd1, SecureDESKey kd1,
            SecureDESKey kd2, byte destinationPINBlockFormat,String secondAccountNumber) throws SMException {
        EncryptedPIN translatedPIN;
        String accountNumber = pinUnderKd1.getAccountNumber();
        // get clear PIN
        byte[] clearPINBlock = jceHandler.decryptData(pinUnderKd1.getPINBlock(),
                decryptFromLMK(kd1));
        String strClearPINBlock = new String(Hex.encode(clearPINBlock));
        String pin = calculatePIN(clearPINBlock, pinUnderKd1.getPINBlockFormat(),
                accountNumber);
        // Reformat PIN Block
        clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, secondAccountNumber);
        // encrypt PIN
        byte[] translatedPINBlock = jceHandler.encryptData(clearPINBlock, decryptFromLMK(kd2));
        translatedPIN = new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat,
        		secondAccountNumber);
        return  translatedPIN;
    }    

    /**
     * Generates a random clear key component.<br>
     * Used by Console, that's why it is package protected.
     * @param keyLength
     * @return clear key componenet
     * @throws SMException
     */
    String generateClearKeyComponent (short keyLength) throws SMException {
        String clearKeyComponenetHexString;
        try {
            Key clearKey = jceHandler.generateDESKey(keyLength);
            byte[] clearKeyData = jceHandler.extractDESKeyMaterial(keyLength, clearKey);
            clearKeyComponenetHexString = ISOUtil.hexString(clearKeyData);
        } catch (SMException e) {
            throw  e;
        } finally {
        }
        return  clearKeyComponenetHexString;
    }

    /**
     * Generates key check value.<br>
     * Though not confidential, it is used only by Console,
     * that's why it is package protected.
     * @param keyLength
     * @param keyType
     * @param KEYunderLMKHexString
     * @return SecureDESKey object with its check value set
     * @throws SMException
     */
    SecureDESKey generateKeyCheckValue (short keyLength, String keyType, String KEYunderLMKHexString) throws SMException {
        SecureDESKey secureDESKey = null;
        byte[] keyCheckValue;
        try {
            secureDESKey = new SecureDESKey(keyLength, keyType,
                KEYunderLMKHexString, "");
            keyCheckValue = calculateKeyCheckValue(decryptFromLMK(secureDESKey));
            secureDESKey.setBKeyCheckValue(keyCheckValue);
        } catch (SMException e) {
            throw  e;
        } finally {
        }
        return  secureDESKey;
    }

    /**
     * Forms a key from 3 clear components and returns it encrypted under its corresponding LMK
     * The corresponding LMK is determined from the keyType
     * @param keyLength e.g. LENGTH_DES, LENGTH_DES3_2, LENGTH_DES3_3, ..
     * @param keyType possible values are those defined in the SecurityModule inteface. e.g., ZMK, TMK,...
     * @param clearComponent1HexString HexString containing the first component
     * @param clearComponent2HexString HexString containing the second component
     * @param clearComponent3HexString HexString containing the second component
     * @return forms an SecureDESKey from two clear components
     * @throws SMException
     */
    SecureDESKey formKEYfromThreeClearComponents (short keyLength, String keyType,
            String clearComponent1HexString, String clearComponent2HexString, String clearComponent3HexString) throws SMException {
        SecureDESKey secureDESKey;
        try {
            byte[] clearComponent1 = ISOUtil.hex2byte(clearComponent1HexString);
            byte[] clearComponent2 = ISOUtil.hex2byte(clearComponent2HexString);
            byte[] clearComponent3 = ISOUtil.hex2byte(clearComponent3HexString);
            byte[] clearKeyBytes = ISOUtil.xor(ISOUtil.xor(clearComponent1, clearComponent2),
                    clearComponent3);
            Key clearKey;
            clearKey = jceHandler.formDESKey(keyLength, clearKeyBytes);
            secureDESKey = encryptToLMK(keyLength, keyType, clearKey);
        } catch (SMException e) {
            throw  e;
        } finally {
        }
        return  secureDESKey;
    }

    /**
     * Calculates a key check value over a clear key
     * @param key
     * @return the key check value
     * @exception SMException
     */
    byte[] calculateKeyCheckValue (Key key) throws SMException {
        byte[] encryptedZeroBlock = jceHandler.encryptData(zeroBlock, key);
        byte[] keyCheckValue = ISOUtil.trim(encryptedZeroBlock, 3);
        return  keyCheckValue;
    }

    /**
     * Encrypts a clear DES Key under LMK to form a SecureKey
     * @param keyLength
     * @param keyType
     * @param clearDESKey
     * @return secureDESKey
     * @throws SMException
     */
    public SecureDESKey encryptToLMK (short keyLength, String keyType, Key clearDESKey) throws SMException {
        SecureDESKey secureDESKey;
        byte[] encryptedKeyDataArray = jceHandler.encryptDESKey(keyLength, clearDESKey,
                getLMK(keyType));
        secureDESKey = new SecureDESKey(keyLength, keyType, encryptedKeyDataArray,
                calculateKeyCheckValue(clearDESKey));
        return  secureDESKey;
    }

    /**
     * Decrypts a secure DES key from encryption under LMK
     * @param secureDESKey (Key under LMK)
     * @return clear key
     * @throws SMException
     */
    public Key decryptFromLMK (SecureDESKey secureDESKey) throws SMException {
        Key key;
        byte[] keyBytes = secureDESKey.getBKeyBytes();
        short keyLength = secureDESKey.getKeyLength();
        String keyType = secureDESKey.getKeyType();
        key = jceHandler.decryptDESKey(keyLength, keyBytes, getLMK(keyType), false);
        return  key;
    }
    
    /**
     * Calculates the clear PIN Block
     * @param pin as entered by the card holder on the PIN entry device
     * @param pinBlockFormat
     * @param accountNumber (the 12 right-most digits of the account number excluding the check digit)
     * @return The clear PIN Block
     * @throws SMException
     *
     */
    private byte[] calculatePINBlock (String pin, byte pinBlockFormat, String accountNumber) throws SMException {
        byte[] pinBlock;
        if (pin.length() > MAX_PIN_LENGTH)
            throw  new SMException("Invalid PIN length: " + pin.length());
        if (accountNumber.length() != 12)
            throw  new SMException("Invalid Account Number: " + accountNumber + ". The length of the account number must be 12 (the 12 right-most digits of the account number excluding the check digit)");
        switch (pinBlockFormat) {
            case FORMAT00: // same as FORMAT01
            case FORMAT01:
                {
                    // Block 1
                    String block1;
                    byte[] block1ByteArray;

                    block1 = ISOUtil.hexString(new byte[] {(byte) pin.length()});
                    for(int i=0; i<14; i++) {
                        if(i<pin.length())
                            block1 += pin.charAt(i);
                        else
                            block1 += 'F';
                    }

                    block1ByteArray = ISOUtil.hex2byte(block1);
                    // Block 2
                    String block2;
                    byte[] block2ByteArray;
                    block2 = "0000" + accountNumber;
                    block2ByteArray = ISOUtil.hex2byte(block2);
                    // pinBlock
                    pinBlock = ISOUtil.xor(block1ByteArray, block2ByteArray);
                }
                break;
            case FORMAT03: 
                {
                    if(pin.length() < 4 || pin.length() > 12) 
                        throw new SMException("Unsupported PIN Length: " + 
                                pin.length());
                    pinBlock = ISOUtil.hex2byte (
                        pin + "FFFFFFFFFFFFFFFF".substring(pin.length(),16)
                    );
                }
                break;
            default:
                throw  new SMException("Unsupported PIN format: " + pinBlockFormat);
        }
        return  pinBlock;
    }

    /**
     * Calculates the clear pin (as entered by card holder on the pin entry device)
     * givin the clear PIN block
     * @param pinBlock clear PIN Block
     * @param pinBlockFormat
     * @param accountNumber
     * @return the pin
     * @throws SMException
     */
    private String calculatePIN (byte[] pinBlock, byte pinBlockFormat, String accountNumber) throws SMException {
        String pin;
        int pinLength;
        if (accountNumber.length() != 12)
            throw  new SMException("Invalid Account Number: " + accountNumber + ". The length of the account number must be 12 (the 12 right-most digits of the account number excluding the check digit)");
        switch (pinBlockFormat) {
            case FORMAT00: // same as format 01
            case FORMAT01:
                {
                    // Block 2
                    String block2;
                    block2 = "0000" + accountNumber;
                    byte[] block2ByteArray = ISOUtil.hex2byte(block2);
                    // get Block1
                    byte[] block1ByteArray = ISOUtil.xor(pinBlock, block2ByteArray);
                    pinLength = Math.abs (block1ByteArray[0]);
                    if (pinLength > MAX_PIN_LENGTH)
                        throw  new SMException("PIN Block Error");
                    // get pin
                    String pinBlockHexString = ISOUtil.hexString(block1ByteArray);
                    pin = pinBlockHexString.substring(2, pinLength
                            + 2);
                    String pad = pinBlockHexString.substring(pinLength + 2);
                    pad = pad.toUpperCase();
                    int i = pad.length();
                    while (--i >= 0)
                        if (pad.charAt(i) != 'F')
                            throw new SMException("PIN Block Error");
                }
                break;
            case FORMAT03: 
                {
                    String block1 = ISOUtil.hexString(pinBlock);
                    int len = block1.indexOf('F');
                    if(len == -1) len = 12;
                    int i = block1.length();
                    pin = block1.substring(0, len);

                    while(--i >= len) 
                        if(block1.charAt(i) != 'F') 
                            throw new SMException("PIN Block Error");
                    while(--i >= 0) 
                        if(pin.charAt(i) >= 'A') 
                            throw new SMException("PIN Block Error");

                    if(pin.length() < 4 || pin.length() > 12) 
                        throw new SMException("Unsupported PIN Length: " + 
                                pin.length());
                }
                break;
            default:
                throw  new SMException("Unsupported PIN Block format: " + pinBlockFormat);
        }
        return  pin;
    }

    /**
     * Initializes the JCE Security Module
     * @param jceProviderClassName
     * @param lmkFilePath Local Master Keys File used by JCE Security Module to store the LMKs
     * @param lmkRebuild if set to true, the lmkFile gets overwritten with newly generated keys (WARNING: this would render all your previously stored SecureKeys unusable)
     * @param lmkPassword
     * @throws SMException
     */
    @SuppressWarnings("unchecked")
	private void init(String jceProviderClassName, String lmkFilePath, boolean lmkRebuild, char[] lmkPassword) throws SMException {
        InputStream lmkInputStream = getClass().getResourceAsStream("/config/LMK.jceks");
        try {
            keyTypeToLMKIndex = new Hashtable();
            keyTypeToLMKIndex.put(SMAdapter.TYPE_ZMK, 0);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_ZPK, 1);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_PVK, 2);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_TPK, 2);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_TMK, 2);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_TAK, 3);
            keyTypeToLMKIndex.put(PINLMKIndex, 4);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_CVK, 5);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_ZAK, 8);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_BDK, 9);
            keyTypeToLMKIndex.put(SMAdapter.TYPE_TDK, 6);
            
            keyTypeToLMKIndex.put(SMAdapter.TYPE_PRIVATE_KEY, 15);
            
            Provider provider = null;
            try {
                if ((jceProviderClassName == null) || (jceProviderClassName.compareTo("")
                        == 0)) {
                    jceProviderClassName = "com.sun.crypto.provider.SunJCE";
                }
                provider = (Provider)Class.forName(jceProviderClassName).newInstance();
                Security.addProvider(provider);
            } catch (Exception e) {
                throw  new SMException("Unable to load jce provider whose class name is: "
                        + jceProviderClassName);
            } finally {
            }
            jceHandler = new JCEHandler(provider);
            if (lmkRebuild) {
                // Creat new LMK file
                // Generate New random Local Master Keys
                generateLMK();
                // Write the new Local Master Keys to file
                writeLMK(lmkInputStream, lmkPassword);
            }
            readLMK(lmkInputStream, lmkPassword);
            /*if (!lmkInputStream.exists()) {
                System.err.println("heeeeey LMK does not exist " + lmk.getName() + lmk.getAbsolutePath());
                // LMK File does not exist
                throw  new SMException("Error loading Local Master Keys, file: \""
                        + lmk.getCanonicalPath() + "\" does not exist." + " Please specify a valid LMK file, or rebuild a new one.");
            }
            else {
                // Read LMK from file
                readLMK(lmkInputStream, lmkPassword);
            }*/
        } catch (Exception e) {
            if (e instanceof SMException) {
                throw  (SMException)e;
            }
            else {
                throw  new SMException(e);
            }
        }
    }

    /**
     * Generates new LMK keys
     * @exception SMException
     */
    private void generateLMK () throws SMException {
        LMK = new SecretKey[0x0f];
        try {
            LMK[0x00] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x01] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x02] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x03] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x04] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x05] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x06] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x07] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x08] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x09] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x0a] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x0b] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x0c] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x0d] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
            LMK[0x0e] = (SecretKey)jceHandler.generateDESKey(LMK_KEY_LENGTH);
        } catch (SMException e) {
            throw  new SMException("Can't generate Local Master Keys", e);
        }
    }

    /**
     * reads (loads) LMK's from lmkFile
     * @param lmkInputStream
     * @param password
     * @exception SMException
     */
    private void readLMK(InputStream lmkInputStream, char[] password) throws SMException {
        LMK = new Key[0x0f+1];
        try {
//            FileInputStream in = new FileInputStream(lmkFile);

//            Properties lmkProps = new Properties();
//            lmkProps.load(in);
            KeyStore keyStore = jceHandler.openKeyStore();
            keyStore.load(lmkInputStream, password);

            lmkInputStream.close();

            for (int i = 0x00; i < 0x0f; i++) {
//                byte[] lmkData = ISOUtil.hex2byte(lmkProps.getProperty("LMK0x0" +
//                        Integer.toHexString(i)));
//                 provider-independent method
//                LMK[i] = new SecretKeySpec(lmkData, JCEHandler.ALG_TRIPLE_DES);
                LMK[i] = keyStore.getKey("LMK0x0" +
                        Integer.toHexString(i), password);
            }
            

            LMK[(Integer)keyTypeToLMKIndex.get(SMAdapter.TYPE_PRIVATE_KEY)] = keyStore.getKey("private-key", password);
            
            
        } catch (Exception e) {
            throw  new SMException("Can't read Local Master Keys from file: " +
                    lmkInputStream, e);
        }
    }


    /**
     * Writes a newly generated LMK's to lmkFile
     * @param lmkInputStream
     * @param password
     * @exception SMException
     */
    private void writeLMK(InputStream lmkInputStream, char[] password) throws SMException {
 /*       try {
//            Properties lmkProps = new Properties();
            KeyStore keyStore = jceHandler.openKeyStore();
            keyStore.load(null);

            for (int i = 0x00; i < 0x0f; i++) {
//                lmkProps.setProperty("LMK0x0" + Integer.toHexString(i), ISOUtil.hexString(LMK[i].getEncoded()));
                KeyStore.SecretKeyEntry keyEntry = new KeyStore.SecretKeyEntry((SecretKey)LMK[i]);
                KeyStore.PasswordProtection protection = new KeyStore.PasswordProtection(password);
                keyStore.setEntry("LMK0x0" + Integer.toHexString(i), keyEntry, protection);
            }

            FileOutputStream out = new FileOutputStream(lmkInputStream);

//            lmkProps.store(out, "Local Master Keys");
            keyStore.store(out, password);

            out.close();
        } catch (Exception e) {
            throw  new SMException("Can't write Local Master Keys to file: " + lmkInputStream,
                    e);
        }*/
    }

    /**
     * gets the suitable LMK for the key type
     * @param keyType
     * @return the LMK secret key for the givin key type
     * @throws SMException
     */
    public Key getLMK (String keyType) throws SMException {
        //int lmkIndex = keyType;
        if (!keyTypeToLMKIndex.containsKey(keyType)) {
            throw  new SMException("Unsupported key type: " + keyType);
        }
        int lmkIndex = (Integer) keyTypeToLMKIndex.get(keyType);
        Key lmk;
        try {
            lmk = LMK[lmkIndex];
        } catch (Exception e) {
            throw  new SMException("Invalid key code: " + "LMK0x0" + Integer.toHexString(lmkIndex));
        }
        return  lmk;
    }
    /**
     * maps a key type to an LMK Index
     */
    private Hashtable keyTypeToLMKIndex;
    /**
     * The clear Local Master Keys
     */
    private Key[] LMK;
    /**
     * A name for the LMK used to encrypt the PINs
     */
    private static final String PINLMKIndex = "PIN";
    /**
     * The key length (in bits) of the Local Master Keys.
     * JCESecurityModule uses Triple DES Local Master Keys
     */
    private static final short LMK_KEY_LENGTH = LENGTH_DES3_2KEY;
    /**
     * The maximum length of the PIN
     */
    private static final short MAX_PIN_LENGTH = 12;
    /**
     * a dummy 64-bit block of zeros used when calculating the check value
     */
    private static final byte[] zeroBlock =  {
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00
    };
    public JCEHandler jceHandler;
    
	@Override
	protected byte[] generateCBC_MACImpl(byte[] data, SecureDESKey kd)
			throws SMException {
//		return jceHandler.encryptData(data, jceHandler.generateDESKey( LENGTH_DES3_2KEY ));
		return jceHandler.generateCBC_MAC(data, decryptFromLMK(kd));
	}

	/*
	public Key formDESKey(short keyLength, byte[] clearKeyBytes)
			throws SMException {
		return jceHandler.formDESKey(keyLength, clearKeyBytes);
	}
*/
	@Override
	protected byte[] decryptImpl(byte[] inputBlock, SecureDESKey fromKey) throws SMException {
    	return jceHandler.decryptData(inputBlock, decryptFromLMK(fromKey));
    }

	@Override
	protected byte[] encryptImpl(byte[] input, SecureDESKey toKey, String padding) throws SMException {
    	return jceHandler.encryptData(input, decryptFromLMK(toKey), padding);
    }
	
	@Override
	public byte[] desDecrypt(byte[] inputBlock, byte[] desKey) throws SMException {
		return jceHandler.decryptData(inputBlock, new SecretKeySpec(desKey, JCEHandler.ALG_DES));
	}
	
	@Override
	public byte[] desEncrypt(byte[] input, byte[] desKey) throws SMException {
		return jceHandler.encryptData(input, new SecretKeySpec(desKey, JCEHandler.ALG_DES));
	}

	@Override
	protected byte[] rsaDecryptImpl(byte[] cipherData) throws SMException {
    	return jceHandler.rsaDecryptData(cipherData, getLMK(TYPE_PRIVATE_KEY));
    }
}



