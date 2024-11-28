package vaulsys.security.hsm.eracom.zka;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

public class ZKA_MAC_Gen_Resp {
    int returnCode;
    byte[] mac;
    byte[] rand;

    public ZKA_MAC_Gen_Resp(byte[] result, int offset) {
        if ((returnCode = result[offset++]) != 0) {
            mac = null;
            rand = null;
            return;
        }

        MyInteger ofset = new MyInteger(offset);
        int len = HSMUtil.getLengthOfVarField(result, ofset);
        offset = ofset.value;

        mac = new byte[len];
        for (int i = 0; i < len; i++)
            mac[i] = result[offset++];

        rand = new byte[16];
        for (int i = 0; i < 16; i++)
            rand[i] = result[offset++];
    }
}
