package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;


public class SessionKeySpecKVC {
    public byte[] encryptedSessionKey;
    public KeySpecifier keySpec;
    public byte[] kvc;


    public byte[] getEncryptedSessionKey() {
        return encryptedSessionKey;
    }

    public KeySpecifier getKeySpec() {
        return keySpec;
    }

    public byte[] getKvc() {
        return kvc;
    }


    public SessionKeySpecKVC(byte[] result, MyInteger offset, int kvcLength) {
        int length = HSMUtil.getLengthOfVarField(result, offset);
        encryptedSessionKey = new byte[length];
        for (int counter = 0; counter < length; counter++)
            encryptedSessionKey[counter] = result[offset.value++];

        int len = HSMUtil.getLengthOfVarField(result, offset);
        keySpec = KeyHandler.getKeySpec(result, --offset.value);

        offset.value += len + 1;

        kvc = new byte[kvcLength];
        for (int i = 0; i < kvcLength; i++)
            kvc[i] = result[offset.value++];
    }


    @Override
    public String toString() {
        return "Host Key:\n" + keySpec + "\n" +
                "Terminal key:\n" + HSMUtil.hexToString((encryptedSessionKey)) + "; KVC = " + HSMUtil.hexToString(kvc);
    }
}
