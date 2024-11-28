package vaulsys.protocols.PaymentSchemes.base;

import java.util.ArrayList;

/**
 * Created by m.rehman on 6/9/2016.
 */
public class BERTLV {
    public String tagName;
    public String tagLength;
    public String tagValue;

    public BERTLV(String inTagName, String inTagLength, String inTagValue) {
        this.tagName = inTagName;
        this.tagLength = inTagLength;
        this.tagValue = inTagValue;
    }

    public BERTLV getTLVTag(String inTLVData, int inTLVCursorPosition) {
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

    //Parse TLV data for the logging purpose
    public void parseTLVData(String inTLVData) {
        int tlvCursorPosition = 0;
        int tlvDataLength = inTLVData.length();
        BERTLV newTLVObject = null;

        while(tlvCursorPosition < tlvDataLength) {
            newTLVObject = getTLVTag(inTLVData, tlvCursorPosition);
            System.out.println("Tag [" + newTLVObject.tagName + "] Length [" + newTLVObject.tagLength + "] " +
                    "Value [" + newTLVObject.tagValue + "]");
            tlvCursorPosition += newTLVObject.tagName.length() + newTLVObject.tagLength.length() +
                    newTLVObject.tagValue.length();

            //remove last reference to avoid memory issues
            newTLVObject = null;
        }
    }

    //Get tags info required to build ARPC
    public String getTLVDataForARPC(String inTLVData, ArrayList<String> inARPCTags) {
        BERTLV newTLVObject = null;
        String tlvTagsForARPC = "";

        int tlvCursorPosition = 0;
        int tlvDataLength = inTLVData.length();

        while(tlvCursorPosition < tlvDataLength) {
            //get TLV one by one and match with the ArrayList tags
            newTLVObject = getTLVTag(inTLVData, tlvCursorPosition);

            if (inARPCTags.contains(newTLVObject.tagName))
                tlvTagsForARPC += newTLVObject.tagName + newTLVObject.tagLength + newTLVObject.tagValue;

            tlvCursorPosition += newTLVObject.tagName.length() + newTLVObject.tagLength.length() +
                    newTLVObject.tagValue.length();

            //remove last reference to avoid memory issues
            newTLVObject = null;
        }

        return tlvTagsForARPC;
    }

    //Find a particular tag
    public String findTLVTag(String inTLVData, String inTag) {
        String tagDetail = "";
        int tlvCursorPosition = 0;
        int tlvDataLength = inTLVData.length();
        BERTLV newTLVObject = null;

        while(tlvCursorPosition < tlvDataLength) {
            //get TLV one by one and match with the requested tag
            newTLVObject = getTLVTag(inTLVData, tlvCursorPosition);

            if (inTag.equals(newTLVObject.tagName)) {
                tagDetail += newTLVObject.tagName + newTLVObject.tagLength + newTLVObject.tagValue;
                break;
            }

            tlvCursorPosition += newTLVObject.tagName.length() + newTLVObject.tagLength.length() +
                    newTLVObject.tagValue.length();

            //remove last reference to avoid memory issues
            newTLVObject = null;
        }

        return tagDetail;
    }
}
