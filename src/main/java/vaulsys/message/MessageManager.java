package vaulsys.message;

import vaulsys.base.Manager;
import vaulsys.calendar.DateTime;
import vaulsys.network.NetworkManager;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Util;
import vaulsys.wfe.process.MainProcess;
import vaulsys.wfe.process.ScheduledProcess;
import vaulsys.wfe.process.SwitchThreadPool;
import vaulsys.wfe.process.TransferManualProcess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

public class MessageManager implements Manager {

    transient Logger logger = Logger.getLogger(MessageManager.class);

    public static SwitchThreadPool threadPool;
    private SwitchThreadPool sorushThreadPool;
    private SwitchThreadPool scheduledThreadPool;
    private SwitchRestarter switchRestarter;
    private long id;
    ////////////honarmand
    private static int tpPerSecond = 0;
    private static int currentSecond = -1;
    ///////////

    private MessageManager() {
        threadPool = new SwitchThreadPool(
                ConfigUtil.getInteger(ConfigUtil.THREADPOOL_MAIN_CORE_SIZE),
                ConfigUtil.getInteger(ConfigUtil.THREADPOOL_MAIN_MAX_SIZE),
                ConfigUtil.getInteger(ConfigUtil.THREADPOOL_MAIN_KEEP_ALIVE_TIME),
                new ArrayBlockingQueue<Runnable>(ConfigUtil.getInteger(ConfigUtil.THREADPOOL_MAIN_MAX_QUEUE_SIZE)));

        sorushThreadPool = new SwitchThreadPool(
                ConfigUtil.getInteger(ConfigUtil.THREADPOOL_SORUSH_CORE_SIZE),
                ConfigUtil.getInteger(ConfigUtil.THREADPOOL_SORUSH_MAX_SIZE),
                ConfigUtil.getInteger(ConfigUtil.THREADPOOL_SORUSH_KEEP_ALIVE_TIME),
                new ArrayBlockingQueue<Runnable>(ConfigUtil.getInteger(ConfigUtil.THREADPOOL_SORUSH_MAX_QUEUE_SIZE)));

        scheduledThreadPool = new SwitchThreadPool(
                ConfigUtil.getInteger(ConfigUtil.THREADPOOL_SCHEDULE_CORE_SIZE),
                ConfigUtil.getInteger(ConfigUtil.THREADPOOL_SCHEDULE_MAX_SIZE),
                ConfigUtil.getInteger(ConfigUtil.THREADPOOL_SCHEDULE_KEEP_ALIVE_TIME),
                new ArrayBlockingQueue<Runnable>(ConfigUtil.getInteger(ConfigUtil.THREADPOOL_SCHEDULE_MAX_QUEUE_SIZE)));

        switchRestarter = new SwitchRestarter();
    }

    private static MessageManager messageManager;

    public static MessageManager getInstance() {
        if (MessageManager.messageManager == null)
            MessageManager.messageManager = new MessageManager();
        return MessageManager.messageManager;
    }

    public void putRequest(Message request, IoSession session, long time) {

    	Transaction trx = request.getTransaction();

        if (request == null)
            return;


        boolean isSorush = false;
        try{
            if (request.isIncomingMessage() && Util.hasText(request.getChannelName()) && request.getChannel().equals("channelSHETABIn")){
                if(request.getBinaryData() != null && request.getBinaryData().length != 0 && request.getBinaryData()[4] >= 56){
                    if(Integer.parseInt((char)request.getBinaryData()[1]+""+(char)request.getBinaryData()[2]+""+(char)request.getBinaryData()[3]) == 200){
                        int cardLen = Integer.parseInt((char)request.getBinaryData()[36]+""+(char)request.getBinaryData()[37]);
                        int beforSorush = 4 + 16 + 16 + 2 + cardLen + 6 + 12 + 12 + 10 + 8 + 6 + 6+ 4 + 4 + 4 + 2;
                        int bankLen = Integer.parseInt((char)request.getBinaryData()[beforSorush]+""+(char)request.getBinaryData()[beforSorush+1]);
                        beforSorush +=2;
                        String bankId = "";
                        for(int i = 0; i < bankLen; i++)
                            bankId += "" + (char)request.getBinaryData()[beforSorush+i];
                        if (ConfigUtil.getProperty(ConfigUtil.SORUSH_CODE).equals(bankId))
                            isSorush = true;
                    }
                }
            }
        }catch (Exception e) {
        }
        if (request.isIncomingMessage() && isSorush){
            MainProcess process = new MainProcess(id++, trx, session, time);
            try {
                logger.debug("sorush activeCount: " + sorushThreadPool.getActiveCount());
                if(sorushThreadPool.getActiveCount() == ConfigUtil.getInteger(ConfigUtil.THREADPOOL_SORUSH_CORE_SIZE))
                    logger.debug("sorush taskCount: "+sorushThreadPool.getTaskCount() + "; sorush completedTaskCount: "+sorushThreadPool.getCompletedTaskCount()+ "; sorush poolSize: "+sorushThreadPool.getPoolSize() + "; sorush QueueSize: "+sorushThreadPool.getQueue().size());
                try{
                    int nowSecond = DateTime.now().getDayTime().getSecond();
                    if(currentSecond == -1){
                        currentSecond = nowSecond;
                        tpPerSecond = 1;
                    } else if (currentSecond == nowSecond){
                        tpPerSecond++;
                    } else {
                        currentSecond = nowSecond;
                        tpPerSecond = 1;
                    }
                    logger.debug("sorush tpPerSecond: " + tpPerSecond);
                } catch(Exception e){
                    logger.error("error: " + e.getMessage());
                }
                sorushThreadPool.execute(process);
            } catch (RejectedExecutionException ex) {
                logger.error("Switch dropped the sorush transaction: " + trx.getId());
                logger.error("Sorush Message cannot be executed..." + ex, ex);
                return;
            }
        } else if (request.isIncomingMessage()){
            MainProcess process = new MainProcess(id++, trx, session, time);
            try {
                threadPool.execute(process);

                switchRestarter.processExecuted();
                logger.debug("activeCount: " + threadPool.getActiveCount());
//            	logger.debug("taskCount: "+threadPool.getTaskCount());
//            	logger.debug("completedTaskCount: "+threadPool.getCompletedTaskCount());
//            	logger.debug("poolSize: "+threadPool.getPoolSize());
//            	logger.debug("QueueSize: "+threadPool.getQueue().size());                
            } catch (RejectedExecutionException ex) {
                logger.error("Switch dropped the transaction: " + trx.getId());
                logger.error("Message cannot be executed..." + ex, ex);
                switchRestarter.processRejected();
                return;
            }
        }else if (request.isScheduleMessage() &&  TransactionService.IsSorush(request.getIfx()) ){
        	TransferManualProcess process = new TransferManualProcess(id++, trx, System.currentTimeMillis());
            try {
                scheduledThreadPool.execute(process);
                logger.debug("scheduled activeCount: " + scheduledThreadPool.getActiveCount());
//            	logger.debug("scheduled taskCount: "+scheduledThreadPool.getTaskCount());
//            	logger.debug("scheduled completedTaskCount: "+scheduledThreadPool.getCompletedTaskCount());
//            	logger.debug("scheduled QueueSize: "+scheduledThreadPool.getQueue().size());                
            } catch (RejectedExecutionException ex) {
                logger.error("Switch dropped the scheduled transaction: " + trx.getId());
                logger.error("Schedule Scheduled Message cannot be executed..." + ex, ex);
                return;
            }
        }else if (request.isScheduleMessage()  ){
        	ScheduledProcess process = new ScheduledProcess(id++, trx, System.currentTimeMillis());
            try {
                scheduledThreadPool.execute(process);


                logger.debug("scheduled activeCount: " + scheduledThreadPool.getActiveCount());
//            	logger.debug("scheduled taskCount: "+scheduledThreadPool.getTaskCount());
//            	logger.debug("scheduled completedTaskCount: "+scheduledThreadPool.getCompletedTaskCount());
//            	logger.debug("scheduled QueueSize: "+scheduledThreadPool.getQueue().size());                
            } catch (RejectedExecutionException ex) {
                logger.error("Switch dropped the scheduled transaction: " + trx.getId());
                logger.error("Schedule Scheduled Message cannot be executed..." + ex, ex);
                return;
            }
        }
    }

    public Collection<Message> putResponse(Message response) {
        // TODO decide based on message what to do
//    	logger.debug("in putResponse");
        if(response.isOutgoingMessage()){
//        	logger.debug("before NetworkManager.getInstance().sendMessage(response)");
            List<ScheduleMessage> sendMessage = NetworkManager.getInstance().sendMessage(response);
            List<Message> pendingRq = null;
            if (sendMessage!= null && !sendMessage.isEmpty()){
                pendingRq = new ArrayList<Message>();
                pendingRq.addAll(sendMessage);
//        	logger.debug("after NetworkManager.getInstance().sendMessage(response)");
//    			putRequests(pendingRq);
            }

            return pendingRq;
        }
        return null;
    }

    public Collection<Message> putResponses(Collection<Message> responses) {
        List<Message> pendingRq = new ArrayList<Message>();
        for (Message response : responses) {
            Collection<Message> pndRq = this.putResponse(response);
            if (pndRq!= null && !pndRq.isEmpty()){
                pendingRq.addAll(pndRq);
            }
        }
        return pendingRq;
    }

    public void putRequests(Collection<Message> requests) {
        if (requests != null)
            for (Message request : requests) {
                this.putRequest(request, null, System.currentTimeMillis());
            }
    }

    public void shutdown() {
    	getInstance().threadPool.shutdown();
    	getInstance().scheduledThreadPool.shutdown();
    }

    public void startup() {
        MessageManager.getInstance();
    }

//	public SwitchThreadPool getThreadPool() {
//		return threadPool;
//	}
//
//	public void setThreadPool(SwitchThreadPool threadPool) {
//		this.threadPool = threadPool;
//	}
//
//	public SwitchThreadPool getScheduledThreadPool() {
//		return scheduledThreadPool;
//	}
//
//	public void setScheduledThreadPool(SwitchThreadPool scheduledThreadPool) {
//		this.scheduledThreadPool = scheduledThreadPool;
//	}

	public int getMaxPossibleScheduleJobs(){
		return SwitchThreadPool.MAX_QUEUE_SIZE_SCHEDULE - scheduledThreadPool.getQueue().size();
	}

	public int getCurrentScheduledThreadQueueSize() {
		return scheduledThreadPool.getQueue().size();
	}

    //Raza NayaPay start
    public void putWSRequest(Message request) {

        Transaction trx = request.getTransaction();

        if (request == null)
            return;

        if (request.isIncomingMessage()){
            MainProcess process = new MainProcess(id++, trx, System.currentTimeMillis());
            try {
                threadPool.execute(process);

                switchRestarter.processExecuted();
                logger.debug("activeCount: " + threadPool.getActiveCount());

            } catch (RejectedExecutionException ex) {
                logger.error("Switch dropped the transaction: " + trx.getId());
                logger.error("Message cannot be executed..." + ex, ex);
                switchRestarter.processRejected();
                return;
            }
        }else if (request.isScheduleMessage() &&  TransactionService.IsSorush(request.getIfx()) ){
            TransferManualProcess process = new TransferManualProcess(id++, trx, System.currentTimeMillis());
            try {
                scheduledThreadPool.execute(process);
                logger.debug("scheduled activeCount: " + scheduledThreadPool.getActiveCount());

            } catch (RejectedExecutionException ex) {
                logger.error("Switch dropped the scheduled transaction: " + trx.getId());
                logger.error("Schedule Scheduled Message cannot be executed..." + ex, ex);
                return;
            }
        }else if (request.isScheduleMessage()  ){
            ScheduledProcess process = new ScheduledProcess(id++, trx, System.currentTimeMillis());
            try {
                scheduledThreadPool.execute(process);


                logger.debug("scheduled activeCount: " + scheduledThreadPool.getActiveCount());

            } catch (RejectedExecutionException ex) {
                logger.error("Switch dropped the scheduled transaction: " + trx.getId());
                logger.error("Schedule Scheduled Message cannot be executed..." + ex, ex);
                return;
            }
        }
    }
    //Raza NayaPay end

}
