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
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.security.ssm.base.Util;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.params.KeyParameter;


/**
 * <p>
 * Provides some higher level methods that are needed by the JCE
 * Security Module, yet they are generic and can be used elsewhere.
 * </p>
 * <p>
 * It depends on the Java<font size=-1><sup>TM</sup></font> Cryptography Extension (JCE).
 * </p>
 * @author Hani S. Kirollos
 * @version $Revision: 1.1 $ $Date: 2007/02/27 12:46:25 $
 */
@SuppressWarnings({"JavaDoc"})
public class JCEHandler {
    static final String ALG_DES = "DES";
    static final String ALG_TRIPLE_DES = "DESede";
    static final String ALG_RSA = "RSA";
    
    /**
     * The JCE provider
     */
    Provider provider = null;
    String desMode = "ECB";
    String desPadding = "NoPadding";
//    String desPadding = "ZeroBytePadding";
    
    String rsaMode = "NONE";
    String rsaPadding = "NoPadding";

    /**
     * Registers the JCE provider whose name is providerName and sets it to be the
     * only provider to be used in this instance of the JCEHandler class.
     * @param jceProviderClassName Name of the JCE provider
     * (e.g. "com.sun.crypto.provider.SunJCE" for Sun's implementation,
     * or "org.bouncycastle.jce.provider.BouncyCastleProvider" for bouncycastle.org
     * implementation)
     * @throws SMException
     */
    public JCEHandler (String jceProviderClassName) throws SMException
    {
        try {
            provider = (Provider)Class.forName(jceProviderClassName).newInstance();
            Security.addProvider(provider);
        } catch (Exception e) {
            throw  new SMException(e);
        }
    }

    /**
     * Uses the JCE provider specified
     * @param provider
     */
    public JCEHandler (Provider provider) {
        this.provider = provider;
    }

    /**
     * Generates a clear DES (DESede) key
     * @param keyLength the bit length (key size) of the generated key (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @return generated clear DES (or DESede) key
     * @exception SMException
     */
    public Key generateDESKey (short keyLength) throws SMException {
        Key generatedClearKey;
        try {
            KeyGenerator k1;
            if (keyLength > SMAdapter.LENGTH_DES) {
                k1 = KeyGenerator.getInstance(ALG_TRIPLE_DES, provider.getName());
            }
            else {
                k1 = KeyGenerator.getInstance(ALG_DES, provider.getName());
            }
            generatedClearKey = k1.generateKey();
            /* These 3 steps not only enforce correct parity, but also enforces
             that when keyLength=128, the third key of the triple DES key is equal
             to the first key. This is needed because, JCE doesn't differenciate
             between Triple DES with 2 keys and Triple DES with 3 keys
             */
            byte[] clearKeyBytes = extractDESKeyMaterial(keyLength, generatedClearKey);
            Util.adjustDESParity(clearKeyBytes);
            generatedClearKey = formDESKey(keyLength, clearKeyBytes);
            
        } catch (Exception e) {
            if (e instanceof SMException)
                throw  (SMException)e;
            else
                throw  new SMException(e);
        }
        return  generatedClearKey;
    }

    /**
     * Encrypts (wraps) a clear DES Key, it also sets odd parity before encryption
     * @param keyLength bit length (key size) of the clear DES key (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @param clearDESKey DES/Triple-DES key whose format is "RAW"
     * (for a DESede with 2 Keys, keyLength = 128 bits, while DESede key with 3 keys keyLength = 192 bits)
     * @param encryptingKey can be a key of any type (RSA, DES, DESede...)
     * @return encrypted DES key
     * @throws SMException
     */
    public byte[] encryptDESKey (short keyLength, Key clearDESKey, Key encryptingKey) throws SMException {
        byte[] encryptedDESKey;
        byte[] clearKeyBytes = extractDESKeyMaterial(keyLength, clearDESKey);
        // enforce correct (odd) parity before encrypting the key
        Util.adjustDESParity(clearKeyBytes);
        encryptedDESKey = doCryptStuff(clearKeyBytes, encryptingKey, Cipher.ENCRYPT_MODE);
        return  encryptedDESKey;
    }

    /**
     * Extracts the DES/DESede key material
     * @param keyLength bit length (key size) of the DES key. (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @param clearDESKey DES/Triple-DES key whose format is "RAW"
     * @return encoded key material
     * @throws SMException
     */
    protected byte[] extractDESKeyMaterial (short keyLength, Key clearDESKey) throws SMException {
        byte[] clearKeyBytes;
        String keyAlg = clearDESKey.getAlgorithm();
        String keyFormat = clearDESKey.getFormat();
        if (keyFormat.compareTo("RAW") != 0) {
            throw  new SMException("Unsupported DES key encoding format: "
                    + keyFormat);
        }
        if (!keyAlg.startsWith(ALG_DES)) {
            throw  new SMException("Unsupported key algorithm: " + keyAlg);
        }
        clearKeyBytes = clearDESKey.getEncoded();
        clearKeyBytes = ISOUtil.trim(clearKeyBytes, getBytesLength(keyLength));
        return  clearKeyBytes;
    }

    /**
     * Decrypts an encrypted DES/Triple-DES key
     * @param keyLength bit length (key size) of the DES key to be decrypted. (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @param encryptedDESKey the byte[] representing the encrypted key
     * @param encryptingKey can be of any algorithm (RSA, DES, DESede...), if null, encryptedKey is assumed to be plaintext
     * @param checkParity if true, the parity of the key is checked
     * @return clear DES (DESede) Key
     * @throws SMException if checkParity==true and the key does not have correct parity
     */
    public Key decryptDESKey (short keyLength, byte[] encryptedDESKey, Key encryptingKey,
            boolean checkParity) throws SMException {
        Key key;
        byte[] clearKeyBytes = encryptingKey == null ? encryptedDESKey : doCryptStuff(encryptedDESKey, encryptingKey, Cipher.DECRYPT_MODE);
        if (checkParity) {
            if (!Util.isDESParityAdjusted(clearKeyBytes)) {
                throw new SMException("Parity not adjusted");
            }
        }
        key = formDESKey(keyLength, clearKeyBytes);
        return  key;
    }

    /**
     * Forms the clear DES key given its "RAW" encoded bytes
     * Does the inverse of extractDESKeyMaterial
     * @param keyLength bit length (key size) of the DES key. (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @param clearKeyBytes the RAW DES/Triple-DES key
     * @return clear key
     * @throws SMException
     */
    public Key formDESKey (short keyLength, byte[] clearKeyBytes) throws SMException {
        Key key = null;
        switch (keyLength) {
            case SMAdapter.LENGTH_DES:
                {
                    key = new SecretKeySpec(clearKeyBytes, ALG_DES);
                }
            break;
            case SMAdapter.LENGTH_DES3_2KEY:
                {
                    // make it 3 components to work with JCE
                    clearKeyBytes = ISOUtil.concat(
                        clearKeyBytes, 0, getBytesLength(SMAdapter.LENGTH_DES3_2KEY),
                        clearKeyBytes, 0, getBytesLength(SMAdapter.LENGTH_DES)
                        );
                }
            case SMAdapter.LENGTH_DES3_3KEY:
                {
                    key = new SecretKeySpec(clearKeyBytes, ALG_TRIPLE_DES);
                }
        }
        if (key == null)
            throw  new SMException("Unsupported DES key length: " + keyLength
                    + " bits");
        return  key;
    }

    /**
     * Encrypts data
     * @param data
     * @param key
     * @return encrypted data
     * @exception SMException
     */
    public byte[] encryptData (byte[] data, Key key) throws SMException {
        byte[] encryptedData;
        encryptedData = doCryptStuff(data, key, Cipher.ENCRYPT_MODE);
        return  encryptedData;
    }

    public byte[] encryptData (byte[] data, Key key, String padding) throws SMException {
        byte[] encryptedData;
        encryptedData = doCryptStuff(data, key, Cipher.ENCRYPT_MODE, padding);
        return  encryptedData;
    }

    /**
     * generate CBC MAC using a block cipher identified by the algorithm of the provided key
     * @param data
     * @param key
     * @return encrypted data
     * @exception SMException
     */
    public byte[] generateCBC_MAC (byte[] data, Key key) throws SMException {
        try {
            KeyParameter param = new KeyParameter(key.getEncoded());
            BlockCipher cipher = new DESEngine();

            org.bouncycastle.crypto.Mac mac = new CBCBlockCipherMac(cipher, 64);
//            javax.crypto.Mac mac = javax.crypto.Mac.getInstance(key.getAlgorithm(), provider);
            mac.init(param);
            mac.update(data, 0, data.length);
            byte[] out = new byte[8];
            mac.doFinal(out, 0);
            return out;
        } catch (Exception e) {
            throw new SMException(e);
        }
    }

    /**
     * Decrypts data
     * @param encryptedData
     * @param key
     * @return clear data
     * @exception SMException
     */
    public byte[] decryptData (byte[] encryptedData, Key key) throws SMException {
        byte[] clearData;
        clearData = doCryptStuff(encryptedData, key, Cipher.DECRYPT_MODE);
        return  clearData;
    }

    /**
     * performs cryptographic operations (encryption/decryption) using JCE Cipher
     * @param data
     * @param key
     * @param CipherMode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * @return result of the cryptographic operations
     * @throws SMException
     */

    byte[] doCryptStuff (byte[] data, Key key, int CipherMode) throws SMException {
    	return doCryptStuff(data, key, CipherMode, desPadding);
    }

    byte[] doCryptStuff (byte[] data, Key key, int CipherMode, String padding) throws SMException {
    	byte[] result;
        String transformation;
        if (key.getAlgorithm().startsWith(ALG_DES)) {
            transformation = key.getAlgorithm() + "/" + desMode + "/" + padding;
        }
        else if (key.getAlgorithm().startsWith(ALG_RSA)) {
            transformation = key.getAlgorithm() + "/" + rsaMode + "/" + padding;
        }
        else {
            transformation = key.getAlgorithm();
        }
        try {
            Cipher c1 = Cipher.getInstance(transformation, provider.getName());
            c1.init(CipherMode, key);
            result = c1.doFinal(data);
        } catch (Exception e) {
            throw  new SMException(e);
        }
        return  result;
    }

/*
    */
/**
     * performs cryptographic operations (encryption/decryption) using JCE Cipher
     * @param data
     * @param start
     *@param end
     * @param key
     * @param CipherMode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * @param desMode @return result of the cryptographic operations
     * @throws SMException
     */
/*
    byte[] doDESCryptStuff(byte[] data, int start, int end, Key key, int CipherMode, String desMode) throws SMException {
        byte[] result;
        String transformation;
        if (key.getAlgorithm().startsWith(ALG_DES)) {
            transformation = key.getAlgorithm() + "/" + desMode + "/" + desPadding;
        }
        else {
            throw new IllegalArgumentException("The provided key should be of DES or DESede type.");
        }
        try {
            Cipher c1 = Cipher.getInstance(transformation, provider.getName());
            c1.init(CipherMode, key);
            result = c1.doFinal(data, start, end);
        } catch (Exception e) {
            throw  new SMException(e);
        }
        return  result;
    }
*/

    /**
     * Calculates the length of key in bytes
     * @param keyLength bit length (key size) of the DES key. (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @return keyLength/8
     * @throws SMException if unknown key length
     */
    int getBytesLength(short keyLength) throws SMException{
        int bytesLength;
        switch (keyLength) {
            case SMAdapter.LENGTH_DES: bytesLength = 8;break;
            case SMAdapter.LENGTH_DES3_2KEY: bytesLength = 16;break;
            case SMAdapter.LENGTH_DES3_3KEY: bytesLength = 24; break;
            default: throw new SMException("Unsupported key length: " + keyLength + " bits");
        }
        return bytesLength;
    }

    public KeyStore openKeyStore() throws KeyStoreException {
        return KeyStore.getInstance("JCEKS");
    }

	public byte[] rsaDecryptData(byte[] cipherData, Key key) throws SMException{
        byte[] clearData;
        clearData = doCryptStuff(cipherData, key, Cipher.DECRYPT_MODE, rsaPadding);
//        Cipher cipher = Cipher.getInstance("RSA/NONE/NoPadding", "BC");      
//        cipher.init(Cipher.DECRYPT_MODE, priv);
//        clearData = cipher.doFinal(cipherData);        
        return  clearData;
	}
	
}



/*
    Code for CBC MAC:
        try {
            Cipher cipher = Cipher.getInstance(transformation, provider.getName());

            int blockSize = cipher.getBlockSize();
            byte[] buf = new byte[blockSize];
            byte[] mac = new byte[blockSize];

            int len = end - start;
            int bufOff = 0;
            if (len > blockSize)
            {
                System.arraycopy(data,  start, buf, bufOff, blockSize);

                cipher.update(buf, 0, blockSize, mac, 0);

                bufOff = 0;
                len -= blockSize;
                start += blockSize;

                while (len > blockSize)
                {
                    cipher.update(data, start, blockSize, mac, 0);

                    len -= blockSize;
                    start += blockSize;
                }
            }

            System.arraycopy(data, start, buf, bufOff, len);

            bufOff += len;

            //
            // pad with zeroes
            //
            while (bufOff < blockSize)
            {
                buf[bufOff] = 0;
                bufOff++;
            }

            cipher.update(buf, 0, blockSize, mac, 0);
            return  mac;
        } catch (Exception e) {
            throw  new SMException(e);
        }

*/