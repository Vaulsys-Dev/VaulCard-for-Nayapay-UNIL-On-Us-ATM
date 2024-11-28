package vaulsys.util.constants;


public class ASCIIConstants {
    public static byte FS = 0x1C;
    public static byte GS = 0x1D;
    public static byte SOH = 0x01;
    public static byte SO = 0x0E;
    public static byte SI = 0x0F;
    public static byte NAK = 0x16;
    public static byte SYN = 0x16;
    public static byte ESC = 0x1B;
    public static byte FF = 0x0C;
    public static byte LF = 0x0A;
    public static byte HT = 0x09;
    public static byte OC = 0x5B;
    public static byte CR = 0x0D;

    public static byte getValue(String name) {
        try {
            return ((Byte) ASCIIConstants.class.getField(name).get(ASCIIConstants.class)).byteValue();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }
}
