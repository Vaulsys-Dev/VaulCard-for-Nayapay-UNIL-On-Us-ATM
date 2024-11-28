package vaulsys.security.ssm.posSSM;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.encoders.Hex;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class POSSSM {

    private Logger logger = Logger.getLogger(POSSSM.class);

    public byte[] generateMAC(int index, byte[] keyData, byte[] data, int algorithm, byte[] IV, int macLength, int padding, byte[] masterKey) {

        if (masterKey != null) {
            keyData = decrypt(keyData, masterKey);
        }

        logger.warn("using key:" + new String(Hex.encode(keyData)).toUpperCase());

        int keyLength = keyData.length;
        IV = (IV == null) ? new byte[8] : IV;

        KeyParameter key = new KeyParameter(keyData);
        ParametersWithIV param = new ParametersWithIV(key, IV);

        BlockCipher cipher;
        if (keyLength == 8)
            cipher = new DESEngine();
        else
            cipher = new DESedeEngine();

        Mac mac = new CBCBlockCipherMac(cipher, 64);
        mac.init(param);
        mac.update(data, 0, data.length);
        byte[] out = new byte[8];
        mac.doFinal(out, 0);

        return out;

    }

    public byte[] translatePIN(byte[] inputPinBlock, int inputIndex, byte[] inputKey, byte PFi, String AccountNumberBlock, byte PFo, int outputIndex, byte[] outputKey, byte[] masterKey) {

        if (masterKey != null) {
            inputKey = decrypt(inputKey, masterKey);
            outputKey = decrypt(outputKey, masterKey);
        }

        int keyLength = inputKey.length;

        KeyParameter inKey = new KeyParameter(inputKey);

        BlockCipher decipher;
        if (keyLength == 8)
            decipher = new DESEngine();
        else
            decipher = new DESedeEngine();

        decipher.init(false, inKey);

        byte[] pinBlock = new byte[8];
        decipher.processBlock(inputPinBlock, 0, pinBlock, 0);

        logger.warn("Received PIN:" + HSMUtil.hexToString(pinBlock));

        KeyParameter outKey = new KeyParameter(outputKey);

        BlockCipher cipher;
        if (keyLength == 8)
            cipher = new DESEngine();
        else
            cipher = new DESedeEngine();

        cipher.init(true, outKey);

        // byte[] pinBlock = getPINBlock(PIN, PAN);

        byte[] encryptedPin = new byte[8];
        cipher.processBlock(pinBlock, 0, encryptedPin, 0);
        return encryptedPin;
    }

    public byte[] encrypt(byte[] data, byte[] key) {
        byte[] encryptedData = new byte[data.length];

        BlockCipher cipher = null;
        KeyParameter keyParam = new KeyParameter(key);
        if (key.length == 8) {
            cipher = new DESEngine();
            cipher.init(true, keyParam);
        } else if (key.length == 16) {
            ParametersWithIV param = new ParametersWithIV(keyParam, new byte[8]);
            cipher = new DESedeEngine();
            cipher.init(true, param);
        }

        if (cipher != null)
            cipher.processBlock(data, 0, encryptedData, 0);

        return encryptedData;
    }

    public byte[] decrypt(byte[] data, byte[] key) {
        byte[] encryptedData = new byte[data.length];

        BlockCipher cipher = null;
        KeyParameter keyParam = new KeyParameter(key);
        if (key.length == 8) {
            cipher = new DESEngine();
            cipher.init(false, keyParam);
        } else if (key.length == 16) {
            ParametersWithIV param = new ParametersWithIV(keyParam, new byte[8]);
            cipher = new DESedeEngine();
            cipher.init(false, param);
        }

        if (cipher != null)
            cipher.processBlock(data, 0, encryptedData, 0);

        return encryptedData;
    }

}
