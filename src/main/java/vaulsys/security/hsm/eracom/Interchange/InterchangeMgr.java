package vaulsys.security.hsm.eracom.Interchange;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.KeySpec.SessionKeySpecKVC;
import vaulsys.security.hsm.eracom.base.CryptoMode;
import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.security.hsm.eracom.base.KeyTypes;
import vaulsys.util.MyInteger;

import java.util.ArrayList;
import java.util.List;

public class InterchangeMgr {

    public static byte[] II_KEY_GEN(KeySpecifier kisSpec,
                                    int[] arKeys, int mode) {
        int index = 0;
        byte[] key = kisSpec.getByteArray();
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
                case KeyTypes.S_Len_Key_Enc__KIS:
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
                case KeyTypes.D_Len_Key_Enc__KIS:
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
//            new HSMFuncs(new byte[] { (byte)0xEE, (byte)0x04, (byte)0x02, 
//                                      (byte)0x00 }, params);
//
//        func.sendRequest();
//
//        MyInteger offset = new MyInteger(3);
//
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

    public static SessionKeySpecKVC[] NI_KEY_GEN(int[] arKeys, int mode,
                                                 KeySpecifier[] keysSpec) {

        int index = 0;
        byte[][] keys = new byte[keysSpec.length][];
        int len = 0;
        for (int i = 0; i < keysSpec.length; i++) {
            keys[i] = keysSpec[i].getByteArray();
            len += keys[i].length;
        }

        byte[] params = new byte[len + 2];

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

        for (int j = 0; j < keys.length; j++)
            for (int i = 0; i < keys[j].length; i++)
                params[index++] = keys[j][i];


        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x04, (byte) 0x04,
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

    public static byte[] II_KEY_RCV(KeySpecifier kirSpec, int[] arKeys,
                                    int mode, List<byte[]> encSessionKey) {

        int index = 0;
        byte[] KIRArr = kirSpec.getByteArray();

        int sessionLen = encSessionKey.size();
        byte[][] sessionArr = new byte[sessionLen][];
        int sessionArrLen = 0;
        for (int i = 0; i < sessionLen; i++) {
            sessionArr[i] =
                    HSMUtil.buildVarLengthParam((byte[]) encSessionKey.get(i));
            sessionArrLen += sessionArr[i].length;
        }

        byte[] params = new byte[KIRArr.length + 2 + sessionArrLen];

        for (int i = 0; i < KIRArr.length; i++)
            params[index++] = KIRArr[i];

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

        for (int j = 0; j < sessionArr.length; j++) {
            for (int i = 0; i < sessionArr[j].length; i++)
                params[index++] = sessionArr[j][i];
        }

        return params;

//        HSMFuncs func = 
//            new HSMFuncs(new byte[] { (byte)0xEE, (byte)0x04, (byte)0x03, 
//                                      (byte)0x00 }, params);
//
//        func.sendRequest();
        //TODO :keyspec kvc 

        /* MyInteger offset = new MyInteger(3);
        if(func.response[offset++] != 0 )
           return null;
        int count = func.response[offset++];
        KeySpecKVC[] resp = new KeySpecKVC[count];
        for (int i = 0 ; i < count ;i++)
            resp[i] = new KeySpecKVC(func.response , offset , 3); */

//        return func.response;

    }

    public static byte[] NI_KEY_RCV(int[] arKeys, int mode,
                                    KeySpecifier[] keysSpec,
                                    ArrayList<byte[]> encSessionKey) {
        int index = 0;
        int length = 0;
        byte[][] key = new byte[keysSpec.length][];
        for (int i = 0; i < keysSpec.length; i++) {
            key[i] = keysSpec[i].getByteArray();
            length += key[i].length;
        }

        int sessionLen = encSessionKey.size();
        byte[][] sessionArr = new byte[sessionLen][];
        int sessionArrLen = 0;
        for (int i = 0; i < sessionLen; i++) {
            sessionArr[i] =
                    HSMUtil.buildVarLengthParam((byte[]) encSessionKey.get(i));
            sessionArrLen += sessionArr[i].length;
        }

        byte[] params = new byte[2 + length + sessionArrLen];

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
                case KeyTypes.D_Len_Data__DPK:
                    bitmap |= 0x0100;
                    break;
                case KeyTypes.D_Len_PIN_E__PPK:
                    bitmap |= 0x0200;
                    break;
                case KeyTypes.D_Len_MAC__MPK:
                    bitmap |= 0x0400;
                    break;
            }
        }

        if (mode == CryptoMode.CBC)
            bitmap |= 0x1000;

        params[index++] = (byte) (bitmap / 256);
        params[index++] = (byte) (bitmap % 256);

        if (keysSpec.length != sessionArr.length)
            return null;

        for (int i = 0; i < keysSpec.length; i++) {
            for (int j = 0; j < key[i].length; j++)
                params[index++] = key[i][j];
            for (int j = 0; j < sessionArr[i].length; j++)
                params[index++] = sessionArr[i][j];
        }

        //        for( int i=0; i<sessionArr.length; i++)
        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x04, (byte) 0x05,
                        (byte) 0x00}, params);

        func.sendRequest();
        //TODO :keyspec kvc 

        /* MyInteger offset = new MyInteger(3);
          if(func.response[offset++] != 0 )
             return null;
          int count = func.response[offset++];
          KeySpecKVC[] resp = new KeySpecKVC[count];
          for (int i = 0 ; i < count ;i++)
              resp[i] = new KeySpecKVC(func.response , offset , 3); */

        return func.response;
    }
}