package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;


public class KeySpec03 extends KeySpecifier {

    public byte[] index;

    public KeySpec03(int index) {
        length = 3;
        format = 0x03;
        this.index = new byte[2];
        this.index[0] = (byte) (index / 256);
        this.index[1] = (byte) (index % 256);
    }

    public KeySpec03(byte[] response, int offset) {
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {
        return HSMUtil.buildVarLengthParam(new byte[]{format, index[0],
                index[1]});
    }

    public int parseByteArray(byte[] response, int offset) {
        HSMUtil.getLengthOfVarField(response, new MyInteger(offset));

        if (format != response[offset + 1]) {
            index[0] = index[1] = -1;
            return -1;
        }

        index[0] = response[offset + 2];
        index[1] = response[offset + 3];
        return 1;
    }
}
