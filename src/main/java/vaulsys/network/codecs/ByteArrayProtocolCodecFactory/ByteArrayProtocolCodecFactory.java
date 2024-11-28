package vaulsys.network.codecs.ByteArrayProtocolCodecFactory;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * Created by HP on 8/1/2018.
 */
public class ByteArrayProtocolCodecFactory implements ProtocolCodecFactory {
    //private ProtocolEncoder encoder;
    //private ProtocolDecoder decoder;

    @Override
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return new RequestEncoder();
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return new RequestDecoder();
    }
}
