package vaulsys.protocols.PaymentSchemes.EMV;

import vaulsys.util.Util;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by m.rehman on 6/9/2016.
 */
public class BERTLV {

    private static Logger logger = Logger.getLogger(BERTLV.class.getName());

    public String tagName;
    public String tagLength;
    public String tagValue;

    public BERTLV(String inTagName, String inTagLength, String inTagValue) {
        this.tagName = inTagName;
        this.tagLength = inTagLength;
        this.tagValue = inTagValue;
    }

    public static BERTLV getTLVTag(String inTLVData, int inTLVCursorPosition) {
        int tagLength;
        String tagLengthBytes, tagLengthBytesNext, tagValue, tagName;

        //check if tag name is passing in the function or not
        tagName = inTLVData.substring(inTLVCursorPosition, inTLVCursorPosition+2);

        if (tagName.equals("5F") || tagName.equals("9F") || tagName.equals("BF"))
            tagLength = 4;
        else
            tagLength = 2;

        tagName = inTLVData.substring(inTLVCursorPosition, inTLVCursorPosition+tagLength);
        inTLVCursorPosition += tagLength;

        //check tag length byte
        //first check first consecutive byte
        tagLengthBytes = inTLVData.substring(inTLVCursorPosition, inTLVCursorPosition+2);

        //convert to decimal
        tagLength = Integer.parseInt(tagLengthBytes, 16);

        //from EMV v4.1 Book 3 Application Specification
		/*  When bit b8 of the most significant byte of the length field is set to 0, the length
			field consists of only one byte. Bits b7 to b1 code the number of bytes of the value
			field. The length field is within the range 1 to 127.

			When bit b8 of the most significant byte of the length field is set to 1, the
			subsequent bits b7 to b1 of the most significant byte code the number of
			subsequent bytes in the length field. The subsequent bytes code an integer
			representing the number of bytes in the value field. Two bytes are necessary to
			express up to 255 bytes in the value field.
		*/

        //if the hex >= 80
        if (tagLength >= 128) {
            tagLength -= 128;	// subtracting 128 will give the remaining 7 bits
            inTLVCursorPosition += 2;
            tagLengthBytesNext = inTLVData.substring(inTLVCursorPosition, inTLVCursorPosition+2);
            tagLength = Integer.parseInt(tagLengthBytesNext, 16);
            tagLengthBytes += tagLengthBytesNext;
        }

        //move cursor forward
        inTLVCursorPosition += 2;

        //multiply converted length to 2 as TLV data is packed byte type
        tagLength *= 2;

        //now we find tag value
        tagValue = inTLVData.substring(inTLVCursorPosition, inTLVCursorPosition+tagLength);

        //fill the object and return
        return (new BERTLV(tagName, tagLengthBytes, tagValue));
    }

    //overloading get tag function
    public static BERTLV getTLVTag(String inTLVData, String tagName) {
        int tagLength, tagPosition;
        String tagLengthBytes, tagLengthBytesNext, tagValue, tagNameInit;

        //find tag position in emv data
        tagPosition = inTLVData.indexOf(tagName);
        if (tagPosition < 0) {
            logger.error("Unable to find Tag [" + tagName + "]");
            return null;
        }

        //get starting 1 byte of tag to identify the length of tag
        tagNameInit = tagName.substring(0, 2);
        if (tagNameInit.equals("5F") || tagNameInit.equals("9F") || tagNameInit.equals("BF"))
            tagLength = 4;
        else
            tagLength = 2;

        //update tag position
        tagPosition += tagLength;

        //check tag length byte
        //first check first consecutive byte
        tagLengthBytes = inTLVData.substring(tagPosition, tagPosition + 2);

        //convert to decimal
        tagLength = Integer.parseInt(tagLengthBytes, 16);

        //update position of cursor
        //tagPosition += 2;

        //from EMV v4.1 Book 3 Application Specification
		/*  When bit b8 of the most significant byte of the length field is set to 0, the length
			field consists of only one byte. Bits b7 to b1 code the number of bytes of the value
			field. The length field is within the range 1 to 127.

			When bit b8 of the most significant byte of the length field is set to 1, the
			subsequent bits b7 to b1 of the most significant byte code the number of
			subsequent bytes in the length field. The subsequent bytes code an integer
			representing the number of bytes in the value field. Two bytes are necessary to
			express up to 255 bytes in the value field.
		*/

        //if the hex >= 80
        if (tagLength >= 128) {
            tagLength -= 128;    // subtracting 128 will give the remaining 7 bits
            tagPosition += 2;
            tagLengthBytesNext = inTLVData.substring(tagPosition, tagPosition + 2);
            tagLength = Integer.parseInt(tagLengthBytesNext, 16);
            tagLengthBytes += tagLengthBytesNext;
        }

        //move cursor forward
        tagPosition += 2;

        //multiply converted length to 2 as TLV data is packed byte type
        tagLength *= 2;

        //now we find tag value
        tagValue = inTLVData.substring(tagPosition, tagPosition + tagLength);

        //fill the object and return
        //finalTLV = tagName + tagLengthBytes + tagValue;
        return (new BERTLV(tagName, tagLengthBytes, tagValue));
    }

    //Parse TLV data for the logging purpose
    public void parseTLVData(String inTLVData) {
        int tlvCursorPosition = 0;
        int tlvDataLength = inTLVData.length();
        BERTLV newTLVObject;

        while(tlvCursorPosition < tlvDataLength) {
            newTLVObject = getTLVTag(inTLVData, tlvCursorPosition);
            System.out.println("Tag [" + newTLVObject.tagName + "] Length [" + newTLVObject.tagLength + "] " +
                    "Value [" + newTLVObject.tagValue + "]");
            tlvCursorPosition += newTLVObject.tagName.length() + newTLVObject.tagLength.length() +
                    newTLVObject.tagValue.length();
        }
    }

    //Get tags info required to build ARPC
    public static String getTLVDataForARPC(String inTLVData, List<String> inARPCTags) {
        BERTLV newTLVObject;
        String tlvTagsForARPC = "";
        Integer tlvCursorPosition, tlvDataLength;

        try {
            tlvCursorPosition = 0;
            tlvDataLength = inTLVData.length();

            /*for (String tag : inARPCTags) {
                //get TLV one by one and match with the ArrayList tags
                newTLVObject = getTLVTag(inTLVData, tag);

                if (newTLVObject != null)
                    tlvTagsForARPC += newTLVObject.tagName + newTLVObject.tagLength + newTLVObject.tagValue;
            }*/

            while(tlvCursorPosition < tlvDataLength) {
                //get TLV tag
                newTLVObject = getTLVTag(inTLVData, tlvCursorPosition);

                //check whether it is required or not
                if (newTLVObject != null && inARPCTags.contains(newTLVObject.tagName)) {
                    tlvTagsForARPC += newTLVObject.tagName + newTLVObject.tagLength +
                            newTLVObject.tagValue;
                }

                //move to next tag
                tlvCursorPosition += newTLVObject.tagName.length() + newTLVObject.tagLength.length() +
                        newTLVObject.tagValue.length();
            }
        } catch (Exception e) {
            logger.error("Unable to find Tags for ARPC with error: " + e.getMessage());
        }

        return tlvTagsForARPC;
    }

    //Find a particular tag
    public static String findTLVTag(String inTLVData, String inTag) {
        String tagDetail = "";
        BERTLV newTLVObject;
        Integer tlvCursorPosition, tlvDataLength;

        try {
            tlvCursorPosition = 0;
            tlvDataLength = inTLVData.length();

            /*
            //get TLV one by one and match with the requested tag
            newTLVObject = getTLVTag(inTLVData, inTag);

            if (newTLVObject != null && inTag.equals(newTLVObject.tagName))
                tagDetail += newTLVObject.tagName + newTLVObject.tagLength + newTLVObject.tagValue;
            else
                logger.error("Unable to find Tag [" + inTag + "]");
            */

            while(tlvCursorPosition < tlvDataLength) {
                //get TLV tag
                newTLVObject = getTLVTag(inTLVData, tlvCursorPosition);

                //check whether it is required or not
                if (newTLVObject != null && inTag.contains(newTLVObject.tagName)) {
                    tagDetail += newTLVObject.tagName + newTLVObject.tagLength + newTLVObject.tagValue;
                    break;
                }

                //move to next tag
                tlvCursorPosition += newTLVObject.tagName.length() + newTLVObject.tagLength.length() +
                        newTLVObject.tagValue.length();
            }

            if (!Util.hasText(tagDetail))
                logger.error("Unable to find Tag [" + inTag + "]");

        } catch (Exception e) {
            logger.error("Unable to find Tag [" + inTag + "] with error: " + e.getMessage());
        }
        return tagDetail;
    }

    //m.rehman: [VS-WMS-PP-SWITCH-127] - PayPak EMV Integration
    //Find a particular tag value
    public static String findTLVTagValue(String inTLVData, String inTag) {

        String tagValue = "";
        BERTLV newTLVObject;
        Integer tlvCursorPosition, tlvDataLength;

        try {
            tlvCursorPosition = 0;
            tlvDataLength = inTLVData.length();

            /*
            //get TLV one by one and match with the requested tag
            newTLVObject = getTLVTag(inTLVData, inTag);

            if (newTLVObject != null && inTag.equals(newTLVObject.tagName))
                tagValue = newTLVObject.tagValue;
            else
                logger.error("Unable to find Tag Value [" + inTag + "]");
            */

            while(tlvCursorPosition < tlvDataLength) {
                //get TLV tag
                newTLVObject = getTLVTag(inTLVData, tlvCursorPosition);

                //check whether it is required or not
                if (newTLVObject != null && inTag.equals(newTLVObject.tagName)) {
                    tagValue = newTLVObject.tagValue;
                    break;
                }

                //move to next tag
                tlvCursorPosition += newTLVObject.tagName.length() + newTLVObject.tagLength.length() +
                        newTLVObject.tagValue.length();
            }

            if (!Util.hasText(tagValue))
                logger.error("Unable to find Tag [" + inTag + "]");

        } catch (Exception e) {
            logger.error("Unable to find Tag Value [" + inTag + "] with error: " + e.getMessage());
        }
        return tagValue;
    }

    //m.rehman: make transaction data for arqc validation/arpc generation
    public static String getTranData(String inTLVData) {

        String tranData = "", value = "";
        Integer mod, length;
        List<String> tranDataList;

        try {
            tranDataList = EMVTags.getTranDataList();

            String tagValue;
            for (String tag : tranDataList) {
                tagValue = findTLVTagValue(inTLVData, tag);

                if (Util.hasText(tagValue)) {
                    tranData += tagValue;
                }
                else {
                    logger.error("Unable to find tag [" + tag + "]");
                    tranData = null;
                    break;
                }
            }

            if (!Util.hasText(tranData)) {
                logger.error("Unable to find tran data");
                return null;
            }

            //make transaction data multiple of 16 by padding 80 + zeroes
            mod = tranData.length() % 16;
            if (mod != 0) {
                length = 16 - mod;
                value += "80";
                tranData = tranData + StringUtils.rightPad(value, length, "0");
            }

        } catch (Exception e) {
            logger.error("Unable to find Tran Data with error: " + e.getMessage());
        }

        return tranData;
    }
}
