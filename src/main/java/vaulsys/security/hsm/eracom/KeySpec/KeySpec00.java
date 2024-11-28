package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;


public class KeySpec00 extends KeySpecifier {
    public int index;

    public KeySpec00(int index) {
        length = 2;
        format = 0x00;
        this.index = index;
    }

    public KeySpec00(byte[] response, int offset) {
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {
        byte bValue;
        bValue = (byte) (index / 10);
        bValue = (byte) (bValue << 4);
        bValue |= (byte) (index % 10);

        return HSMUtil.buildVarLengthParam(new byte[]{format, bValue});
    }

    public int parseByteArray(byte[] response, int offset) {
        HSMUtil.getLengthOfVarField(response, new MyInteger(offset));

        if (format != response[offset + 1]) {
            index = -1;
            return -1;
        }

        index = response[offset + 2] >> 4;
        index *= 10;
        index += response[offset + 2] & 0x0F;
        return 1;
    }

    @Override
    public String toString() {
        return "Format" + HSMUtil.hexToString(new byte[]{format}) + "[" + index + "]";
    }
}
