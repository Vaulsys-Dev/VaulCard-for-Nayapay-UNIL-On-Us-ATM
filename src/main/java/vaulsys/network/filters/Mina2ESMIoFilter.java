package vaulsys.network.filters;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;

public class Mina2ESMIoFilter extends IoFilterAdapter {
    transient Logger logger = Logger.getLogger(Mina2ESMIoFilter.class);

    @SuppressWarnings("unchecked")
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
        logger.debug("Filter Message Received from: " + session.getRemoteAddress());
        ArrayList<Byte> binaryReceiveBytes = (ArrayList<Byte>) session.getAttribute("binaryReceiveBytes");

        if (binaryReceiveBytes == null) {
            binaryReceiveBytes = new ArrayList<Byte>();
            session.setAttribute("binaryReceiveBytes", binaryReceiveBytes);
        }

        IoBuffer byteMessage = (IoBuffer) message;

        while (byteMessage.hasRemaining()) {
            binaryReceiveBytes.add(byteMessage.get());
        }
        logger.debug("Total yet received:" + binaryReceiveBytes.toString());
        if (binaryReceiveBytes.size() < 6) {
            return;
        }

        while (binaryReceiveBytes.size() >= 6) {
            int b1 = binaryReceiveBytes.get(4);
            int b2 = binaryReceiveBytes.get(5);

            int len = (b2 >= 0 ? b2 : b2 + 256) + (b1 >= 0 ? b1 : b1 + 256) * 256 + 6;

            if (binaryReceiveBytes.size() >= len) {
                byte[] actualMessage = new byte[len];

                // Object[] part1 = (Object[]) binaryReceiveBytes.toArray();
                // System.arraycopy(part1, 0, actualMessage, 0, part1.length);

                for (int i = 0; i < len; i++) {
                    actualMessage[i] = (byte) binaryReceiveBytes.get(i);
                }

                // List<Byte> subList = binaryReceiveBytes.subList(len, binaryReceiveBytes.size());
                ArrayList<Byte> subList = new ArrayList<Byte>();
                for (int i = len; i < binaryReceiveBytes.size(); i++)
                    subList.add(binaryReceiveBytes.get(i));
                binaryReceiveBytes.clear();// = (ArrayList<Byte>)
                binaryReceiveBytes.addAll(subList);
                // byteMessage.get(actualMessage, binaryReceiveBytes.size(), len-binaryReceiveBytes.size());

                // binaryReceiveBytes.clear();
                // while(byteMessage.hasRemaining()) {
                // binaryReceiveBytes.add(byteMessage.get());
                // }
                super.messageReceived(nextFilter, session, actualMessage);
            } else {
                return;
                // while(byteMessage.hasRemaining()) {
                // binaryReceiveBytes.add(byteMessage.get());
                // }
            }
        }
    }

    @Override
    public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {

        byte[] messageBytes = (byte[]) writeRequest.getMessage(); // ((OutgoingMessage)writeRequest.getMessage()).getBinaryData();
        IoBuffer buff = IoBuffer.wrap(messageBytes);
        // buff.flip();
        super.filterWrite(nextFilter, session, new DefaultWriteRequest(buff));
    }
}
