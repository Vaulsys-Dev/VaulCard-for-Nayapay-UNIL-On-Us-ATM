package vaulsys.network.codecs.ByteArrayProtocolCodecFactory;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by HP on 8/1/2018.
 */
public class RequestDecoder extends CumulativeProtocolDecoder {
    private static final Logger logger = Logger.getLogger(RequestDecoder.class);
    public static final int MAX_MSG_SIZE = 1000;
    //byte[] CumulativeBytes;


    private static class DecoderState {
        /** whether we have already read the fixed-length header */
        boolean headerRead = false;
        /** the length of the payload */
        int length;
        byte[] buffer;
    }

    /** the header has a fixed length of 2 bytes  */
    private final static int HEADER_LENGTH = 2;

    private final static String DECODER_STATE_KEY = RequestDecoder.class.getName() + ".DECODER_STATE";

    @Override
    protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
        logger.info("Going to Decode Received message");
        DecoderState state = (DecoderState) ioSession.getAttribute (DECODER_STATE_KEY);

        if (state == null) {
            state = new DecoderState();
            ioSession.setAttribute(DECODER_STATE_KEY, state);
        }

        if (!state.headerRead) {
            // we have not yet read the header => check if have enough bytes to read the fixed-length header
            if (ioBuffer.remaining() >= HEADER_LENGTH) {
                state.length = ioBuffer.getShort();

                //if state.length is zero, it means length is of four byte, doing getShort again to read next two bytes
                //msg will not be more than 2 bytes in length (2 bytes length = 65535)
                // i.e 3rd and 4th byte will not be used for length
                if (state.length == 0)
                    state.length = ioBuffer.getShort();

                state.headerRead = true;
            } else {
                // not enough bytes to decode a message, MINA will call us again when there is more data available
                logger.info("Decode Failed");
                return false;
            }
        }



        if (state.headerRead) {
            // we have already read the lengt header, check if all the data is available
            logger.info("Message Length [" + state.length + "]");
            if(state.length > MAX_MSG_SIZE)
            {
                logger.info("Invalid Message with length [" + state.length + "] recevied. rejecting...");
                return false;
            }

            if(ioBuffer.remaining() == state.length)
            {
                logger.info("Single Message Received Expected Length [" + state.length + "], Received Length [" + ioBuffer.remaining() + "]");
                state.buffer = new byte[ioBuffer.remaining()]; //new byte[state.length];
                logger.debug("Message HexDump: [" + ioBuffer.getHexDump() + "]");
                ioBuffer.get(state.buffer);
                //RequestMessage rm = new RequestMessage(state.buffer ,state.buffer);
                protocolDecoderOutput.write(state.buffer);
                ioSession.removeAttribute(DECODER_STATE_KEY);
                logger.info("Decode Message Done OK");
                return true;
            }
            else if((RequestMessage.rm != null) && (RequestMessage.rm.getMsglength() -  ioBuffer.remaining()) == RequestMessage.rm.getLastmsgcount()) //176 - 50 = 126
            {
                logger.info("Message in Chunks Received Expected Length [" + state.length + "], Received Length [" + ioBuffer.remaining() + "]");
                RequestMessage.rm.setLastmsgcount(ioBuffer.remaining());
                logger.info("Count Bytes Read [" + RequestMessage.rm.getLastmsgcount() + "]");
                byte[] temp = new byte[ioBuffer.remaining()];
                ioBuffer.get(temp);
                appendBytes(temp);
                logger.info("Next Message Chunks Received Expected Length [" + state.length + "], Received Length [" + ioBuffer.remaining() + "]");
            }
            else
            {
                logger.info("Partial Message Received Expected Length [" + state.length + "], Received Length [" + ioBuffer.remaining() + "]");
                int i = ioBuffer.remaining();
                logger.info("Count Bytes Read [" + i + "]");
                byte[] temp = new byte[ioBuffer.remaining()];
                ioBuffer.get(temp);
                RequestMessage.rm = new RequestMessage(temp);
                RequestMessage.rm.setMsglength(state.length);
                RequestMessage.rm.setLastmsgcount(i);
                logger.info("Will wait for next Chunk of Message...");
                return true;
            }

            if((RequestMessage.rm != null) && (RequestMessage.rm.getCumulativeBytes().length == state.length)) {
                logger.info("Complete Message Chunks Received for Message");
                protocolDecoderOutput.write(RequestMessage.rm.getCumulativeBytes());
                ioSession.removeAttribute(DECODER_STATE_KEY);
                logger.info("Decode Message Done OK");
                RequestMessage.rm = null;
                return true;
            }

//            //int count = 0;
//            int iterations = 0;
//            while(lastMsgCount < state.length && iterations < 5)
//            {
//                if(ioBuffer.remaining() == state.length)
//                {
//                    logger.info("Single Message Received Expected Length [" + state.length + "], Received Length [" + ioBuffer.remaining() + "]");
//                    state.buffer = new byte[ioBuffer.remaining()]; //new byte[state.length];
//                    logger.info("first part: [" + ioBuffer.getHexDump() + "]");
//                    ioBuffer.get(state.buffer);
//                    //RequestMessage rm = new RequestMessage(state.buffer ,state.buffer);
//                    protocolDecoderOutput.write(state.buffer);
//                    ioSession.removeAttribute(DECODER_STATE_KEY);
//                    logger.info("Decode Message Done OK");
//                    return true;
//                }
//                else
//                {
//                    lastMsgCount += ioBuffer.remaining();
//                    logger.info("Count Bytes Read [" + lastMsgCount + "]");
//                    byte[] temp = new byte[ioBuffer.remaining()];
//                    ioBuffer.get(temp);
//                    appendBytes(temp);
//                }
//                Thread.sleep(100); //Raza please update this.
//                iterations++;
//            }




//            if(state.buffer == null) //1st part of message
//            {
//                logger.info("reading message first part");
//                if (ioBuffer.remaining() >= HEADER_LENGTH) {
//                    state.buffer = new byte[ioBuffer.remaining()]; //new byte[state.length];
//                    logger.info("first part: [" + ioBuffer.getHexDump() + "]");
//                    ioBuffer.get(state.buffer);
//                }
//                else
//                {
//                    // not enough bytes available
//                    logger.info("Decode Failed for first message, not enough bytes");
//                    return false;
//                }
//
//            }
//            if(state.buffer != null) //2nd part of message
//            {
//                logger.info("reading message second part");
//                logger.info("message second part ramining [" + ioBuffer.remaining() + "] Header Length [" + HEADER_LENGTH + "]");
//                if (ioBuffer.remaining() >= HEADER_LENGTH) {
//                    byte[] bytes = new byte[ioBuffer.remaining()];  //new byte[state.length];
//                    logger.info("second part: [" + ioBuffer.getHexDump() + "]");
//                    ioBuffer.get(bytes);
//                    RequestMessage rm = new RequestMessage(state.buffer ,bytes);
//                    protocolDecoderOutput.write(rm);
//                    ioSession.removeAttribute(DECODER_STATE_KEY);
//                    logger.info("Decode Message Done OK");
//                    return true;
//                }
//                else
//                {
//                    // not enough bytes available
//                    logger.info("Second Message not Present");
//                    RequestMessage rm = new RequestMessage(state.buffer ,null);
//                    protocolDecoderOutput.write(rm);
//                    ioSession.removeAttribute(DECODER_STATE_KEY);
//                    logger.info("Decode Message Done OK");
//                    return true;
//                }


                //**********************************************************

//                if (ioBuffer.prefixedDataAvailable(2, MAX_MSG_SIZE)) //if (ioBuffer.remaining() > 2) //as header is of length 2 bytes
//                {
//                    byte[] bytes = null;
//                    bytes = getBuffer(ioBuffer);
//                    RequestMessage rm = new RequestMessage(state.buffer ,bytes);
//                    protocolDecoderOutput.write(rm);
//
//                    // remove the decoder state to be ready for the next message
//                    ioSession.removeAttribute(DECODER_STATE_KEY);
//                    logger.info("Decode Message Done OK");
//                    return true;
//                }
//                else
//                {
//                    // not enough bytes available
//                    logger.info("Decode Failed for second message, not enough bytes");
//                    return false;
//                }
            }

            /* //Raza commenting working
            if (ioBuffer.remaining() >= state.length) {
                // ok, message complete
                byte[] bytes = new byte[ioBuffer.remaining()];  //new byte[state.length];
                ioBuffer.get(bytes);
                // this will cause IoHandler.messageReceived() to be called with a byte[] as the message
                protocolDecoderOutput.write(bytes);

                // remove the decoder state to be ready for the next message
                ioSession.removeAttribute(DECODER_STATE_KEY);
                logger.info("Decode Message Done OK");
                return true;
            }
            // not enough bytes available
            logger.info("Decode Failed not enough bytes");
            return false;*/
//        }
        logger.info("Decode Failed eventually");
        RequestMessage.rm = null;
        return false;
    }


    /*
    @Override
    protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
        logger.info("Going to Decode Received message");
        DecoderState state = (DecoderState) ioSession.getAttribute (DECODER_STATE_KEY);

        if (state == null) {
            state = new DecoderState();
            ioSession.setAttribute(DECODER_STATE_KEY, state);
        }

        if (!state.headerRead) {
            // we have not yet read the header => check if have enough bytes to read the fixed-length header
            if (ioBuffer.remaining() >= HEADER_LENGTH) {
                state.length = ioBuffer.getShort();
                state.headerRead = true;
            } else {
                // not enough bytes to decode a message, MINA will call us again when there is more data available
                logger.info("Decode Failed");
                return false;
            }
        }
        if (state.headerRead) {
            // we have already read the lengt header, check if all the data is available
            logger.info("Message Length [" + state.length + "]");

            if (ioBuffer.remaining() >= state.length) {
                // ok, message complete
                byte[] bytes = new byte[ioBuffer.remaining()];  //new byte[state.length];
                ioBuffer.get(bytes);

                // this will cause IoHandler.messageReceived() to be called with a byte[] as the message
                protocolDecoderOutput.write(bytes);

                // remove the decoder state to be ready for the next message
                ioSession.removeAttribute(DECODER_STATE_KEY);
                logger.info("Decode Message Done OK");
                return true;
            }
            // not enough bytes available
            logger.info("Decode Failed not enough bytes");
            return false;
        }
        logger.info("Decode Failed eventually");
        return false;
    }*/


    private byte[] getBuffer(IoBuffer in)
    {
            int length = in.getInt();
            logger.info("getBuffer:: Length [" + length + "]");
            byte[] bytes = new byte[length];
            in.get(bytes);
            logger.info("getBuffer:: Bytes [" + new String(bytes) + "]");
            return bytes;
    }

    public void appendBytes(byte[] input)
    {
        if(RequestMessage.rm == null)
        {
            RequestMessage.rm = new RequestMessage(input);
        }
        else
        {
            byte[] c = new byte[RequestMessage.rm.getCumulativeBytes().length + input.length];
            System.arraycopy(RequestMessage.rm.getCumulativeBytes(), 0, c, 0, RequestMessage.rm.getCumulativeBytes().length);
            System.arraycopy(input, 0, c, RequestMessage.rm.getCumulativeBytes().length, input.length);

            RequestMessage.rm.setCumulativeBytes(new byte[c.length]);
            RequestMessage.rm.setCumulativeBytes(c);
        }
    }

    private CharsetDecoder getCharsetDecoder ( final IoSession session )
    {
        if ( session.containsAttribute ( "charsetDecoder" ) )
        {
            return (CharsetDecoder)session.getAttribute ( "charsetDecoder" );
        }

        final CharsetDecoder decoder = Charset.forName ( "UTF-8" ).newDecoder ();
        session.setAttribute ( "charsetDecoder", decoder );
        return decoder;
    }
}
