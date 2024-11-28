package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class KeySpec92 extends KeySpecifier {
    public byte subType;
    public byte versionNumber;
    public byte generationNumber;
    public int expiryDate;


    public KeySpec92(byte subType, byte versionNumber,
                     byte generationNumber, int expiryDate) {
        format = (byte) 0x92;
        this.subType = subType;
        this.versionNumber = versionNumber;
        this.generationNumber = generationNumber;
        this.expiryDate = expiryDate;
    }

    public KeySpec92(byte[] response, int offset) {
        format = (byte) 0x92;
        parseByteArray(response, offset);
    }


    public byte[] getByteArray() {
        byte[] result = new byte[1 + 1 + 1 + 1 + 2];

        int index = 0;

        result[index++] = format;
        result[index++] = subType;
        result[index++] = versionNumber;
        result[index++] = generationNumber;

        if (expiryDate > 9999)
            return null;
        byte[] expiryDateArr = HSMUtil.intToBCD(expiryDate);
        System.arraycopy(expiryDateArr, 0, result, index, expiryDateArr.length);
//        result[index] = (byte)(expiryDate / 1000);
//        result[index] = (byte)(result[index] << 4);
//        result[index++] |= (byte)((expiryDate % 1000) / 100);

//        result[index] = (byte)(((expiryDate % 1000) % 100) / 10);
//        result[index] = (byte)(result[index] << 4);
//        result[index++] |= (byte)(expiryDate % 10);

        return HSMUtil.buildVarLengthParam(result);
    }


    public int parseByteArray(byte[] response, int offset) {
        MyInteger ofset = new MyInteger(offset);
        HSMUtil.getLengthOfVarField(response, ofset);
        offset = ofset.value;

        if (format != response[offset++]) {
            subType = versionNumber = generationNumber = -1;
            expiryDate = -1;
            return -1;
        }

        subType = response[offset++];
        versionNumber = response[offset++];
        generationNumber = response[offset++];

        //TODO: BCDToInt Function!!!
        expiryDate = response[offset] >> 4;
        expiryDate *= 1000;
        expiryDate += (response[offset++] & 0x0F) * 100;

        expiryDate += (response[offset] >> 4) * 10;
        expiryDate += (response[offset++] & 0x0F);

        return 1;
    }
}
