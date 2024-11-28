package vaulsys.network.mina2;

import vaulsys.netmgmt.extended.NetworkInfoStatus;
import vaulsys.network.channel.base.OutputChannel;
import vaulsys.network.codecs.ByteArrayProtocolCodecFactory.ByteArrayProtocolCodecFactory;
import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Mina2Connector {
	transient Logger logger = Logger.getLogger(Mina2Connector.class);

	private OutputChannel channel;
	private List<IoFilter> filters;
	private IoHandler handler;

	public NioSocketConnector connector;
	private ConcurrentLinkedQueue<IoSession> sessionQueue;
	private Integer maxSessionNumber;
	private Integer numPendingConnections;
	private Boolean isClosing;

	public Mina2Connector(OutputChannel channel, List<IoFilter> filters, IoHandler handler) {
		this.isClosing = false;
		this.channel = channel;
		this.filters = filters;
		this.maxSessionNumber = channel.getMaxSessionNumber();
		if (this.maxSessionNumber == null)
			maxSessionNumber = 1;

		if (this.filters == null) {
			this.filters = new ArrayList<IoFilter>();
		}

		this.handler = handler;

		this.connector = new NioSocketConnector();

		if(channel.getSslEnable())
			connector.getFilterChain().addFirst("sslFilter", SSLContextGenerator.createSslFilter(true));

		//m.rehman: for message chunk handling on socket
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayProtocolCodecFactory()));

        for (IoFilter filter : this.filters) {
			if (!connector.getFilterChain().contains(filter.getClass().getName()))
				connector.getFilterChain().addLast(filter.getClass().getName(), filter);
		}

		connector.setHandler(handler);

		sessionQueue = new ConcurrentLinkedQueue<IoSession>();
		numPendingConnections = 0;

		connector.setConnectTimeoutMillis(20000);
		// connector.getSessionConfig().setReuseAddress(true);

		connector.getSessionConfig().setSendBufferSize(Mina2IoHandler.MAX_BUFFER_SIZE);
		connector.getSessionConfig().setTcpNoDelay(true);
		connector.getSessionConfig().setReadBufferSize(Mina2IoHandler.MAX_BUFFER_SIZE);
		connector.getSessionConfig().setReceiveBufferSize(Mina2IoHandler.MAX_BUFFER_SIZE);
		connector.getSessionConfig().setBothIdleTime(channel.getKeepAlive());

//		connector.getSessionConfig().setWriteTimeout(10000);

//		connect();
	}

	public synchronized void connect() {
    	synchronized (isClosing) {
    		if (isClosing.equals(Boolean.TRUE)){
    			//logger.info("Connector "+channel.getName()+"is closing, return from connect method..."); //Raza LOGGING ENHANCES - Removing from Info
				logger.debug("Connector "+channel.getName()+"is closing, return from connect method...");
				//Raza TPSP Channel Add setting Comms Status OK start
				//NetworkManager.SetCommsDOWN(channel);
				channel.setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED);
				//Raza TPSP Channel Add setting Comms Status OK end
				return;
    		}
		}

		int queueSize = sessionQueue.size();

		for (int i = queueSize; i < maxSessionNumber; i++) {
			synchronized (numPendingConnections) {
				if (numPendingConnections + sessionQueue.size() == maxSessionNumber) {
					//logger.info(channel.getName()+" numPendingConnections(" + numPendingConnections + ")+sessionQueue.size("
							//+ sessionQueue.size() + ") == maxSessionNumber"); //Raza LOGGING ENHANCED - Removing From Info
					logger.debug(channel.getName()+" numPendingConnections(" + numPendingConnections + ")+sessionQueue.size("
							+ sessionQueue.size() + ") == maxSessionNumber"); //Raza LOGGING ENHANCED
					break;
				}
				numPendingConnections++;
				//logger.info(channel.getName()+" 1-numPendingConnections=" + numPendingConnections); //Raza LOGGING ENHANCED
				logger.debug(channel.getName()+" 1-numPendingConnections=" + numPendingConnections); //Raza LOGGING ENHANCED
			}

			//logger.info("Connecting to "+channel.getName()+" [" + i + "]: " + channel.getRemoteAddress()); //Raza LOGGING ENHANCED
			logger.debug("Connecting to "+channel.getName()+" [" + i + "]: " + channel.getRemoteAddress()); //Raza LOGGING ENHANCED
			connector.connect(channel.getRemoteAddress()).addListener(new IoFutureListener<ConnectFuture>() {
				@Override
				public void operationComplete(ConnectFuture future) {
					if (future.isConnected()) {
						boolean offer = sessionQueue.offer(future.getSession());
						//logger.info("Session created with "+channel.getName()+": " + future.getSession()); //Raza LOGGING ENHANCED
						logger.debug("Session created with "+channel.getName()+": " + future.getSession()); //Raza LOGGING ENHANCED
						if (offer == false) {
							//logger.info("Could not add session to queue "+channel.getName()+"...."); //Raza LOGGING ENHANCED
							logger.debug("Could not add session to queue "+channel.getName()+"...."); //Raza LOGGING ENHANCED
						}
					} else if (future.getException() != null) {
						//logger.error("Could not connect to "+channel.getName()+":" + future.getException()); //Raza LOGGING ENHANCED
						logger.debug("Could not connect to "+channel.getName()+":" + future.getException()); //Raza LOGGING ENHANCED
						//Raza TPSP Channel Add setting Comms Status OK start
						//NetworkManager.SetCommsDOWN(channel); //Set Comms Down of Provided Channel
						channel.setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED);
						//Raza TPSP Channel Add setting Comms Status OK end
						closeImmediately0();
					}
					synchronized (numPendingConnections) {
						numPendingConnections--;
						//logger.info(channel.getName()+"2-numPendingConnections=" + numPendingConnections); //Raza LOGGING ENHANCED
						logger.debug(channel.getName()+"2-numPendingConnections=" + numPendingConnections); //Raza LOGGING ENHANCED
					}
				}
			});
		}
	}
	//Raza Client I am connection start
	public synchronized void reconnect() {
		synchronized (isClosing) {
			if (isClosing.equals(Boolean.TRUE)){
				//logger.info("Connector "+channel.getName()+"is closing, return from connect method...");
				//Raza TPSP Channel Add setting Comms Status OK start
				//NetworkManager.SetCommsDOWN(channel);
				channel.setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED);
				//Raza TPSP Channel Add setting Comms Status OK end
				return;
			}
		}

		int queueSize = sessionQueue.size();

			connector.connect(channel.getRemoteAddress()).addListener(new IoFutureListener<ConnectFuture>() {
				@Override
				public void operationComplete(ConnectFuture future) {
                    //try {
					//	future.await(2000); //Raza TEMP
					//}
					//catch (Exception e)
					//{
					//	e.printStackTrace();
					//}

					if (future.isConnected()) {
						boolean offer = sessionQueue.offer(future.getSession());
						//logger.info("Session created with "+channel.getName()+": " + future.getSession()); //Raza LOGGING ENHANCED
						logger.debug("Session created with "+channel.getName()+": " + future.getSession()); //Raza LOGGING ENHANCED
						if (offer == false) {
							//logger.info("Could not add session to queue "+channel.getName()+"...."); //Raza LOGGING ENHANCED
							logger.debug("Could not add session to queue "+channel.getName()+"...."); //Raza LOGGING ENHANCED
						}

					} else if (future.getException() != null) {
						//logger.error("Could not connect to "+channel.getName()+":" + future.getException()); //Raza LOGGING ENHANCED
						logger.debug("Could not connect to "+channel.getName()+":" + future.getException()); //Raza LOGGING ENHANCED
						//Raza TPSP Channel Add setting Comms Status OK start
						//NetworkManager.SetCommsDOWN(channel); //Set Comms Down of Provided Channel
						channel.setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED);
						//Raza TPSP Channel Add setting Comms Status OK end
						closeImmediately0();
					}

				}
			});
		}
	//Raza Client I am connection end

    public void closeImmediately0() {
        // We need to close the channel immediately to remove it from the
        // server session's channel table and *not* send a packet to the
        // client.  A notification was already sent by our caller, or will
        // be sent after we return.
        //
        close(true);

        // We also need to dispose of the connector, but unfortunately we
        // are being invoked by the connector thread or the connector's
        // own processor thread.  Disposing of the connector within either
        // causes deadlock.  Instead create a new thread to dispose of the
        // connector in the background.
        //
//        new Thread("ChannelDirectTcpip-ConnectorCleanup") {
//            @Override
//            public void run() {
//                connector.dispose();
//            }
//        }.start();
    }

    public void close(boolean immediately) {
    	synchronized (isClosing) {
    		isClosing = true;
		}

    	for(IoSession session:sessionQueue){
    		logger.info("Closing session "+channel.getName()+":" + session);
    		session.close(immediately).addListener(new IoFutureListener<CloseFuture>(){
              public void operationComplete(CloseFuture closeFuture) {
            	  logger.info("session closed "+channel.getName()+":"+closeFuture.toString());
              }
    		});
    	}

    	sessionQueue.clear();
		isClosing = false; //Mati Adding in case of Server Reconnect while switch is UP. Switch was getting stuck in closing state
    }

    public void open(){
    	synchronized (isClosing) {
    		isClosing = false;
    	}

    	connect();
    }

//	public IoSession reconnect(IoSession session) {
//		ConnectFuture future1 = connector.connect(((OutputChannel) channel).getRemoteAddress(), handler, config);
//		future1.join();
//		if (!future1.isConnected()) {
//			logger.error("Connection to " + ((OutputChannel) channel).getRemoteAddress().toString() + "failed!");
//			return null;
//		}
//
//		IoSession session2 = future1.getSession();
//		for (int i = 0; i < sessionList.length; i++) {
//			if (sessionList[i] == null || sessionList[i].equals(session)) {
//				sessionList[i] = session2;
//				break;
//			}
//		}
//
//		logger.info("Connecting to " + ((OutputChannel) channel).getRemoteAddress().toString() + " session: "
//						+ session);
//
//		return session2;
//	}

	public OutputChannel getChannel() {
		return channel;
	}

	public void setChannel(OutputChannel channel) {
		this.channel = channel;
	}

	public List<IoFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<IoFilter> filters) {
		this.filters = filters;
	}

	public IoHandler getHandler() {
		return handler;
	}

	public void setHandler(IoHandler handler) {
		this.handler = handler;
	}

	public synchronized IoSession getSession(){
    	if(sessionQueue == null || sessionQueue.size() < maxSessionNumber){
			return null; //m.rehman returning null for getseesion avoid multiple session
			//connect();
    	}
    	IoSession session = sessionQueue.poll();
		boolean isSessionFound = false;


		while(session != null && !isSessionFound){
    		if(session != null && session.isConnected()){
    			sessionQueue.offer(session);
    			isSessionFound = true;
    		}else if(!session.isConnected()){
    			session.close(true);
    			session = sessionQueue.poll();
    		}
		}

		//again it checks the pool, since it is possible that we removed some session from queue
//    	if(sessionQueue == null || sessionQueue.size() < maxSessionNumber){
//    		connect();
//    	}

		return session;
	}

	public int getMaxSessionNumber() {
		return maxSessionNumber;
	}

	public void setMaxSessionNumber(int maxSessionNumber) {
		this.maxSessionNumber = maxSessionNumber;
	}

	public void removeSessionFromQueue(IoSession session){
		sessionQueue.remove(session);
	}
}
