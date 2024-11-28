package vaulsys.security.hsm.base.lock;

import org.apache.log4j.Logger;


import java.util.ArrayList;
import java.util.List;

public class FairLock {

    Logger logger = Logger.getLogger(FairLock.class);

    private boolean           isLocked       = false;
    private boolean           enable       = true;
    private Thread            lockingThread  = null;
    private List<QueueObject> waitingThreads =new ArrayList<QueueObject>();


    public void lock() throws InterruptedException{
        QueueObject queueObject= new QueueObject();
        queueObject.threadId=Thread.currentThread().getId();
        boolean     isLockedForThisThread = true;
        synchronized(this){
            logger.debug("Thread " + queueObject.threadId +" is waiting" );
            waitingThreads.add(queueObject);
        }

        while(isLockedForThisThread){
            synchronized(this){
                isLockedForThisThread =
                        isLocked || waitingThreads.get(0) != queueObject;
                if(!isLockedForThisThread){
                    logger.debug("Starting Thread " + Thread.currentThread().getId() );
                    isLocked = true;
                    waitingThreads.remove(queueObject);
                    lockingThread = Thread.currentThread();
                    return;
                }
            }
            try{
                queueObject.doWait();
                if(queueObject.isShouldBeTransferred())
                    return;
            }catch(InterruptedException e){
                synchronized(this) { waitingThreads.remove(queueObject); }
                throw e;
            }
        }
    }


    public synchronized void unlock(){
        if(this.lockingThread != Thread.currentThread()){
            throw new IllegalMonitorStateException(
                    "Calling thread has not locked this lock");
        }
        isLocked      = false;
        lockingThread = null;
        if(waitingThreads.size() > 0){
            waitingThreads.get(0).doNotify();
        }
    }


    public List<QueueObject> getWaitingThreads() {
        return waitingThreads;
    }

    public List<QueueObject> getAndRemoveWaitingThreads() {
        List<QueueObject> waitingThreadList =new ArrayList<QueueObject>();
        waitingThreadList.addAll(waitingThreads);
        waitingThreads.removeAll(waitingThreadList);
        return waitingThreadList;
    }

    public void addWaitingThread(QueueObject waitingThread) {
        if(!waitingThreads.contains(waitingThread)){
            waitingThreads.add(waitingThread);
        }
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
