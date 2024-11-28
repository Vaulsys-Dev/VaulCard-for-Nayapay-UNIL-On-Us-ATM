package vaulsys.network.filters;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;

import java.util.ArrayList;

public class Mina2PayShield2000IoFilter extends IoFilterAdapter {
    transient Logger logger = Logger.getLogger(Mina2PayShield2000IoFilter.class);

    @SuppressWarnings("unchecked")
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
        logger.debug("Filter Message Received from: " + session.getRemoteAddress());
        ArrayList<Byte> binaryReceiveBytes = (ArrayList<Byte>) session.getAttribute("binaryReceiveBytes");

        if (binaryReceiveBytes == null) {
            binaryReceiveBytes = new ArrayList<Byte>();
            session.setAttribute("binaryReceiveBytes", binaryReceiveBytes);
        }

        IoBuffer byteMessage = (IoBuffer) message;
        //System.out.println("Mina2PayShield2000IOFilter:: Clearing binaryReceiveBytes..!"); //Raza TEMP
        binaryReceiveBytes.clear(); //Raza Clearing previous messages
        //System.out.println("Mina2PayShield2000IOFilter:: Here.."); //Raza TEMP
        while (byteMessage.hasRemaining()) {
            binaryReceiveBytes.add(byteMessage.get());
        }
        logger.debug("Total yet received:" + binaryReceiveBytes.toString());
        if (binaryReceiveBytes.size() < 6) {
            return;
        }
        byte[] actualMessage = new byte[binaryReceiveBytes.size()];
        //String temp = new String(actualMessage); //Raza TEMP
        //System.out.println("Mina2PayShield2000IoFilter:: actualMessage [" + temp + "]"); //Raza TEMP
        for (int i = 0; i <  binaryReceiveBytes.size(); i++) {
            actualMessage[i] = (byte) binaryReceiveBytes.get(i);
        }

                super.messageReceived(nextFilter, session, actualMessage);
        }


    @Override
    public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {

        byte[] messageBytes = (byte[]) writeRequest.getMessage(); // ((OutgoingMessage)writeRequest.getMessage()).getBinaryData();
        IoBuffer buff = IoBuffer.wrap(messageBytes);
        // buff.flip();
        super.filterWrite(nextFilter, session, new DefaultWriteRequest(buff));
    }
}
