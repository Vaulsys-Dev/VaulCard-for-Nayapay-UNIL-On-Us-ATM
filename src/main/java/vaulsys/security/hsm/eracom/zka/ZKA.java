package vaulsys.security.hsm.eracom.zka;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.KeySpec.KeyHandler;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.base.*;

public class ZKA {
    public ZKA() {
    }

    public static KeySpecifier ZKA_IMPORT_MK(byte[] eKTK, KeySpecifier KTKSpec, int encMode,
                                             int keyType, int icm, byte[] icv) {
        int index = 0;
        byte[] key = KTKSpec.getByteArray();
        icv = HSMUtil.buildVarLengthParam(icv);
        byte[] params = new byte[eKTK.length + key.length + 1 + 1 + 1 + icv.length];

        for (int i = 0; i < eKTK.length; i++)
            params[index++] = eKTK[i];

        for (int i = 0; i < key.length; i++)
            params[index++] = key[i];

        if (encMode == CryptoMode.ECB)
            params[index++] = 0x00;
        else if (encMode == CryptoMode.CBC)
            params[index++] = 0x01;

        if (keyType == KeyTypes.KGK)
            params[index++] = 0x10;
        else if (keyType == KeyTypes.KK_BLZ)
            params[index++] = 0x11;
        else if (keyType == KeyTypes.MK)
            params[index++] = 0x12;

        if (icm == ICM.NO_CHECK)
            params[index++] = 0x0;
        else if (icm == ICM.STANDARD_KVC)
            params[index++] = 0x1;
        else if (icm == ICM.MDC_2)
            params[index++] = 0x2;

        for (int i = 0; i < icv.length; i++)
            params[index++] = icv[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x02, (byte) 0x10,
                        (byte) 0x00}, params);

        func.sendRequest();

        int offset = 3;
        if (func.response[offset++] != 0)
            return null;

        return KeyHandler.getKeySpec(func.response, offset);
    }

    public static ZKA_PIN_TRANS_Resp ZKA_PIN_TRANS(byte[] ePPKi, KeySpecifier PPKiSpec,
                                                   int PFi, String ANB, int PFo, KeySpecifier MKSpec) {

        int index = 0;
        byte[] PPK = PPKiSpec.getByteArray();
        byte[] anb = HSMUtil.stringToHex(ANB);
        byte[] MK = MKSpec.getByteArray();
        byte[] params = new byte[ePPKi.length + PPK.length + 1 + anb.length + 1 + MK.length];

        for (int i = 0; i < ePPKi.length; i++)
            params[index++] = ePPKi[i];

        for (int i = 0; i < PPK.length; i++)
            params[index++] = PPK[i];

        params[index++] = (byte) PFi;

        for (int i = 0; i < anb.length; i++)
            params[index++] = anb[i];

        params[index++] = (byte) PFo;

        for (int i = 0; i < MK.length; i++)
            params[index++] = MK[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x10,
                        (byte) 0x00}, params);

        func.sendRequest();

        int offset = 3;
        ZKA_PIN_TRANS_Resp response = new ZKA_PIN_TRANS_Resp(func.response, offset);
        return response;

//        return func.response;
    }

    public static byte[] ZKA_PIN_VER(byte[] ePPK, KeySpecifier PPKSpec,
                                     int PF, String ANB, KeySpecifier KK_BLZSpec,
                                     String accNumStr, int CSN, int expYear, int PVNType, byte[] PVN) {
        int index = 0;
        byte[] PPK = PPKSpec.getByteArray();
        byte[] anb = HSMUtil.stringToHex(ANB);
        byte[] KK_BLZ = KK_BLZSpec.getByteArray();
        byte[] accNum = HSMUtil.stringToBCD(accNumStr);
        byte csn = (HSMUtil.intToBCD(CSN))[0]; //!!!!!!
        byte ExpYear = (HSMUtil.intToBCD(expYear))[0]; //!!!!!!
        byte[] pvn = HSMUtil.buildVarLengthParam(PVN); //HSMUtil.intToBCD( Integer.parseInt(PVN) ); //HSMUtil.buildVarLengthParam( HSMUtil.stringToHex(PVN) );

        byte[] params = new byte[ePPK.length + PPK.length + 1 + anb.length +
                KK_BLZ.length + accNum.length + 1 + 1 + 1 + pvn.length];

        for (int i = 0; i < ePPK.length; i++)
            params[index++] = ePPK[i];

        for (int i = 0; i < PPK.length; i++)
            params[index++] = PPK[i];

        params[index++] = (byte) PF;

        for (int i = 0; i < anb.length; i++)
            params[index++] = anb[i];

        for (int i = 0; i < KK_BLZ.length; i++)
            params[index++] = KK_BLZ[i];

        for (int i = 0; i < accNum.length; i++)
            params[index++] = accNum[i];

        params[index++] = csn;

        params[index++] = ExpYear;

        params[index++] = (byte) PVNType;
        /*
                if( pvn.length == 1 ){
                    params[index++] = 0x00;
                    params[index++] = pvn[0];
                }
        */
        for (int i = 0; i < pvn.length; i++)
            params[index++] = pvn[i];


        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x11,
                        (byte) 0x00}, params);
        int offset = 3;
        if (func.response[offset++] != 0)
            return null;

        func.sendRequest();

        return func.response;
    }

    public static ZKA_CALC_PVN_Resp ZKA_CALC_PVN(byte[] ePPK, KeySpecifier PPKSpec,
                                                 int PF, String ANB, KeySpecifier KK_BLZSpec,
                                                 String accNumStr, int CSN, int expYear, int PVNType) {
        int index = 0;
        byte[] PPK = PPKSpec.getByteArray();
        byte[] anb = HSMUtil.stringToHex(ANB);
        byte[] KK_BLZ = KK_BLZSpec.getByteArray();
        byte[] accNum = HSMUtil.stringToBCD(accNumStr);
        byte csn = (HSMUtil.intToBCD(CSN))[0]; //!!!!!!
        byte ExpYear = (HSMUtil.intToBCD(expYear))[0]; //!!!!!!

        byte[] params = new byte[ePPK.length + PPK.length + 1 + anb.length +
                KK_BLZ.length + accNum.length + 1 + 1 + 1];

        for (int i = 0; i < ePPK.length; i++)
            params[index++] = ePPK[i];

        for (int i = 0; i < PPK.length; i++)
            params[index++] = PPK[i];

        params[index++] = (byte) PF;

        for (int i = 0; i < anb.length; i++)
            params[index++] = anb[i];

        for (int i = 0; i < KK_BLZ.length; i++)
            params[index++] = KK_BLZ[i];

        for (int i = 0; i < accNum.length; i++)
            params[index++] = accNum[i];

        params[index++] = csn;

        params[index++] = ExpYear;

        params[index++] = (byte) PVNType;

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x12,
                        (byte) 0x00}, params);

        func.sendRequest();

        int offset = 3;
        ZKA_CALC_PVN_Resp response = new ZKA_CALC_PVN_Resp(func.response, offset);
        return response;

    }

    public static byte[] ZKA_PIN_TRANS_1(byte[] ePPKi, KeySpecifier PPKiSpec,
                                         int PFi, String ANB, int PFo, KeySpecifier MK2_Spec1) {
        int index = 0;
        byte[] PPKi = PPKiSpec.getByteArray();
        byte[] anb = HSMUtil.stringToHex(ANB);
        byte[] MK2 = MK2_Spec1.getByteArray();

        byte[] params = new byte[ePPKi.length + PPKi.length + 1 + anb.length +
                1 + MK2.length];

        for (int i = 0; i < ePPKi.length; i++)
            params[index++] = ePPKi[i];

        for (int i = 0; i < PPKi.length; i++)
            params[index++] = PPKi[i];

        params[index++] = (byte) PFi;

        for (int i = 0; i < anb.length; i++)
            params[index++] = anb[i];

        params[index++] = (byte) PFo;

        for (int i = 0; i < MK2.length; i++)
            params[index++] = MK2[i];


        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x13,
                        (byte) 0x00}, params);

        func.sendRequest();

        return func.response;
    }

    public static ZKA_MAC_Gen_Resp ZKA_MAC_GEN(int Alg, int MACLength, byte[] icd, KeySpecifier MK_Spec,
                                               byte[] Data, int c) {
        int index = 0;
        byte[] MK = MK_Spec.getByteArray();

        if ((Data.length % 8) != 0)
            Data = Padding.zeroPadding(Data, 8 - (Data.length % 8));

        Data = HSMUtil.buildVarLengthParam(Data);

        byte[] params = new byte[1 + 1 + icd.length + MK.length +
                Data.length + 2];

        if (Alg == AlgorithmQualifier.Retail_MAC_Method)
            params[index++] = 0x00;
        else if (Alg == AlgorithmQualifier.Triple_DES_CBC_Method)
            params[index++] = 0x01;

        params[index++] = (byte) MACLength;

        for (int i = 0; i < icd.length; i++)
            params[index++] = icd[i];

        for (int i = 0; i < MK.length; i++)
            params[index++] = MK[i];

        for (int i = 0; i < Data.length; i++)
            params[index++] = Data[i];

        params[index++] = (byte) (c / 256);
        params[index++] = (byte) (c % 256);
//            params[ index++ ] = (byte) c;

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x07, (byte) 0x10,
                        (byte) 0x00}, params);

        func.sendRequest();

        return new ZKA_MAC_Gen_Resp(func.response, 3);
    }

    public static byte[] ZKA_MAC_GEN_1(int Alg, int MACLength, byte[] icd, KeySpecifier MK2_Spec1,
                                       byte[] Data, int offset1, int offset2, int offset3) {
        int index = 0;
        byte[] MK2 = MK2_Spec1.getByteArray();
        Data = HSMUtil.buildVarLengthParam(Data);

        byte[] params = new byte[1 + 1 + icd.length +
                MK2.length + Data.length +
                2 + 2 + 2];

        if (Alg == AlgorithmQualifier.Retail_MAC_Method)
            params[index++] = 0x00;
        else if (Alg == AlgorithmQualifier.Triple_DES_CBC_Method)
            params[index++] = 0x01;

        params[index++] = (byte) MACLength;

        for (int i = 0; i < icd.length; i++)
            params[index++] = icd[i];

        for (int i = 0; i < MK2.length; i++)
            params[index++] = MK2[i];

        for (int i = 0; i < Data.length; i++)
            params[index++] = Data[i];

        params[index++] = (byte) (offset1 / 256);
        params[index++] = (byte) (offset1 % 256);

        params[index++] = (byte) (offset2 / 256);
        params[index++] = (byte) (offset2 % 256);

        params[index++] = (byte) (offset3 / 256);
        params[index++] = (byte) (offset3 % 256);

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x07, (byte) 0x11,
                        (byte) 0x00}, params);

        func.sendRequest();

        return func.response;
    }
}

