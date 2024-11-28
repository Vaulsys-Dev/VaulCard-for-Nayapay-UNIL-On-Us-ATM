package vaulsys.network.filters;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;

import java.util.ArrayList;

public class Mina2NetworkUiIoFilter extends IoFilterAdapter{
    transient Logger logger = Logger.getLogger(Mina2NetworkUiIoFilter.class);

    @SuppressWarnings("unchecked")
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
        logger.debug("Filter Message Received from: " + session.getRemoteAddress());
        ArrayList<Byte> binaryReceiveBytes = (ArrayList<Byte>) session.getAttribute("binaryReceiveBytes");

        if (binaryReceiveBytes == null) {
            binaryReceiveBytes = new ArrayList<Byte>();
            session.setAttribute("binaryReceiveBytes", binaryReceiveBytes);
        }

        IoBuffer byteMessage = (IoBuffer) message;

        //for logging purpose
        System.out.println(byteMessage.getHexDump());

        //get hex length and convert in decimal
        String lengthInHex = ((IoBuffer) message).getHexDump();
        lengthInHex = lengthInHex.substring(0,11);
        lengthInHex = lengthInHex.replaceAll(" ", "");
        int lengthInInt = Integer.parseInt(lengthInHex, 16);

        while (byteMessage.hasRemaining())
            binaryReceiveBytes.add(byteMessage.get());

        logger.debug("Total yet received:" + binaryReceiveBytes.toString());

        if (binaryReceiveBytes.size() < 4)
            return;

        while (binaryReceiveBytes.size() >= 4) {
            int len = lengthInInt + 4;

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
    	
    	byte[] messageBytes = (byte[]) writeRequest.getMessage(); 
    	IoBuffer buff = IoBuffer.wrap(messageBytes);

        byte[] data = buff.array();
        byte[] binData = new byte[data.length + 4];
        System.arraycopy(data, 0, binData, 4, data.length);

        // TODO does not work for message larger than 255 byte

        String length;
        length = Integer.toHexString(data.length);
        length = StringUtils.leftPad(length, 8, "0");
        binData[0] = (byte)(Integer.parseInt(length.substring(0,2), 16));
        binData[1] = (byte)(Integer.parseInt(length.substring(2,4), 16));
        binData[2] = (byte)(Integer.parseInt(length.substring(4,6), 16));
        binData[3] = (byte)(Integer.parseInt(length.substring(6,8), 16));

        super.filterWrite(nextFilter, session, new DefaultWriteRequest(IoBuffer.wrap(binData)));

    }
}
