package vaulsys.security.hsm.eracom.PINIssuance;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.base.HSMUtil;


public class PINIssuance {
    public static byte[] PIN_Generate(int pinLength, int pinBlockFormat,
                                      String ANB, KeySpecifier keySpec) {

        byte[] anb = HSMUtil.stringToHex(ANB);

        byte[] key = keySpec.getByteArray();

        byte[] params = new byte[1 + 1 + anb.length + key.length];
        int index = 0;

        params[index++] = (byte) pinLength;

        params[index++] = (byte) pinBlockFormat;

        for (int i = 0; i < anb.length; i++)
            params[index++] = anb[i];

        for (int i = 0; i < key.length; i++)
            params[index++] = key[i];

        byte[] funcCode = new byte[]{(byte) 0xEE, (byte) 0x0E, (byte) 0x04, (byte) 0x00};
        HSMFuncs func = new HSMFuncs(funcCode, params);

        func.sendRequest();

        int offset = 4;
        byte[] ePPK_PIN = new byte[8];
        for (int i = 0; i < ePPK_PIN.length; i++)
            ePPK_PIN[i] = func.response[i + offset];

        return ePPK_PIN;
    }
}
