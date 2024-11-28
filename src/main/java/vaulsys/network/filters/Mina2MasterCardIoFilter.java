package vaulsys.network.filters;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;

//import org.apache.axis2.addressing.AddressingConstants;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;

public class Mina2MasterCardIoFilter extends IoFilterAdapter {
    transient Logger logger = Logger.getLogger(Mina2ISOIoFilter.class);

    @SuppressWarnings("unchecked")
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
    	if(logger.isTraceEnabled()){
    		logger.trace("Filter Message Received from: " + session.getRemoteAddress());
    	}
        ////ArrayList<String> strReceiveBytes = (ArrayList<String>) session.getAttribute("strReceiveBytes");
        ArrayList<Byte> binaryReceiveBytes = (ArrayList<Byte>) session.getAttribute("binaryReceiveBytes");


        if (binaryReceiveBytes == null) {
            binaryReceiveBytes = new ArrayList<Byte>();
            session.setAttribute("binaryReceiveBytes", binaryReceiveBytes);
        }
        ////if (strReceiveBytes == null) {
        ////    strReceiveBytes = new ArrayList<String>();
        ////    session.setAttribute("binaryReceiveBytes", strReceiveBytes);
        ////}
        //Raza Decode Msg From EBCDIC start
        //System.out.println("Mina2MasterCardIoFilter:: Message [" + message.toString() + "]"); //Raza TEMP
        String HexDump = ((IoBuffer)message).getHexDump();
        //System.out.println("Mina2MasterCardIoFilter:: Message HexDump [" + HexDump + "]"); //Raza TEMP
        String strMsgLength = HexDump.substring(0,5);
        //System.out.println("Mina2MasterCardIoFilter:: Message Length HEX [" + strMsgLength + "]"); //Raza TEMP
        strMsgLength = strMsgLength.replaceAll(" ","");
        int intMsgLength = Integer.parseInt(strMsgLength,16) + 2;
        //System.out.println("Mina2MasterCardIoFilter:: Message Length [" + intMsgLength + "]"); //Raza TEMP
        /*String strtMsg="",RemMsg="",FinalMsg="";
        //String temp = new String(actualMessage,"Cp1047");
        //Raza Decode Msg From EBCDIC end
        //strtMsg = new String(actualMessage,"Cp1047");
        FinalMsg = HexDump.substring(0,5);
        System.out.println("Mina2MasterCardIoFilter:: Final Msg [" + FinalMsg + "]"); //Raza TEMP
        strtMsg = HexDump.substring(6,17);
        System.out.println("Mina2MasterCardIoFilter:: strt Msg [" + strtMsg + "]"); //Raza TEMP
        strtMsg = strtMsg.replaceAll(" ","");
        System.out.println("Mina2MasterCardIoFilter:: strt Msg WO [" + strtMsg + "]"); //Raza TEMP
        String teeemp = new String(strtMsg.getBytes(),"Cp1047");
        System.out.println("Mina2MasterCardIoFilter:: strt Msg NOW [" + teeemp + "]"); //Raza TEMP

        FinalMsg += strtMsg;
        System.out.println("Mina2MasterCardIoFilter:: Final Msg [" + FinalMsg + "]"); //Raza TEMP*/
        //String PrimaryBitmap = HexDump.substring(18,41);
        //System.out.println("Mina2MasterCardIoFilter:: Message HexDump Primary Bitmap [" + PrimaryBitmap + "]"); //Raza TEMP
        //FinalMsg += PrimaryBitmap;
        //System.out.println("Mina2MasterCardIoFilter:: Final Msg [" + FinalMsg + "]"); //Raza TEMP
        //String PrimaryBitmap_binary = new BigInteger(PrimaryBitmap.substring(0,1), 16).toString(2);
        //System.out.println("Mina2MasterCardIoFilter:: Message HexDump Binary Bitmap 2-Bytes [" + PrimaryBitmap_binary + "]"); //Raza TEMP
        //HexDump = String.valueOf(Hex.decodeHex(HexDump.toCharArray()));
        //String Secondary_Bitmap = "";
        //if(PrimaryBitmap_binary.charAt(1) == '1')
        //{
         //   Secondary_Bitmap = HexDump.substring(42,65);
         //   System.out.println("Mina2MasterCardIoFilter:: Message HexDump Secondary Bitmap [" + Secondary_Bitmap + "]"); //Raza TEMP
        //}

        IoBuffer byteMessage = (IoBuffer) message;

        //System.out.println("Mina2MasterCardIoFilter:: IOBuffer [" + (IoBuffer)message + "]"); //Raza TEMP
        //System.out.println("Mina2MasterCardIoFilter:: ByteMessage [" + byteMessage.get() + "]"); //Raza TEMP


        while (byteMessage.hasRemaining())
            binaryReceiveBytes.add(byteMessage.get());

            ////strReceiveBytes.add(""+byteMessage.get());
    	////if(logger.isTraceEnabled()){
    	////	logger.trace("Total yet received:" + strReceiveBytes.toString());
    	////}
        /*for(int i=0 ; i<binaryReceiveBytes.size() ; i++) {
            System.out.println("Mina2MasterCardIoFilter:: binaryReceiveBytes at i [" + i + "] = [" + binaryReceiveBytes.get(i) + "]"); //Raza TEMP
        }*/

        if(logger.isTraceEnabled()){
            logger.trace("Total yet received:" + binaryReceiveBytes.toString());
        }
        ////System.out.println("Mina2MaserCardFilter:: StrReceiveBytes [" + strReceiveBytes + "]"); //Raza TEMP
        if (binaryReceiveBytes.size() < 4)
            return;
        ////if (strReceiveBytes.size() < 4)
        ////    return;

        while (binaryReceiveBytes.size() >= 2) {
            int b1 = binaryReceiveBytes.get(0);
            int b2 = binaryReceiveBytes.get(1);
            ////while (strReceiveBytes.size() >= 2) {
            ////    int b1 = Integer.parseInt(strReceiveBytes.get(0));
            ////    int b2 = Integer.parseInt(strReceiveBytes.get(1));
            //int b3 = binaryReceiveBytes.get(2); //Raza commenting for MasterCard
            //int b4 = binaryReceiveBytes.get(3); //Raza commenting for MasterCard

            /*
            int b1 = binaryReceiveBytes.get(0) - 48;
            int b2 = binaryReceiveBytes.get(1) - 48;
            int b3 = binaryReceiveBytes.get(2) - 48;
            int b4 = binaryReceiveBytes.get(3) - 48;
            */

            int len = (b1 * 10) + b2 + 2;

            //System.out.println("Mina2MasterCardFilter:: Message Length [" + len + "], b1 [" + b1 + "], b2 [" + b2 + "]"); //Raza TEMP
            //int len = (b1 * 1000) + b2 * 100 + b3 * 10 + b4 + 4; //Raza commenting
            //System.out.println("Mina2MasterCardFilter:: binaryReceiveBytes Size [" + binaryReceiveBytes.size() + "], MsgLength [" + intMsgLength + "]"); //Raza TEMP
            if (binaryReceiveBytes.size() >= intMsgLength) { //len) {
            ////if (strReceiveBytes.size() >= len) {
                //System.out.println("Mina2MasterCardFilter:: Here 1"); //Raza TEMP
                    byte[] actualMessage = new byte[intMsgLength - 2];

                    for (int i = 0; i < intMsgLength-2; i++) //len - 2; i++)
                    {
                        actualMessage[i] = (byte) binaryReceiveBytes.get(i + 2);
                        //System.out.println("Mina2MasterCardIoFilter:: iteration [" + i + "] Item [" + binaryReceiveBytes.get(i + 2) + "]"); //Raza TEMP
                    }
                        ////actualMessage[i] = (byte) Integer.parseInt(strReceiveBytes.get(i + 2));

                    ArrayList<Byte> subList = new ArrayList<Byte>();
                    for (int i = intMsgLength; i < binaryReceiveBytes.size(); i++)
                        subList.add(binaryReceiveBytes.get(i));
                ////subList.add(strReceiveBytes.get(i).getBytes());
                        binaryReceiveBytes.clear();// = (ArrayList<Byte>)
                        binaryReceiveBytes.addAll(subList);


                    //Raza Printing Byte Array start
                    //for(int i=0; i<actualMessage.length ; i++)
                    //{
                     //   System.out.println("Mina2MasterCardFilter:: actual Msg BYTE ARRAY [" + actualMessage[i] + "]"); //Raza TEMP
                    //}
                    //Raza Printing Byte Array temp



                    //String temp0 = new String(actualMessage);
                    //String temp = new String(actualMessage,"Cp1047");
                    //System.out.println("Mina2MasterCardFilter:: actual Msg EBCDIC [" + temp + "]"); //Raza TEMP
                    ////System.out.println("Mina2MaserCardFilter:: StrReceiveBytes [" + strReceiveBytes + "]"); //Raza TEMP
                    //System.out.println("Mina2MasterCardFilter:: actual Msg Str [" + temp0 + "]"); //Raza TEMP

                    //String tempBitmap = temp.substring(4,22);
                //System.out.println("Mina2MasterCardFilter:: EBCDIC SubStr [" + tempBitmap + "]"); //Raza TEMP
                /*try {
                    String str1 = new String(tempBitmap.getBytes(), 38, 8, "Cp037");
                    System.out.println("Mina2MasterCardFilter:: This Str [" + str1 + "]"); //Raza TEMP
                }catch (UnsupportedEncodingException ue){
                    ue.printStackTrace();
                }*/

                    //System.out.println("Mina2MasterCardFilter:: Message Length [" + len + "], b1 [" + b1 + "], b2 [" + b2 + "]"); //Raza TEMP

                    //////actualMessage = temp.getBytes();
                    //byte[] PBitmapbytes = Hex.decodeHex(tempBitmap.toCharArray());
                    //System.out.println("Bitmap [" + new String(PBitmapbytes, "UTF-8") + "]");

                /*//Raza converting Bytes array to Hex String start
                char[] hexChars = new char[actualMessage.length * 2];
                for ( int j = 0; j < actualMessage.length; j++ ) {
                    int v = actualMessage[j] & 0xFF;
                    hexChars[j * 2] = temp0.toCharArray()[v >>> 4];
                    hexChars[j * 2 + 1] = temp0.toCharArray()[v & 0x0F];
                }
                String Hextemp = new String(hexChars);
                System.out.println("Mina2MasterCardFilter:: actual Msg HEX [" + Hextemp + "]"); //Raza TEMP
                //Raza converting Bytes array to Hex String end*/


                    super.messageReceived(nextFilter, session, actualMessage);
                } else {
                    return;
                }
            }
        }


    @Override
    public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {

        byte[] messageBytes = (byte[]) writeRequest.getMessage(); // ((OutgoingMessage)writeRequest.getMessage()).getBinaryData();
        IoBuffer buff = IoBuffer.wrap(messageBytes);
        String MsgLength;

        byte[] data = buff.array();
        byte[] binData = new byte[data.length + 2];
        System.arraycopy(data, 0, binData, 2, data.length);

        // TODO does not work for message larger than 255 byte
        MsgLength = Integer.toHexString(data.length);
        MsgLength  = StringUtils.leftPad(MsgLength,4,'0');

        binData[0] =  (byte) (Integer.parseInt(MsgLength.substring (0,2),16));
        binData[1] =  (byte) (Integer.parseInt(MsgLength.substring (2,4),16));

        /*binData[0] = (byte) (data.length / 1000 + 48);
        binData[1] = (byte) ((data.length % 1000) / 100 + 48);
        binData[2] = (byte) ((data.length % 100) / 10 + 48);
        binData[3] = (byte) (data.length % 10 + 48);*/

        super.filterWrite(nextFilter, session, new DefaultWriteRequest(IoBuffer.wrap(binData)));
    }
}
