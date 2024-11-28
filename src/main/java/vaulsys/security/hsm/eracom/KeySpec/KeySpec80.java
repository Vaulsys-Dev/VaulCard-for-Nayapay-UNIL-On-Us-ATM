package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class KeySpec80 extends KeySpecifier {
    public byte[] modulus;
    public byte[] exponent;


    public KeySpec80(byte[] modules, byte[] exponent) {
        format = (byte) 0x80;
        this.modulus = modules;
        this.exponent = exponent;
    }

    public KeySpec80(byte[] response, int offset) {
        format = (byte) 0x80;
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {

        byte[] modulusArr = HSMUtil.buildVarLengthParam(modulus);
        byte[] exponentArr = HSMUtil.buildVarLengthParam(exponent);
        byte[] b = new byte[1 + modulusArr.length + exponentArr.length];
        int index = 0;

        b[index++] = format;
        System.arraycopy(modulusArr, 0, b, index, modulusArr.length);
        index += modulusArr.length;
//        for (int i = 0; i < modulusArr.length; i++)
//            b[index++] = modulusArr[i];

        System.arraycopy(exponentArr, 0, b, index, exponentArr.length);
//        for (int i = 0; i < exponentArr.length; i++)
//            b[index++] = exponentArr[i];

        return HSMUtil.buildVarLengthParam(b);
    }

    public int parseByteArray(byte[] response, int offset) {
        MyInteger ofset = new MyInteger(offset);
        HSMUtil.getLengthOfVarField(response, ofset);
        offset = ofset.value;

        if (format != response[offset++]) {
            modulus = exponent = null;
            return -1;
        }

        MyInteger ofsetModulus = new MyInteger(offset);
        int lenModulus = HSMUtil.getLengthOfVarField(response, ofsetModulus);
        System.arraycopy(response, offset, modulus, 0, lenModulus);
        offset = ofsetModulus.value;
        offset += lenModulus;
//        for (int i = 0; i < length; i++)
//            modulus[i] = response[offset++];

        MyInteger ofsetExponent = new MyInteger(offset);
        int lenExponent = HSMUtil.getLengthOfVarField(response, ofsetExponent);
        System.arraycopy(response, offset, exponent, 0, lenExponent);
//        offset = ofsetExponent.intValue();
//        offset += lenExponent;

//        for (int i = 0; i < length2; i++)
//            exponent[i] = response[offset++ ];

        return 1;
    }
}
