package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;


public class KeySpec01 extends KeySpecifier {
    public byte index;

    public KeySpec01(int index) {
        length = 2;
        format = 0x01;
        this.index = (byte) index;
    }

    public KeySpec01(byte[] response, int offset) {
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {
        return HSMUtil.buildVarLengthParam(new byte[]{format, index});
    }

    public int parseByteArray(byte[] response, int offset) {
        HSMUtil.getLengthOfVarField(response, new MyInteger(offset));

        if (format != response[offset + 1]) {
            index = -1;
            return -1;
        }

        index = response[offset + 2];
        return 1;
    }


}
