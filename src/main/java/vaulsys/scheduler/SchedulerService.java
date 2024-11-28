package vaulsys.scheduler;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementReport;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.message.Message;
import vaulsys.message.MessageManager;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.channel.base.Channel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.protocols.ifx.enums.IfxDirection;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.scheduler.job.CycleSettlementJob;
import vaulsys.terminal.ATMTerminalService;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.LifeCycleStatus;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.transaction.TransactionType;
import vaulsys.util.ConfigUtil;
import vaulsys.util.NotUsed;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;

public class SchedulerService {
	private static final Logger logger = Logger.getLogger(SchedulerService.class);

    public static ScheduleMessage addReversalAndRepeatTrigger(Message messageToSend) {
        logger.debug("Trying to put reversal schedule message...");
        
        TransactionService.updateMessageForNotSuccessful(messageToSend.getIfx(), messageToSend.getTransaction());
                
        Transaction firstTransaction = messageToSend.getTransaction().getFirstTransaction();
        Long amount = (messageToSend.getIfx()!= null)? 0L:null;
        
        if (IfxType.PARTIAL_DISPENSE_RS.equals(messageToSend.getIfx().getIfxType()) ){
        	firstTransaction = messageToSend.getTransaction().getReferenceTransaction();
        	amount = ATMTerminalService.getDispenseAmount((ATMTerminal)messageToSend.getEndPointTerminal(), messageToSend.getIfx());
        }
        
        firstTransaction.getAndLockLifeCycle(LockMode.UPGRADE);
		//m.rehman: for wallet topup reversal message
		ScheduleMessage reversalMsg;
		if (IfxType.WALLET_TOPUP_RS.equals(messageToSend.getIfx().getIfxType())) {
			reversalMsg = createReversalScheduleMsg(firstTransaction, ISOResponseCodes.CUSTOMER_RELATION_NOT_FOUND, amount,
					SchedulerConsts.WALLET_TOPUP_REVERSAL_MSG_TYPE);
		} else {
			reversalMsg = createReversalScheduleMsg(firstTransaction, ISOResponseCodes.CUSTOMER_RELATION_NOT_FOUND, amount);
		}
        
        createReversalJobInfo(firstTransaction, amount);        

        /**** LEILA: check it ****/
        firstTransaction.getLifeCycle().setIsComplete(false);
        /********/
        
        return reversalMsg;
    }
    
    public static ScheduleMessage addReversalAndRepeatTrigger(Message messageToSend, Long amount) {
    	if (amount == null)
    		return addReversalAndRepeatTrigger(messageToSend);
    	
        logger.debug("Trying to put reversal schedule message...");
        
        TransactionService.updateMessageForNotSuccessful(messageToSend.getIfx(), messageToSend.getTransaction());
                
        Transaction firstTransaction = messageToSend.getTransaction().getFirstTransaction();
        
        if (IfxType.PARTIAL_DISPENSE_RS.equals(messageToSend.getIfx().getIfxType()) ){
        	firstTransaction = messageToSend.getTransaction().getReferenceTransaction();
        }
        
        firstTransaction.getAndLockLifeCycle(LockMode.UPGRADE);
        
		ScheduleMessage reversalMsg = createReversalScheduleMsg(firstTransaction, ISOResponseCodes.CUSTOMER_RELATION_NOT_FOUND, amount);
        
        createReversalJobInfo(firstTransaction, amount);        

        /**** LEILA: check it ****/
        firstTransaction.getLifeCycle().setIsComplete(false);
        /********/
        
        return reversalMsg;
    }
    
    public static ScheduleMessage addInstantReversalAndRepeatTriggerAndRemoveOldTriggers(Transaction transaction/*Message messageToSend*/, String cause, Long amount) {
        logger.debug("Trying to put reversal schedule message...");
        
        transaction.getAndLockLifeCycle(LockMode.UPGRADE);
        
//        Transaction transaction = messageToSend.getTransaction();
		ScheduleMessage reversalMsg = createReversalScheduleMsg(transaction, cause, amount);
//        if (putRequest){
//            MessageManager.getInstance().putRequest(reversalMsg);
//        }

        removeReversalJobInfo(transaction.getId());
        createReversalJobInfo(transaction, amount);
        
        /**** LEILA: check it ****/
        transaction.getLifeCycle().setIsComplete(false);
        /********/

        return reversalMsg;
    }

    @NotUsed
    public static ScheduleMessage addInstantSettlementTriggers(boolean putRequest, SettlementData settlementData) {
    	logger.debug("Trying to put Settlement schedule message...");
    	ScheduleMessage settlementMsg = createSettlementScheduleMsg(settlementData, ISOResponseCodes.APPROVED);
    	if (putRequest)
    		MessageManager.getInstance().putRequest(settlementMsg, null, System.currentTimeMillis());
    	createSettlementJobInfo(settlementMsg.getTransaction(), settlementData);
    	return settlementMsg;
    }
    
    public static void addInstantIssuingFCBDocumentTriggers(SettlementReport report, IssuingDocumentAction action) {
		logger.debug("Trying to add Issuing FCB_Document triggers...");
		//removeIssuingFCBDocumentJobInfo(report, action);
		if(SchedulerService.getFCBDocumentsJobInfoOfStlReport(report.getId()) == null){
			IssuingFCBDocumentJobInfo jobInfo = new IssuingFCBDocumentJobInfo(DateTime.fromNow(ConfigUtil.getLong(ConfigUtil.REVERSAL_TIMEOUT)), report, action);
			GeneralDao.Instance.saveOrUpdate(jobInfo);
		}
	}
    
    @NotUsed
    private static void removeIssuingFCBDocumentJobInfo(SettlementReport report, IssuingDocumentAction action) {
    	logger.debug("IssuingFCBDocumentJobInfo-report: "+report.getId());
    	String query = "from " + IssuingFCBDocumentJobInfo.class.getName() + " i " +
                " where i.report = :report and i.action = :action";
		Map<String, Object> params = new HashMap<String, Object>(1);
		params.put("report", report);
		params.put("action", action);
		IssuingFCBDocumentJobInfo job = null;
		job = (IssuingFCBDocumentJobInfo) GeneralDao.Instance.findObject(query, params);

    	if (job != null) {
        	logger.debug("job found: "+job.getId());
			try {
				GeneralDao.Instance.lockReadAndWrite(job);
		    	logger.debug("after lockReadAndWrite");
				GeneralDao.Instance.delete(job);
		    	logger.debug("after delete");
				GeneralDao.Instance.flush();
			} finally {
				GeneralDao.Instance.releaseLock(job);
		    	logger.debug("after releaseLock");
			}
		}
	}

    @NotUsed
	public static <T extends JobInfo> List<T> getAllJobInfo(Class<T> clazz) {
        String query = "from " + clazz.getName();
        return GeneralDao.Instance.find(query, null);
    }
    
    @NotUsed
    public static CycleSettlementJob getCycleSettlementJob(ClearingProfile clearingProfile) {
        String query = "from " + CycleSettlementJob.class.getName() + " i where " +
                "i.clearingProfile = :clearingProfile";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("clearingProfile", clearingProfile);
        return (CycleSettlementJob) GeneralDao.Instance.findObject(query, params);
    }
    
    public static List<CycleSettlementJob> getAllCycleSettlementJob() {
    	String query = "from " + CycleSettlementJob.class.getName() + " cp order by cp.clearingProfile"
//    	+ " where not (cp.clearingProfile.settlementClass = :onlineSettlement and cp.clearingProfile.settlementClass = :onlinePerTrxSettlement)"
    	;
    	Map<String, Object> params = new HashMap<String, Object>();
//    	params.put("onlineSettlement", OnlineSettlementService.class);
//    	params.put("onlinePerTrxSettlement", OnlinePerTransactionSettlementServiceImpl.class);
    	return GeneralDao.Instance.find(query, params);
    }

    public static <T extends JobInfo> List<T> getToBeFiredJobInfo(Class<T> clazz, Integer maxJobs) {
        String query = "from " + clazz.getName() + " i where " +
                "i.fireTime.dayDate < :dayDate or " +
        		"(i.fireTime.dayDate = :dayDate and " +
        		"i.fireTime.dayTime <= :dayTime) ";

		//m.rehman: fetch records which count is greater than zero, means not expired and response code is null
		query += " and i.count > 0 ";
		query += " and i.responseCode is null ";

        if(clazz.equals(ReversalJobInfo.class) || clazz.equals(ConfirmationJobInfo.class) || clazz.equals(TransferSorushJobInfo.class)){
        	query += " and i.deleted = false ";
            query += " order by i.id";
        }
        if(clazz.equals(IssuingFCBDocumentJobInfo.class)){
        	query += "order by mod(i.id, 8)";
        }
        Map<String, Object> params = new HashMap<String, Object>();
        DateTime now = DateTime.now();
        params.put("dayDate", now.getDayDate());
        params.put("dayTime", now.getDayTime());

		//System.out.println("SchedularService:: query [" + query.toString() + "]"); //Raza TEMP

        if(maxJobs == null)
        	return GeneralDao.Instance.find(query, params);
        else
        	return GeneralDao.Instance.find(query, params, 0, maxJobs);    	
    }
    
    public static <T extends JobInfo> List<T> getToBeFiredJobInfo(Class<T> clazz) {
    	return getToBeFiredJobInfo(clazz, null);
    }

    public static IssuingFCBDocumentJobInfo getFCBDocumentsJobInfoOfStlReport(Long stlReport) {
        String query = "from IssuingFCBDocumentJobInfo i where i.report.id = :report ";
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("report", stlReport);
        return (IssuingFCBDocumentJobInfo) GeneralDao.Instance.findUniqueObject(query, params);
    }

    public static void removeRepeatJobInfo(Serializable transactionId) {
    	logger.debug("removeRepeatJobInfo-trx: "+transactionId);
    	String query = "delete from " + RepeatJobInfo.class.getName() + " i " +
                " where i.transaction.id = :transactionId";
		Map<String, Object> params = new HashMap<String, Object>(1);
		params.put("transactionId", transactionId);
		GeneralDao.Instance.executeUpdate(query, params);
    }

    public static void createReversalJobInfo(Transaction transaction, Long amount) {
		ReversalJobInfo reversalJobInfo;
		Channel channel = transaction.getInputMessage().getChannel(); //Raza CHANNEL TIMEOUT
		if(channel.getTimeOut() != null)
		{
			reversalJobInfo = new ReversalJobInfo(DateTime.fromNow(channel.getTimeOut()), transaction, amount, ConfigUtil.getInteger(ConfigUtil.REVERSAL_COUNT)); //Raza CHANNEL TIMEOUT
		}
		else
		{
			reversalJobInfo = new ReversalJobInfo(DateTime.fromNow(ConfigUtil.getInteger(ConfigUtil.REVERSAL_TIMEOUT)), transaction, amount, ConfigUtil.getInteger(ConfigUtil.REVERSAL_COUNT)); //Raza CHANNEL TIMEOUT
		}
        //ReversalJobInfo reversalJobInfo = new ReversalJobInfo(DateTime.fromNow(ConfigUtil.getInteger(ConfigUtil.REVERSAL_TIMEOUT)), transaction, amount, ConfigUtil.getInteger(ConfigUtil.REVERSAL_COUNT)); //Raza CHANNEL TIMEOUT

        GeneralDao.Instance.saveOrUpdate(reversalJobInfo);
    }
    
    public static void createSettlementJobInfo(Transaction transaction, SettlementData settlementData) {
    	SettlementJobInfo jobInfo = new SettlementJobInfo(DateTime.now(), transaction, settlementData,-1);
    	GeneralDao.Instance.saveOrUpdate(jobInfo);
    }
    
    public static void processReversalJob(Transaction transaction_rq, Transaction transaction_rs, String cause, Long amount, boolean isForced) {
		if (transaction_rq.getIncomingIfx()/*getInputMessage().getIfx()*/ != null &&
				!ISOFinalMessageType.isMessageNotToBeReverse(transaction_rq.getIncomingIfx()/*getInputMessage().getIfx()*/.getIfxType())) {

			if (transaction_rq.getOutputMessage() != null 
					&& transaction_rq.getOutgoingIfx()/*getOutputMessage().getIfx()*/ != null
					&& transaction_rq.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getRsCode() != null
					&& 
					(!ISOResponseCodes.APPROVED.equals(transaction_rq.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getRsCode()) &&
						!ISOResponseCodes.shouldBeRepeated(transaction_rq.getOutgoingIfx().getRsCode())	)
					) {
				return;
			}

			LifeCycle lifeCycle = transaction_rq.getAndLockLifeCycle(LockMode.UPGRADE);

			if(lifeCycle.getIsFullyReveresed() != null)
				return;
			
			if (transaction_rs != null 
					&& transaction_rs.getInputMessage() != null
					&& transaction_rs.getIncomingIfx()/*getInputMessage().getIfx()*/ != null
					&& transaction_rs.getIncomingIfx()/*getInputMessage().getIfx()*/.getRsCode() != null
					&& 
					(!ISOResponseCodes.APPROVED.equals(transaction_rs.getIncomingIfx()/*getInputMessage().getIfx()*/.getRsCode()) &&
						!ISOResponseCodes.shouldBeRepeated(transaction_rs.getIncomingIfx().getRsCode())	)
					) {
				return;
			}
			
			if (!isForced && TransactionService.isReferenceTrxSettled(transaction_rq)) {
				logger.debug("Originator Transaction already settled.(refTrx: " + transaction_rq.getId() + ")");
				return;
			}

			lifeCycle.setIsComplete(false);
			lifeCycle.setIsFullyReveresed(LifeCycleStatus.REQUEST);
			GeneralDao.Instance.saveOrUpdate(lifeCycle);
			
			logger.debug("try to put reversal job info for trx: " + transaction_rq.getId());
			
			SchedulerService.createReversalJobInfo(transaction_rq, cause, amount);
		}
	}
    
    public static void createReversalJobInfo(Transaction transaction, String cause, Long amount) {
    	ReversalJobInfo reversalJobInfo = new ReversalJobInfo(DateTime.fromNow(ConfigUtil.getInteger(ConfigUtil.REVERSAL_TIMEOUT)), transaction, amount, cause, ConfigUtil.getInteger(ConfigUtil.REVERSAL_COUNT));
    	GeneralDao.Instance.saveOrUpdate(reversalJobInfo);
    }

    public static void removeReversalJobInfo(Serializable transactionId) {
    	
    	logger.debug("removeReversalJobInfo-trx: "+transactionId);
    	String query = "update " + ReversalJobInfo.class.getName() + " i " +
                " set i.deleted = true where i.transaction.id = :transactionId and i.deleted = false";
		Map<String, Object> params = new HashMap<String, Object>(1);
		params.put("transactionId", transactionId);
		GeneralDao.Instance.executeUpdate(query, params);
		
//		logger.debug("removeReversalJobInfo-trx: "+transactionId);
//    	String query = "delete from " + ReversalJobInfo.class.getName() + " i " +
//                " where i.transaction.id = :transactionId";
//		Map<String, Object> params = new HashMap<String, Object>(1);
//		params.put("transactionId", transactionId);
//		GeneralDao.Instance.executeUpdate(query, params);
    }

    @NotUsed
    public static void removeSettlementJobInfo(Serializable transactionId) {
    	logger.debug("removeSettlementJobInfo-trx: "+transactionId);
    	String query = "from " + SettlementJobInfo.class.getName() + " i " +
    	" where i.transaction.id = :transactionId";
    	Map<String, Object> params = new HashMap<String, Object>(1);
    	params.put("transactionId", transactionId);
    	SettlementJobInfo job = null;
    	job = (SettlementJobInfo) GeneralDao.Instance.findObject(query, params);
    	
    	if (job != null) {
    		logger.debug("job found: "+job.getId());
    		try {
    			logger.debug("before lockReadAndWrite: "+job.getId());
    			GeneralDao.Instance.lockReadAndWrite(job);
    			logger.debug("after lockReadAndWrite");
    			GeneralDao.Instance.delete(job);
    			logger.debug("after delete");
    			GeneralDao.Instance.flush();
    			logger.debug("after flush");
    		} finally {
    			logger.debug("before releaseLock"+job.getId());
    			GeneralDao.Instance.releaseLock(job);
    			logger.debug("after releaseLock");
    		}
    	}
    }
    
    
    
//    public static void removeRepeatOrReversalJobInfo(Serializable transactionId) {
//    	removeReversalJobInfo(transactionId);
////        removeRepeatJobInfo(transactionId);
//    }

    public static ScheduleMessage createReversalScheduleMsg(Transaction transaction, String cause, Long amount) {
        ScheduleMessage reversalMessage = new ScheduleMessage(SchedulerConsts.REVERSAL_MSG_TYPE, amount);
        reversalMessage.setEndPointTerminal(transaction.getOutgoingIfxOrMessageEndpoint());
        reversalMessage.setResponseCode(cause);
        Transaction newTransaction = new Transaction(TransactionType.SELF_GENERATED);
        newTransaction.setDebugTag("REVERSAL_"+ transaction.getDebugTag());
//        newTransaction.setAuthorized(true);
        newTransaction.setInputMessage(reversalMessage);
//        newTransaction.setStatus(TransactionStatus.RECEIVED);
        newTransaction.setReferenceTransaction((Transaction) transaction);
        newTransaction.setFirstTransaction(newTransaction);

        reversalMessage.setTransaction(newTransaction);
        GeneralDao.Instance.saveOrUpdate(newTransaction);
        GeneralDao.Instance.saveOrUpdate(reversalMessage);
        GeneralDao.Instance.saveOrUpdate(reversalMessage.getMsgXml());
        return reversalMessage;
    }

/*
    public static ScheduleMessage createEODScheduleMsg(Institution institution) {
        ScheduleMessage EODMessage = new ScheduleMessage(SchedulerConsts.CLEAR_MSG_TYPE, null);
        
        if (TransactionService.isPSPSwitch(institution.getCode()))
        	EODMessage.setEndPointTerminal(ProcessContext.get().getAcquireSwitchTerminal(institution));
		else
			EODMessage.setEndPointTerminal(ProcessContext.get().getIssuerSwitchTerminal(institution));
		if (EODMessage.getEndPointTerminal() == null)
			EODMessage.setEndPointTerminal(ProcessContext.get().getAcquireSwitchTerminal(institution));
        
//        EODMessage.setEndPointTerminal(ProcessContext.get().getAcquireSwitchTerminal(institution));
        
        
        EODMessage.setInstCode(institution.getCode());

        Transaction newTransaction = new Transaction(TransactionType.SELF_GENERATED);

        newTransaction.setDebugTag("EOD_"+ institution.getCode());
        newTransaction.setInputMessage(EODMessage);
        newTransaction.setFirstTransaction(newTransaction);

        EODMessage.setTransaction(newTransaction);
        GeneralDao.Instance.saveOrUpdate(newTransaction);
        GeneralDao.Instance.saveOrUpdate(EODMessage);
        GeneralDao.Instance.saveOrUpdate(EODMessage.getMsgXml());
        return EODMessage;
    }
*/    
    @NotUsed
    //Only in a garbage class
    public static ScheduleMessage createRepeatScheduleMsg(Transaction transaction, Long amount) {
        ScheduleMessage scheduleMessage;
        scheduleMessage = new ScheduleMessage(SchedulerConsts.REPEAT_MSG_TYPE, amount);
//        scheduleMessage.setEndPoint(transaction.getOutputMessage().getEndPoint());
        scheduleMessage.setEndPointTerminal(transaction.getOutgoingIfxOrMessageEndpoint());
        Transaction newTransaction = new Transaction(TransactionType.SELF_GENERATED);
        newTransaction.setDebugTag("REPEAT_"+ transaction.getDebugTag());
//        newTransaction.setAuthorized(true);
        newTransaction.setInputMessage(scheduleMessage);
//        newTransaction.setStatus(TransactionStatus.RECEIVED);
        newTransaction.setReferenceTransaction((Transaction) transaction);
        newTransaction.setFirstTransaction(newTransaction);

        scheduleMessage.setTransaction(newTransaction);

        GeneralDao.Instance.saveOrUpdate(newTransaction);
        GeneralDao.Instance.saveOrUpdate(scheduleMessage);
        GeneralDao.Instance.saveOrUpdate(scheduleMessage.getMsgXml());
        return scheduleMessage;
    }

	public static ScheduleMessage createTimeOutMsgScheduleMsg(Transaction transaction, String shetabTimeOut) {
		ScheduleMessage scheduleMessage;
        scheduleMessage = new ScheduleMessage(SchedulerConsts.TIME_OUT_MSG_TYPE, transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getAuth_Amt());
        scheduleMessage.setEndPointTerminal(transaction.getIncomingIfxOrMessageEndpoint());
        scheduleMessage.setResponseCode(shetabTimeOut);
        Transaction newTransaction = new Transaction(TransactionType.SELF_GENERATED);
        newTransaction.setDebugTag("TIME_OUT_"+ transaction.getDebugTag());
//        newTransaction.setAuthorized(true);
        newTransaction.setInputMessage(scheduleMessage);
//        newTransaction.setStatus(TransactionStatus.RECEIVED);
        newTransaction.setReferenceTransaction((Transaction) transaction);
        newTransaction.setFirstTransaction(newTransaction);

        scheduleMessage.setTransaction(newTransaction);
        GeneralDao.Instance.saveOrUpdate(newTransaction);
        GeneralDao.Instance.saveOrUpdate(scheduleMessage);
        GeneralDao.Instance.saveOrUpdate(scheduleMessage.getMsgXml());
        return scheduleMessage;
	}
	
	public static ScheduleMessage createReversalTimeOutMsgScheduleMsg(Transaction transaction, String shetabTimeOut) {
		Terminal endPointTerminal = transaction.getIncomingIfxOrMessageEndpoint();
		if ( endPointTerminal == null || TerminalType.ATM.equals(endPointTerminal.getTerminalType()))
			return null;
		
		ScheduleMessage scheduleMessage;
		scheduleMessage = new ScheduleMessage(SchedulerConsts.REVERSAL_TIME_OUT_MSG_TYPE, transaction.getIncomingIfx()/*getInputMessage().getIfx()*/.getAuth_Amt());
		scheduleMessage.setEndPointTerminal(transaction.getIncomingIfxOrMessageEndpoint());
		scheduleMessage.setResponseCode(shetabTimeOut);
		Transaction newTransaction = new Transaction(TransactionType.SELF_GENERATED);
		newTransaction.setDebugTag("TIME_OUT_"+ transaction.getDebugTag());
//		newTransaction.setAuthorized(true);
		newTransaction.setInputMessage(scheduleMessage);
//		newTransaction.setStatus(TransactionStatus.RECEIVED);
		newTransaction.setReferenceTransaction(transaction);
		newTransaction.setFirstTransaction(newTransaction);
		
		scheduleMessage.setTransaction(newTransaction);
		GeneralDao.Instance.saveOrUpdate(newTransaction);
		GeneralDao.Instance.saveOrUpdate(scheduleMessage);
        GeneralDao.Instance.saveOrUpdate(scheduleMessage.getMsgXml());
		return scheduleMessage;
	}

	
	public static ScheduleMessage createSettlementScheduleMsg(SettlementData settlementData, String cause) {
		ScheduleMessage scheduleMessage;
        scheduleMessage = new ScheduleMessage(SchedulerConsts.SETTLEMENT_MSG_TYPE, settlementData.getTotalSettlementAmount());
//        scheduleMessage.setEndPointTerminal(transaction.getInputMessage().getEndPointTerminal());
        scheduleMessage.setResponseCode(cause);
        Transaction newTransaction = new Transaction(TransactionType.SELF_GENERATED);
        newTransaction.setDebugTag("SETTLEMENT_"+ settlementData.getId());
//        newTransaction.setAuthorized(true);
        newTransaction.setInputMessage(scheduleMessage);
//        newTransaction.setStatus(TransactionStatus.RECEIVED);
//        newTransaction.setReferenceTransaction((Transaction) transaction);
        newTransaction.setFirstTransaction(newTransaction);

        scheduleMessage.setTransaction(newTransaction);
        FinancialEntity entity = settlementData.getFinancialEntity();
        Terminal terminal = settlementData.getTerminal();
        Long terminalCode = (terminal != null)? terminal.getCode(): entity.getCode();
        Ifx ifx = generateSettlementScheduleMsgIfx(entity.getCode(), terminalCode, entity.getAccount().getCardNumber(), settlementData.getTotalSettlementAmount());
        scheduleMessage.setIfx(ifx);
        LifeCycle lifeCycle = new LifeCycle();
        GeneralDao.Instance.saveOrUpdate(lifeCycle);
        newTransaction.setLifeCycle(lifeCycle);
        GeneralDao.Instance.saveOrUpdate(newTransaction);
        GeneralDao.Instance.saveOrUpdate(ifx);
        GeneralDao.Instance.saveOrUpdate(scheduleMessage);
        GeneralDao.Instance.saveOrUpdate(scheduleMessage.getMsgXml());
        return scheduleMessage;
	}

	
	private static Ifx generateSettlementScheduleMsgIfx(Long orgIdNum, Long terminalId, String cardNumber, Long amount) {
		DateTime now = DateTime.now();
		Ifx ifx = new Ifx();
		ifx.setIfxDirection(IfxDirection.INCOMING);
		ifx.setIfxType(IfxType.SETTLEMENT_TRANSFER_TO_ACCOUNT_RQ);
		ifx.setTrnType(TrnType.INCREMENTALTRANSFER);
		
		ifx.setAccTypeFrom(AccType.MAIN_ACCOUNT);
		ifx.setAccTypeTo(AccType.MAIN_ACCOUNT);
		
//		ifx.setAuth_Currency(GlobalContext.getInstance().getRialCurrency().getCode());
		ifx.setAuth_Currency(ProcessContext.get().getRialCurrency().getCode());
		ifx.setAuth_CurRate("1");
		ifx.setAuth_Amt(amount);
		ifx.setReal_Amt(amount);
		ifx.setTrx_Amt(amount);
		
		ifx.setSec_Currency(ifx.getAuth_Currency());
//		ifx.setSec_CurRate("1");
		ifx.setSec_Amt(ifx.getAuth_Amt());
		
		ifx.setOrigDt(now);
		ifx.setTrnDt(now);
		ifx.setReceivedDt(now);
		
		ifx.setSrc_TrnSeqCntr( Util.trimLeftZeros(Util.generateTrnSeqCntr(6)));
        ifx.setMy_TrnSeqCntr(ifx.getSrc_TrnSeqCntr());
		ifx.setNetworkRefId(Util.generateTrnSeqCntr(11));
        
        ifx.setTerminalType(TerminalType.ATM);
        ifx.setTerminalId(terminalId.toString());
        ifx.setOrgIdNum(orgIdNum.toString());
        
//		ifx.setBankId(GlobalContext.getInstance().getMyInstitution().getBin());
		ifx.setBankId(ProcessContext.get().getMyInstitution().getBin().toString());
		ifx.setRecvBankId(ifx.getBankId());
		
		ifx.setAppPAN(cardNumber);
		ifx.setDestBankId(cardNumber.substring(0, 6));
		ifx.setFwdBankId(ifx.getDestBankId());
		ifx.setTrk2EquivData("5022291100000011=93071204105500000000");
		ifx.setPINBlock("C5433EE9B5D7FC78");
		ifx.setSecondAppPan("5022291100000011");
		return ifx;
	}

	//m.rehman: following routines/functions for Confirmation Job (SAF/Loro)
	public static void createConfirmationJobInfo(Transaction transaction) {
		//Mirkamali(940926): Confirmation log
		logger.debug("@" + transaction.getId() + " ConfirmationLog_2: Adding confirmationJobInfo");
		if(transaction.getLifeCycle() != null && transaction.getLifeCycle().getIsFullyReveresed() == null || LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsFullyReveresed())){
			ConfirmationJobInfo confirmationJobInfo = new ConfirmationJobInfo(DateTime.fromNow(ConfigUtil.getInteger(ConfigUtil.REVERSAL_TIMEOUT)), transaction, 0L, ConfigUtil.getInteger(ConfigUtil.REVERSAL_COUNT));
			GeneralDao.Instance.saveOrUpdate(confirmationJobInfo);
		} else {
			logger.debug("OHOH: didn't add Confirmation job because in transaction " + transaction.getId() + " the isFullyReverse Flag is : " + transaction.getLifeCycle().getIsFullyReveresed() );
		}
	}

	public static void removeConfirmationJobInfo(Serializable transactionId) {

		logger.debug("removeConfirmationJobInfo-trx: " + transactionId);
		String query = "update " + ConfirmationJobInfo.class.getName() + " i " +
				" set i.deleted = true where i.transaction.id = :transactionId and i.deleted = false";
		Map<String, Object> params = new HashMap<String, Object>(1);
		params.put("transactionId", transactionId);
		GeneralDao.Instance.executeUpdate(query, params);

	}

	public static ScheduleMessage createConfirmationScheduleMsg(Transaction transaction, String scheduleMsgType) {
		ScheduleMessage scheduleMessage;
		scheduleMessage = new ScheduleMessage(scheduleMsgType, transaction.getFirstTransaction().getOutgoingIfx().getAuth_Amt());
		scheduleMessage.setEndPointTerminal(transaction.getOutgoingIfxOrMessageEndpoint());
		Transaction newTransaction = new Transaction(TransactionType.SELF_GENERATED);
		newTransaction.setInputMessage(scheduleMessage);

		String debugTag, transactionDebugTag;
		transactionDebugTag = transaction.getDebugTag();
		if (scheduleMsgType.equals(SchedulerConsts.ADVICE_MSG_TYPE))
			debugTag = "ADVICE_" + transactionDebugTag;
		else if (scheduleMsgType.equals(SchedulerConsts.ADVICE_REPEAT_MSG_TYPE))
			debugTag = "ADVICE_REPEAT_" + transactionDebugTag;
		else if (scheduleMsgType.equals(SchedulerConsts.LORO_MSG_TYPE))
			debugTag = "LORO_" + transactionDebugTag;
		else if (scheduleMsgType.equals(SchedulerConsts.LORO_REPEAT_MSG_TYPE))
			debugTag = "LORO_REPEAT_" + transactionDebugTag;
		else
			debugTag = "CONFIRMATION_" + transactionDebugTag;

		newTransaction.setDebugTag(debugTag);
		newTransaction.setFirstTransaction(newTransaction);
		newTransaction.setReferenceTransaction(transaction);

		newTransaction.setLifeCycle(transaction.getLifeCycle());
		GeneralDao.Instance.saveOrUpdate(newTransaction);
		scheduleMessage.setTransaction(newTransaction);
		GeneralDao.Instance.saveOrUpdate(scheduleMessage);
		GeneralDao.Instance.saveOrUpdate(scheduleMessage.getMsgXml());
		return scheduleMessage;
	}

	public static void updateJobInfo(Serializable transactionId, String respCode) {

		logger.debug("updateJobInfo-trx: " + transactionId);
		Integer maxCount = ConfigUtil.getInteger(ConfigUtil.REVERSAL_COUNT);
		String query = "update JobInfo i " +
				"set i.responseCode = :respCode, " +
				"i.count = i.count+1 " +
				"where i.transaction.id = :transactionId " +
				"and i.count < :maxCount";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("transactionId", transactionId);
		params.put("respCode", respCode);
		params.put("maxCount", maxCount);
		GeneralDao.Instance.executeUpdate(query, params);
	}

	//m.rehman: for reversal, reversal repeat, loro reversal and loro repeat reversal
	public static ScheduleMessage createReversalScheduleMsg(Transaction transaction, String cause, Long amount,
															String scheduleMgConst) {
		ScheduleMessage reversalMessage = new ScheduleMessage(scheduleMgConst, amount);
		reversalMessage.setEndPointTerminal(transaction.getOutgoingIfxOrMessageEndpoint());
		reversalMessage.setResponseCode(cause);
		Transaction newTransaction = new Transaction(TransactionType.SELF_GENERATED);

		String debugTag, transactionDebugTag;
		transactionDebugTag = transaction.getDebugTag();
		if (scheduleMgConst.equals(SchedulerConsts.REVERSAL_MSG_TYPE))
			debugTag = "REVERSAL_" + transactionDebugTag;
		else if (scheduleMgConst.equals(SchedulerConsts.REVERSAL_REPEAT_MSG_TYPE))
			debugTag = "REVERSAL_REPEAT_" + transactionDebugTag;
		else if (scheduleMgConst.equals(SchedulerConsts.LORO_REVERSAL_MSG_TYPE))
			debugTag = "LORO_REVERSAL_" + transactionDebugTag;
		else if (scheduleMgConst.equals(SchedulerConsts.LORO_REVERSAL_REPEAT_MSG_TYPE))
			debugTag = "LORO_REVERSAL_REPEAT_" + transactionDebugTag;
		else if (scheduleMgConst.equals(SchedulerConsts.WALLET_TOPUP_REVERSAL_MSG_TYPE))
			debugTag = "REVERSAL_" + transactionDebugTag;
		else
			debugTag = "REVERSAL_" + transactionDebugTag;

		newTransaction.setDebugTag(debugTag);
		newTransaction.setInputMessage(reversalMessage);
		newTransaction.setReferenceTransaction((Transaction) transaction);
		newTransaction.setFirstTransaction(newTransaction);

		reversalMessage.setTransaction(newTransaction);
		GeneralDao.Instance.saveOrUpdate(newTransaction);
		GeneralDao.Instance.saveOrUpdate(reversalMessage);
		GeneralDao.Instance.saveOrUpdate(reversalMessage.getMsgXml());
		return reversalMessage;
	}
}
