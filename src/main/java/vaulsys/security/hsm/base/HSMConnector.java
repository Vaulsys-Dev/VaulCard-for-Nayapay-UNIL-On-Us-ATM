package vaulsys.security.hsm.base;


import vaulsys.netmgmt.extended.NetworkInfoStatus;
import vaulsys.network.base.Connector;
import vaulsys.network.codecs.ByteArrayProtocolCodecFactory.ByteArrayProtocolCodecFactory;
import vaulsys.persistence.GeneralDao;
import vaulsys.security.hsm.base.lock.FairLock;
import vaulsys.wfe.GlobalContext;
import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HSMConnector implements Connector {

    Logger logger = Logger.getLogger(HSMConnector.class);

    private Boolean isClosing;
    public NioSocketConnector connector;
    private IoSession session;
    private AtomicBoolean isSessionConnected;
    private List<IoFilter> filters;
    private IoHandler handler;

    private HSMChannel channel;

    private AtomicInteger loadCount;

    private FairLock fairLock;


    public HSMConnector(IoHandler handler, List<IoFilter> filters, HSMChannel channel) {
        this.isClosing = false;
        this.isSessionConnected = new AtomicBoolean(false);
        this.handler = handler;
        this.connector = new NioSocketConnector();
        this.filters = filters;
        if (this.filters == null) {
            this.filters = new ArrayList<IoFilter>();
        }

        //m.rehman: for message chunk handling on socket
        //connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayProtocolCodecFactory()));

        for (IoFilter filter : this.filters) {
            if (!connector.getFilterChain().contains(filter.getClass().getName()))
                connector.getFilterChain().addLast(filter.getClass().getName(), filter);
        }
        connector.setHandler(this.handler);

        this.channel = channel;

        this.loadCount = new AtomicInteger(0);

        this.fairLock = new FairLock();

        connector.setConnectTimeoutMillis(20000);
        connector.getSessionConfig().setSendBufferSize(HSMIOHandler.MAX_BUFFER_SIZE);
        connector.getSessionConfig().setTcpNoDelay(true); //dddd(true); //Raza commenting
        connector.getSessionConfig().setReadBufferSize(HSMIOHandler.MAX_BUFFER_SIZE);
        connector.getSessionConfig().setReceiveBufferSize(HSMIOHandler.MAX_BUFFER_SIZE);
        connector.getSessionConfig().setBothIdleTime(180);
        connector.getSessionConfig().setKeepAlive(true);

    }

    @Override
    public synchronized void connect() {
        synchronized (isClosing) {
            if (isClosing.equals(Boolean.TRUE)) {
                logger.info("Connector " + channel.getName() + "is closing, return from connect method...");
                return;
            }
        }
        logger.info("Connecting to HSM Channel");
        connector.connect(new InetSocketAddress(channel.getIp(), channel.getPort())).addListener(new IoFutureListener<ConnectFuture>() {
            @Override
            public void operationComplete(ConnectFuture connectFuture) {
                if (connectFuture.isConnected()) {
                    connectFuture.awaitUninterruptibly(); //Raza adding 19-06-2019 Socket Hang/Halt issue
                    session = connectFuture.getSession();
                    isSessionConnected.set(Boolean.TRUE);
                    logger.info("Session created with " + channel.getName() + " : " + connectFuture.getSession());
                    channel.setConnectionStatus(NetworkInfoStatus.SOCKET_CONNECTED);
                    //GlobalContext.getInstance().removeConnector(channel.getIP() + "/" + channel.getPort());
                } else if (connectFuture.getException() != null) {
                    logger.error("Could not connect to " + channel.getName() + " : " + connectFuture.getException());
                    channel.setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED); //Raza update status
                    isSessionConnected.set(Boolean.FALSE);
                    close(true);
                }

            }
        });
    }

    // Use to  remotely close the channel from UI : should be implemented in Remote manager
    public void close(boolean immediately) {
        synchronized (isClosing) {
            isClosing = true;
        }
        logger.info("Closing session  " + channel.getName() + " : " + session);

        if(session != null) { //Raza adding 08-07-2019 -- Switch Start HSM not UP, HSM avaiable after switch is UP
            session.close(immediately).addListener(new IoFutureListener<CloseFuture>() {
                public void operationComplete(CloseFuture closeFuture) {
                    logger.info("session closed  " + channel.getName() + " : " + closeFuture.toString());
                    isClosing = false; //Raza adding 19062019
                }
            });
        }

        isClosing = false; //Mati Adding in case of Server Reconnect while switch is UP. Switch was getting stuck in closing state
        //session.getService().dispose(); //Raza adding for Socket Hand/Halt issue 19-06-2019
        session = null;
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

    public synchronized IoSession getSession() {
        if (this.session == null) {
            return null; //m.rehman returning null for getseesion avoid multiple session
            //connect();
        }

        IoSession session = this.session;

        if (session != null && !session.isConnected()) {
            session.close(true);
        }
        return session;
    }


    public AtomicInteger getLoadCount() {
        return loadCount;
    }


    public void setLoadCount(Integer loadCount) {
        this.loadCount.set(loadCount);
    }

    public FairLock getFairLock() {
        return fairLock;
    }

    public void setFairLock(FairLock fairLock) {
        this.fairLock = fairLock;
    }

    public AtomicBoolean getIsSessionConnected() {
        return isSessionConnected;
    }

    public Boolean getIsClosing() {
        return isClosing;
    }

    public void setIsClosing(Boolean isClosing) {
        this.isClosing = isClosing;
    }

    public void setIsSessionConnected(AtomicBoolean isSessionConnected) {
        this.isSessionConnected = isSessionConnected;
    }

    public HSMChannel getChannel() {
        return channel;
    }

    public void setChannel(HSMChannel channel) {
        this.channel = channel;
    }

    //Raza adding for socket Halt Issue 20-06-2019
    public void reconnect() {

        logger.info("Closing Session....");
        session.close(true);
        logger.info("Session Closed!");

        logger.info("Reconnecting...");
        connect();
        logger.info("Reconnecting Done!");
    }

}
