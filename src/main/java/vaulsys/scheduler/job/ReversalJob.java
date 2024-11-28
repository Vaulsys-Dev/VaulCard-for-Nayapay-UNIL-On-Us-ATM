package vaulsys.scheduler.job;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.message.Message;
import vaulsys.message.MessageManager;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.NetworkManager;
import vaulsys.network.channel.base.ChannelManager;
import vaulsys.network.channel.base.CommunicationMethod;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.enums.TerminalType;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.scheduler.JobLog;
import vaulsys.scheduler.ReversalJobInfo;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.TerminalService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.LifeCycle;
import vaulsys.transaction.LifeCycleStatus;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionService;
import vaulsys.util.ConfigUtil;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.exception.LockAcquisitionException;
import org.quartz.JobExecutionContext;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@DiscriminatorValue(value = "Reversal")
public class ReversalJob extends AbstractSwitchJob {
	private static final Logger logger = Logger.getLogger(ReversalJob.class);

	private static long REVERSAL_COUNT_MAX;
	private static long REVERSAL_TIMEOUT;
	private static long REVERSAL_SLEEP_TIME;
	private static final int MAX_ITERATIONS_TO_DELETE_REVERSAL_JOBS = 100;
	private static final int MAX_ROWS_TO_DELETE_FROM_REVERSAL_JOBS = 30;
	
	private static boolean isFree = true;
	
	public void execute(JobExecutionContext switchJobContext, JobLog log) {
		// for UI issues, the two following initializations are put here
		REVERSAL_COUNT_MAX = ConfigUtil.getLong(ConfigUtil.REVERSAL_COUNT);
		REVERSAL_TIMEOUT = ConfigUtil.getLong(ConfigUtil.REVERSAL_TIMEOUT);
		REVERSAL_SLEEP_TIME = ConfigUtil.getLong(ConfigUtil.REVERSAL_SLEEP_TIME);

		
		logger.info("Starting Reversal Job");

		if(!isJobFree()){
			logger.error("Another thread is running... Exiting from ReversalJob");
			log.setStatus(SwitchJobStatus.FINISHED);
			log.setExceptionMessage("Job is not free");
			return;
		}

		ProcessContext.get().init();
		int numDeleted;
		
		GeneralDao.Instance.beginTransaction();
		for(int i=0; i<MAX_ITERATIONS_TO_DELETE_REVERSAL_JOBS; i++){
			logger.debug("ReversalJob,Iteration number: "+ i);
			numDeleted = GeneralDao.Instance.executeSqlUpdate("delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".job_info where job='Reversal' and deleted=1 and rownum<="+MAX_ROWS_TO_DELETE_FROM_REVERSAL_JOBS);
			if(numDeleted < MAX_ROWS_TO_DELETE_FROM_REVERSAL_JOBS)
				break;
		}
		GeneralDao.Instance.endTransaction();
		
		GeneralDao.Instance.beginTransaction();

		List<Message> requests = new ArrayList<Message>(); 

		try {			
			// devided by two just to insure that there are enough space for TimeOut Msgs
			int maxScheduleMsg = (MessageManager.getInstance().getMaxPossibleScheduleJobs() / 10);

			List<ReversalJobInfo> reversalJobInfos = SchedulerService.getToBeFiredJobInfo(ReversalJobInfo.class, maxScheduleMsg);
			List<ReversalJobInfo> deletingJobInfos = new ArrayList<ReversalJobInfo>();
			logger.debug("Num messages to reverse: " + reversalJobInfos.size());

			int count = MessageManager.getInstance().getCurrentScheduledThreadQueueSize();
			long timeDelay = (switchJobContext.getNextFireTime().getTime() - System.currentTimeMillis()) / 2;

			logger.debug("maxScheduleMsg: " + maxScheduleMsg + " ,count:" + count);
			for (ReversalJobInfo reversalJobInfo : reversalJobInfos) {
				try {
					if (count >= maxScheduleMsg) {
						logger.debug("maxScheduleMsg reached, breaking loop...");
						break;
					}

					if (count * REVERSAL_SLEEP_TIME > timeDelay) {
						logger.debug("maxScheduleMsg reached, breaking loop (timeDelay)...");
						break;
					}

					try {
						
						/**
						 * @author k.khodadi
						 * for transaction sourosh succes but fail from atm or switch
						 */
						if( TransactionService.checkReverseSorush(reversalJobInfo) ){
							Long trxID = reversalJobInfo.getTransaction()== null?0: reversalJobInfo.getTransaction().getId();
							logger.info("can not reverse sorush transaction with ID "
									       + trxID.toString() +
											" Success");				
							deletingJobInfos.add(reversalJobInfo);
							continue;
						}
						
//						Map<String, Object> params = new HashMap<String, Object>();
//						params.put("jobInfoId", reversalJobInfo.getId());
//						GeneralDao.Instance.findUnique("select r from " + ReversalJobInfo.class.getName() + " r where r.id = :jobInfoId for update nowait", params );
						GeneralDao.Instance.refresh(reversalJobInfo);
						GeneralDao.Instance.synchObject(reversalJobInfo, LockMode.UPGRADE_NOWAIT);
						if (Boolean.TRUE.equals(reversalJobInfo.getDeleted())) {
							logger.info("reversal for trx: " + reversalJobInfo.getTransaction().getId() + " is ignored, deleted is TRUE!"); 
							continue;
						}
					}catch(Exception e){
						logger.error(e,e);
						continue;
					}
                    if (reversalJobInfo.getCount() == ConfigUtil.getLong(ConfigUtil.REVERSAL_COUNT)) { //Raza adding for TIMEOUT start
                        logger.info("try to send TimeOut Resp of Trx: " + reversalJobInfo.getTransaction().getId());
                        Message timeoutMessage = getTimeOutMsg(reversalJobInfo);

                        if (timeoutMessage != null) {
                            //count++;
                            //response = timeoutMessage;
                            //timeoutMessage.setType(MessageType.OUTGOING);
                            //timeoutMessage.setChannel(ChannelManager.getInstance().getChannel(reversalJobInfo.getTransaction().getInputMessage().getChannelName()));
                            //MessageManager.getInstance().putResponse(timeoutMessage);
                            requests.add(timeoutMessage);
                            if (timeoutMessage.getPendingRequests() != null && !timeoutMessage.getPendingRequests().isEmpty()) {
                                requests.addAll(timeoutMessage.getPendingRequests());
                                timeoutMessage.setPendingRequests(null);
                            }
                        }
                    } //Raza adding for TIMEOUT end
                    logger.info("try to reverse (id): " + reversalJobInfo.getId() + ") on trx("
							+ reversalJobInfo.getTransaction().getId() + ")");
					Message reverseMessage = reverse(reversalJobInfo);
					//m.rehman: will not remove expired jobs
					//boolean done = reversalJobInfo.getCount() <= 1;
					if (reverseMessage != null) {
						count++;
						requests.add(reverseMessage);
						if (reverseMessage.getPendingRequests() != null && !reverseMessage.getPendingRequests().isEmpty()) {
							requests.addAll(reverseMessage.getPendingRequests());
							reverseMessage.setPendingRequests(null);
						}
					}
					//m.rehman: will not remove expired jobs
					/*else {
						// TODO if reverseMessage wasn't generated,shall we delete
						// its reversalJobInfo?!
						done = true;
					}

					if (done) {
//					getService().createRepeatJobInfo(reverseMessage.getTransaction());
//						if(deletingJobInfos.size() < 400){
						
							logger.debug("try to remove reverse job ("+reversalJobInfo.getId()+") on trx("+reversalJobInfo.getTransaction().getId()+")");				
							deletingJobInfos.add(reversalJobInfo);
//						}else{
//							logger.debug("reverse job ("+reversalJobInfo.getId()+") on trx("+reversalJobInfo.getTransaction().getId()+") should be removed but there are more than 400 jobs to be deleted. delete it next time");										
//						}
					}*/
				} catch (LockAcquisitionException e) {
					logger.warn("reverse job ("+reversalJobInfo.getId()+") on trx("+reversalJobInfo.getTransaction().getId()+" was put back to the next round!", e);
				}
				
				GeneralDao.Instance.endTransaction();
				GeneralDao.Instance.beginTransaction();
			}

			//m.rehman: will not remove expired jobs
			/*
			if (!deletingJobInfos.isEmpty()) {
				
				List<ReversalJobInfo> jobForQuery = new ArrayList<ReversalJobInfo>();
				int counter = 0;
				
				for (int i = 0; i < deletingJobInfos.size(); i++) {
					jobForQuery.add(deletingJobInfos.get(i));
					counter++;
					if(counter == 500 || i == deletingJobInfos.size() - 1) {
						String query = "delete from ReversalJobInfo j where j in (:ids)";
				        Map<String, Object> params = new HashMap<String, Object>(1);
				        params.put("ids", jobForQuery);
						int numAffected = GeneralDao.Instance.executeUpdate(query, params);
						
						logger.debug("Num affected jobInfo with batch update of delete: "+numAffected);
						
						counter = 0;
						jobForQuery = new ArrayList<ReversalJobInfo>();
					}
				}

				
//				//TODO: Fix the bug on using "in" in query 
//				String query = "delete from ReversalJobInfo j where j in (:ids)";
//		        Map<String, Object> params = new HashMap<String, Object>(1);
//		        params.put("ids", deletingJobInfos);
//				GeneralDao.Instance.executeUpdate(query, params);
			}*/
			log.setStatus(SwitchJobStatus.FINISHED);
		} catch (Exception e) {
			logger.error(e);
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e.getMessage());
		} finally {
			setJobFree();
			GeneralDao.Instance.endTransaction();
		}

		if (!requests.isEmpty()){
			MessageManager.getInstance().putRequests(requests);
		}

		logger.info("Ending Reversal Job");
	}

	private Message reverse(ReversalJobInfo reversalJobInfo) throws LockAcquisitionException{
		Terminal endpoint = null;
		try{
			Ifx outgoingIfx = reversalJobInfo.getTransaction().getOutgoingIfx();
			if (ProcessContext.get().getMyInstitution().getBin().equals(outgoingIfx.getBankId()) &&
					FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())) {
				endpoint = TerminalService.getMatchingTerminal(outgoingIfx);
			}
		}catch (Exception e) {
			logger.error("could not get endpoint terminal",e);
			return null;
		}
		
		try {
			if(endpoint!=null && 
					TerminalType.isPhisycalDeviceTerminal(endpoint.getTerminalType()) && 
					endpoint.getLastIncomingTransactionId().equals(reversalJobInfo.getTransaction().getId())) {
				
				logger.debug("Try to get Lock of Terminal["+endpoint.getCode()+"]");
//				GeneralDao.Instance.synchObject(endpoint, LockMode.UPGRADE_NOWAIT);
				TerminalService.lockTerminal(endpoint.getCode().toString(), LockMode.UPGRADE_NOWAIT);	
				logger.debug("Terminal["+ endpoint.getCode()+"] has beeb locked and it's reloaded!");
			}
		} catch (LockAcquisitionException e) {
			logger.error("could not lock endpoint terminal: "+endpoint.getCode(),e);
		}


		
		boolean canBeDone = checkAndUpdateLifeCycle(reversalJobInfo.getTransaction());
		
		if (!canBeDone)
			return null;

		
//		reversalJobInfo.getTransaction().getAndLockLifeCycle(LockMode.UPGRADE);
		LifeCycleStatus reveresed = reversalJobInfo.getTransaction().getLifeCycle().getIsFullyReveresed();

		/*
		m.rehman: introducing count variable to check whether its reversal/loro reversal or
		 			reversal repeat/loro reversal repeat transaction
		*/
		//Message scheduleMessage = createMessage(reversalJobInfo.getTransaction(), reversalJobInfo.getAmount(), reversalJobInfo.getResponseCode());
		Message scheduleMessage = createMessage(reversalJobInfo.getTransaction(), reversalJobInfo.getAmount(),
					reversalJobInfo.getResponseCode(), reversalJobInfo.getCount());
		
		if (LifeCycleStatus.NOTHING.equals(reveresed)) {
			Message timeOutResponse = null;
			try {
				timeOutResponse = SchedulerService.createReversalTimeOutMsgScheduleMsg(
						reversalJobInfo.getTransaction(), ISOResponseCodes.INVALID_TO_ACCOUNT);
			} catch (Exception e) {
			}
			Set<Message> messages = new HashSet<Message>();
			if (timeOutResponse != null)
				messages.add(timeOutResponse);
			scheduleMessage.setPendingRequests(messages);
		}
		long delayTime = ReversalJob.REVERSAL_TIMEOUT;
		try {
			delayTime *= ((ReversalJob.REVERSAL_COUNT_MAX - reversalJobInfo.getCount())/5) +1;
//			delayTime = (delayTime>=ReversalJob.REVERSAL_TIMEOUT)? delayTime :ReversalJob.REVERSAL_TIMEOUT;
			delayTime = (delayTime > 3600000)? 3600000: delayTime;
		} catch (Exception e) {
			delayTime = ReversalJob.REVERSAL_TIMEOUT;
		}
		
		/***********/
		try { //TODO verify BELOW CHECK
			if (!(ChannelManager.getInstance().getChannel(reversalJobInfo.getTransaction().getInputMessage().getChannelName()).getCommunicationMethod().equals(CommunicationMethod.SAME_SOCKET)))
			{
				NetworkManager.getInstance().removeResponseOnSameSocketConnectionById(reversalJobInfo.getTransaction().getInputMessage().getId());
				logger.info("removing removeResponseOnSameSocketConnectionById: " + reversalJobInfo.getTransaction().getInputMessage().getId());
			}
		}
		catch(Exception e)
		{
			logger.error("Exception caught while removing ResponseonSameSocket..!");
		}
        //NetworkManager.getInstance().removeResponseOnSameSocketConnectionById(reversalJobInfo.getTransaction().getInputMessage().getId());
        //logger.info("removing removeResponseOnSameSocketConnectionById: " + reversalJobInfo.getTransaction().getInputMessage().getId());
		/***********/
		
		reversalJobInfo.setCount(reversalJobInfo.getCount() - 1);
		reversalJobInfo.setFireTime(DateTime.fromNow(delayTime));
		GeneralDao.Instance.saveOrUpdate(reversalJobInfo);

		return scheduleMessage;
	}

	private boolean checkAndUpdateLifeCycle(Transaction transaction) throws LockAcquisitionException {
		LifeCycle lifeCycle = null;
		try {
			logger.debug("Try to get Lock of LifeCycle["+ transaction.getLifeCycleId()+")");
			lifeCycle = transaction.getAndLockLifeCycle(LockMode.UPGRADE_NOWAIT);
//			GeneralDao.Instance.optimizedSynchObject(transaction.getLifeCycle(), LockMode.UPGRADE_NOWAIT);
			logger.debug("LifeCycle["+ transaction.getLifeCycleId()+"] has beeb locked and it's reloaded!");
		} catch (LockAcquisitionException e) {
//			e.printStackTrace();
			throw e;
		}
		
		if (lifeCycle.getIsComplete()){
			logger.debug("LifeCycle["+ transaction.getLifeCycleId()+"] is complete so it won't be reversed!");
			return false;
		}
		
		if(transaction.getOutputMessage() == null || transaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/ == null)
			return false;
		
		if (ISOFinalMessageType.isReturnMessage(transaction.getOutgoingIfx()/*getOutputMessage().getIfx()*/.getIfxType())
			&& LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsReturnReversed()))
		{
			transaction.getLifeCycle().setIsReturnReversed(LifeCycleStatus.REQUEST);
			GeneralDao.Instance.saveOrUpdate(transaction.getLifeCycle());

		} else if (LifeCycleStatus.NOTHING.equals(transaction.getLifeCycle().getIsFullyReveresed())) {
			transaction.getLifeCycle().setIsFullyReveresed(LifeCycleStatus.REQUEST);
			GeneralDao.Instance.saveOrUpdate(transaction.getLifeCycle());
		}
		
		return true;
	}

//	private Message createTimeOutResponse(Transaction transaction) {
//		ScheduleMessage timeOutMsgScheduleMsg = SwitchApplication.get().getSchedulerService()
//				.createTimeOutMsgScheduleMsg(transaction, ErrorCodes.INVALID_TO_ACCOUNT);
//		return timeOutMsgScheduleMsg;
//	}

    private Message createMessage(Transaction transaction, Long amount, String cause) {
    	if (cause == null)
    		cause = ISOResponseCodes.INVALID_TO_ACCOUNT;
		return SchedulerService.createReversalScheduleMsg(transaction, cause, amount);
    }

	//m.rehman: for reversal, reversal repeat, loro reversal and loro repeat reversal
	private Message createMessage(Transaction transaction, Long amount, String cause, Integer count) {
		String scheduleConst;
		IfxType ifxType = transaction.getOutgoingIfx().getIfxType();

		if (cause == null)
			cause = ISOResponseCodes.INVALID_TO_ACCOUNT;

		if (ISOFinalMessageType.isLoroMessage(ifxType)) {
			if (count.longValue() == REVERSAL_COUNT_MAX)
				scheduleConst = SchedulerConsts.LORO_REVERSAL_MSG_TYPE;
			else
				scheduleConst = SchedulerConsts.LORO_REVERSAL_REPEAT_MSG_TYPE;
		} else if (ISOFinalMessageType.isWalletMessage(ifxType)) {	//m.rehman: for wallet messages
			if (count.longValue() == REVERSAL_COUNT_MAX)
				scheduleConst = SchedulerConsts.WALLET_TOPUP_REVERSAL_MSG_TYPE;
			else
				scheduleConst = SchedulerConsts.WALLET_TOPUP_REVERSAL_REPEAT_MSG_TYPE;
		} else {
			if (count.longValue() == REVERSAL_COUNT_MAX)
				scheduleConst = SchedulerConsts.REVERSAL_MSG_TYPE;
			else
				scheduleConst = SchedulerConsts.REVERSAL_REPEAT_MSG_TYPE;
		}

		return SchedulerService.createReversalScheduleMsg(transaction, cause, amount, scheduleConst);
	}

	public void interrupt() {
	}

	public void updateExecutionInfo() {
	}

	@Override
	public void submitJob() throws Exception {
        ReversalJob newJob = new ReversalJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.REVERSAL);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setJobName("ReversalJob");
        JobServiceQuartz.submit(newJob);
	}
	
	@Override
	public boolean doLog() {
		return false;
	}
	
	public synchronized boolean isJobFree(){
		if(isFree == true){
			isFree = false;
			return true;
		}
		return false;
	}
	
	public void setJobFree(){
		isFree = true;
	}
    private Message getTimeOutMsg(ReversalJobInfo reversalJobInfo) throws LockAcquisitionException { //Raza adding to send TimeOut Response
        Terminal endpoint = null;
        try {
            Ifx outgoingIfx = reversalJobInfo.getTransaction().getIncomingIfx().copy();
            if (ProcessContext.get().getMyInstitution().getBin().equals(outgoingIfx.getBankId()) &&
                    FinancialEntityRole.MY_SELF.equals(ProcessContext.get().getMyInstitution().getRole())) {
                endpoint = TerminalService.getMatchingTerminal(outgoingIfx);
            }
        } catch (Exception e) {
            logger.error("could not get endpoint terminal", e);
            return null;
        }

        try {
            if (endpoint != null &&
                    TerminalType.isPhisycalDeviceTerminal(endpoint.getTerminalType()) &&
                    endpoint.getLastIncomingTransactionId().equals(reversalJobInfo.getTransaction().getId())) {

                logger.debug("Try to get Lock of Terminal[" + endpoint.getCode() + "]");
//				GeneralDao.Instance.synchObject(endpoint, LockMode.UPGRADE_NOWAIT);
                TerminalService.lockTerminal(endpoint.getCode().toString(), LockMode.UPGRADE_NOWAIT);
                logger.debug("Terminal[" + endpoint.getCode() + "] has beeb locked and it's reloaded!");
            }
        } catch (LockAcquisitionException e) {
            logger.error("could not lock endpoint terminal: " + endpoint.getCode(), e);
        }

        //boolean canBeDone = checkAndUpdateLifeCycle(reversalJobInfo.getTransaction());

        //if (!canBeDone)
            //return null;


        //LifeCycleStatus reveresed = reversalJobInfo.getTransaction().getLifeCycle().getIsFullyReveresed();
        ScheduleMessage timeOutMsg = null;

        try { //Raza adding for Timeout start
                Ifx timeoutIfx = reversalJobInfo.getTransaction().getOutgoingIfx().copy();
                GeneralDao.Instance.saveOrUpdate(timeoutIfx);
                TransactionService.updateMessageForNotSuccessful(timeoutIfx, reversalJobInfo.getTransaction());
                logger.info("MTI of TimeOut [" + reversalJobInfo.getTransaction().getOutgoingIfx().getMti() + "]"); //Raza TEMP
                logger.info("Channel Name [" + reversalJobInfo.getTransaction().getInputMessage().getChannelName() + "]");
                timeOutMsg = SchedulerService.createTimeOutMsgScheduleMsg(reversalJobInfo.getTransaction(), ISOResponseCodes.INVALID_TO_ACCOUNT);
                //timeOutMsg.setIfx(timeoutIfx);

        } catch (Exception e) {
            logger.error("Exception caught while sending reply in case of TimeOut...");
        } //Raza adding for Timeout end

//        if (LifeCycleStatus.NOTHING.equals(reveresed)) {
//            Message timeOutResponse = null;
//            try {
//                timeOutResponse = SchedulerService.createReversalTimeOutMsgScheduleMsg(
//                        reversalJobInfo.getTransaction(), ISOResponseCodes.INVALID_TO_ACCOUNT);
//            } catch (Exception e) {
//            }
//        }
        return timeOutMsg;
    }
}
