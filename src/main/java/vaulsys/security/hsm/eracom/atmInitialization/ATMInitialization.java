package vaulsys.security.hsm.eracom.atmInitialization;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.base.HSMUtil;

public class ATMInitialization {
    public static RSAKeyPair generateRSAKeyPair(int[] keyType, int modulusLenght,
                                                int exponenet, byte[] userData) {

        byte[] publicExponenet;

        if (exponenet > 256 * 256) {
            publicExponenet = new byte[3];
            publicExponenet[2] = (byte) (exponenet / (256 * 256));
            publicExponenet[1] = (byte) ((exponenet % (256 * 256)) / 256);
            publicExponenet[0] = (byte) (exponenet % (256 * 256));
        } else {
            publicExponenet = new byte[1];
            publicExponenet[0] = (byte) exponenet;
        }

        publicExponenet = HSMUtil.buildVarLengthParam(publicExponenet);
        userData = HSMUtil.buildVarLengthParam(userData);

        byte[] params = new byte[2 + 2 + publicExponenet.length + userData.length];
        int index = 0;

        params[index] = 0x00;

        for (int i = 0; i < keyType.length; i++) {
            switch (keyType[i]) {
                case RSAKeyUsage.Data_Signature:
                    params[index] |= 0x02;
                    break;
                case RSAKeyUsage.Key_Transport:
                    params[index] |= 0x04;
                    break;
            }
        }

        index++;
        params[index++] = 0x00;

        params[index++] = (byte) (modulusLenght / 256);
        params[index++] = (byte) (modulusLenght % 256);

        for (int i = 0; i < publicExponenet.length; i++)
            params[index++] = publicExponenet[i];

        for (int i = 0; i < userData.length; i++)
            params[index++] = userData[i];

        byte[] funcCode = new byte[]{(byte) 0xEE, (byte) 0x90, (byte) 0x01, (byte) 0x00};

        HSMFuncs func = new HSMFuncs(funcCode, params);

        func.sendRequest();

        int offset = 3;

        RSAKeyPair rsa = new RSAKeyPair(func.response, offset);

        return rsa;
    }
}