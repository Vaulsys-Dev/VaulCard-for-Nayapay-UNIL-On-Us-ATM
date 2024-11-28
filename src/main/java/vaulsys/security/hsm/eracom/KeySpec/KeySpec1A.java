package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class KeySpec1A extends KeySpecifier {
    public byte Type;
    public byte KMid;
    public byte[] eKMv27;

    public KeySpec1A(byte[] eKMv27) {
        format = (byte) 0x1A;
        this.Type = 0x01;
        this.KMid = 0x00;
        this.eKMv27 = eKMv27;
    }

    public KeySpec1A(byte[] response, int offset) {
        format = (byte) 0x1A;
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {
        byte[] result = new byte[1 + 1 + 1 + eKMv27.length];

        int index = 0;

        result[index++] = format;
        result[index++] = Type;
        result[index++] = KMid;

        System.arraycopy(eKMv27, 0, result, index, eKMv27.length);
//        for( int i = 0; i < eKMv27.length; i++, index++ )
//            result[index] = eKMv27[i];

        return HSMUtil.buildVarLengthParam(result);
    }


    public int parseByteArray(byte[] response, int offset) {
        MyInteger ofset = new MyInteger(offset);
        HSMUtil.getLengthOfVarField(response, ofset);
        offset = ofset.value;

        if (format != response[offset++]) {
            Type = KMid = -1;
            eKMv27 = null;
            return -1;
        }

        Type = response[offset++];
        KMid = response[offset++];

//        Integer ofseteKMv27 = new Integer(offset);
//        int leneKMv27 = HSMUtil.getLengthOfVarField(response, ofseteKMv27);
//        offset = ofseteKMv27.intValue();
//        eKMv27 = new byte[leneKMv27];

        System.arraycopy(response, offset, eKMv27, 0, eKMv27.length);
//        for (int counter = 0; counter < length; counter++)
//            eKMv27[counter] = eKMv27[offset++];

        return 1;
    }
}
