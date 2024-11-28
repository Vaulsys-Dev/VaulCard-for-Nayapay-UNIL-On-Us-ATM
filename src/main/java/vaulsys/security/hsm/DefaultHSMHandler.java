package vaulsys.security.hsm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class DefaultHSMHandler implements IoHandler {
    Logger logger = Logger.getLogger(DefaultHSMHandler.class);

    IoSession session = null;

    public IoSession getSession() {
        return session;
    }

    byte[] sentMessage;
    BlockingQueue<byte[]> receivedMessages = new LinkedBlockingQueue<byte[]>();
    ;
    //	boolean msgReceived;
    boolean msgSent;
    boolean sessionClosed;
    boolean free = true;

    Object sendLock = new Object();
    Object receiveLock = new Object();

    Object sessionLock = new Object();

    public void waitForSession() {
        synchronized (sessionLock) {
            try {
                if (session == null)
                    sessionLock.wait();
            } catch (InterruptedException ex) {
                logger.error("Exception", ex);
            }
        }
    }

    public boolean isMessageAvailable() {
        return !receivedMessages.isEmpty();
    }

    public byte[] sendMessageReceiveResponse(byte[] msg) {

        sendMessage(msg);

/*
		msgReceived = false;
		synchronized (receiveLock) {
			try {
				while (!msgReceived && !sessionClosed)
					receiveLock.wait();
			} catch (InterruptedException ex) {
				logger.error("Exception", ex);
			}
		}
		
		free = true;
//		byte[] result = receivedMessage; 
//		receivedMessage = null; 
		return receivedMessage;
		*/
        free = false;
        byte[] retval = null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            byte[] receivedMessage = getMessage();

            buffer.write(receivedMessage);

            while (isMessageAvailable()) {

                buffer.write(getMessage());
            }
            retval = buffer.toByteArray();
            buffer.reset();
            buffer.close();
        }
        catch (IOException ex) {
            logger.error("Exception", ex);
        }
        free = true;
        return retval;

    }

    public void sendMessage(byte[] msg) {
        free = false;
        msgSent = false;
        session.write(msg);

        synchronized (sendLock) {
            try {
                while (!msgSent && !sessionClosed)
                    sendLock.wait();

            } catch (InterruptedException ex) {
                logger.error("Exception", ex);
            }
        }

        free = true;
    }

    public byte[] getMessage() {
        free = false;
/*		
		if (msgReceived){
			msgReceived = false;
			return receivedMessage;
		}
		
		msgReceived = false;
		synchronized (receiveLock) {
			try {
				while (!msgReceived && ! sessionClosed)
					receiveLock.wait();
				
			} catch (InterruptedException ex) {
				logger.error("Exception", ex);
			}
		}
		
		free = true;
		*/
        byte[] receivedMessage = null;
        try {
            receivedMessage = receivedMessages.take();
        } catch (InterruptedException ex) {
            receivedMessage = null;
        }
        free = true;
        return receivedMessage;
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable throwable) throws Exception {
        this.session = session;
        synchronized (sendLock) {
            sentMessage = null;
            msgSent = true;
            sendLock.notify();
        }
/*		
		synchronized (receiveLock) {
			receivedMessage = null;
			msgReceived = true; 
			receiveLock.notify(); 
		}
*/
        session.close();
    }

    @Override
    public void messageReceived(IoSession session, Object msg) throws Exception {
        this.session = session;

        receivedMessages.put((byte[]) msg);
/*		synchronized (receiveLock) {
			msgReceived = true;
			receiveLock.notify();
		}
*/
    }

    @Override
    public void messageSent(IoSession session, Object msg) throws Exception {
        this.session = session;
        sentMessage = (byte[]) msg;
        synchronized (sendLock) {
            msgSent = true;
            sendLock.notify();
        }
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        this.session = session;
        sessionClosed = true;
        synchronized (sendLock) {
            sendLock.notify();
        }
        synchronized (receiveLock) {
            receiveLock.notify();
        }

        sentMessage = null;
//		receivedMessage = null; 
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        this.session = session;
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        this.session = session;
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        this.session = session;
        sessionClosed = false;
        synchronized (sessionLock) {
            sessionLock.notify();
        }
//		msgReceived = false;
        msgSent = false;
    }

    public boolean isFree() {
        return free;
    }

    public void setBusy() {
        free = false;
    }

}
