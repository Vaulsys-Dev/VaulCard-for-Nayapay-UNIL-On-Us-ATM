package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;


public class KeySpec02 extends KeySpecifier {
    public int index;

    public KeySpec02(int index) {
        length = 3;
        format = 0x03;
        this.index = index;
    }

    public KeySpec02(byte[] response, int offset) {
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {
        byte bValue1, bValue2;

        bValue1 = (byte) (index / 1000);
        bValue1 = (byte) (bValue1 << 4);
        bValue1 |= (byte) ((index % 1000) / 100);

        bValue2 = (byte) (((index % 1000) % 100) / 10);
        bValue2 = (byte) (bValue2 << 4);
        bValue2 |= (byte) (index % 10);

        return HSMUtil.buildVarLengthParam(new byte[]{format, bValue1,
                bValue2});
    }

    public int parseByteArray(byte[] response, int offset) {
        HSMUtil.getLengthOfVarField(response, new MyInteger(offset));

        if (format != response[offset + 1]) {
            index = -1;
            return -1;
        }

        index = response[offset + 2] >> 4;
        index *= 1000;
        index += (response[offset + 2] & 0x0F) * 100;

        index += (response[offset + 3] >> 4) * 10;
        index += (response[offset + 3] & 0x0F);

        return 1;
    }
}
