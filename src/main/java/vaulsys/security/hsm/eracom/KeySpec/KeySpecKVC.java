package vaulsys.security.hsm.eracom.KeySpec;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;


public class KeySpecKVC {
    public KeySpecifier keySpec;
    public byte[] kvc;

    public KeySpecKVC(byte[] response, MyInteger offset, int kvcLength) {

        int length = HSMUtil.getLengthOfVarField(response, offset);
        keySpec = KeyHandler.getKeySpec(response, --offset.value);

        offset.value += length + 1;

        kvc = new byte[kvcLength];
        for (int i = 0; i < kvcLength; i++)
            kvc[i] = response[offset.value++];
    }

    @Override
    public String toString() {
        return "Host Key:\n" + keySpec +
                "; KVC = " + HSMUtil.hexToString(kvc);
    }

}
