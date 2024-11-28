package vaulsys.wfe.process;

import vaulsys.eft.base.MessageProcessHandler;
import vaulsys.message.Message;
import vaulsys.message.MessageManager;
import vaulsys.message.ScheduleMessage;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.handlers.IfxToProtocolHandler;
import vaulsys.protocols.handlers.ProtocolToBinaryHandler;
import vaulsys.routing.components.RoutingHandler;
import vaulsys.routing.exception.ScheduleMessageFlowBreakDown;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.scheduler.base.AddRepeatReversalTriggerHandler;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.ConfigUtil;
import vaulsys.wfe.ProcessContext;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;

public class TransferManualProcess implements Runnable  {

    private static final Logger logger = Logger.getLogger(TransferManualProcess.class);

	public long time;
    public Long trxId = 0L;
    private long id;
    private ProcessContext processContext = null;

    public TransferManualProcess(long processId, Transaction trx, long time) {
        this.id = processId;
        this.processContext = new ProcessContext();
        this.processContext.setTransaction(trx);
        this.time = time;
    }

    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void run() {
		processContext.init();

		GeneralDao.Instance.beginTransaction();
        Transaction transaction = processContext.getTransaction();
       if(transaction.getId() != null){
        	transaction = GeneralDao.Instance.getObject(Transaction.class, transaction.getId());
        	processContext.setTransaction(transaction);
        }
        Message message = transaction.getInputMessage();
        ScheduleMessage scheduleMessage = (ScheduleMessage) message;
        
        if (message.getEndPointTerminalId() != null) {
        	Terminal terminal = GeneralDao.Instance.getObject(message.getEndPointTerminal().getTerminalType().getClassType(), message.getEndPointTerminalId());
        	scheduleMessage.setEndPointTerminal(terminal);
        }
        
        String msgType = scheduleMessage.getMessageType();

        try {
            if(transaction.getId() == null){
            	GeneralDao.Instance.saveOrUpdate(transaction);
            }

       		processScheduledTransaction();
            
            trxId = processContext.getTransaction().getId();
            GeneralDao.Instance.saveOrUpdate(transaction);

			byte[] binaryData = null;
			
			if(transaction.getOutputMessage() != null )
				binaryData = transaction.getOutputMessage().getBinaryData();

			/*GeneralDao.Instance.endTransaction();
            logger.info("Scheduled process commited...");
        	
            GeneralDao.Instance.beginTransaction();
			GeneralDao.Instance.refresh(transaction);*/
			
            if(transaction.getOutputMessage() != null )
            	transaction.getOutputMessage().setBinaryData(binaryData);
			
			sendOutputMessages();
			GeneralDao.Instance.endTransaction();
		
            
//            sendPendingRequests();
        } catch (Exception e) {
        	GeneralDao.Instance.endTransaction();
            logger.error("Killer exception: " + e);
            logger.debug("The exception that killed the flow: ", e);
            return;
        }

       /* if (SchedulerConsts.REPEAT_MSG_TYPE.equals(msgType) || SchedulerConsts.REVERSAL_MSG_TYPE.equals(msgType)) {
			logger.debug("This is a reversal message, wait for 10 second");
			try {
				Thread.sleep(ConfigUtil.getLong(ConfigUtil.REVERSAL_SLEEP_TIME)GlobalContext.REVERSAL_PROCESS_WAIT_TIME);
			} catch (InterruptedException e) {
	            logger.error(e);
			}
			logger.debug("after wait");
		}*/
    }

	private void sendPendingRequests() {
		Set<Message> pendingRequests = this.getPendingRequests();
		if (pendingRequests!= null && !pendingRequests.isEmpty()){
			int messagestobesendNo = (pendingRequests!= null)? pendingRequests.size(): 0;
		    logger.info("Put Pending Requests : "+ messagestobesendNo +" messages are about to be sent!");
			MessageManager.getInstance().putRequests(pendingRequests);
		}
	}
    
	private void sendOutputMessages() {
      Message response = null;
      Set<Message> pendingResponses = null;

      response = this.getOutputMessage();
      pendingResponses = this.getPendingResponses();


      if (response != null) {
			int messagestobesendNo = 0;
			messagestobesendNo = (response != null) ? messagestobesendNo + 1 : messagestobesendNo;
			logger.info("Put Response Messages: " + messagestobesendNo + " messages are about to be sent!");

			Collection<Message> pendingRqs = MessageManager.getInstance().putResponse(response);
			if (pendingRqs != null && !pendingRqs.isEmpty())
				this.processContext.addPendingRequests(pendingRqs);

		} else if (pendingResponses != null && pendingResponses.size() > 0) {
			for (Message res : pendingResponses)
				MessageManager.getInstance().putResponse(res);
		} else {
      	logger.warn("IMPORTANT: Flow generated no response.");
      }
	}

    private void processScheduledTransaction() throws Exception{
    	
//        Message message = processContext.getTransaction().getInputMessage();
//        ScheduleMessage scheduleMessage = (ScheduleMessage) message;
//        String msgType = scheduleMessage.getMessageType();
    	createSorushTrx();
    	logger.info("Sorush Message Create in sorush " );

    }

    private void createSorushTrx(){
    	Transaction transaction = processContext.getTransaction();
		try {
			RoutingHandler.Instance.execute(processContext);
		} catch (Exception e) {
			logger.error(" Error In routing processor: "+ e , e);
		}
		try {
			MessageProcessHandler.Instance.execute(processContext);
		} catch (Exception e) {
			if (e instanceof ScheduleMessageFlowBreakDown)
				return;

			if (processContext.isNextStateToEnd())
				return;
			logger.error(e.getMessage());
		}
		try {
			
			IfxToProtocolHandler.Instance.execute(processContext);
			
			ProtocolToBinaryHandler.Instance.execute(processContext);
			
			AddRepeatReversalTriggerHandler.Instance.execute(processContext);
		} catch (Exception e) {
			logger.error(" Error In processor: "+ e , e);
		}

		byte[] binaryData = null;
		
		
		if(transaction.getOutputMessage() != null )
			binaryData = transaction.getOutputMessage().getBinaryData();
		
		processContext.setOutputMessage(transaction.getOutputMessage());
		
		GeneralDao.Instance.endTransaction();
		logger.info("Main process commited...");
		if (processContext.getOutputMessage() != null || (processContext.getPendingResponses() != null && processContext.getPendingResponses().size() > 0)) {

			GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);

			if (processContext.getOutputMessage().getId() != null) {
				processContext.getOutputMessage().setBinaryData(binaryData);
				GeneralDao.Instance.refresh(processContext.getOutputMessage());
			}

		} else {
			logger.warn("IMPORTANT: Flow generated no response");
		}
    } 
    
    public Message getOutputMessage() {
        return processContext.getOutputMessage();
    }

    public Set<Message> getPendingRequests() {
        return processContext.getPendingRequests();
    }
    
    public Set<Message> getPendingResponses() {
    	return processContext.getPendingResponses();
    }

}
