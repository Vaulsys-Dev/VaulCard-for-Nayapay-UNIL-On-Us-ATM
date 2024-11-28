package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class KeySpec16 extends KeySpecifier {
    public byte algorithm;
    public byte keylength;
    public byte blocklength;
    public byte mode;
    public byte[] enckey;

    public KeySpec16(int mode, byte[] enckey) {
        format = 0x16;
        algorithm = (byte) 0xE0;
        keylength = 0x02;
        blocklength = 0x02;
        this.mode = (byte) mode;
        this.enckey = enckey;
    }

    public KeySpec16(byte[] response, int offset) {
        format = 0x16;
        parseByteArray(response, offset);
    }


    public byte[] getByteArray() {
        byte[] encKeyArr;
        encKeyArr = HSMUtil.buildVarLengthParam(enckey);
        byte[] result = new byte[1 + 1 + 1 + 1 + 1 + enckey.length];
        int index = 0;

        result[index++] = format;
        result[index++] = algorithm;
        result[index++] = keylength;
        result[index++] = blocklength;
        result[index++] = mode;

        System.arraycopy(encKeyArr, 0, result, index, encKeyArr.length);
//        for (int i = 5; i < (5 + encKeyArr.length); i++)
//            result[i] = encKeyArr[i - 5];

        return HSMUtil.buildVarLengthParam(result);
    }


    public int parseByteArray(byte[] response, int offset) {
        MyInteger ofset = new MyInteger(offset);
        HSMUtil.getLengthOfVarField(response, ofset);
        offset = ofset.value;

        if (format != response[offset++]) {
            algorithm = keylength = blocklength = mode = -1;
            enckey = null;
            return -1;
        }

        algorithm = response[offset++];
        keylength = response[offset++];
        blocklength = response[offset++];
        mode = response[offset++];

//        Integer ofsetENCKey = new Integer(offset);
//        int lenENCKey = HSMUtil.getLengthOfVarField(response, ofsetENCKey);
//        offset = ofsetENCKey.intValue();
//        enckey = new byte[lenENCKey];

        System.arraycopy(response, offset, enckey, 0, enckey.length);
//        for (int counter = 0; counter < length; counter++)
//            enckey[counter] = response[offset++];

        return 1;
    }
}
