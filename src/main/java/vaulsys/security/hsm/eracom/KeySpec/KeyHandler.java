package vaulsys.security.hsm.eracom.KeySpec;


public class KeyHandler {
    public static KeySpecifier getKeySpec(byte[] response, int offset) {
        int format = response[offset + 1];

        switch (format) {

            case 0x00:
                return new KeySpec00(response, offset);
            case 0x01:
                return new KeySpec01(response, offset);
            case 0x02:
                return new KeySpec02(response, offset);
            case 0x03:
                return new KeySpec03(response, offset);
            case 0x10:
                return new KeySpec10(response, offset);
            case 0x11:
                return new KeySpec11(response, offset);
            case 0x13:
                return new KeySpec13(response, offset);
            case 0x14:
                return new KeySpec14(response, offset);
                // case 0x15:
                //     return new KeySpec15( response, offset );


        }

        return null;
    }
}
