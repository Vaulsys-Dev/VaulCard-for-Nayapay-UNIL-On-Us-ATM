package vaulsys.network.mina2;

import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.network.codecs.ByteArrayProtocolCodecFactory.ByteArrayProtocolCodecFactory;
import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;

public class Mina2Acceptor {
    private static final Logger logger = Logger.getLogger(Mina2Acceptor.class);

    NioSocketAcceptor acceptor = null;

    Channel channel = null;
    List<IoFilter> filters = null;
    IoHandler handler = null;

    public Mina2Acceptor(Channel channel, List<IoFilter> filters, IoHandler handler) {
        super();
        this.channel = channel;
        this.filters = filters;

        if (this.filters == null) {
            this.filters = new ArrayList<IoFilter>();
        }

        // this.filters.add(new DefaultIoFilter());
        // this.filters.add(new LoggingFilter());
        this.handler = handler;
    }

    public void listen() throws IOException {
        if (channel instanceof InputChannel) {
            InputChannel inChannel = (InputChannel) channel;

            acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);

            if(channel.getSslEnable())
            	acceptor.getFilterChain().addFirst("sslFilter", SSLContextGenerator.createSslFilter(false));

            //m.rehman: for message chunk handling on socket
            acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayProtocolCodecFactory()));
    		acceptor.getFilterChain().addLast("toProtocolSession", filters.get(0));
    		acceptor.setHandler(handler);

            acceptor.getSessionConfig().setSendBufferSize(Mina2IoHandler.MAX_BUFFER_SIZE);
            acceptor.getSessionConfig().setTcpNoDelay(true);
            acceptor.getSessionConfig().setReadBufferSize(Mina2IoHandler.MAX_BUFFER_SIZE);
            acceptor.getSessionConfig().setReceiveBufferSize(Mina2IoHandler.MAX_BUFFER_SIZE);
            acceptor.getSessionConfig().setBothIdleTime(inChannel.getKeepAlive());
            acceptor.setReuseAddress(true);

            int retry = 0;
            IOException e1 = null;
            boolean successful = false;
            while(retry < 100 && !successful){
	            try {
	                acceptor.bind(inChannel.getLocalAddress());
	                successful = true;
	                break;
	            } catch (IOException e) {
	                if (e instanceof BindException) {
	                	logger.error("BindException retry: "+retry+" "+e.getMessage() + " " + inChannel.getLocalAddress());
	                	e1 = new BindException(e.getMessage() + " " + inChannel.getLocalAddress()+e.getCause());
	                } else{
	                	logger.error("IOException retry: "+retry+" "+e.getMessage() + " " + inChannel.getLocalAddress());
	                	e1 = e;
	                }
	            }
	            retry++;
            	try {
					Thread.sleep(5000);
				} catch (InterruptedException e2) {
				}
	        }
            if(!successful){
            	throw e1;
            }
            
            logger.debug("Listening on "+inChannel.getLocalAddress().toString());
        }
    }

    public void close() {
        if (acceptor != null) {
            acceptor.unbind();
        }
        
        acceptor.dispose();
    }
}
