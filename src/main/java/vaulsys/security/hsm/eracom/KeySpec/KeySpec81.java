package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class KeySpec81 extends KeySpecifier {
    public byte[] modulus;
    public byte[] exponent;
    public byte kmId;
    public byte[] keyType; //???????
    public byte authenticationAlgorithmId;
    public byte[] userData;
    public byte[] authenticationValue; //??????

    public KeySpec81(byte[] modulus, byte[] exponent, byte[] keyType,
                     byte[] userData, byte[] authenticationValue) {
        format = (byte) 0x81;
        this.modulus = modulus;
        this.exponent = exponent;
        this.kmId = 0x00;
        this.keyType = keyType; //??
        this.authenticationAlgorithmId = 0x01;
        this.userData = userData;
        this.authenticationValue = authenticationValue; //??
    }

    public KeySpec81(byte[] response, int offset) {
        format = (byte) 0x81;
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {

        byte[] modulusArr = HSMUtil.buildVarLengthParam(modulus);
        byte[] exponentArr = HSMUtil.buildVarLengthParam(exponent);
        byte[] userDataArr = HSMUtil.buildVarLengthParam(userData);
        byte[] authenticationValueArr =
                HSMUtil.buildVarLengthParam(authenticationValue);

        byte[] b =
                new byte[1 + modulusArr.length + exponentArr.length + 1 + 2 + 1 +
                        userDataArr.length + authenticationValueArr.length];

        int index = 0;
        b[index++] = format;

        System.arraycopy(modulusArr, 0, b, index, modulusArr.length);
        index += modulusArr.length;
//        for (int i = 0; i < modulusArr.length; i++, index++)
//            b[index] = modulusArr[i];

        System.arraycopy(exponentArr, 0, b, index, exponentArr.length);
        index += exponentArr.length;
//        for (int i = 0; i < exponentArr.length; i++, index++)
//            b[index] = exponentArr[i];

        b[index++] = kmId;

        // TODO: check the keyType
        System.arraycopy(keyType, 0, b, index, keyType.length);
        index += keyType.length;
//        for (int i = 0; i < 2; i++, index++)
//            b[index] = keyType[i];

        b[index++] = authenticationAlgorithmId;

        System.arraycopy(userDataArr, 0, b, index, userDataArr.length);
        index += userDataArr.length;
//        for (int i = 0; i < userDataArr.length; i++, index++)
//            b[index] = userDataArr[i];

        System.arraycopy(authenticationValueArr, 0, b, index, authenticationValueArr.length);
//        for (int i = 0; i < authenticationValueArr.length; i++, index++)
//            b[index] = authenticationValueArr[i];

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
//        for (int i = 0; i < length2; i++)
//            modulus[i] = response[offset++];

        MyInteger ofsetExponent = new MyInteger(offset);
        int lenExponent = HSMUtil.getLengthOfVarField(response, ofsetExponent);
        System.arraycopy(response, offset, exponent, 0, lenExponent);
        offset = ofsetExponent.value;
        offset += lenExponent;
//        for (int i = 0; i < length3; i++)
//            exponent[i] = response[offset + i];
//        offset += length2;

        kmId = response[offset++];

        // TODO:check keyType
        System.arraycopy(response, offset, keyType, 0, keyType.length);
        offset += keyType.length;
//        for (int i = 0; i < 2; i++)
//            exponent[i] = response[offset++];

        authenticationAlgorithmId = response[offset++];

        MyInteger ofsetUserData = new MyInteger(offset);
        int lenUserData = HSMUtil.getLengthOfVarField(response, ofsetUserData);
        System.arraycopy(response, offset, userData, 0, lenUserData);
        offset = ofsetUserData.value;
        offset += lenUserData;
//        for (int i = 0; i < length4; i++)
//            userData[i] = response[offset++];

        MyInteger ofsetAuthen = new MyInteger(offset);
        int lenAuthen = HSMUtil.getLengthOfVarField(response, ofsetAuthen);
        System.arraycopy(response, offset, authenticationValue, 0, lenAuthen);
//        offset = ofsetAuthen.intValue();
//        offset += lenAuthen;
//        for (int i = 0; i < length5; i++)
//            authenticationValue[i] = response[offset++];

        return 1;
    }
}
