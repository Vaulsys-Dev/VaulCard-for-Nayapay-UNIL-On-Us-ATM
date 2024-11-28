package vaulsys.security.hsm.base;


import vaulsys.netmgmt.extended.NetworkInfoStatus;
import vaulsys.persistence.GeneralDao;
import vaulsys.security.hsm.base.lock.QueueObject;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class HSMIOHandler extends IoHandlerAdapter {

    Logger logger = Logger.getLogger(HSMIOHandler.class);

    public static final int MAX_BUFFER_SIZE = 4096;

    private IoSession session = null;
    private HSMChannel channel;
    BlockingQueue<byte[]> receivedMessages = new LinkedBlockingQueue<byte[]>();


    @Override
    public void sessionCreated(IoSession session) throws Exception {
        logger.info("Session created: " + session.getRemoteAddress());
        this.session = session;
        this.channel = HSMNetworkManager.getInstance().getChannelOfSession(session);
        HSMNetworkManager.getInstance().addToConnectedConnectorList(channel.getConnector());
        channel.getConnector().getFairLock().setEnable(true);
        channel.setConnectionStatus(NetworkInfoStatus.SOCKET_CONNECTED); //Raza update status in DB
        if(!channel.getSignonreq()) { //Raza HSM check flag then decide
            channel.setProcessingStatus(NetworkInfoStatus.PROCESSING_ENABLED); //Raza HSM doesn't need SignOn but check flag
        }
        //GeneralDao.Instance.saveOrUpdate(channel); //Raza reflect status in DB start
        if(GeneralDao.Instance.getCurrentSession().getTransaction().isActive()) {
            GeneralDao.Instance.saveOrUpdate(channel); //Raza Update Channel Connect Status in DB
        }
        else
        {
            logger.info("No Active Transaction, will commit in a while..");
            GeneralDao.Instance.beginTransaction();
            GeneralDao.Instance.saveOrUpdate(channel);
            GeneralDao.Instance.endTransaction();
        } //Raza reflect status in DB start

    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        logger.info("Session opened: " + session.getRemoteAddress());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        this.session = null;
        HSMNetworkManager.getInstance().removeFromConnectedConnectorList(channel.getConnector());
        logger.info("channel: " + channel.getName() + " ,IP: " + channel.getIp() + " ,Port: " + channel.getPort()+ " is closed.");
        HSMNetworkManager.getInstance().addToNotConnectedConnectors(channel.getConnector());
        channel.getConnector().getIsSessionConnected().compareAndSet(true, false);
        channel.getConnector().setLoadCount(0);
        channel.getConnector().getFairLock().setEnable(false);
        List<QueueObject> waitingThreads = channel.getConnector().getFairLock().getAndRemoveWaitingThreads();
        for(QueueObject queueObject : waitingThreads){
            queueObject.setShouldBeTransferred(true);
            queueObject.doNotify();
        }
        channel.setConnectionStatus(NetworkInfoStatus.SOCKET_DISCONNECTED); //Raza update status in DB

        //Raza commenting 20-06-2019 start
        /*logger.info("Try to reconnect to channel: " + channel.getName() + " ,IP: " + channel.getIp() + " ,Port: " + channel.getPort());
        HSMNetworkManager.getInstance().addToNotConnectedConnectors(channel.getConnector());
        channel.getConnector().connect();*/
        //Raza commenting 20-06-2019 end
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        logger.error("Exception caught: " + cause.toString() + "Remote Address : " + session.getRemoteAddress(), cause);
        channel.getConnector().getLoadCount().decrementAndGet();

        //TODO:Find the reason why
/*        if (session.isConnected()) {
            session.close(true);
        }*/
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        try {
            logger.debug("Message [" + message.toString() + "]");
            logger.info(" Message is sent to HSM Channel :" + channel.getName() + " ,IP: " + channel.getIp() + " ,Port: " + channel.getPort());
        } catch (Exception exp) {
            logger.info("An exception caught while sending request to" + channel.getName() + " ,IP: " + channel.getIp() + " ,Port: " + channel.getPort() + " ,Cause: " + exp.getCause(), exp);
            throw exp;
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        try {
            logger.debug("Message is received from HSM Channel" + channel.getName() + " ,IP: " + channel.getIp() + " ,Port: " + channel.getPort());
            byte[] binaryData = (byte[]) message;
            //m.rehman: 22-11-2021, HSM response logging
            //receivedMessages.put(binaryData);
            HSMNetworkManager.getInstance().receiveResponseFromHSM(binaryData, channel);
        } catch (Exception exp) {
            logger.info("An exception caught while receiving request from" + channel.getName() + " ,IP: " + channel.getIp() + " ,Port: " + channel.getPort() + " ,Cause: " + exp.getCause(), exp);
            throw exp;
        }
    }

    public byte[] getReceivedMessage() throws InterruptedException {
        byte[] receivedMessage = null;
        try {
            //IF the response of the last request received after timeout remove it from queue.
            //Always get the last responses in the queue because it is the response related to request.
            while (receivedMessages.size() > 1) {
                receivedMessages.take();
            }
            receivedMessage = receivedMessages.poll(channel.getTimeoutMilliSeconds(), TimeUnit.MILLISECONDS);
            //receivedMessage = receivedMessages.take(); //Raza TEMP
            //String temp = new String(receivedMessage); //Raza TEMP
            //System.out.println("HSMIOHandler:: receivedMessage Str [" + temp + "]"); //Raza TEMP
            //System.out.println("HSMIOHandler:: Removing Queue...!"); //Raza TEMP
            //receivedMessages.remove(); //clear();
            /*try
            {
                System.out.println("HSMIOHandler:: Going to Clear Queue Directly after message is received...!"); //Raza TEMP
                receivedMessages.clear();
            }
            catch(Exception e)
            {
                System.out.println("HSMIOHandler:: Exception is caught while taking msg [" + e.getMessage() + "]"); //Raza TEMP
            }*/
            channel.getConnector().getLoadCount().decrementAndGet();
        } catch (InterruptedException exp) {
            logger.info("An exception caught while receiving response from " + channel.getName() + " ,IP: " + channel.getIp() + " ,Port: " + channel.getPort() + " ,Cause: " + exp.getCause(), exp);
            throw exp;
        }
        //System.out.println("HSMIOHandler:: Returning Received Message...!"); //Raza TEMP
        return receivedMessage;
    }


}
