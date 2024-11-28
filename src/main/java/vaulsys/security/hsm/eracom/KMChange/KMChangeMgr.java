package vaulsys.security.hsm.eracom.KMChange;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;

public class KMChangeMgr {

    public static byte[] establishKM() {
        HSMFuncs func = new HSMFuncs(new byte[]{0x11}, null);
        func.sendRequest();
        return func.response;
    }

    public static byte[] kmMigrate(byte kmVariant, byte numberKeys, KeySpecifier KSpec) {

        int index = 0;
        byte[] KSpecArr = KSpec.getByteArray();
        byte[] params = new byte[1 + 1 + KSpecArr.length];

        params[index++] = (byte) kmVariant;
        params[index++] = (byte) numberKeys;
        for (int i = 0; i < KSpecArr.length; i++)
            params[index++] = KSpecArr[i];

        HSMFuncs func = new HSMFuncs(new byte[]{0x12}, params);
        func.sendRequest();
        return func.response;

    }

    public static byte[] eraseOldKM() {
        HSMFuncs func = new HSMFuncs(new byte[]{0x13}, null);
        func.sendRequest();
        return func.response;


    }

}
