package vaulsys.network.filters;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class Mina2OnelinkIoFilter extends IoFilterAdapter {
    transient Logger logger = Logger.getLogger(Mina2OnelinkIoFilter.class);

    //@SuppressWarnings("unchecked")
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {


    	if(logger.isTraceEnabled()){
    		logger.trace("Filter Message Received from: " + session.getRemoteAddress());
    	}
        ArrayList<Byte> binaryReceiveBytes = (ArrayList<Byte>) session.getAttribute("binaryReceiveBytes");

        if (binaryReceiveBytes == null) {
            binaryReceiveBytes = new ArrayList<Byte>();
            session.setAttribute("binaryReceiveBytes", binaryReceiveBytes);
        }

        IoBuffer byteMessage = (IoBuffer) message;

        //get hex length and convert in decimal
        String lengthInHex = ((IoBuffer) message).getHexDump();
        lengthInHex = lengthInHex.substring(0,2) + lengthInHex.substring(3,5);
        int lengthInInt = Integer.parseInt(lengthInHex, 16);

        //to reset the buffer
        binaryReceiveBytes.clear();

        while (byteMessage.hasRemaining())
            binaryReceiveBytes.add(byteMessage.get());

    	if(logger.isTraceEnabled()){
    		logger.trace("Total yet received:" + binaryReceiveBytes.toString());
    	}
       	
        if (binaryReceiveBytes.size() < 2)
            return;

        while (binaryReceiveBytes.size() >= 2) {
            int len = lengthInInt + 2;

            if (binaryReceiveBytes.size() >= len) {
                byte[] actualMessage = new byte[len - 2];

                for (int i = 0; i < len - 2; i++)
                    actualMessage[i] = (byte) binaryReceiveBytes.get(i + 2);

                ArrayList<Byte> subList = new ArrayList<Byte>();
                for (int i = len; i < binaryReceiveBytes.size(); i++)
                    subList.add(binaryReceiveBytes.get(i));

                binaryReceiveBytes.clear();// = (ArrayList<Byte>)
                binaryReceiveBytes.addAll(subList);

                super.messageReceived(nextFilter, session, actualMessage);
            } else {
                return;
            }
        }
    }

    @Override
    public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {

        writeRequest.getMessage().toString().getBytes(Charset.forName("ISO-8859-1"));
        byte[] messageBytes = (byte[]) writeRequest.getMessage(); // ((OutgoingMessage)writeRequest.getMessage()).getBinaryData();
        IoBuffer buff = IoBuffer.wrap(messageBytes);
        String length;

        byte[] data = buff.array();
        byte[] binData = new byte[data.length + 2];
        System.arraycopy(data, 0, binData, 2, data.length);
        
        // TODO does not work for message larger than 255 byte

        //binData[0] = (byte) (data.length / 1000 + 48);
        //binData[1] = (byte) ((data.length % 1000) / 100 + 48);
        length = Integer.toHexString(data.length);
        //length = String.format("-%4s", length).replace(' ', '0');
        length = StringUtils.leftPad(length, 4, "0");
        binData[0] = (byte)(Integer.parseInt(length.substring(0,2), 16));
        binData[1] = (byte)(Integer.parseInt(length.substring(2,4), 16));
        /*if (length > 256) {
            binData[0] = (byte) ((length > 384) ? (length - 512) : (length - 256));
        } else {
            binData[0] = 0;
        }
        length = length - 256;
        binData[1] = (byte) ((length > 128) ? (length - 256) : length);*/

        super.filterWrite(nextFilter, session, new DefaultWriteRequest(IoBuffer.wrap(binData)));
    }
}
