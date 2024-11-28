package vaulsys.wfe.process;

import vaulsys.clearing.components.EODHandler;
import vaulsys.message.Message;
import vaulsys.message.MessageManager;
import vaulsys.message.ScheduleMessage;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.NetworkUi.NetworkUiMessageProcessorHandler;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.handlers.IfxToProtocolHandler;
import vaulsys.protocols.handlers.ProtocolToBinaryHandler;
import vaulsys.repeatreversal.*;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.scheduler.SchedulerService;
import vaulsys.scheduler.base.AddRepeatReversalTriggerHandler;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;

public class ScheduledProcess implements Runnable {
    private static final Logger logger = Logger.getLogger(ScheduledProcess.class);

	public long time;
    public Long trxId = 0L;

    private long id;
    private ProcessContext processContext = null;
//    private Transaction trx;

    private static Float sum = 0.F;
    private static Integer count = 0;
    
    public ScheduledProcess(long processId, Transaction trx, long time) {
        this.id = processId;
        this.processContext = new ProcessContext();
        this.processContext.setTransaction(trx);
        this.time = time;
//        this.trx = trx;
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
//        transaction.setStatus(TransactionStatus.IN_PROGRESS);
//        this.processContext = new ProcessContext();
//        this.processContext.setTransaction(transaction);
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
//            getGeneralDao().flush();

       		processScheduledTransaction();
            
//            transaction.setStatus(TransactionStatus.PROCESSED);
            trxId = processContext.getTransaction().getId();
            GeneralDao.Instance.saveOrUpdate(transaction);

			byte[] binaryData = null;
			
			if(transaction.getOutputMessage() != null )
				binaryData = transaction.getOutputMessage().getBinaryData();

			GeneralDao.Instance.endTransaction();
            logger.info("Scheduled process commited...");
        	
            GeneralDao.Instance.beginTransaction();
			GeneralDao.Instance.refresh(transaction);
			
            if(transaction.getOutputMessage() != null )
            	transaction.getOutputMessage().setBinaryData(binaryData);
			
			sendOutputMessages();
			GeneralDao.Instance.endTransaction();
		
            
            sendPendingRequests();
        } catch (Exception e) {
        	GeneralDao.Instance.endTransaction();
            logger.error("Killer exception: " + e);
            logger.debug("The exception that killed the flow: ", e);
            return;
        }

        if (SchedulerConsts.REPEAT_MSG_TYPE.equals(msgType) || SchedulerConsts.REVERSAL_MSG_TYPE.equals(msgType)) {
			logger.debug("This is a reversal message, wait for 10 second");
			try {
				Thread.sleep(ConfigUtil.getLong(ConfigUtil.REVERSAL_SLEEP_TIME)/*GlobalContext.REVERSAL_PROCESS_WAIT_TIME*/);
			} catch (InterruptedException e) {
	            logger.error(e);
			}
			logger.debug("after wait");
		}
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

		  	//m.rehman: need to check whether network is alive, if yes then send message
			if (Util.hasText(response.getIfx().getRsCode())
					&& response.getIfx().getRsCode().equals(ISOResponseCodes.ISSUER_REVERSAL)) {
				logger.info("Unable to send message as issuer is inoperative");
			} else {
				Collection<Message> pendingRqs = MessageManager.getInstance().putResponse(response);
				if (pendingRqs != null && !pendingRqs.isEmpty())
					this.processContext.addPendingRequests(pendingRqs);
			}
	  	} else if (pendingResponses != null && pendingResponses.size() > 0) {
			for (Message res : pendingResponses)
				MessageManager.getInstance().putResponse(res);
		} else {
      		logger.warn("IMPORTANT: Flow generated no response.");
      	}
	}

    private void processScheduledTransaction() throws Exception{
        Message message = processContext.getTransaction().getInputMessage();
        ScheduleMessage scheduleMessage = (ScheduleMessage) message;
        String msgType = scheduleMessage.getMessageType();

        if (SchedulerConsts.REPEAT_MSG_TYPE.equals(msgType)) {
			logger.debug("Received schedule message with repeat type for Message with transaction id= "
					+ scheduleMessage.getTransaction().getReferenceTransaction().getId());
			createRepeatProcess();
		} else if (SchedulerConsts.REVERSAL_MSG_TYPE.equals(msgType)
				|| SchedulerConsts.REVERSAL_REPEAT_MSG_TYPE.equals(msgType)//m.rehman: for reversal advice support SAF/Loro
				|| SchedulerConsts.LORO_REVERSAL_MSG_TYPE.equals(msgType)//m.rehman: for reversal advice support SAF/Loro
				|| SchedulerConsts.LORO_REVERSAL_REPEAT_MSG_TYPE.equals(msgType)) {//m.rehman: for reversal advice support SAF/Loro
			logger.debug("Received schedule message with reversal type");
			createReversalProcess();
		} else if (SchedulerConsts.CLEAR_MSG_TYPE.equals(msgType)) {
			logger.debug("Received schedule message with clearing type");
			createEODProcess();
	    }else if (SchedulerConsts.TIME_OUT_MSG_TYPE.equals(msgType)
	    		  || SchedulerConsts.REVERSAL_TIME_OUT_MSG_TYPE.equals(msgType)){
	    	logger.debug("Received schedule message with time-out type");
	    	createTimeOutProcess();
	    } else if (SchedulerConsts.SETTLEMENT_MSG_TYPE.equals(msgType)){
	    	logger.debug("Received schedule message with "+msgType+" type");
	    	createSettlementProcess();
		} else if (SchedulerConsts.CONFIRMATION_TRX_TYP.equals(msgType)
				|| SchedulerConsts.ADVICE_MSG_TYPE.equals(msgType)	//m.rehman: for advice support SAF/Loro
				|| SchedulerConsts.ADVICE_REPEAT_MSG_TYPE.equals(msgType)	//m.rehman: for advice support SAF/Loro
				|| SchedulerConsts.LORO_MSG_TYPE.equals(msgType)	//m.rehman: for advice support SAF/Loro
				|| SchedulerConsts.LORO_REPEAT_MSG_TYPE.equals(msgType)) {	//m.rehman: for advice support SAF/Loro
			logger.debug("Received schedule message with Advice type");
			createConfirmationJobProcess();
		}
		//m.rehman: for wallet topup reversal
		else if (SchedulerConsts.WALLET_TOPUP_REVERSAL_MSG_TYPE.equals(msgType)) {
			logger.debug("Received schedule message with Wallet reversal type");
			createWalletTopupReversalProcess();

		} else{
	    	logger.error("Schedule Message not supported!!!, switch cannot dispatch schedule message to an appropriate path. destination is: "
						+ msgType);
	    	throw new Exception(
				"Schedule Message not supported!!!, switch cannot dispatch schedule message to an appropriate path. destination is: "
						+ msgType);
	    }
    }
    
    private void createTimeOutProcess() throws Exception{
//    	CreateTimeOutHandler createTimeOutHandler = new CreateTimeOutHandler();
    	CreateTimeOutHandler.Instance.execute(processContext);  	
    	
    	IfxToProtocolHandler.Instance.execute(processContext);
    	
    	ProtocolToBinaryHandler.Instance.execute(processContext);
	}

    private void createSettlementProcess() throws Exception{
    	CreateSettlementHandler.Instance.execute(processContext);
    	
    	IfxToProtocolHandler.Instance.execute(processContext);
    	
    	ProtocolToBinaryHandler.Instance.execute(processContext);
    	
    	AddRepeatReversalTriggerHandler.Instance.execute(processContext);
    }
    
	private void createRepeatProcess() throws Exception{
    	CreateRepeatHandler.Instance.execute(processContext);

    	if(processContext.isNextStateToEnd())
    		return;
    	
    	if(processContext.getNextState() != null && processContext.getNextState().equals("to IFX to Protocol") ){
			IfxToProtocolHandler.Instance.execute(processContext);
    	}
		
    	ProtocolToBinaryHandler.Instance.execute(processContext);
    }
    
    private void createReversalProcess() throws Exception{
    	CreateReversalHandler.Instance.execute(processContext);
    	
		IfxToProtocolHandler.Instance.execute(processContext);
		
		
		ProtocolToBinaryHandler.Instance.execute(processContext);
    }

    private void createEODProcess() throws Exception{
    	EODHandler.Instance.execute(processContext);
    	
		//Protocol to binary
    	try {
    		ProtocolToBinaryHandler.Instance.execute(processContext);
    		
    		AddRepeatReversalTriggerHandler.Instance.execute(processContext);
    		
		} catch (Exception e) {
			logger.error(e);
    		throw e;
		}
    }

	//m.rehman: for confirmation job process in case of SAF/Loro
	private void createConfirmationJobProcess() throws Exception{
		CreateConfirmationJobHandler.Instance.execute(processContext);

		//need to check whether destination channel is alive or not, if no then do not send advice
		if (processContext.getTransaction().getOutputMessage().getIfx().getRsCode().equals(ISOResponseCodes.APPROVED)) {
			IfxToProtocolHandler.Instance.execute(processContext);

			ProtocolToBinaryHandler.Instance.execute(processContext);

		} else {
			SchedulerService.updateJobInfo(processContext.getTransaction().getReferenceId(), "");

		}
	}

	//m.rehman: for wallet topup reversal
	private void createWalletTopupReversalProcess() throws Exception {
		NetworkUiMessageProcessorHandler.Instance.ProcessWalletTopupReversal(processContext);
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
