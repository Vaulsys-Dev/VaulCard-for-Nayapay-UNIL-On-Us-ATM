package vaulsys.security.hsm.eracom.RemoteATM;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.KeySpec.KeyHandler;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.base.HSMUtil;

public class ATMInitialization {
    public static HashResp Generate_MD5_Hash(int mode, byte[] bitCount,
                                             byte[] hashValue, byte[] data) {

        data = HSMUtil.buildVarLengthParam(data);

        byte[] params = new byte[1 + bitCount.length + hashValue.length + data.length];
        int index = 0;

        if (mode == HashState.STATE_ONLY)
            params[index++] = 0x00;
        else if (mode == HashState.STATE_INITIAL)
            params[index++] = 0x01;
        else if (mode == HashState.STATE_INTERMEDIATE)
            params[index++] = 0x02;
        else if (mode == HashState.STATE_LAST)
            params[index++] = 0x03;

        for (int i = 0; i < bitCount.length; i++)
            params[index++] = bitCount[i];

        for (int i = 0; i < hashValue.length; i++)
            params[index++] = hashValue[i];

        for (int i = 0; i < data.length; i++)
            params[index++] = data[i];

        byte[] funCode = new byte[]{(byte) 0xEE, (byte) 0x90, (byte) 0x07, (byte) 0x00};

        HSMFuncs func = new HSMFuncs(funCode, params);

        func.sendRequest();

        return new HashResp(func.response, 3, 8, 16);
    }

    public static Generate_RSA_Key_Pair_Resp Generate_RSA_Key_Pair(byte[] keyType, int modulusLen, int publicExponent, byte[] userData) {

        userData = HSMUtil.buildVarLengthParam(userData);

        byte[] params = new byte[2 + 2 + (int) Math.ceil(publicExponent / 256.0) + userData.length];
        int index = 0;

        for (int i = 0; i < keyType.length; i++)
            params[index++] = keyType[i];

        if (modulusLen < 256) {
            params[index++] = 0x00;
            params[index++] = (byte) modulusLen;
        } else {
            params[index++] = (byte) (modulusLen % 256);
            params[index++] = (byte) (modulusLen / 256);
        }

        int temp = publicExponent;
        for (int i = 0; i < Math.ceil(temp / 256.0); i++) {
            params[index++] = (byte) (temp % 256);
            temp /= 256;
        }

        for (int i = 0; i < userData.length; i++)
            params[index++] = userData[i];

        byte[] funCode = new byte[]{(byte) 0xEE, (byte) 0x90, (byte) 0x01, (byte) 0x00};

        HSMFuncs func = new HSMFuncs(funCode, params);

        func.sendRequest();

        return new Generate_RSA_Key_Pair_Resp(func.response, 3);
    }

    public static KeySpecifier Import_Public_Key(byte[] keyType, KeySpecifier PK, byte[] userData) {

        userData = HSMUtil.buildVarLengthParam(userData);
        byte[] pk = PK.getByteArray();

        byte[] params = new byte[2 + userData.length + pk.length];
        int index = 0;

        for (int i = 0; i < keyType.length; i++)
            params[index++] = keyType[i];

        for (int i = 0; i < pk.length; i++)
            params[index++] = pk[i];

        for (int i = 0; i < userData.length; i++)
            params[index++] = userData[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x90, (byte) 0x03,
                        (byte) 0x00}, params);

        func.sendRequest();

        int offset = 3;
        if (func.response[offset++] != 0)
            return null;

        return KeyHandler.getKeySpec(func.response, offset);
    }
}
