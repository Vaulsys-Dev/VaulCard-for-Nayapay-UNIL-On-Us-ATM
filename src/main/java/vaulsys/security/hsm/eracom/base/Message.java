package vaulsys.security.hsm.eracom.base;

import vaulsys.util.MyInteger;


public class Message {

    // The ASCII Start of Header character (Hex 01)
    public static byte SOH = (byte) 0x01;

    // Header Version Number : Currently binary 1
    public static byte HVN = (byte) 0x01;

    // Reserved Byte of Meta function header: Reserved currently 00
    public static byte MRB = (byte) 0x00;

    //Meta-function type version: currently restricted to 01
    public static byte MTV = (byte) 0x01;

    /*Meta-function type identifier :
      * 01:The Message ID and Data Fields are used when meta-function type = 01
      * 00: The Message ID and Data field are not used when meta-function type = 00.
     */
    public static byte MTI_01 = (byte) 0x01;
    public static byte MTI_00 = (byte) 0x00;


    // 6 : Function code (real function code+ F..F)
    public static int FUNCTION_CODE_FIX_LENGTH = 6;

    // 1: FM
    public static int FM_LENGTH = 1;

    // 9 : Meta Function code
    public static int META_FUNTION_LENGTH = 8;

    // private byte[] data;

    public static byte[] putFuncCode(byte[] data, byte[] funcode, byte FM, boolean fixFunctionCode) {
        byte[] result;
        int currentPos = 0;

        if (fixFunctionCode) {
            result = new byte[data.length + FM_LENGTH + FUNCTION_CODE_FIX_LENGTH];
        } else {

            result = new byte[data.length + FM_LENGTH + funcode.length];
        }

        System.arraycopy(funcode, 0, result, 0, funcode.length);
        currentPos += funcode.length;

        if (fixFunctionCode) {
            for (int i = funcode.length; i < FUNCTION_CODE_FIX_LENGTH; i++)
                result[i] = (byte) 0xFF;

            result[FUNCTION_CODE_FIX_LENGTH] = FM;

            currentPos = FUNCTION_CODE_FIX_LENGTH + 1;

        } else {
//			for (int i=funcode.length ; i <funcode.length+3; i++)
//				result[i] = (byte) 0xFF;
            result[currentPos++] = FM;
        }

        System.arraycopy(data, 0, result, currentPos, data.length);

        return result;
    }


    public static byte[] takeFuncCode(byte[] data, byte[] funCode, boolean fixFunctionCode) {
        byte[] result = null;
        int currentPos = 0;

        if (fixFunctionCode) {
            currentPos = FUNCTION_CODE_FIX_LENGTH;
        } else {
            currentPos = funCode.length;
        }

        // error code
        if (data[currentPos] != ErrorCode.getErrorCode("No_error")) {
            //result = {-1, errorCode}
            result = new byte[]{-1, data[currentPos]};
            return result;
        }

        result = new byte[data.length - currentPos - 1];
        System.arraycopy(data, currentPos + 1, result, 0, result.length);

        return result;
    }


    public static byte[] putMetaFunctionHeader(byte[] data, byte metaFunctionID, byte version, byte[] messageID) {
        byte[] result;
        int currentPos = 0;

        byte[] inputLength = HSMUtil.getLengthByte(data.length);
        result = new byte[data.length + META_FUNTION_LENGTH + inputLength.length];

        // meta-function code : E3
        result[currentPos++] = (byte) 0xE3;
        // Reserved Byte
        result[currentPos++] = (byte) 0x00;
        //Meta-function ID
        result[currentPos++] = metaFunctionID;
        //Version
        result[currentPos++] = version;
        // 4 bytes for message id
        System.arraycopy(messageID, 0, result, currentPos, messageID.length);
        currentPos += messageID.length;


        System.arraycopy(inputLength, 0, result, currentPos, inputLength.length);
        currentPos += inputLength.length;

        System.arraycopy(data, 0, result, currentPos, data.length);

        return result;
    }

    public static byte[] takeMetaFunctionHeader(byte[] data, byte metaFunctionID, byte version, byte[] messageID) {
        byte[] result = null;
        int currentPos = 0;

        if (data[currentPos++] != (byte) 0xE3) {
            result = new byte[]{-1, ErrorCode.getErrorCode("Invalid_Meta_Function_Code")};
            return result;
        }

        if (data[currentPos++] != (byte) 0x00) {
            result = new byte[]{-1, ErrorCode.getErrorCode("Invalid_Reserved_Byte")};
            return result;
        }


        if (data[currentPos++] != metaFunctionID) {
            result = new byte[]{-1, ErrorCode.getErrorCode("Invalid_meta_Function_Id")};
            return result;
        }


        if (data[currentPos++] != version) {
            result = new byte[]{-1, ErrorCode.getErrorCode("Invalid_version")};
            return result;
        }

        // error code
        if (data[currentPos++] != ErrorCode.getErrorCode("No_error")) {
            result = new byte[]{-1, data[currentPos - 1]};
            return result;
        }

        for (int msgID = 0; msgID < 4; msgID++)
            if (data[currentPos++] != messageID[msgID]) {
                result = new byte[]{-1, ErrorCode.getErrorCode("Invalid_Message_Id")};
                return result;
            }


        MyInteger offfset = new MyInteger(currentPos);
        int dataLength = HSMUtil.getLengthOfVarField(data, offfset);


        result = new byte[dataLength];
        System.arraycopy(data, offfset.value, result, 0, dataLength);

        return result;
    }


    public static byte[] putCommonHeader(byte[] data, byte version, byte[] sequence) {
        byte[] result = new byte[6 + data.length];
        int currentPos = 0;

        result[currentPos++] = Message.SOH;
        result[currentPos++] = version; // Version numbrt
        result[currentPos++] = sequence[1]; // Sequence number MSB
        result[currentPos++] = sequence[0]; // Sequence number LSB
        result[currentPos++] =
                (byte) (data.length / 256); //Message length MSB
        result[currentPos++] =
                (byte) (data.length % 256); // Message Length LSB

        System.arraycopy(data, 0, result, currentPos, data.length);

        return result;
    }

    public static byte[] createMessageID(int messageID, int messageIDLength) {
        byte[] result;

        result = HSMUtil.intToByte(messageID);
        if (result.length < messageIDLength) {
            result = HSMUtil.makeLongger(result, messageIDLength);
        }

        return result;
    }

    public static byte[] takeCommonHeader(byte[] data, byte version, byte[] sequence) {
        byte[] result = null;
        int currentPos = 0;

        if (data[currentPos++] != Message.SOH || data[currentPos++] != version) {
            result = new byte[]{-1, ErrorCode.getErrorCode("Invalid_Version_Commen_Header")};
            return result;
        }

        if (data[currentPos++] != sequence[1] || data[currentPos++] != sequence[0]) {
            result = new byte[]{-1, ErrorCode.getErrorCode("Invalid_Sequence")};
            return result;
        }

        // Bytes[4] and [5] indicate the length of rest of response messge
        // Note that byte data type is signed and therefore there is a
        // possibility of number below than zero that should be converted
        int respLength =
                (data[currentPos] >= 0 ? data[currentPos] : data[currentPos] + 256) * 256 + (data[currentPos + 1] >=
                        0 ? data[currentPos + 1] :
                        data[currentPos + 1] +
                                256);

        if ((respLength + 6) != data.length) {
            result = new byte[]{-1, ErrorCode.getErrorCode("Invalid_data_Length")};
            return result;
        }

        result = new byte[respLength];
        System.arraycopy(data, currentPos + 2, result, 0, respLength);

        return result;

    }

}
