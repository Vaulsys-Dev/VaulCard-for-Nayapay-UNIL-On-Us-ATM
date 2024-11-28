package vaulsys.security.hsm.eracom.KeySpec;


import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;


public class KeySpec91 extends KeySpecifier {
    public KeySpecifier KGK1Spec;
    public KeySpecifier KGK2Spec;
    public byte[] BLZ;

    public KeySpec91(KeySpecifier KGK1Spec, KeySpecifier KGK2Spec, byte[] BLZ) {
        format = (byte) 0x91;
        this.KGK1Spec = KGK1Spec;
        this.KGK2Spec = KGK2Spec;
        this.BLZ = BLZ;
    }

    public KeySpec91(byte[] response, int offset) {
        format = (byte) 0x91A;
        parseByteArray(response, offset);
    }


    public byte[] getByteArray() {
        byte[] result;

        byte[] KGK1 = KGK1Spec.getByteArray();
        byte[] KGK2 = KGK2Spec.getByteArray();

        result = new byte[1 + KGK1.length + KGK2.length + BLZ.length];
        int index = 0;

        result[index++] = format;

        System.arraycopy(KGK1, 0, result, index, KGK1.length);
//        for( int i = 0; i < KGK1.length; i++, index++ )
//            result[index] = KGK1[i];

        System.arraycopy(KGK2, 0, result, index, KGK2.length);
//        for( int i = 0; i < KGK2.length; i++, index++ )
//            result[index] = KGK2[i];

        System.arraycopy(BLZ, 0, result, index, BLZ.length);
//        for( int i = 0; i < BLZ.length; i++, index++ )
//            result[index] = BLZ[i];

        return HSMUtil.buildVarLengthParam(result);
    }


    public int parseByteArray(byte[] response, int offset) {
        MyInteger ofset = new MyInteger(offset);
        HSMUtil.getLengthOfVarField(response, ofset);
        offset = ofset.value;

        if (format != response[offset++]) {
            KGK1Spec = KGK2Spec = null;
            BLZ = null;
            return -1;
        }

        KGK1Spec = KeyHandler.getKeySpec(response, offset);
        MyInteger ofsetKGK1 = new MyInteger(offset);
        int lenKGK1 = HSMUtil.getLengthOfVarField(response, ofsetKGK1);
        offset = ofsetKGK1.value;
        offset += lenKGK1;


        KGK2Spec = KeyHandler.getKeySpec(response, offset);
        MyInteger ofsetKGK2 = new MyInteger(offset);
        int lenKGK2 = HSMUtil.getLengthOfVarField(response, ofsetKGK2);
        offset = ofsetKGK2.value;
        offset += lenKGK2;

        System.arraycopy(response, offset, BLZ, 0, BLZ.length);
//        for( int i = 0; i < BLZ.length; i++ )
//            BLZ[i] = response[offset++];       

        return 1;
    }
}
