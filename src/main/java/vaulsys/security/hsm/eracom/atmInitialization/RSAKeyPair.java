package vaulsys.security.hsm.eracom.atmInitialization;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

import java.util.Arrays;

public class RSAKeyPair {
    public int returnCode;
    public byte[] privateKey;
    public byte[] publicKey;

    public RSAKeyPair(byte[] result, int offset) {
        if ((returnCode = result[offset++]) != 0) {
            privateKey = null;
            publicKey = null;
            return;
        }

        MyInteger ofset = new MyInteger(offset);

        int lenPK = HSMUtil.getLengthOfVarField(result, ofset);
        publicKey = new byte[lenPK];
        for (int i = 0; i < lenPK; i++)
            publicKey[i] = result[ofset.value++];

        int lenSK = HSMUtil.getLengthOfVarField(result, ofset);
        privateKey = new byte[lenSK];
        for (int i = 0; i < lenPK; i++)
            privateKey[i] = result[ofset.value++];
    }

    public String toString() {
        return "Public Key: " + Arrays.toString(publicKey) +
                "Private Key: " + Arrays.toString(privateKey);
    }
}
