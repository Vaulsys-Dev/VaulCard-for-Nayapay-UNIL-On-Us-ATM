package vaulsys.security.ssm.fanapSSM;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.security.exception.SMException;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.security.ssm.base.Util;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherKeyGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.generators.DESKeyGenerator;
import org.bouncycastle.crypto.generators.DESedeKeyGenerator;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class SecurityHandler {

    private Logger logger = Logger.getLogger(this.getClass());

    static final String ALG_DES = "DES";
    static final String ALG_TRIPLE_DES = "DESede";

    Provider provider;
    String desMode = "CBC";
    String desPadding = "NoPadding";
    static byte[] ivBytes = new byte[8];
    AlgorithmParameters parameters;

    /**
     * Registers the SM provider whose name is providerName and sets it to be the only provider to be used in this instance of the JCEHandler class.
     *
     * @param smProviderClassName Name of the SM provider (e.g. "com.sun.crypto.provider.SunJCE" for Sun's implementation, or "org.bouncycastle.jce.provider.BouncyCastleProvider" for bouncycastle.org implementation)
     * @throws SMException
     */
    public SecurityHandler(String smProviderClassName) throws SMException {
        try {
            provider = (Provider) Class.forName(smProviderClassName).newInstance();
            Security.addProvider(provider);
        } catch (Exception e) {
            throw new SMException(e);
        }
    }

    /**
     * Uses the SM provider specified
     *
     * @param provider
     */
    public SecurityHandler(Provider provider) {
        this.provider = provider;
    }

    /**
     * Generates a clear DES (DESede) key
     *
     * @param keyLength the bit length (key size) of the generated key (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @return generated clear DES (or DESede) key
     * @throws SMException
     */
    public Key generateDESKey(short keyLength) throws SMException {
        byte[] clearKeyBytes;
        CipherKeyGenerator k1 = null;
        Key clearkey;
        try {
            if (keyLength > SMAdapter.LENGTH_DES) {

                k1 = new DESedeKeyGenerator();
                k1.init(new KeyGenerationParameters(new SecureRandom(), keyLength));
                clearKeyBytes = k1.generateKey();
                clearkey = new SecretKeySpec(clearKeyBytes, ALG_TRIPLE_DES);
            } else {
                k1 = new DESKeyGenerator();
                k1.init(new KeyGenerationParameters(new SecureRandom(), keyLength));
                clearKeyBytes = k1.generateKey();
                clearkey = new SecretKeySpec(clearKeyBytes, ALG_DES);
            }
        } catch (Exception e) {
            if (e instanceof SMException)
                throw (SMException) e;
            else
                throw new SMException(e);
        }
        return clearkey;
    }

    /**
     * Encrypts (wraps) a clear DES Key, it also sets odd parity before encryption
     *
     * @param keyLength     bit length (key size) of the clear DES key (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @param clearDESKey   DES/Triple-DES key whose format is "RAW" (for a DESede with 2 Keys, keyLength = 128 bits, while DESede key with 3 keys keyLength = 192 bits)
     * @param encryptingKey can be a key of any type (RSA, DES, DESede...)
     * @return encrypted DES key
     * @throws SMException
     */

    public byte[] encryptDESKey(short keyLength, Key clearDESKey, Key encryptingKey) throws SMException {

        byte[] encryptedDESKey = null;
        byte[] clearKeyBytes = extractDESKeyMaterial(keyLength, clearDESKey);
        // enforce correct (odd) parity before encrypting the key
        Util.adjustDESParity(clearKeyBytes);
        encryptedDESKey = doCryptStuff(clearKeyBytes, encryptingKey, Cipher.ENCRYPT_MODE);
        return encryptedDESKey;
    }

    /**
     * performs cryptographic operations (encryption/decryption) using JCE Cipher
     *
     * @param data
     * @param key
     * @param CipherMode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * @return result of the cryptographic operations
     * @throws SMException
     */
    byte[] doCryptStuff(byte[] data, Key key, int CipherMode) throws SMException {
        byte[] result;
        String transformation;
        if (key.getAlgorithm().startsWith(ALG_DES)) {
            transformation = key.getAlgorithm() + "/" + desMode + "/" + desPadding;
        } else {
            transformation = key.getAlgorithm();
        }

        try {
            AlgorithmParameters parameters = AlgorithmParameters.getInstance(key.getAlgorithm());
            parameters.init(new IvParameterSpec(getIV(key)));

            Cipher c1 = Cipher.getInstance(transformation, provider.getName());
            c1.init(CipherMode, key, parameters);

            result = c1.doFinal(data);

        } catch (Exception e) {
            throw new SMException(e);
        }
        return result;
    }

    /**
     * Extracts the DES/DESede key material
     *
     * @param keyLength   bit length (key size) of the DES key. (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @param clearDESKey DES/Triple-DES key whose format is "RAW"
     * @return encoded key material
     * @throws SMException
     */
    protected byte[] extractDESKeyMaterial(short keyLength, Key clearDESKey) throws SMException {
        byte[] clearKeyBytes = null;
        String keyAlg = clearDESKey.getAlgorithm();
        String keyFormat = clearDESKey.getFormat();
        if (keyFormat.compareTo("RAW") != 0) {
            throw new SMException("Unsupported DES key encoding format: " + keyFormat);
        }
        if (!keyAlg.startsWith(ALG_DES)) {
            throw new SMException("Unsupported key algorithm: " + keyAlg);
        }
        clearKeyBytes = clearDESKey.getEncoded();
        clearKeyBytes = ISOUtil.trim(clearKeyBytes, getBytesLength(keyLength));
        return clearKeyBytes;
    }

    /**
     * Calculates the length of key in bytes
     *
     * @param keyLength bit length (key size) of the DES key. (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @return keyLength/8
     * @throws SMException if unknown key length
     */
    int getBytesLength(short keyLength) throws SMException {
        int bytesLength = 0;
        switch (keyLength) {
            case SMAdapter.LENGTH_DES:
                bytesLength = 8;
                break;
            case SMAdapter.LENGTH_DES3_2KEY:
                bytesLength = 16;
                break;
            case SMAdapter.LENGTH_DES3_3KEY:
                bytesLength = 24;
                break;
            default:
                throw new SMException("Unsupported key length: " + keyLength + " bits");
        }
        return bytesLength;
    }

    /**
     * Decrypts an encrypted DES/Triple-DES key
     *
     * @param keyLength       bit length (key size) of the DES key to be decrypted. (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @param encryptedDESKey the byte[] representing the encrypted key
     * @param encryptingKey   can be of any algorithm (RSA, DES, DESede...)
     * @param checkParity     if true, the parity of the key is checked
     * @return clear DES (DESede) Key
     * @throws SMException if checkParity==true and the key does not have correct parity
     */
    public Key decryptDESKey(short keyLength, byte[] encryptedDESKey, Key encryptingKey, boolean checkParity) throws SMException {
        // Decrypt encryptedDES key
        Key key = null;
        byte[] clearKeyBytes = doCryptStuff(encryptedDESKey, encryptingKey, Cipher.DECRYPT_MODE);
        if (checkParity) {
            if (!Util.isDESParityAdjusted(clearKeyBytes)) {
                throw new SMException("Parity not adjusted");
            }
        }
        key = formDESKey(keyLength, clearKeyBytes);
        return key;
    }

    /**
     * Forms the clear DES key given its "RAW" encoded bytes Does the inverse of extractDESKeyMaterial
     *
     * @param keyLength     bit length (key size) of the DES key. (LENGTH_DES, LENGTH_DES3_2KEY or LENGTH_DES3_3KEY)
     * @param clearKeyBytes the RAW DES/Triple-DES key
     * @return clear key
     * @throws SMException
     */
    public Key formDESKey(short keyLength, byte[] clearKeyBytes) throws SMException {
        Key key = null;
        switch (keyLength) {
            case SMAdapter.LENGTH_DES: {
                key = new SecretKeySpec(clearKeyBytes, ALG_DES);
            }
            ;
            break;
            case SMAdapter.LENGTH_DES3_2KEY: {
                // make it 3 components to work with JCE
                clearKeyBytes = ISOUtil.concat(clearKeyBytes, 0, getBytesLength(SMAdapter.LENGTH_DES3_2KEY), clearKeyBytes, 0,
                        getBytesLength(SMAdapter.LENGTH_DES));
            }
            case SMAdapter.LENGTH_DES3_3KEY: {
                key = new SecretKeySpec(clearKeyBytes, ALG_TRIPLE_DES);
            }
        }
        if (key == null)
            throw new SMException("Unsupported DES key length: " + keyLength + " bits");
        return key;
    }

    /**
     * Encrypts data
     *
     * @param data
     * @param key
     * @return encrypted data
     * @throws SMException
     */
    public byte[] encryptData(byte[] data, Key key) throws SMException {
        byte[] encryptedData;
        encryptedData = doCryptStuff(data, key, Cipher.ENCRYPT_MODE);
        return encryptedData;
    }

    /**
     * Decrypts data
     *
     * @param encryptedData
     * @param key
     * @return clear data
     * @throws SMException
     */
    public byte[] decryptData(byte[] encryptedData, Key key) throws SMException {
        byte[] clearData;
        clearData = doCryptStuff(encryptedData, key, Cipher.DECRYPT_MODE);
        return clearData;
    }

    byte[] generateMAC(byte[] data, Key clearKey) throws SMException {
        KeyParameter keyParameter = new KeyParameter(clearKey.getEncoded());

        ParametersWithIV parametersWithIV = new ParametersWithIV(keyParameter, getIVZero());
        Mac mac = null;

        if (clearKey.getAlgorithm().equals(ALG_TRIPLE_DES)) {
            BlockCipher cipher = new DESedeEngine();
            mac = new CBCBlockCipherMac(cipher, SMAdapter.LENGTH_DES);
        } else {
            if (clearKey.getAlgorithm().equals(ALG_DES)) {
                BlockCipher cipher = new DESEngine();
                mac = new CBCBlockCipherMac(cipher, SMAdapter.LENGTH_DES);

            }
        }

        if (mac == null) {
            throw new SMException(" Algorithm not supported!");
        }

        mac.init(parametersWithIV);
        mac.update(data, 0, data.length);
        byte[] out = new byte[8];
        mac.doFinal(out, 0);
        return out;
    }

    boolean verifyMAC(byte[] data, Key clearKey) throws SMException {
        if (data.length < 8)
            throw new SMException("incorrect data: No MAC appended to data");
        byte[] mac = new byte[8];
        byte[] rawData = new byte[data.length - 8];

        System.arraycopy(data, 0, rawData, 0, data.length - 8);
        System.arraycopy(data, data.length - 8, mac, 0, 8);

        byte[] newMac = generateMAC(rawData, clearKey);

        for (int j = 0; j < mac.length; j++) {
            if (newMac[j] != mac[j])
                return false;
        }

        return true;
    }

    private byte[] getIV(Key key) {

        byte[] ivarray = new byte[8];

        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(key.getEncoded());
            byte[] digest = md.digest();

            for (int i = 0; i < digest.length && i < 8; i++)
                ivarray[i] = 0;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return ivarray;

	}

	private byte[] getIVZero() {
		byte[] iv = new byte[8];
		for (int i = 0; i < iv.length; i++)
			iv[i] = 0;
		return iv;
	}
}
