package vaulsys.security.hsm.eracom.KMManagement;

import vaulsys.security.hsm.eracom.HSMFuncs;


public class KMManagement {
    public static byte HSM_Establish_KM() {
        HSMFuncs func = new HSMFuncs(new byte[]{(byte) 0x11}, null);
        func.sendRequest();

        return func.response[1];
    }

    public static HSMKMMigrate HSM_KM_Migrate(int i, int n, int KeySpec) {
        /*
        check the correct value
     */

        int var = KeySpec & 0xE0;
        int length = 0;

        if (var < 0x80) //the first bit is zero
            length = 1;
        else if (var < 0xC0)
            length = 2;


        byte[] parameters = new byte[2 + length];

        //add value to parameters

        HSMFuncs func = new HSMFuncs(new byte[]{(byte) 0x12}, parameters);
        func.sendRequest();
        HSMKMMigrate resp = new HSMKMMigrate(func.response, 1);
        return resp;

    }

    public static byte HSM_Erase_Old_KM() {
        HSMFuncs func = new HSMFuncs(new byte[]{(byte) 0x13}, null);
        func.sendRequest();
        return func.response[1];
    }

}


