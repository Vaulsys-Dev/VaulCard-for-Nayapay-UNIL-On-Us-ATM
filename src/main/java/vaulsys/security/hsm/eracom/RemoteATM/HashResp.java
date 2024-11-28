package vaulsys.security.hsm.eracom.RemoteATM;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class HashResp {
    int returnCode;
    byte[] bitCount;
    byte[] hashValue;

    public HashResp(byte[] result, int offset, int bitCountLen, int hashLength) {
        if ((returnCode = result[offset++]) != 0) {
            bitCount = hashValue = null;
            return;
        }

        bitCount = new byte[bitCountLen];
        for (int i = 0; i < bitCountLen; i++)
            bitCount[i] = result[offset++];

        if (hashLength == -1) {
            MyInteger ofset = new MyInteger(offset);
            hashLength = HSMUtil.getLengthOfVarField(result, ofset);
            offset = ofset.value;
        }

        hashValue = new byte[hashLength];
        for (int i = 0; i < hashLength; i++)
            hashValue[i] = result[offset++];
    }
}
