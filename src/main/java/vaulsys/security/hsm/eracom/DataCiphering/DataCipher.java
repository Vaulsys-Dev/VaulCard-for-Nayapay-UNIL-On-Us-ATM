package vaulsys.security.hsm.eracom.DataCiphering;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

import java.util.Arrays;


public class DataCipher {
    public byte[] outputChaining;
    public byte[] cipherText; //it is not text!!!

    public DataCipher(byte[] result, int offset, boolean varIV) {
        if (result[offset++] != 0) {
            outputChaining = null;
            cipherText = null;
            return;
        }

        int outputChainingLength;
        if (varIV) {
            MyInteger ofset = new MyInteger(offset);
            offset = ofset.value;
            outputChainingLength = HSMUtil.getLengthOfVarField(result, ofset);
            offset = ofset.value;
        } else {
            outputChainingLength = 8;
        }

        outputChaining = new byte[outputChainingLength];
        for (int i = 0; i < outputChaining.length; i++)
            outputChaining[i] = result[offset++];

        MyInteger ofset = new MyInteger(offset);
        int length = HSMUtil.getLengthOfVarField(result, ofset);
        offset = ofset.value;

        cipherText = new byte[length];
        for (int i = 0; i < length; i++)
            cipherText[i] = result[offset++];
    }

    public String toString() {
        return "outputChaining: " + Arrays.toString(outputChaining) + "\n" +
                "cipherText: " + Arrays.toString(cipherText) + "\n";
    }
}
