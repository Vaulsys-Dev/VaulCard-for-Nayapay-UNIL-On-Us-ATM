package vaulsys.security.hsm.eracom.base;

import vaulsys.util.MyInteger;
import vaulsys.util.encoders.Hex;


public class HSMUtil {
    public static byte[] buildVarLengthParam(byte[] input) {
        // int length = input.length;
        byte[] output = null;
        int index = 0;

        byte[] length = getLengthByte(input.length);

        // if (length <= 127) {
        // output = new byte[1 + length];
        // output[index++] = (byte)length;
        // } else if (length <= 16383) {
        // output = new byte[2 + length];
        // output[index++] = (byte)(0x80 | ((byte)(length / 256)));
        // output[index++] = (byte)(length % 256);
        // } else if (length <= 2097151) {
        // output = new byte[3 + length];
        // output[index++] = (byte)(0xC0 | ((byte)(length / (256 * 256))));
        // output[index++] = (byte)((length % (256 * 256)) / 256);
        // output[index++] = (byte)(length % 256);
        // } else if (length <= 268435455) {
        // output = new byte[4 + length];
        // output[index++] =
        // (byte)(0xE0 | ((byte)(length / (256 * 256 * 256))));
        // output[index++] =
        // (byte)((length % (256 * 256 * 256)) / (256 * 256));
        // output[index++] = (byte)((length % (256 * 256)) / 256);
        // output[index++] = (byte)(length % 256);
        // }

        output = new byte[length.length + input.length];
        System.arraycopy(length, 0, output, 0, length.length);
        System.arraycopy(input, 0, output, length.length, input.length);

        // for (int i = 0; i < input.length; )
        // output[index++] = input[i++];

        return output;
    }

    public static byte[] getLengthByte(int inputLength) {
        byte[] output = null;
        int index = 0;

        if (inputLength <= 127) {
            output = new byte[1];
            output[index++] = (byte) inputLength;
        } else if (inputLength <= 16383) {
            output = new byte[2];
            output[index++] = (byte) (0x80 | ((byte) (inputLength / 256)));
            output[index++] = (byte) (inputLength % 256);
        } else if (inputLength <= 2097151) {
            output = new byte[3];
            output[index++] = (byte) (0xC0 | ((byte) (inputLength / (256 * 256))));
            output[index++] = (byte) ((inputLength % (256 * 256)) / 256);
            output[index++] = (byte) (inputLength % 256);
        } else if (inputLength <= 268435455) {
            output = new byte[4];
            output[index++] = (byte) (0xE0 | ((byte) (inputLength / (256 * 256 * 256))));
            output[index++] = (byte) ((inputLength % (256 * 256 * 256)) / (256 * 256));
            output[index++] = (byte) ((inputLength % (256 * 256)) / 256);
            output[index++] = (byte) (inputLength % 256);
        }

        return output;
    }

    public static int getLengthOfVarField(byte[] result, MyInteger ofset) {
        int offset = ofset.value;
        int var = result[offset] & 0xE0;
        int length;
        if (var < 0x80) // the first bit is zero
            length = result[offset++];
        else if (var < 0xC0)
            length = 256 * (result[offset++] & 0x3F) + (result[offset] >= 0 ? result[offset++] : result[offset++] + 256);
        else if (var < 0xE0)
            length = 256 * 256 * (result[offset++] & 0x1F) + 256 * (result[offset] >= 0 ? result[offset++] : result[offset++] + 256)
                    + (result[offset] >= 0 ? result[offset++] : result[offset++] + 256);
        else
            length = 256 * 256 * 256 * (result[offset++] & 0x0F) + 256 * 256 * (result[offset] >= 0 ? result[offset++] : result[offset++] + 256)
                    + 256 * (result[offset] >= 0 ? result[offset++] : result[offset++] + 256)
                    + (result[offset] >= 0 ? result[offset++] : result[offset++] + 256);

        ofset.value = offset;
        return length;
    }

    public static int getDataLength(byte lengthByte) {

        if ((lengthByte & 0x80) == 0x00) {
            return 1;
        }

        if ((lengthByte & 0xB0) == 0xB0) {
            return 2;
        }

        if ((lengthByte & 0xE0) == 0xE0) {
            return 3;
        }

        if ((lengthByte & 0xF0) == 0xF0) {
            return 4;
        }

        return -1;
    }

    public static byte[] stringToHex(String input) {
        String strHexChars = "0123456789ABCDEF";

        byte[] output = new byte[(int) Math.ceil(input.length() / 2.)];
        for (int counter = 0, index = 0; counter < output.length; counter++) {
            output[counter] = (byte) strHexChars.indexOf(input.charAt(index++));
            output[counter] = (byte) (output[counter] << 4);
            if (index == input.length())
                break;
            output[counter] |= (byte) strHexChars.indexOf(input.charAt(index++));
        }
        return output;
    }

    public static String hexToString(byte[] bytes) {
        return (bytes == null) ? null : new String(Hex.encode(bytes)).toUpperCase();
    }

    public static byte[] intToByte(int input) {

        byte[] result;

        if (input == 0) {
            result = new byte[1];
            result[0] = new Integer(0).byteValue();
        } else {

            result = new byte[new Double(Math.log(new Integer(input).doubleValue()) / Math.log(256D)).intValue() + 1];

            for (int i = result.length - 1; i >= 0; i--) {
                result[i] = new Integer(input).byteValue();
                input = (input / 256);
            }
        }
        return result;
    }

    public static String intToHex(int input) {
        String strHexChars = "0123456789ABCDEF";
        String result = "";

        int length = new Double(Math.log(new Integer(input).doubleValue()) / Math.log(256D)).intValue() + 1;

        for (int i = length - 1; i >= 0; i--) {
            int byteValue = input % 256;
            int r = byteValue % 16;
            int l = byteValue / 16;
            result = strHexChars.substring(l, l + 1) + strHexChars.substring(r, r + 1) + result;

            input = input / 256;
        }

        return result;
    }

    public static byte[] stringToBCD(String input) {
        String strBCDChars = "0123456789";

        //TODO: Padding...
        byte[] output = new byte[(int) Math.ceil(input.length() / 2.0)];
        for (int counter = 0, index = 0; counter < output.length; counter++) {
            output[counter] = (byte) strBCDChars.indexOf(input.charAt(index++));
            output[counter] = (byte) (output[counter] << 4);
            if (index == input.length())
                break;
            output[counter] |=
                    (byte) strBCDChars.indexOf(input.charAt(index++));
        }
        return output;
    }

    public static byte[] intToBCD(int input) {
        int inputLength = 1 + (int) Math.floor(Math.log10(input));
        byte[] output = new byte[(int) Math.ceil(inputLength / 2.0)];
        for (int counter = 0; counter < inputLength; counter++) {
            if ((counter % 2) == 1) {
                output[counter / 2] |= (byte) ((input % 10) << 4);
            } else {
                output[counter / 2] = (byte) (input % 10);
            }

            input /= 10;
        }

        byte b;
        for (int i = 0, j = output.length - 1; i < j; i++, j--) {
            b = output[i];
            output[i] = output[j];
            output[j] = b;
        }
        return output;
    }

    public static byte[] makeLongger(byte[] data, int finalLength) {
        byte[] result = new byte[finalLength];
        for (int i = 0; i < finalLength - data.length; i++) {
            result[i] = 0x00;
        }
        System.arraycopy(data, 0, result, finalLength - data.length, data.length);

        return result;
    }

    public static String byteToString(byte[] key) {
        String result = "";
        if (key != null)
            for (int i = 0; i < key.length; i++) {
                result += key[i] + " ";
            }
        return result;
    }

    public static int byteToInt(byte b) {
        return (b >= 0) ? b : b + 256;
    }

}


