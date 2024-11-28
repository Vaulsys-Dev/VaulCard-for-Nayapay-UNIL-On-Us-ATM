package vaulsys.security.hsm.eracom.KMManagement;

public class HSMKMMigrate {
    public byte returnCode;
    public byte n;
    public String keySpec;


    public HSMKMMigrate(byte[] result, int offset) {
        if ((returnCode = result[offset++]) != 0) {
            //an error occured so all the variables are set to zero
            n = -1;
            keySpec = "";
            return;

        }

        n = result[offset++];

        int var = result[offset] & 0xE0;
        int length = 0;

        if (var < 0x80) //the first bit is zero
            length = 1;
        else if (var < 0xC0)
            length = 2;

        /* if( var < 0x80  ) //the first bit is zero
            length = result[ offset++ ];
        else if( var < 0xC0)
            length = 256 * (result[ offset++ ] & 0x3F) +
                (result[offset]>=0?result[ offset++ ]:result[ offset++ ]+256); */

        for (int j = 0; j < length; j++)
            keySpec += (char) result[offset++];

    }

    public String toString() {
        return "n: " + n + " keySpec: " + keySpec;
    }

}
