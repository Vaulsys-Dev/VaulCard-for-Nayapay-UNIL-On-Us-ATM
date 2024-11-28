package vaulsys.security.hsm.eracom.PINManagement;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.base.HSMUtil;


public class PINManagement {
    // TODO: key spciefier classs as input

    public static byte[] Clr_PIN_Encrypt(String PIN, String AccountNumberBlock,
                                         int keyIndex) {

        byte[] pin = HSMUtil.stringToHex(PIN);
        pin = HSMUtil.buildVarLengthParam(pin);

        byte[] anb = HSMUtil.stringToHex(AccountNumberBlock);

        //TODO: params length
        byte[] params = new byte[1 + pin.length + anb.length + 3];
        int index = 0;

        params[index++] = (byte) PIN.length();

        for (int i = 0; i < pin.length; i++)
            params[index++] = pin[i];

        for (int i = 0; i < anb.length; i++)
            params[index++] = anb[i];

        //TODO: to be created automatically in KeySpec class
        params[index++] = (byte) 0x02; // var-length
        params[index++] = (byte) 0x00;
        params[index++] = (byte) keyIndex;

/*        HSMFuncs func = 
            new HSMFuncs(new byte[] { (byte)0xEE, (byte)0x06, (byte)0x00 }, 
                         params);

        func.sendRequest();
        int offset = 3;
        if (func.response[offset++] != 0)
            return null;

        byte[] resp = new byte[func.response.length - 4];
        for (int i = 0; i < resp.length; i++)
            resp[i] = func.response[offset++];

        return resp;
*/
        return params;
    }

    public static byte[] Generate_KM_Encrypted_PIN(int pinLength,
                                                   String AccountNumberBlock) {

        byte[] anb = HSMUtil.stringToHex(AccountNumberBlock);

        byte[] params = new byte[1 + anb.length];
        int index = 0;

        params[index++] = (byte) pinLength;

        for (int i = 0; i < anb.length; i++)
            params[index++] = anb[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x06, (byte) 0x40,
                        (byte) 0x00}, params);

        func.sendRequest();
        int offset = 3;

        return func.response;
    }

    public static byte[] Translate_PIN(byte[] inputPinBlock,
                                       KeySpecifier inputKeySpec, byte PFi,
                                       String AccountNumberBlock, byte PFo, KeySpecifier outputKeySpec) {
        byte[] anb = HSMUtil.stringToHex(AccountNumberBlock);

        byte[] inputKey = inputKeySpec.getByteArray();
        byte[] outputKey = outputKeySpec.getByteArray();

        //TODO: params length
        byte[] params = new byte[inputPinBlock.length + inputKey.length + outputKey.length + anb.length + 2];
        int index = 0;

        System.arraycopy(inputPinBlock, 0, params, index, inputPinBlock.length);
        index += inputPinBlock.length;

        System.arraycopy(inputKey, 0, params, index, inputKey.length);
        index += inputKey.length;

        params[index++] = PFi;

        System.arraycopy(anb, 0, params, index, anb.length);
        index += anb.length;

        params[index++] = PFo;

        System.arraycopy(outputKey, 0, params, index, outputKey.length);
        index += outputKey.length;

        return params;
    }
}
