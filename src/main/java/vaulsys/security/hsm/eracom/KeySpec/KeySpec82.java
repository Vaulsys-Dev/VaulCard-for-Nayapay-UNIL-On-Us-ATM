package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class KeySpec82 extends KeySpecifier {
    public byte[] modLen;
    public byte keyFormat;
    public byte KMid;
    public byte[] keyType;
    public byte AuthenticationAlgId;
    public byte[] userData;
    public byte[] eKMv20;
    public byte[] AutenticationValue;

    public KeySpec82(byte[] modLen, byte keyFormat, byte[] keyType,
                     byte AuthenticationAlgId, byte[] userData, byte[] eKMv20,
                     byte[] AutenticationValue) {
        format = (byte) 0x82;
        this.modLen = modLen;
        this.keyFormat = keyFormat;
        this.KMid = 0x00;
        this.keyType = keyType;
        this.AuthenticationAlgId = AuthenticationAlgId;
        this.userData = userData;
        this.eKMv20 = eKMv20;
        this.AutenticationValue = AutenticationValue;
    }

    public KeySpec82(byte[] response, int offset) {
        format = (byte) 0x82;
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {
        byte[] result = new byte[1 + 2 + 1 + 1 + 2 + 1 +
                userData.length + eKMv20.length + AutenticationValue.length];

        int index = 0;
        result[index++] = format;

        System.arraycopy(modLen, 0, result, index, modLen.length);
        index += modLen.length;
//        for( int i = 0; i < modLen.length; i++, index++ )
//            result[index] = modLen[i];

        result[index++] = keyFormat;
        result[index++] = KMid;

        System.arraycopy(keyType, 0, result, index, keyType.length);
        index += keyType.length;
//        for( int i = 0; i < keyType.length; i++, index++ )
//            result[index] = keyType[i];

        result[index++] = AuthenticationAlgId;

        System.arraycopy(userData, 0, result, index, userData.length);
        index += userData.length;
//        for( int i = 0; i < userData.length; i++, index++ )
//            result[index] = userData[i];

        System.arraycopy(eKMv20, 0, result, index, eKMv20.length);
        index += eKMv20.length;
//        for( int i = 0; i < eKMv20.length; i++, index++ )
//            result[index] = eKMv20[i];

        System.arraycopy(AutenticationValue, 0, result, index, AutenticationValue.length);
//        for( int i = 0; i < AutenticationValue.length; i++, index++ )
//            result[index] = AutenticationValue[i];

        return HSMUtil.buildVarLengthParam(result);
    }

    public int parseByteArray(byte[] response, int offset) {
        MyInteger ofset = new MyInteger(offset);
        HSMUtil.getLengthOfVarField(response, ofset);
        offset = ofset.value;

        if (format != response[offset++]) {
            modLen = keyType = userData = eKMv20 = AutenticationValue = null;
            keyFormat = KMid = AuthenticationAlgId = -1;
            return -1;
        }

        System.arraycopy(response, offset, modLen, 0, modLen.length);
        offset += modLen.length;
//        for( int i = 0; i < modLen.length; i++ )
//            modLen[i] = response[offset++];

        keyFormat = response[offset++];
        KMid = response[offset++];

        System.arraycopy(response, offset, keyType, 0, keyType.length);
        offset += keyType.length;
//        for( int i = 0; i < keyType.length; i++ )
//            keyType[i] = response[offset++];

        AuthenticationAlgId = response[offset++];

        System.arraycopy(response, offset, userData, 0, userData.length);
        offset += userData.length;
//        for( int i = 0; i < userData.length; i++ )
//            userData[i] = response[offset++];

        System.arraycopy(response, offset, eKMv20, 0, eKMv20.length);
        offset += eKMv20.length;
//        for( int i = 0; i < eKMv20.length; i++ )
//            eKMv20[i] = response[offset++];

        System.arraycopy(response, offset, AutenticationValue, 0, AutenticationValue.length);
//        offset += AutenticationValue.length;
//        for( int i = 0; i < AutenticationValue.length; i++ )
//            AutenticationValue[i] = response[offset++];

        return 1;
    }
}