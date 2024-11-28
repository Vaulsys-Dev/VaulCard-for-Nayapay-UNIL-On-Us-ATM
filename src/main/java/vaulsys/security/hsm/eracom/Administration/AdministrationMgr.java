package vaulsys.security.hsm.eracom.Administration;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.base.HSMUtil;

public class AdministrationMgr {

    public static byte[] getKVC(boolean FM, int keyType, int index) {

        int i = 0;
        byte[] params = new byte[1 + 2 + 2];
        String strKeyType, strIndex;

        if (FM)
            params[i++] = 0x00; //Get details on specified Key
        else
            params[i++] = 0x01; //Get details on next Key

        strKeyType = Integer.toHexString(keyType);
        byte[] temp = HSMUtil.stringToHex(strKeyType);
        if (temp.length == 1)
            params[i++] = 0;
        for (int j = 0; j < temp.length; j++)
            params[i++] = temp[j];

        //TODO: KVC Algorithm (not applicable to the PHW ?
        params[i++] = 0x00;
        params[i++] = 0x00;


        strIndex = Integer.toHexString(index);
        byte[] temp2 = HSMUtil.stringToHex(strIndex);
        if (temp2.length == 1)
            params[i++] = 0;
        for (int j = 0; j < temp2.length; j++)
            params[i++] = temp2[j];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0xBF, (byte) 0x29},
                        params);

        func.sendRequest();
        return func.response;
    }
}
