package vaulsys.network.mina2;

import vaulsys.netmgmt.component.EchoMessageGenerator;
import vaulsys.netmgmt.extended.ConnectionManager;
import vaulsys.netmgmt.extended.NetworkInfoStatus;
import vaulsys.network.ATMSessionManager;
import vaulsys.network.NetworkManager;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.network.channel.base.OutputChannel;
import vaulsys.network.channel.endpoint.EndPointType;
import vaulsys.persistence.GeneralDao;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.net.InetSocketAddress;

public class Mina2IoHandler extends IoHandlerAdapter {
    transient Logger logger = Logger.getLogger(Mina2IoHandler.class);

    public static final int MAX_BUFFER_SIZE = 4096;

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        long time = System.currentTimeMillis();
        logger.debug("Message Received from: " + session.getRemoteAddress());
        Channel channel;
        // set session's channel
        if (session.containsAttribute("Channel")) {
            channel = (Channel) session.getAttribute("Channel");
        } else {
            channel = NetworkManager.getInstance().getInputChannelOfSession(session);
            if (channel == null) {
                channel = NetworkManager.getInstance().getSameSocketOutputChannelOfSession(session);
                NetworkManager.getInstance().removeSameSocketOutputChannel(session);
            }
            session.setAttribute("Channel", channel);
        }

        byte[] binaryData = (byte[]) message;
        if (logger.isTraceEnabled()) {
            logger.trace("Actual Message Received: Binary=" + Util.byteToHex(binaryData));
        }

        NetworkManager.getInstance().receiveMessage(channel, session, binaryData, time);
        logger.debug("receive message called.");
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        //super.exceptionCaught(session, cause);
        logger.info("exceptionCaught:" + cause.toString() + " : " + session.getRemoteAddress());
        cause.printStackTrace(); //Raza uncommenting
        if (session.isConnected())
            session.close(true);
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        logger.info("Session created: " + session.getRemoteAddress());

//    	((SocketSessionConfig) session.getConfig()).setSendBufferSize(IoHandler.MAX_BUFFER_SIZE);
//		((SocketSessionConfig) session.getConfig()).setTcpNoDelay(true);

        Channel channel = NetworkManager.getInstance().getInputChannelOfSession(session);
        if (channel == null)
            channel = NetworkManager.getInstance().getOutputChannelOfSession(session);

        if (channel == null) {
            logger.error("channel is null!!! closing session....");
            session.close(true);
            return;
        }
        //logger.error("Going for Comms UP..!");
        //NetworkManager.SetCommsUP(channel); //Set Comms UP of Provided Channel
        channel.setConnectionStatus(NetworkInfoStatus.SOCKET_CONNECTED);
        channel.setProcessingStatus(NetworkInfoStatus.PROCESSING_ENABLED); //Raza for TEST ONLY REMOVE IT
        if (!channel.getSignonreq()) {
            channel.setProcessingStatus(NetworkInfoStatus.PROCESSING_ENABLED); //Raza set Proc enable only when SIGNON msg not supported
        }
        session.setAttribute("Channel", channel);

        if (channel instanceof InputChannel) {
            InputChannel inChannel = ((InputChannel) channel);
            if (CommunicationMethod.ANOTHER_SOCKET.equals(inChannel.getCommunicationMethod())) {
                OutputChannel outChannel = (OutputChannel) GlobalContext.getInstance().getChannel(inChannel.getOriginatorChannelId());
                if (outChannel != null) {
                    outChannel.getConnector().getSession();
                }
            }
            //Raza for I am Client connection start
            Thread t = new Thread(new ConnectionManager(session, channel));
            t.setName(channel.getName() + "_ConnectionThread");
            t.setDaemon(false);
            ConnectionManager.ManageConnection(t);
            //t.start();
            //Raza for I am Client connection end
        }

        if (channel.getEndPointType() == EndPointType.ATM_TERMINAL) {
            String hostAddress = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
            logger.info("ATM with IP: " + hostAddress + " added to the IP list....");

            IoSession oldSession = NetworkManager.getInstance().getTerminalOpenConnection(hostAddress);
            if (oldSession != null) {
                logger.info("ATM last session " + oldSession);
            }

            if (oldSession != null && oldSession.isConnected()) {
                logger.info("a session is still opened from IP " + oldSession);
                oldSession.close(true);
                logger.info("The previous session from IP " + oldSession + " is " + oldSession.isClosing() + ", " + oldSession.isConnected());
            }

            NetworkManager.getInstance().addATMConnection(hostAddress);
            NetworkManager.getInstance().addTerminalOpenConnection(hostAddress, session);
            ATMSessionManager.get().addATMSessionStatus(hostAddress, true);
        }
        if (GeneralDao.Instance.getCurrentSession().getTransaction().isActive()) { //Raza reflect status in DB start
            GeneralDao.Instance.saveOrUpdate(channel); //Raza Update Channel Connect Status in DB
        } else {
            //logger.info("No Active Transaction, will commit in a while..");
            GeneralDao.Instance.beginTransaction();
            GeneralDao.Instance.saveOrUpdate(channel);
            GeneralDao.Instance.endTransaction();
        } //Raza reflect status in DB end
//        logger.info("End Session created: " + session.getRemoteAddress());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        logger.info("A session is idled with: " + session.getRemoteAddress());
        //m.rehman: commenting out session.close as we need to send echo message on idle
//        session.close(true);
        Channel channel = NetworkManager.getInstance().getOutputChannelOfSession(session);
        if (channel != null) {
            if (channel.getEchoReq() != null && channel.getEchoReq().equals(Boolean.TRUE)) {
                EchoMessageGenerator.generateEchoMessage((OutputChannel) channel);
            }
        }
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        logger.info("A session is opened with: " + session.getRemoteAddress());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
//        logger.info("Session closed "+((InetSocketAddress)session.getLocalAddress()).getPort());
        Channel channel = NetworkManager.getInstance().getInputChannelOfSession(session);
        if (channel != null && channel.getEndPointType() == EndPointType.ATM_TERMINAL) {
            String hostAddress = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
            logger.info("ATM with IP: " + hostAddress + " on session" + session.getLocalAddress() + " disconnected ...");
            IoSession oldSession = NetworkManager.getInstance().getTerminalOpenConnection(hostAddress);
            if (oldSession.equals(session)) {
                NetworkManager.getInstance().removeATMConnection(hostAddress);
                NetworkManager.getInstance().removeTerminalOpenConnection(hostAddress);
                ATMSessionManager.get().addATMSessionStatus(hostAddress, false);
            } else {
                logger.info("another session is in the Map!");
            }
        } else if (channel == null) {
            channel = NetworkManager.getInstance().getOutputChannelOfSession(session);
            if (channel != null) {
                ((OutputChannel) channel).getConnector().removeSessionFromQueue(session);
                ((OutputChannel) channel).getConnector().connect();
            }
        }

        if (channel != null) {
            //NetworkManager.SetCommsDOWN(channel); //Set Comms Down of Provided Channel
            channel.setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED);
        }
    }
}
