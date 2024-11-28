package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;


public class KeySpec14 extends KeySpecifier {

    byte[] data;

    public KeySpec14(byte[] data) {
        format = 0x14;
        this.data = data;
    }

    public KeySpec14(byte[] response, int offset) {
        parseByteArray(response, offset);
    }

    public byte[] getByteArray() {
        byte[] b = new byte[1 + data.length];
        b[0] = format;
        for (int i = 0; i < b.length; i++)
            b[i + 1] = data[i];

        return HSMUtil.buildVarLengthParam(b);
    }

    public int parseByteArray(byte[] response, int offset) {
        int len = HSMUtil.getLengthOfVarField(response, new MyInteger(offset));

        if (format != response[offset + 1]) {
            data = null;
            return -1;
        }

        for (int i = 0; i < len; i++)
            data[i] = response[offset + 2 + i];

        return 1;
    }
}
