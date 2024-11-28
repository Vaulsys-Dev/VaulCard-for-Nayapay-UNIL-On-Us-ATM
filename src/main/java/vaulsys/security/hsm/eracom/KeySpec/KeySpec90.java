package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class KeySpec90 extends KeySpecifier {
    KeySpecifier MKSpec;
    byte CVindex;
    byte[] RND;

    public KeySpec90(KeySpecifier MKSpec, byte CVindex, byte[] RND) {
        format = (byte) 0x90;
        this.MKSpec = MKSpec;
        this.CVindex = CVindex;
        this.RND = RND;
    }

    public KeySpec90(byte[] response, int offset) {
        format = (byte) 0x90;
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {
        byte[] MKarr = MKSpec.getByteArray();
        byte[] result = new byte[1 + MKarr.length + 1 + RND.length];

        int index = 0;
        result[index++] = format;

        System.arraycopy(MKarr, 0, result, index, MKarr.length);
        index += MKarr.length;
//        for( int i = 0; i < MKarr.length; i++, index++ )
//            result[index] = MKarr[i];

        result[index++] = CVindex;

        System.arraycopy(RND, 0, result, index, RND.length);
//        for( int i = 0; i < RND.length; i++, index++ )
//            result[index] = RND[i];

        return HSMUtil.buildVarLengthParam(result);
    }

    public int parseByteArray(byte[] response, int offset) {
        MyInteger ofset = new MyInteger(offset);
        HSMUtil.getLengthOfVarField(response, ofset);
        offset = ofset.value;

        if (format != response[offset++]) {
            MKSpec = null;
            CVindex = -1;
            RND = null;
            return -1;
        }

        MKSpec = KeyHandler.getKeySpec(response, offset);
        MyInteger ofsetMK = new MyInteger(offset);
        int lenMK = HSMUtil.getLengthOfVarField(response, ofsetMK);
        offset = ofsetMK.value;
        offset += lenMK;

        CVindex = response[offset++];

        System.arraycopy(response, offset, RND, 0, RND.length);
//        for( int i = 0 ;i < RND.length ; i++ )
//             RND[i] = response[offset++];

        return 1;
    }
}
