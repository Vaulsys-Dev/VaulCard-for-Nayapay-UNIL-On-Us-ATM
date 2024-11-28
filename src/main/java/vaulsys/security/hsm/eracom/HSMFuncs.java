package vaulsys.security.hsm.eracom;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.security.hsm.eracom.communication.TCP;


public class HSMFuncs {

    // 6 : Function code (real function code+ F..F)
    public static int FUNCTION_CODE_FIX_LENGTH = 6;

    // 1: FM
    public static int FM_LENGTH = 1;

    // 9 : Meta Function code
    public static int META_FUNTION_LENGTH = 8;


    public byte[] functionCode;
    public byte[] parameters;
    public byte[] response;

    public HSMFuncs(byte[] funcCode, byte[] params) {
        functionCode = funcCode;
        parameters = params;
    }

    public byte[]/*void*/ sendRequest() {
        byte[] request;
        if (parameters != null && parameters.length > 0) {

            byte[] inputLength = HSMUtil.getLengthByte(HSMFuncs.FUNCTION_CODE_FIX_LENGTH + HSMFuncs.FM_LENGTH + parameters.length);

            request = new byte[parameters.length + HSMFuncs.FUNCTION_CODE_FIX_LENGTH +
                    HSMFuncs.FM_LENGTH + HSMFuncs.META_FUNTION_LENGTH + inputLength.length];
            int j = 0;

            request[j++] = (byte) 0xE3;
            request[j++] = (byte) 0x00;
            request[j++] = (byte) 0x01;
            request[j++] = (byte) 0x01;

            request[j++] = (byte) 0x00;
            request[j++] = (byte) 0x00;
            request[j++] = (byte) 0x00;
            request[j++] = (byte) 0x01;
            System.arraycopy(inputLength, 0, request, j, inputLength.length);
            j = j + inputLength.length;

            //request[j++] = (byte) (0x7F & (HSMFuncs.FUNCTION_CODE_FIX_LENGTH + HSMFuncs.FM_LENGTH + parameters.length));
            //HSMUtil.buildVarLengthParam(input);

            for (int i = 0; i < functionCode.length - 1;)
                request[j++] = functionCode[i++];

            for (int i = functionCode.length - 1; i < HSMFuncs.FUNCTION_CODE_FIX_LENGTH; i++)
                request[j++] = (byte) 0xFF;

            request[j++] = functionCode[functionCode.length - 1];

            for (int i = 0; i < parameters.length;)
                request[j++] = parameters[i++];
        } else {
            int j = 0;
            request = new byte[6 + 9];

            request[j++] = (byte) 0xE3;
            request[j++] = (byte) 0x00;
            request[j++] = (byte) 0x01;
            request[j++] = (byte) 0x01;

            request[j++] = (byte) 0x00;
            request[j++] = (byte) 0x00;
            request[j++] = (byte) 0x00;
            request[j++] = (byte) 0x01;

            request[j++] = (byte) (0x7F & (6)); //HSMUtil.buildVarLengthParam(input);

            for (int i = 0; i < functionCode.length;)
                request[j++] = functionCode[i++];
            for (int i = functionCode.length; i < 6; i++)
                request[j++] = (byte) 0xFF;
        }

        // return request;

        response = TCP.sendRequest(request, false);
        return response;
    }
}
