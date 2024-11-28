package vaulsys.security.hsm.eracom.EMV;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;


public class EMV {
    public static byte[] Gen_Random(int length) {
        byte[] params = new byte[1];
        int index = 0;

        params[index++] = (byte) length;

        byte[] funcCode = new byte[]{(byte) 0xEE, (byte) 0x00, (byte) 0x02, (byte) 0x00};
        HSMFuncs func = new HSMFuncs(funcCode, params);

        func.sendRequest();

        int offset = 3;

        if (func.response[offset++] != 0)
            return null;

        MyInteger ofset = new MyInteger(offset);
        int len2 = HSMUtil.getLengthOfVarField(func.response, ofset);
        offset = ofset.value;

        byte[] random = new byte[len2];
        for (int i = 0; i < random.length; i++)
            random[i] = func.response[i + offset];

        return random;
    }
}
