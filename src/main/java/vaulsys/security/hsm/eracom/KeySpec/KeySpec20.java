package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class KeySpec20 extends KeySpecifier {
    public KeySpecifier BDKSpec;
    public byte[] KNS;
    public byte derivedKeyType;

    public KeySpec20(KeySpecifier BDKSpec, byte[] KNS, byte derivedKeyType) {
        format = (byte) 0x20;
        this.BDKSpec = BDKSpec;
        this.KNS = KNS;
        this.derivedKeyType = derivedKeyType;
    }

    public KeySpec20(byte[] response, int offset) {
        format = (byte) 0x20;
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {
        byte[] BDKarr = BDKSpec.getByteArray();
        byte[] result = new byte[1 + BDKarr.length + KNS.length + 1];

        int index = 0;
        result[index++] = format;

        System.arraycopy(BDKarr, 0, result, index, BDKarr.length);
        index += BDKarr.length;
//        for( int i = 0; i < BDKarr.length; i++ )
//            result[index++] = BDKarr[i];

        System.arraycopy(KNS, 0, result, index, KNS.length);
        index += KNS.length;
//        for( int i = 0; i < KNS.length; i++ )
//            result[index++] = KNS[i];

        result[index++] = derivedKeyType;

        return HSMUtil.buildVarLengthParam(result);
    }

    public int parseByteArray(byte[] response, int offset) {
        MyInteger ofset = new MyInteger(offset);
        HSMUtil.getLengthOfVarField(response, ofset);
        offset = ofset.value;

        if (format != response[offset++]) {
            BDKSpec = null;
            KNS = null;
            derivedKeyType = -1;
            return -1;
        }

        BDKSpec = KeyHandler.getKeySpec(response, offset);

        MyInteger ofsetKNS = new MyInteger(offset);
        int lenKNS = HSMUtil.getLengthOfVarField(response, ofsetKNS);
        offset = ofsetKNS.value;
//        KNS = new byte[lenKNS];
        offset += lenKNS;

        System.arraycopy(response, offset, KNS, 0, KNS.length);
        offset += KNS.length;
//        for( int i = 0; i < KNS.length ; i++ )
//             KNS[i] = response[offset++];

        derivedKeyType = response[offset++];

        return 1;
    }
}
