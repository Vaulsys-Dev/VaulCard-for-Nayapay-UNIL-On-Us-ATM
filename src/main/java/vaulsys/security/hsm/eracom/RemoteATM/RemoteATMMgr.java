package vaulsys.security.hsm.eracom.RemoteATM;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.base.HSMUtil;

public class RemoteATMMgr {

    public static HSMGenerateMD5Hash generateMD5Hash(int mode, byte[] bitCount,
                                                     byte[] hashValue,
                                                     byte[] data) {

        int index = 0;
        byte[] dataArr = HSMUtil.buildVarLengthParam(data);
        byte[] params = new byte[1 + 8 + 16 + dataArr.length];

        params[index++] = (byte) mode;
        for (int i = 0; i < bitCount.length; i++)
            params[index++] = bitCount[i];
        for (int i = 0; i < hashValue.length; i++)
            params[index++] = hashValue[i];
        for (int i = 0; i < dataArr.length; i++)
            params[index++] = dataArr[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x90, (byte) 0x07,
                        (byte) 0x00}, params);

        func.sendRequest();
        HSMGenerateMD5Hash resp = new HSMGenerateMD5Hash(func.response, 3);
        return resp;

    }

    public static byte[] generateSHAHash(int mode, byte[] bitCount,
                                         byte[] hashValue, byte[] data) {

        int index = 0;
        byte[] hashValueArr = HSMUtil.buildVarLengthParam(hashValue);
        byte[] dataArr = HSMUtil.buildVarLengthParam(data);
        byte[] params =
                new byte[1 + 1 + 8 + hashValueArr.length + dataArr.length];

        params[index++] = 0x00; //Algoritm
        params[index++] = (byte) mode;

        for (int i = 0; i < bitCount.length; i++)
            params[index++] = bitCount[i];
        for (int i = 0; i < hashValueArr.length; i++)
            params[index++] = hashValueArr[i];
        for (int i = 0; i < dataArr.length; i++)
            params[index++] = dataArr[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x90, (byte) 0x08,
                        (byte) 0x00}, params);

        func.sendRequest();
        return func.response;


    }

    public static byte[] signData(KeySpecifier sk, int hashFunction,
                                  byte[] data) {

        int index = 0;
        byte[] skArr = sk.getByteArray();
        byte[] dataArr = HSMUtil.buildVarLengthParam(data);
        byte[] params = new byte[skArr.length + 1 + 1 + dataArr.length];

        for (int i = 0; i < skArr.length; i++)
            params[index++] = skArr[i];

        params[index++] = 0x01; //Signature Algorithm     
        params[index++] = (byte) hashFunction;

        for (int i = 0; i < dataArr.length; i++)
            params[index++] = dataArr[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x90, (byte) 0x05,
                        (byte) 0x00}, params);

        func.sendRequest();
        return func.response;


    }

    public static byte[] verifySignedData(KeySpecifier pk, int hashFunction,
                                          byte[] data, byte[] signature) {

        int index = 0;
        byte[] pkArr = pk.getByteArray();
        byte[] dataArr = HSMUtil.buildVarLengthParam(data);
        byte[] signArr = HSMUtil.buildVarLengthParam(signature);
        byte[] params =
                new byte[pkArr.length + 1 + 1 + dataArr.length + signArr.length];

        for (int i = 0; i < pkArr.length; i++)
            params[index++] = pkArr[i];

        params[index++] = 0x01; //Signature Algorithm     
        params[index++] = (byte) hashFunction;

        for (int i = 0; i < dataArr.length; i++)
            params[index++] = dataArr[i];
        for (int i = 0; i < signArr.length; i++)
            params[index++] = signArr[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x90, (byte) 0x06,
                        (byte) 0x00}, params);

        func.sendRequest();
        return func.response;

    }

    public static HSMGenerateKeyDiebold generateKeyDiebold(byte[] idHost,
                                                           byte[] idATM,
                                                           byte[] ATMrandomNonce,
                                                           KeySpecifier keyspecATMPublicKey,
                                                           KeySpecifier keySpecHostPrivateKey,
                                                           byte keyLen) {
        int index = 0;
        byte[] idHostArr = HSMUtil.buildVarLengthParam(idHost);
        byte[] idAtmArr = HSMUtil.buildVarLengthParam(idATM);
        byte[] ATMrandomNonceArr = HSMUtil.buildVarLengthParam(ATMrandomNonce);
        byte[] ATMPublicKeyArr = keyspecATMPublicKey.getByteArray();
        byte[] HostPrivateKey = keySpecHostPrivateKey.getByteArray();

        byte[] params =
                new byte[idHostArr.length + idAtmArr.length + ATMrandomNonceArr.length +
                        ATMPublicKeyArr.length + HostPrivateKey.length + 1 + 1];


        for (int i = 0; i < idHostArr.length; i++)
            params[index++] = idHostArr[i];
        for (int i = 0; i < idAtmArr.length; i++)
            params[index++] = idAtmArr[i];
        for (int i = 0; i < ATMrandomNonceArr.length; i++)
            params[index++] = ATMrandomNonceArr[i];
        for (int i = 0; i < ATMPublicKeyArr.length; i++)
            params[index++] = ATMPublicKeyArr[i];
        for (int i = 0; i < HostPrivateKey.length; i++)
            params[index++] = HostPrivateKey[i];

        params[index++] = keyLen;
        params[index++] = 0x05;

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x91, (byte) 0x01,
                        (byte) 0x00}, params);

        func.sendRequest();
        HSMGenerateKeyDiebold resp =
                new HSMGenerateKeyDiebold(func.response, 3);

        return resp;


    }

    public static byte[] verifyATMResponseDiebold(byte[] PKCS7Message,
                                                  byte[] idHost,
                                                  byte[] AtmRandomNonce,
                                                  byte[] HostRandomNonce,
                                                  KeySpecifier keySpecATMPublicKey) {
        int index = 0;
        byte[] PKCS7MessageArr = HSMUtil.buildVarLengthParam(PKCS7Message);
        byte[] idHostArr = HSMUtil.buildVarLengthParam(idHost);
        byte[] AtmRandomNonceArr = HSMUtil.buildVarLengthParam(AtmRandomNonce);
        byte[] HostRandomNonceArr =
                HSMUtil.buildVarLengthParam(HostRandomNonce);
        byte[] AtmPublicKeyArr = keySpecATMPublicKey.getByteArray();

        byte[] params =
                new byte[PKCS7MessageArr.length + idHostArr.length + AtmRandomNonceArr.length +
                        HostRandomNonceArr.length + AtmPublicKeyArr.length];

        for (int i = 0; i < PKCS7MessageArr.length; i++)
            params[index++] = PKCS7MessageArr[i];
        for (int i = 0; i < idHostArr.length; i++)
            params[index++] = idHostArr[i];
        for (int i = 0; i < AtmRandomNonceArr.length; i++)
            params[index++] = AtmRandomNonceArr[i];
        for (int i = 0; i < HostRandomNonceArr.length; i++)
            params[index++] = HostRandomNonceArr[i];
        for (int i = 0; i < AtmPublicKeyArr.length; i++)
            params[index++] = AtmPublicKeyArr[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x91, (byte) 0x02,
                        (byte) 0x00}, params);

        func.sendRequest();
        return func.response;

    }

    public static byte[] generateKMNCR(KeySpecifier SKHSM,
                                       KeySpecifier PKEPP) {

        int index = 0;
        byte[] skHsmArr = SKHSM.getByteArray();
        byte[] pkEppArr = PKEPP.getByteArray();
        byte[] params = new byte[skHsmArr.length + pkEppArr.length];

        for (int i = 0; i < skHsmArr.length; i++)
            params[index++] = skHsmArr[i];
        for (int i = 0; i < pkEppArr.length; i++)
            params[index++] = pkEppArr[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x92, (byte) 0x01,
                        (byte) 0x00}, params);

        func.sendRequest();
        return func.response;

    }

}
