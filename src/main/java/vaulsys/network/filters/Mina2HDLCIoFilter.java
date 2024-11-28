package vaulsys.network.filters;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;

public class Mina2HDLCIoFilter extends IoFilterAdapter {
    transient Logger logger = Logger.getLogger(Mina2HDLCIoFilter.class);
    public static byte[] syncHDLCHeader = new byte[]{ 0x60, 0x00, 0x01};
	public static int realLen = 4+syncHDLCHeader.length+2;

    @SuppressWarnings("unchecked")
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
    	logger.debug("Filter Message Received from: " + session.getRemoteAddress());
        ArrayList<Byte> binaryReceiveBytes = (ArrayList<Byte>) session.getAttribute("binaryReceiveBytes");

        if (binaryReceiveBytes == null) {
            binaryReceiveBytes = new ArrayList<Byte>();
            session.setAttribute("binaryReceiveBytes", binaryReceiveBytes);
        }

        IoBuffer byteMessage = (IoBuffer) message;

        while (byteMessage.hasRemaining())
            binaryReceiveBytes.add(byteMessage.get());

        logger.debug("Total yet received:" + binaryReceiveBytes.toString());
        if (binaryReceiveBytes.size() < realLen){
        	int index = 0;
            for(; index<syncHDLCHeader.length && index<binaryReceiveBytes.size(); index++){
            	if(binaryReceiveBytes.get(index) != syncHDLCHeader[index]){
            		logger.debug("Invalid syncHDLCHeader, expected: "+syncHDLCHeader[index]+" found: "+binaryReceiveBytes.get(index));
            		binaryReceiveBytes.clear();
            	}
            }
            return;
        }

        while (binaryReceiveBytes.size() >= realLen) {
        	int index = 0;
            for(; index<syncHDLCHeader.length; index++){
            	if(binaryReceiveBytes.get(index) != syncHDLCHeader[index]){
            		logger.debug("Invalid syncHDLCHeader, expected: "+syncHDLCHeader[index]+" found: "+binaryReceiveBytes.get(index));
            		binaryReceiveBytes.clear();
            		return;
            	}
            }
            index = syncHDLCHeader.length;
            byte hdlc1 = binaryReceiveBytes.get(index++);
            byte hdlc2 = binaryReceiveBytes.get(index++);
            session.setAttribute("HDLC-1", hdlc1);
            session.setAttribute("HDLC-2", hdlc2);
//            index += 2;
            
        	int b1 = binaryReceiveBytes.get(index++) - 48;
            int b2 = binaryReceiveBytes.get(index++) - 48;
            int b3 = binaryReceiveBytes.get(index++) - 48;
            int b4 = binaryReceiveBytes.get(index++) - 48;

            int len = (b1 * 1000) + b2 * 100 + b3 * 10 + b4 + realLen;

            if (binaryReceiveBytes.size() >= len) {
                byte[] actualMessage = new byte[len - realLen];

                for (int i = 0; i < len - realLen; i++)
                    actualMessage[i] = (byte) binaryReceiveBytes.get(i + realLen);

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

        byte[] messageBytes = (byte[]) writeRequest.getMessage(); // ((OutgoingMessage)writeRequest.getMessage()).getBinaryData();
        IoBuffer buff = IoBuffer.wrap(messageBytes);

        byte[] data = buff.array();
        byte[] binData = new byte[data.length + realLen];
        System.arraycopy(data, 0, binData, realLen, data.length);

        int index = 0;
        
        binData[index+0] = (byte) syncHDLCHeader[index+0];
        binData[index+1] = (Byte) session.getAttribute("HDLC-1");
        binData[index+2] = (Byte) session.getAttribute("HDLC-2");
        binData[index+3] = (byte) syncHDLCHeader[index+1];
        binData[index+4] = (byte) syncHDLCHeader[index+2];

        index = syncHDLCHeader.length+2;
        
        binData[index+0] = (byte) (data.length / 1000 + 48);
        binData[index+1] = (byte) ((data.length % 1000) / 100 + 48);
        binData[index+2] = (byte) ((data.length % 100) / 10 + 48);
        binData[index+3] = (byte) (data.length % 10 + 48);

        super.filterWrite(nextFilter, session, new DefaultWriteRequest(IoBuffer.wrap(binData)));
    }
}
