package vaulsys.security.hsm.eracom.MAC;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.security.hsm.eracom.base.Padding;

public class MAC {
    // TODO: check block algorithm


    private static String b2s(byte[] key) {
        String result = "";
        for (int i = 0; i < key.length; i++) {
            result += key[i] + " ";
        }
        return result;
    }


    public static byte[] generateMAC(KeySpecifier keySpec, byte[] data, int algorithm, byte[] IV, int macLength, int padding) {

        byte[] d = new byte[8];

        int i;

        for (i = 0; i < (data.length / 8); i++) {
            for (int j = 0; j < 8; j++)
                d[j] = data[i * 8 + j];
            IV = MAC_Gen_Update(keySpec, d, algorithm, IV);
            System.out.println(b2s(IV) + "\n");
        }


        d = new byte[data.length - i * 8];
        for (int j = 0; i * 8 + j < data.length; j++)
            d[j] = data[i * 8 + j];

        return MAC_Gen_Final(keySpec, d, algorithm, IV, macLength, padding);

        // IV = MAC_Gen_Update( keySpec, data, algorithm, IV );
        // return MAC_Gen_Final( keySpec, data, algorithm, IV, macLength, padding );
    }

    public static boolean verifyMAC(KeySpecifier keySpec, byte[] data, int algorithm, byte[] IV, byte[] MAC, int padding) {
        byte[] d = new byte[8];

        int i;

        for (i = 0; i < (data.length / 8); i++) {
            for (int j = 0; j < 8; j++)
                d[j] = data[i * 8 + j];
            IV = MAC_Gen_Update(keySpec, d, algorithm, IV);
        }

        // i--;
        d = new byte[data.length - i * 8];
        for (int j = 0; i * 8 + j < data.length; j++)
            d[j] = data[i * 8 + j];

        return false;
//		return MAC_Ver_Final(keySpec, d, algorithm, IV, MAC, padding);

        // IV = MAC_Gen_Update( keySpec, data, algorithm, IV );
        // return MAC_Ver_Final( keySpec, data, algorithm, IV, MAC, padding );
    }

    public static byte[] MAC_Ver_Final(KeySpecifier keySpec, byte[] data, int algorithm, byte[] IV, byte[] mac, int padding) {

        data = HSMUtil.buildVarLengthParam(data);

        byte[] key = keySpec.getByteArray();

        mac = HSMUtil.buildVarLengthParam(mac);

        byte[] params = new byte[1 + mac.length + key.length + IV.length + data.length];
        int index = 0;

        if (padding == Padding.Zero_Padding)
            params[index++] = (byte) (0x00 | ((byte) algorithm));
        else
            params[index++] = (byte) (0x10 | ((byte) algorithm));

        System.arraycopy(IV, 0, params, index, IV.length);
        index += IV.length;

        System.arraycopy(key, 0, params, index, key.length);
        index += key.length;

        System.arraycopy(data, 0, params, index, mac.length);
        index += mac.length;

        System.arraycopy(data, 0, params, index, data.length);
        index += data.length;

        return params;
    }

    public static byte[] MAC_Gen_Final(KeySpecifier keySpec, byte[] data, int algorithm, byte[] IV, int macLength, int padding) {
        data = HSMUtil.buildVarLengthParam(data);

        byte[] key = keySpec.getByteArray();

        byte[] params = new byte[1 + 1 + key.length + IV.length + data.length];
        int index = 0;

        if (padding == Padding.Zero_Padding)
            params[index++] = (byte) (0x00 | ((byte) algorithm));
        else
            params[index++] = (byte) (0x10 | ((byte) algorithm));

        params[index++] = (byte) macLength;


        System.arraycopy(IV, 0, params, index, IV.length);
        index += IV.length;

        System.arraycopy(key, 0, params, index, key.length);
        index += key.length;

        System.arraycopy(data, 0, params, index, data.length);
        index += data.length;

//		byte[] funcCode = new byte[] { (byte) 0xEE, (byte) 0x07, (byte) 0x01, (byte) 0x00 };
//		HSMFuncs func = new HSMFuncs(funcCode, params);
//
//		func.sendRequest();
//
//		MyInteger ofset = new MyInteger(7);
//		int macLen = HSMUtil.getLengthOfVarField(func.response, ofset);
//		int offset = ofset.value;
//
//		byte[] mac = new byte[macLen];
//		System.arraycopy(func.response, offset, mac, 0, macLen);
//
//		return mac;

        return params;
    }


    public static byte[] MAC_Gen_Update(KeySpecifier keySpec, byte[] data, int algorithm, byte[] IV) {

        data = HSMUtil.buildVarLengthParam(data);

        byte[] key = keySpec.getByteArray();

        byte[] params = new byte[1 + key.length + IV.length + data.length];
        int index = 0;

        params[index++] = (byte) algorithm;

        System.arraycopy(IV, 0, params, index, IV.length);
        index += IV.length;

        System.arraycopy(key, 0, params, index, key.length);
        index += key.length;

        System.arraycopy(data, 0, params, index, data.length);

//		byte[] funcCode = new byte[] { (byte) 0xEE, (byte) 0x07, (byte) 0x00, (byte) 0x00 };
//		        
//		        
//		 HSMFuncs func = new HSMFuncs( funcCode, params);
//		
//		 byte[] sentReq = func.sendRequest();
//		
//		 int offset = 7;
//		 byte[] mac = new byte[ 8 ];
//		 for( int i=0; i<mac.length; i++ )
//		 mac[ i ] = func.response[ i + offset ];
//		            
//		 return mac;

        return params;
    }

    public static byte[] KTM_MAC_Gen(byte[] data, int KTMIndex) {

        if (data.length % 8 != 0) {
            data = Padding.zeroPadding(data, 8 - (data.length % 8));
        }
        // data = HSMUtil.buildVarLengthParam(data);

        byte[] params = new byte[1 + 1 + data.length];
        int index = 0;

        params[index++] = (byte) (data.length / 8);

        params[index++] = (byte) (((KTMIndex / 10) << 4) | (KTMIndex % 10)); // BCD
        for (int i = 0; i < data.length; i++)
            params[index++] = data[i];

        byte[] funcCode = new byte[]{(byte) 0x73};
        HSMFuncs func = new HSMFuncs(funcCode, params);

        func.sendRequest();

        int offset = 2;
        byte[] resp = new byte[4];

        for (int i = 0; i < resp.length; i++)
            resp[i] = func.response[i + offset];

        return resp;
    }
}
