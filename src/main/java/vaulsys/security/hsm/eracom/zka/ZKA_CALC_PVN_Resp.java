package vaulsys.security.hsm.eracom.zka;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class ZKA_CALC_PVN_Resp {
    public byte[] PVN;
    public byte PINLEN;

    public ZKA_CALC_PVN_Resp(byte[] result, int offset) {

        if (result[offset++] != 0)
            return;
        MyInteger ofset = new MyInteger(offset);
        offset = ofset.value;
        int PVNLength = HSMUtil.getLengthOfVarField(result, ofset);
        PVN = new byte[PVNLength];

        for (int i = 0; i < PVN.length; i++)
            PVN[i] = result[offset++];

        PINLEN = result[offset++];
    }
}
