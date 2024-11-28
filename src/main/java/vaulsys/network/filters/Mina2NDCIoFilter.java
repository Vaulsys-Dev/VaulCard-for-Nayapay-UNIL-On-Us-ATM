package vaulsys.network.filters;

import vaulsys.security.hsm.eracom.base.HSMUtil;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;

public class Mina2NDCIoFilter  extends IoFilterAdapter {
    transient Logger logger = Logger.getLogger(Mina2NDCIoFilter.class);

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
        if (binaryReceiveBytes.size() < 2)
            return;

        while (binaryReceiveBytes.size() >= 2) {
            int b1 = HSMUtil.byteToInt(binaryReceiveBytes.get(0));
            int b2 = HSMUtil.byteToInt(binaryReceiveBytes.get(1));

            int len = (b1 * 256) + b2 + 2;

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
        byte[] messageBytes = (byte[]) writeRequest.getMessage(); // ((OutgoingMessage)writeRequest.getMessage()).getBinaryData();
        IoBuffer buff = IoBuffer.wrap(messageBytes);

        byte[] data = buff.array();
        byte[] binData = new byte[data.length + 2];
        System.arraycopy(data, 0, binData, 2, data.length);

        // TODO does not work for message larger than 255 byte

        binData[0] = (byte) (data.length / 256);
        binData[1] = (byte) ((data.length % 256));

        super.filterWrite(nextFilter, session, new DefaultWriteRequest(IoBuffer.wrap(binData)));
    }
}
