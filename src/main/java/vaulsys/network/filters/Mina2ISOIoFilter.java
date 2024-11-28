package vaulsys.network.filters;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;

public class Mina2ISOIoFilter extends IoFilterAdapter {
    transient Logger logger = Logger.getLogger(Mina2ISOIoFilter.class);

    @SuppressWarnings("unchecked")
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

        while (byteMessage.hasRemaining())
            binaryReceiveBytes.add(byteMessage.get());

    	if(logger.isTraceEnabled()){
    		logger.trace("Total yet received:" + binaryReceiveBytes.toString());
    	}
       	
        if (binaryReceiveBytes.size() < 4)
            return;

        while (binaryReceiveBytes.size() >= 4) {
            int b1 = binaryReceiveBytes.get(0) - 48;
            int b2 = binaryReceiveBytes.get(1) - 48;
            int b3 = binaryReceiveBytes.get(2) - 48;
            int b4 = binaryReceiveBytes.get(3) - 48;

            int len = (b1 * 1000) + b2 * 100 + b3 * 10 + b4 + 4;

            if (binaryReceiveBytes.size() >= len) {
                byte[] actualMessage = new byte[len - 4];

                for (int i = 0; i < len - 4; i++)
                    actualMessage[i] = (byte) binaryReceiveBytes.get(i + 4);

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
        byte[] binData = new byte[data.length + 4];
        System.arraycopy(data, 0, binData, 4, data.length);

        // TODO does not work for message larger than 255 byte

        binData[0] = (byte) (data.length / 1000 + 48);
        binData[1] = (byte) ((data.length % 1000) / 100 + 48);
        binData[2] = (byte) ((data.length % 100) / 10 + 48);
        binData[3] = (byte) (data.length % 10 + 48);

        super.filterWrite(nextFilter, session, new DefaultWriteRequest(IoBuffer.wrap(binData)));
    }
}
