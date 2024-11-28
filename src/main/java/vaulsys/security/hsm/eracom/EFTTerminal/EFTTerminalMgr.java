package vaulsys.security.hsm.eracom.EFTTerminal;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.KeySpec.SessionKeySpecKVC;
import vaulsys.security.hsm.eracom.base.CryptoMode;
import vaulsys.security.hsm.eracom.base.KeyTypes;
import vaulsys.util.MyInteger;


public class EFTTerminalMgr {
    //    public static KeyMailerResp Key_Mailer( )
    //TODO: key spec is returned, 

    public static byte[] IT_Key_Gen(KeySpecifier keySpec,
                                    int[] arKeys, int mode) {


        int index = 0;
        byte[] key = keySpec.getByteArray();
        byte[] params = new byte[key.length + 2];

        System.arraycopy(key, 0, params, index, key.length);
        index += key.length;

        short bitmap = 0x0000;
        for (int i = 0; i < arKeys.length; i++) {
            switch (arKeys[i]) {
                case KeyTypes.S_Len_Data__DPK:
                    bitmap |= 0x0001;
                    break;
                case KeyTypes.S_Len_PIN_E__PPK:
                    bitmap |= 0x0002;
                    break;
                case KeyTypes.S_Len_MAC__MPK:
                    bitmap |= 0x0004;
                    break;
                case KeyTypes.S_Len_Terminal_Master__KTM:
                    bitmap |= 0x0008;
                    break;
                case KeyTypes.D_Len_Data__DPK:
                    bitmap |= 0x0100;
                    break;
                case KeyTypes.D_Len_PIN_E__PPK:
                    bitmap |= 0x0200;
                    break;
                case KeyTypes.D_Len_MAC__MPK:
                    bitmap |= 0x0400;
                    break;
                case KeyTypes.D_Len_Terminal_Master__KTM:
                    bitmap |= 0x0800;
                    break;
            }
        }

        if (mode == CryptoMode.CBC)
            bitmap |= 0x1000;

        params[index++] = (byte) (bitmap / 256);
        params[index++] = (byte) (bitmap % 256);

        return params;

//        HSMFuncs func = 
//            new HSMFuncs(new byte[] { (byte)0xEE, (byte)0x04, (byte)0x00, 
//                                      (byte)0x00 }, params);
//
//        func.sendRequest();
//
//        MyInteger offset = new MyInteger(3);
//        if (func.response[offset.value++] != 0)
//            return null;
//
//        int count = func.response[offset.value++];
//        SessionKeySpecKVC[] resp = new SessionKeySpecKVC[count];
//
//        for (int i = 0; i < count; i++)
//            resp[i] = new SessionKeySpecKVC(func.response, offset, 3);
//
//        return resp;
    }

    public static SessionKeySpecKVC[] NT_KEY_GEN(int[] arKeys, int mode,
                                                 KeySpecifier[] keySpec) {

        int index = 0;
        byte[][] key = new byte[keySpec.length][];
        int keysLen = 0;

        for (int i = 0; i < keySpec.length; i++) {
            key[i] = keySpec[i].getByteArray();
            keysLen += key[i].length;
        }


        byte[] params = new byte[2 + keysLen];

        short bitmap = 0x0000;
        for (int i = 0; i < arKeys.length; i++) {
            switch (arKeys[i]) {
                case KeyTypes.S_Len_Data__DPK:
                    bitmap |= 0x0001;
                    break;
                case KeyTypes.S_Len_PIN_E__PPK:
                    bitmap |= 0x0002;
                    break;
                case KeyTypes.S_Len_MAC__MPK:
                    bitmap |= 0x0004;
                    break;
                case KeyTypes.S_Len_Terminal_Master__KTM:
                    bitmap |= 0x0008;
                    break;
                case KeyTypes.D_Len_Data__DPK:
                    bitmap |= 0x0100;
                    break;
                case KeyTypes.D_Len_PIN_E__PPK:
                    bitmap |= 0x0200;
                    break;
                case KeyTypes.D_Len_MAC__MPK:
                    bitmap |= 0x0400;
                    break;
                case KeyTypes.D_Len_Terminal_Master__KTM:
                    bitmap |= 0x0800;
                    break;
            }
        }

        if (mode == CryptoMode.CBC)
            bitmap |= 0x1000;

        params[index++] = (byte) (bitmap / 256);
        params[index++] = (byte) (bitmap % 256);

        for (int j = 0; j < key.length; j++) {
            for (int i = 0; i < key[j].length; i++)
                params[index++] = key[j][i];
        }

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x04, (byte) 0x01,
                        (byte) 0x00}, params);

        func.sendRequest();

        MyInteger offset = new MyInteger(3);
        if (func.response[offset.value++] != 0)
            return null;

        int count = func.response[offset.value++];
        SessionKeySpecKVC[] resp = new SessionKeySpecKVC[count];

        for (int i = 0; i < count; i++)
            resp[i] = new SessionKeySpecKVC(func.response, offset, 3);

        return resp;


    }

    public static byte[] TERM_VER_2(KeySpecifier ktmSpec, byte[] secNo,
                                    byte[] logonData) {

        int index = 0;
        byte[] ktm = ktmSpec.getByteArray();
        byte[] params = new byte[ktm.length + 8 + 8];

        for (int i = 0; i < ktm.length; i++)
            params[index++] = ktm[i];
        for (int i = 0; i < 8; i++)
            params[index++] = secNo[i];
        for (int i = 0; i < 8; i++)
            params[index++] = logonData[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x04, (byte) 0x06,
                        (byte) 0x00}, params);

        func.sendRequest();
        return func.response;


    }

    public static byte[] BDKGEN(byte keyLength) {

        byte[] params = new byte[1];
        params[0] = keyLength;
        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x04, (byte) 0x08,
                        (byte) 0x00}, params);

        func.sendRequest();
        return func.response;

    }

}
