package vaulsys.security.hsm.eracom.Visa;

import vaulsys.security.hsm.eracom.HSMFuncs;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.util.MyInteger;

public class VisaMgr {
    public static byte[] CVVGenerate(KeySpecifier CVKSpec, byte[] CVVData) {
        int index = 0;
        byte[] CVKspecs = CVKSpec.getByteArray();
        byte[] params = new byte[CVKspecs.length + 16];

        for (int i = 0; i < CVKspecs.length; i++)
            params[index++] = CVKspecs[i];

        for (int i = 0; i < CVVData.length; i++)
            params[index++] = CVVData[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x08, (byte) 0x02,
                        (byte) 0x00}, params);

        func.sendRequest();

        MyInteger offset = new MyInteger(3);
        if (func.response[offset.value++] != 0)
            return null;
        byte[] CVV = new byte[2];
        for (int i = 0; i < CVV.length; i++)
            CVV[i] = func.response[i + offset.value];

        return func.response;


    }

    public static byte[] CVVVerify(KeySpecifier CVKSpec, byte[] CVVData,
                                   byte[] CVV) {
        int index = 0;
        byte[] CVKspecs = CVKSpec.getByteArray();
        byte[] params = new byte[CVKspecs.length + 16 + 2];

        for (int i = 0; i < CVKspecs.length; i++)
            params[index++] = CVKspecs[i];

        for (int i = 0; i < CVVData.length; i++)
            params[index++] = CVVData[i];

        for (int i = 0; i < CVV.length; i++)
            params[index++] = CVV[i];

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xEE, (byte) 0x08, (byte) 0x03,
                        (byte) 0x00}, params);

        func.sendRequest();
        return func.response;


    }


}
