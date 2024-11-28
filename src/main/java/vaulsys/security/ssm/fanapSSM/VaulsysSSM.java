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

package vaulsys.security.ssm.fanapSSM;

import vaulsys.config.ConfigurationManager;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.network.mina2.Mina2Acceptor;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.security.exception.SMException;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.ssm.DefaultSSMHandler;
import vaulsys.security.ssm.base.BaseSMAdapter;
import vaulsys.security.ssm.base.EncryptedPIN;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.configuration.Configuration;

/**
 * SecurityModule is an implementation of a security module in software. It
 * doesn't require any hardware device to work.<br>
 * JCESecurityModule also implements the SMAdapter, so you can view it: either
 * as a self contained security module adapter that doesn't need a security
 * module or a security module that plugs directly to jpos, so doesn't need a
 * separate adapter.<br>
 */
public class VaulsysSSM extends BaseSMAdapter {
    // maps a key type to an LMK Index
    private Hashtable keyTypeToLMKIndex;
    // The clear Local MASTER Keys
    private SecretKey[] LMK;
    // A name for the LMK used to encrypt the PINs
    private static final String PINLMKIndex = "PVK";                                                /* "PIN"; */
    // The key length (in bits) of the Local MASTER Keys. JCESecurityModule uses
    // Triple DES Local MASTER Keys
    private static final short LMK_KEY_LENGTH = LENGTH_DES3_2KEY;
    // The maximum length of the PIN
    private static final short MAX_PIN_LENGTH = 12;
    // a dummy 64-bit block of zeros used when calculating the check value
    private static final byte[] zeroBlock = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    private final String Sun_Provider = "com.sun.crypto.provider.SunJCE";
    private final String BouncyCatle_Provider = "org.bouncycastle.jce.provider.BouncyCastleProvider";

    NetworkService networkService;

    class NetworkService {
        List<DefaultSSMHandler> ssmHandlers;
        private final ExecutorService pool;
        int poolSize;

        public NetworkService(int poolSize, InputChannel ssmChannel) throws IOException {
            this.poolSize = poolSize;
            ssmHandlers = new ArrayList<DefaultSSMHandler>();
            for (int i = 0; i < poolSize; i++) {
                DefaultSSMHandler handler = new DefaultSSMHandler();
                ssmHandlers.add(handler);
                Mina2Acceptor nc = new Mina2Acceptor(ssmChannel, null, handler);
                nc.listen();
                // handler.waitForSession();
            }

            pool = Executors.newCachedThreadPool();
        }

        public void run() { // run the service
            for (int i = 0; i < poolSize; i++) {
                pool.execute(new Handler(ssmHandlers.get(i)));
            }
            pool.shutdown();
        }
    }

    class Handler implements Runnable {
        private final DefaultSSMHandler handler;

        Handler(DefaultSSMHandler handler) {
            this.handler = handler;
        }

        public void run() {
            byte[] message = handler.getMessage();
            logger.debug("The Recieved message = " + message);
            handler.sendMessage("I am your response...".getBytes());
        }
    }

    private void initSSMConnection() {
        InputChannel ssmChannel = null;
        try {
            ssmChannel = new InputChannel("192.168.1.182", 2000, "FanapSSMChannel", "", "", "", "",
                    CommunicationMethod.SAME_SOCKET, "1", "", false, false, -1, "", 1, false, 0, false, 0, "", "",null); //Raza Channel TIMEOUT
        } /*catch (InstantiationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }*/
        catch (Exception e) {
			// TODO: handle exception
		}
        // BusSvc.getInstance().getBusElement("Bus.Configuration.ChannelList").addBusElement(ssmChannel.toXML());
        try {
            this.networkService = new NetworkService(VaulsysSSMDriver.MAX_NUMBER_OF_CONNECTION, ssmChannel);
            networkService.run();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    SecurityHandler securityHandler;

    private static VaulsysSSM fanapSSM;

    public static VaulsysSSM getInstance() {
        // if (fanapSSM == null) {
        try {
            fanapSSM = new VaulsysSSM();
        } catch (SMException e) {
            e.printStackTrace();
            // }
        }

        return fanapSSM;
    }

    public VaulsysSSM() throws SMException {
        securityHandler = new SecurityHandler(BouncyCatle_Provider);
        // TODO I(Noroozi) comment this line (1386/12/12)
        // initSSMConnection();
    }

    public SecureDESKey generateKeyImpl(short keyLength, String keyType) throws SMException {
        SecureDESKey generatedSecureKey = null;

        // logger.debug("\n\t It is going to generate key with length "+
        // keyLength);
        Key generatedClearKey = securityHandler.generateDESKey(keyLength);

        // logger.debug("\n\t The generated Key of "+
        // generatedClearKey.getAlgorithm()+ "is "+ generatedClearKey);

        generatedSecureKey = encryptToLMK(keyLength, keyType, generatedClearKey);
        return generatedSecureKey;
    }

    public SecureDESKey importKeyImpl(short keyLength, String keyType, byte[] encryptedKey, SecureDESKey kek,
                                      boolean checkParity) throws SMException {
        SecureDESKey importedKey = null;
        // decrypt encrypted key
        // logger.debug("\n\n\t decryptFromLMK(kek) = "+ decryptFromLMK(kek));

        Key clearKEY = securityHandler.decryptDESKey(keyLength, encryptedKey, decryptFromLMK(kek), checkParity);

        // intermediate
        // logger.debug("The intermediate Key is "+ clearKEY);
        // Encrypt Key under LMK
        importedKey = encryptToLMK(keyLength, keyType, clearKEY);
        return importedKey;
    }

    public byte[] exportKeyImpl(SecureDESKey key, SecureDESKey kek) throws SMException {
        byte[] exportedKey = null;
        // get key in clear
        Key clearKey = decryptFromLMK(key);
        exportedKey = securityHandler.encryptDESKey(key.getKeyLength(), clearKey, decryptFromLMK(kek));

        // logger.debug("\n\n\n\t" +
        // " First key = "+ key.getKeyBytes()+ "\n\t exported Key = "
        // + exportedKey +
        // " \n\t importedKey = "+ importKey(key.getKeyLength(),
        // kek.getKeyType(), exportedKey, kek, true).getKeyBytes() +"\n\n");
        return exportedKey;
    }

    public EncryptedPIN encryptPINImpl(String pin, String accountNumber) throws SMException {
        EncryptedPIN encryptedPIN = null;
        byte[] clearPINBlock = calculatePINBlock(pin, FORMAT00, accountNumber);
        // Encrypt
        byte[] translatedPINBlock = securityHandler.encryptData(clearPINBlock, getLMK(PINLMKIndex));
        encryptedPIN = new EncryptedPIN(translatedPINBlock, FORMAT00, accountNumber);
        return encryptedPIN;
    }

    public String decryptPINImpl(EncryptedPIN pinUnderLmk) throws SMException {
        String pin = null;
        byte[] clearPINBlock = securityHandler.decryptData(pinUnderLmk.getPINBlock(), getLMK(PINLMKIndex));
        pin = calculatePIN(clearPINBlock, pinUnderLmk.getPINBlockFormat(), pinUnderLmk.getAccountNumber());
        return pin;
    }

    public EncryptedPIN importPINImpl(EncryptedPIN pinUnderKd1, SecureDESKey kd1) throws SMException {
        EncryptedPIN pinUnderLmk = null;
        // read inputs
        String accountNumber = pinUnderKd1.getAccountNumber();
        // Use FORMAT00 for encrypting PIN under LMK
        byte destinationPINBlockFormat = FORMAT00;
        // get clear PIN

        byte[] clearPINBlock = securityHandler.decryptData(pinUnderKd1.getPINBlock(), decryptFromLMK(kd1));
        // extract clear pin (as entered by card holder)
        String pin = calculatePIN(clearPINBlock, pinUnderKd1.getPINBlockFormat(), accountNumber);
        // Format PIN Block using proprietary FORMAT00 to be encrypetd under LMK
        clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, accountNumber);
        // encrypt PIN
        byte[] translatedPINBlock = securityHandler.encryptData(clearPINBlock, getLMK(PINLMKIndex));
        pinUnderLmk = new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat, accountNumber);
        return pinUnderLmk;
    }

    public EncryptedPIN exportPINImpl(EncryptedPIN pinUnderLmk, SecureDESKey kd2, byte destinationPINBlockFormat)
            throws SMException {
        EncryptedPIN exportedPIN = null;

        String accountNumber = pinUnderLmk.getAccountNumber();
        // process
        // get clear PIN
        byte[] clearPINBlock = securityHandler.decryptData(pinUnderLmk.getPINBlock(), getLMK(PINLMKIndex));
        // extract clear pin
        String pin = calculatePIN(clearPINBlock, pinUnderLmk.getPINBlockFormat(), accountNumber);
        clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, accountNumber);
        // encrypt PIN
        byte[] translatedPINBlock = securityHandler.encryptData(clearPINBlock, decryptFromLMK(kd2));
        exportedPIN = new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat, accountNumber);
        return exportedPIN;
    }

    public EncryptedPIN translatePINImpl(EncryptedPIN pinUnderKd1, SecureDESKey kd1, SecureDESKey kd2,
                                         byte destinationPINBlockFormat) throws SMException {
        EncryptedPIN translatedPIN = null;
        String accountNumber = pinUnderKd1.getAccountNumber();
        // get clear PIN
        byte[] clearPINBlock = securityHandler.decryptData(pinUnderKd1.getPINBlock(), decryptFromLMK(kd1));
        String pin = calculatePIN(clearPINBlock, pinUnderKd1.getPINBlockFormat(), accountNumber);
        // Reformat PIN Block
        clearPINBlock = calculatePINBlock(pin, destinationPINBlockFormat, accountNumber);
        // encrypt PIN
        byte[] translatedPINBlock = securityHandler.encryptData(clearPINBlock, decryptFromLMK(kd2));
        translatedPIN = new EncryptedPIN(translatedPINBlock, destinationPINBlockFormat, accountNumber);
        return translatedPIN;
    }

    /**
     * Generates key check value.<br>
     * Though not confidential, it is used only by Console, that's why it is
     * package protected.
     *
     * @param keyLength
     * @param keyType
     * @param KEYunderLMKHexString
     * @return SecureDESKey object with its check value set
     * @throws SMException
     */
    SecureDESKey generateKeyCheckValue(short keyLength, String keyType, String KEYunderLMKHexString) throws SMException {
        SecureDESKey secureDESKey = null;
        byte[] keyCheckValue;
        try {
            secureDESKey = new SecureDESKey(keyLength, keyType, KEYunderLMKHexString, "");
            keyCheckValue = calculateKeyCheckValue(decryptFromLMK(secureDESKey));
            secureDESKey.setBKeyCheckValue(keyCheckValue);
        } catch (SMException e) {
            throw e;
        } finally {
        }
        return secureDESKey;
    }

    /**
     * Calculates a key check value over a clear key
     *
     * @param key
     * @return the key check value
     * @throws SMException
     */
    byte[] calculateKeyCheckValue(Key key) throws SMException {
        byte[] encryptedZeroBlock = securityHandler.encryptData(zeroBlock, key);
        byte[] keyCheckValue = ISOUtil.trim(encryptedZeroBlock, 3);
        return keyCheckValue;
    }

    /**
     * Encrypts a clear DES Key under LMK to form a SecureKey
     *
     * @param keyLength
     * @param keyType
     * @param clearDESKey
     * @return secureDESKey
     * @throws SMException
     */
    private SecureDESKey encryptToLMK(short keyLength, String keyType, Key clearDESKey) throws SMException {
        SecureDESKey secureDESKey = null;
        byte[] encryptedKeyDataArray = securityHandler.encryptDESKey(keyLength, clearDESKey, getLMK(keyType));
        // logger.debug("\n\t The encrypted key is "+ encryptedKeyDataArray);
        secureDESKey = new SecureDESKey(keyLength, keyType, encryptedKeyDataArray, calculateKeyCheckValue(clearDESKey));
        return secureDESKey;
    }

    /**
     * Decrypts a secure DES key from encryption under LMK
     *
     * @param secureDESKey (Key under LMK)
     * @return clear key
     * @throws SMException
     */
    private Key decryptFromLMK(SecureDESKey secureDESKey) throws SMException {
        Key key = null;
        // logger.debug("\n\t It's going to decrypt the key "+
        // secureDESKey.getKeyBytes());
        byte[] keyBytes = secureDESKey.getBKeyBytes();
        short keyLength = secureDESKey.getKeyLength();
        String keyType = secureDESKey.getKeyType();
        key = securityHandler.decryptDESKey(keyLength, keyBytes, getLMK(keyType), true);
        return key;
    }

    /**
     * Calculates the clear PIN Block
     *
     * @param pin            as entered by the card holder on the PIN entry device
     * @param pinBlockFormat
     * @param accountNumber  (the 12 right-most digits of the account number excluding the
     *                       check digit)
     * @return The clear PIN Block
     * @throws SMException
     */
    private byte[] calculatePINBlock(String pin, byte pinBlockFormat, String accountNumber) throws SMException {
        byte[] pinBlock = null;
        if (pin.length() > MAX_PIN_LENGTH)
            throw new SMException("Invalid PIN length: " + pin.length());
        if (accountNumber.length() != 12)
            throw new SMException(
                    "Invalid Account Number: "
                            + accountNumber
                            + ". The length of the account number must be 12 (the 12 right-most digits of the account number excluding the check digit)");
        switch (pinBlockFormat) {
            case FORMAT00: // same as FORMAT01
            case FORMAT01: {
                // Block 1
                String block1 = null;
                byte[] block1ByteArray;
                switch (pin.length()) {
                    // pin length then pad with 'F'
                    case 4:
                        block1 = "04" + pin + "FFFFFFFFFF";
                        break;
                    case 5:
                        block1 = "05" + pin + "FFFFFFFFF";
                        break;
                    case 6:
                        block1 = "06" + pin + "FFFFFFFF";
                        break;
                    case 7:
                        block1 = "07" + pin + "FFFFFFF";
                        break;
                    case 8:
                        block1 = "08" + pin + "FFFFFF";
                        break;
                    default:
                        throw new SMException("Unsupported PIN Length: " + pin.length());
                }
                block1ByteArray = ISOUtil.hex2byte(block1);
                // Block 2
                String block2;
                byte[] block2ByteArray = null;
                block2 = "0000" + accountNumber;
                block2ByteArray = ISOUtil.hex2byte(block2);
                // pinBlock
                pinBlock = ISOUtil.xor(block1ByteArray, block2ByteArray);
            }
            ;
            break;
            case FORMAT03: {
                if (pin.length() < 4 || pin.length() > 12)
                    throw new SMException("Unsupported PIN Length: " + pin.length());
                pinBlock = ISOUtil.hex2byte(pin + "FFFFFFFFFFFFFFFF".substring(pin.length(), 16));
            }
            break;
            default:
                throw new SMException("Unsupported PIN format: " + pinBlockFormat);
        }
        return pinBlock;
    }

    /**
     * Calculates the clear pin (as entered by card holder on the pin entry
     * device) givin the clear PIN block
     *
     * @param pinBlock       clear PIN Block
     * @param pinBlockFormat
     * @param accountNumber
     * @return the pin
     * @throws SMException
     */
    private String calculatePIN(byte[] pinBlock, byte pinBlockFormat, String accountNumber) throws SMException {
        String pin = null;
        int pinLength;
        if (accountNumber.length() != 12)
            throw new SMException(
                    "Invalid Account Number: "
                            + accountNumber
                            + ". The length of the account number must be 12 (the 12 right-most digits of the account number excluding the check digit)");
        switch (pinBlockFormat) {
            case FORMAT00: // same as format 01
            case FORMAT01: {
                // Block 2
                String block2;
                block2 = "0000" + accountNumber;
                byte[] block2ByteArray = ISOUtil.hex2byte(block2);
                // get Block1
                byte[] block1ByteArray = ISOUtil.xor(pinBlock, block2ByteArray);
                pinLength = Math.abs(block1ByteArray[0]);
                if (pinLength > MAX_PIN_LENGTH)
                    throw new SMException("PIN Block Error");
                // get pin
                String pinBlockHexString = ISOUtil.hexString(block1ByteArray);
                pin = pinBlockHexString.substring(2, pinLength + 2);
                String pad = pinBlockHexString.substring(pinLength + 2);
                pad = pad.toUpperCase();
                int i = pad.length();
                while (--i >= 0)
                    if (pad.charAt(i) != 'F')
                        throw new SMException("PIN Block Error");
            }
            break;
            case FORMAT03: {
                String block1 = ISOUtil.hexString(pinBlock);
                int len = block1.indexOf('F');
                if (len == -1)
                    len = 12;
                int i = block1.length();
                pin = block1.substring(0, len);

                while (--i >= len)
                    if (block1.charAt(i) != 'F')
                        throw new SMException("PIN Block Error");
                while (--i >= 0)
                    if (pin.charAt(i) >= 'A')
                        throw new SMException("PIN Block Error");

                if (pin.length() < 4 || pin.length() > 12)
                    throw new SMException("Unsupported PIN Length: " + pin.length());
            }
            break;
            default:
                throw new SMException("Unsupported PIN Block format: " + pinBlockFormat);
        }
        return pin;
    }

    /**
     * gets the suitable LMK for the key type
     *
     * @param keyType
     * @return the LMK secret key for the givin key type
     * @throws SMException
     */
    private SecretKey getLMK(String keyType) throws SMException {
        Configuration lmkConfig = ConfigurationManager.getInstance().getConfiguration("LMK");
        String queryString = "LMKS/LMK[@name = '" + keyType + "']";
        if (lmkConfig.containsKey(queryString + "/value")) {
            String keyString = lmkConfig.getString(queryString + "/value");
            Integer keyLength = lmkConfig.getInt(queryString + "/length");
            // logger.debug("\n\tThe LMK of TYPE "+ keyType + " with Value = "+
            // keyString+ " was retrived");
            return new SecretKeySpec(ISOUtil.hex2byte(keyString), securityHandler.ALG_TRIPLE_DES);
        } else
            throw new SMException("Unsupported key type: " + keyType);
    }

    @Override
    protected byte[] generateCBC_MACImpl(byte[] data, SecureDESKey kd) throws SMException {
        return securityHandler.generateMAC(data, decryptFromLMK(kd));

    }

    public boolean verifyMAC(byte[] data, SecureDESKey kd) throws SMException {
        return securityHandler.verifyMAC(data, decryptFromLMK(kd));
    }
}
