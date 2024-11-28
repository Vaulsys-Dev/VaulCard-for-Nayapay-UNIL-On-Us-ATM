package vaulsys.wfe.process;

import vaulsys.config.Configure;
import vaulsys.util.ConfigUtil;
import vaulsys.util.MyDateFormatNew;
import vaulsys.wfe.ProcessContext;

import java.util.concurrent.*;

import org.apache.log4j.Logger;

public class SwitchThreadPool extends ThreadPoolExecutor {
    Logger logger = Logger.getLogger(SwitchThreadPool.class);

    private Float sum = 0.F;
    private Integer count = 0;

    @Configure("CoreSize")
    public static int CORE_POOL_SIZE;

    @Configure("MaxSize")
    public static int MAX_POOL_SIZE;
    
    @Configure("MaxQueueSize")
    public static int MAX_QUEUE_SIZE;

    @Configure("KeepAliveTime")
    public static long KEEP_ALIVE_TIME;
    
    static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

    @Configure("CoreSizeScheduled")
    public static int CORE_POOL_SIZE_SCHEDULE;
    
    @Configure("MaxSizeScheduled")
    public static int MAX_POOL_SIZE_SCHEDULE;
    
    @Configure("MaxQueueSizeScheduled")
    public static int MAX_QUEUE_SIZE_SCHEDULE;

    @Configure("KeepAliveTimeScheduled")
    public static long KEEP_ALIVE_TIME_SCHEDULE;
    
    public SwitchThreadPool() {
        super(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TIME_UNIT, new LinkedBlockingQueue<Runnable>());
	}
    
    public SwitchThreadPool(int coreSize, int maxSize, long keepAliveTime, BlockingQueue<Runnable> queue) {
        super(coreSize, maxSize, keepAliveTime, TIME_UNIT, queue);
	}

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
//    	long time = System.currentTimeMillis();
//
//    	if(r instanceof MainProcess) {
//        	((MainProcess) r).time = time;       
//        }
//        else{
//        	((ScheduledProcess) r).time = time;
//        }
    	
        super.beforeExecute(t, r);
//        logger.debug("/////////// Initializing Process: TaskCount " + + this.getTaskCount());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable throwable) {
        if (throwable != null) {
            logger.debug(throwable.getMessage());
            logger.debug(throwable.getCause());
            logger.debug("", throwable);
            
//            throwable.printStackTrace();
        }

//        Message response = null;
//        Set<Message> pendingRequests = null;
//        Set<Message> pendingResponses = null;
        
//        if(r instanceof MainProcess) {
//        	MainProcess workerProcess = (MainProcess) r;       
//	        response = workerProcess.getOutputMessage();
//	        pendingRequests = workerProcess.getPendingRequests();
//	        pendingResponses = workerProcess.getPendingResponses();
//        }
//        else{
//        	ScheduledProcess workerProcess = (ScheduledProcess) r;
//	        response = workerProcess.getOutputMessage();
//	        pendingRequests = workerProcess.getPendingRequests();
//	        pendingResponses = workerProcess.getPendingResponses();
//        }
        
        
//        if (response != null || (pendingRequests != null && pendingRequests.size() > 0)) {
//        	
//        	int messagestobesendNo = 0;
//        	messagestobesendNo = (response != null)? messagestobesendNo+1 : messagestobesendNo;
//        	messagestobesendNo = (pendingRequests!= null)? messagestobesendNo+ pendingRequests.size(): messagestobesendNo;
//            logger.info("Put Response Messages: "+ messagestobesendNo +" messages are about to be sent!");
//            
//            
//            Set<Message> pendingMessages = new HashSet<Message>(); 
//            
//            if (pendingRequests!= null)
//            	pendingMessages.addAll(pendingRequests);
//
//            if (response != null){
//            	response.setPendingRequests(pendingMessages);
////                logger.info("Network is notified to send messages");
//            	MessageManager.getInstance().putResponse(response);
////                logger.info("After Network is notified to send messages");
//            }
//            else{
////                logger.info("Network is notified to send pendingMessages");
//                MessageManager.getInstance().putRequests(pendingMessages);
////                logger.info("After Network is notified to send pendingMessages");
//            }
//        } else if (pendingResponses != null && pendingResponses.size()>0){
////            logger.info("Network is notified to send pendingResponses");
//            for (Message res: pendingResponses)
//            	MessageManager.getInstance().putResponse(res);
////            logger.info("Network is notified to send pendingResponses");
//        }else{
//        	logger.warn("IMPORTANT: Flow generated no response.");
//        }
        super.afterExecute(r, throwable);

    	long time;
    	Long trxId;
    	String trxType = null;
    	String processStr;

    	if(r instanceof MainProcess) {
        	time = ((MainProcess) r).time;
        	trxId = ((MainProcess) r).trxId;
        	trxType = ((MainProcess) r).trxType;
        	processStr = "MainProcess";
        }
        else if(r instanceof TransferManualProcess){
        	time = ((TransferManualProcess) r).time;
        	trxId = ((TransferManualProcess) r).trxId;
        	processStr = "TransferManualProcess";
        	time -= ConfigUtil.getLong(ConfigUtil.REVERSAL_SLEEP_TIME);
        }
        else if(r instanceof FutureTask){
            logger.info("WebService FutureTask thread Finished Executing ");
            return;
        }
        else{
        	time = ((ScheduledProcess) r).time;
        	trxId = ((ScheduledProcess) r).trxId;
        	processStr = "ScheduledProcess";
        	time -= ConfigUtil.getLong(ConfigUtil.REVERSAL_SLEEP_TIME);
        }

    	long diff = (System.currentTimeMillis() - time);

        synchronized (sum) {
            sum += diff;
        }
        synchronized (count) {
            ++count;
        }

        if(trxType == null || trxType.length() == 0){
			logger.info("Ending "+processStr+" trx:" + trxId + " in: " + diff + "ms\t, av:" + ((int) ((sum / count) * 100)) / 100.+"ms");
        }else{
			logger.info("Ending "+processStr+" trx["+trxType+"]:" + trxId + " in: " + diff + "ms\t, av:" + ((int) ((sum / count) * 100)) / 100.+"ms");        	
        }
        
        MyDateFormatNew.remove();
        ProcessContext.remove();
    }
}
