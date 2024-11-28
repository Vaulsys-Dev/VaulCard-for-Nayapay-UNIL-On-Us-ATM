package vaulsys.security.hsm.eracom.DataCiphering;

import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.base.CryptoMode;
import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.security.hsm.eracom.base.Padding;

public class DataCiphering {
    public static /*DataCipher */ byte[] Encipher_2(KeySpecifier keySpec, int cipherMode, byte[] IV, byte[] plaintext, int paddingMethod) {
        return encipherDecipher(/*FunctionCode.ENCIPHER_2, (byte) 0x00 ,*/ keySpec, cipherMode, IV, false, plaintext,
                paddingMethod);
    }

    public static /*DataCipher */ byte[] Decipher_2(KeySpecifier keySpec, int cipherMode, byte[] IV, byte[] plaintext, int paddingMethod) {
        return encipherDecipher(/*FunctionCode.DECIPHER_2, (byte) 0x00 ,*/ keySpec, cipherMode, IV, false, plaintext,
                paddingMethod);
    }

    // TODO: SEED algorithm

    public static /*DataCipher */ byte[] Encipher_3(KeySpecifier keySpec, int cipherMode, byte[] IV, byte[] plaintext, int paddingMethod) {
        return encipherDecipher(/*FunctionCode.ENCIPHER_3, (byte) 0x00 ,*/ keySpec, cipherMode, IV, true, plaintext,
                paddingMethod);
    }

    // TODO: SEED algorithm

    public static /*DataCipher */ byte[] Decipher_3(KeySpecifier keySpec, int cipherMode, byte[] IV, byte[] plaintext, int paddingMethod) {
        return encipherDecipher(/*FunctionCode.DECIPHER_3, (byte) 0x00 ,*/ keySpec, cipherMode, IV, true, plaintext,
                paddingMethod);
    }

    private static /*DataCipher*/byte[] encipherDecipher(/*byte[] funCode, byte FM,*/ KeySpecifier keySpec, int cipherMode, byte[] IV, boolean varIV, byte[] plaintext,
                                                         int paddingMethod) {
        if (plaintext.length % 8 != 0) {
            plaintext = Padding.padData(plaintext, (8 - (plaintext.length % 8)), paddingMethod);
        }

        byte[] data = HSMUtil.buildVarLengthParam(plaintext);

        byte[] iv;
        if (varIV == true)
            iv = HSMUtil.buildVarLengthParam(IV);
        else
            iv = IV;

        byte[] key = keySpec.getByteArray();

        byte[] params = new byte[key.length + 1 + iv.length + data.length];
        int index = 0;

        for (int i = 0; i < key.length; i++)
            params[index++] = key[i];

        if (cipherMode == CryptoMode.ECB)
            params[index++] = (byte) 0x00;
        else if (cipherMode == CryptoMode.CBC)
            params[index++] = (byte) 0x01;

        for (int i = 0; i < iv.length; i++)
            params[index++] = iv[i];

        for (int i = 0; i < data.length; i++)
            params[index++] = data[i];

//		byte[] functionCode = new byte[funCode.length+1];
//		System.arraycopy(funCode, 0, functionCode, 0, funCode.length);
//		functionCode[funCode.length]= FM; 
//		HSMFuncs func = new HSMFuncs(functionCode, params);
//
//		func.sendRequest();
//
//		DataCipher cipher = new DataCipher(func.response, 3, varIV);
//
//		return cipher;
        return params;
    }
}
