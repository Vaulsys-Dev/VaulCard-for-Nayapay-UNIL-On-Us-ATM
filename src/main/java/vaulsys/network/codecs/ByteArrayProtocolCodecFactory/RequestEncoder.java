package vaulsys.network.codecs.ByteArrayProtocolCodecFactory;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.nio.ByteBuffer;

/**
 * Created by HP on 8/1/2018.
 */
public class RequestEncoder extends ProtocolEncoderAdapter implements ProtocolEncoder {
    transient Logger logger = Logger.getLogger(RequestEncoder.class);

    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        byte[] bytes = (byte[]) message;
        logger.info("Encoding Message");
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 2);
        // write the fixed length header
        buffer.putShort((short)bytes.length);
        // write the payload
        buffer.put(bytes);
        buffer.flip();
        logger.debug("Encoded Message [" + new String(bytes) + "]");
        out.write(buffer);
    }

    public void dispose(IoSession session) throws Exception {
        // nothing to dispose
    }
}
