package vaulsys.security.hsm.eracom.Transfer;

import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;

public class TransferManagement {

    public static /*HsmRetrieveKey*/byte[] Retrieve_Key(KeySpecifier KXTSpec) {

        int index = 0;
        byte[] kxt = KXTSpec.getByteArray();
        byte[] params = new byte[kxt.length];

//        for (int i = 0; i < kxt.length; i++)
//            params[index++] = kxt[i];

        System.arraycopy(kxt, 0, params, 0, kxt.length);

        /* HSMFuncs func =
                    new HSMFuncs(new byte[] { (byte)0x21, (byte)0x00, (byte)0x00 },
                                 params);

                func.sendRequest();
                HsmRetrieveKey resp = new HsmRetrieveKey(func.response, 2);
                return resp;
        */
        return params;
    }

    public static byte[] Store_Key(KeySpecifier KXTSpec, byte keyType,
                                   KeySpecifier keySpec, byte[] kvc) {

        int index = 0;
        byte[] kxt = KXTSpec.getByteArray();
        byte[] key = keySpec.getByteArray();

        byte[] params = new byte[kxt.length + 1 + key.length + 3];

        for (int i = 0; i < kxt.length; i++)
            params[index++] = kxt[i];

        params[index++] = keyType;

        for (int i = 0; i < key.length; i++)
            params[index++] = key[i];

        for (int i = 0; i < kvc.length; i++)
            params[index++] = kvc[i];

/*
        HSMFuncs func = 
            new HSMFuncs(new byte[] { (byte)0x22, (byte)0x00, (byte)0x00 }, 
                         params);

        func.sendRequest();
        return func.response;
*/
        return params;
    }


    public static byte[] KEY_IMPORT(KeySpecifier KIRSpec, int keyType, int mode, KeySpecifier keySpec) {
        int index = 0;
        byte[] kir = KIRSpec.getByteArray();
        byte[] key = keySpec.getByteArray();
        byte[] params = new byte[kir.length + 2 + key.length];

        System.arraycopy(kir, 0, params, index, kir.length);
        index += kir.length + 1;
        params[index++] = (byte) keyType;
        params[index++] = (byte) mode;
        System.arraycopy(key, 0, params, index, key.length);
        return params;
    }

}
