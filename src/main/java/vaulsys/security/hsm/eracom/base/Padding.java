package vaulsys.security.hsm.eracom.base;

public class Padding {

    public static final int Zero_Padding = 0;
    public static final int Single_One_Other_Zero = 1;

    public static byte[] padData(byte[] text, int size, int algorithm) {
        switch (algorithm) {
            case Zero_Padding:
                return zeroPadding(text, size);
        }

        return text;
    }

    public static byte[] zeroPadding(byte[] text, int size) {
        byte[] newText = new byte[text.length + size];
        int i;

        for (i = 0; i < text.length; i++)
            newText[i] = text[i];

        for (; i < newText.length; i++)
            newText[i] = 0x00;

        return newText;
    }
}
