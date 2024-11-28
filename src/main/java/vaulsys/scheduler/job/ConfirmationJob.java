package vaulsys.scheduler.job;

import vaulsys.calendar.DateTime;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.message.Message;
import vaulsys.message.MessageManager;
import vaulsys.message.ScheduleMessage;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.ChannelManager;
import vaulsys.network.channel.base.OutputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.scheduler.ConfirmationJobInfo;
import vaulsys.scheduler.JobLog;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.scheduler.SchedulerService;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.util.ConfigUtil;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.exception.LockAcquisitionException;
import org.quartz.JobExecutionContext;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@DiscriminatorValue(value = "Confirmation")
public class ConfirmationJob extends AbstractSwitchJob {
    private static final Logger logger = Logger.getLogger(ConfirmationJob.class);

    private static long CONFIRMATION_COUNT_MAX;
    private static long CONFIRMATION_TIMEOUT;
    private static long CONFIRMATION_SLEEP_TIME;
    private static final int MAX_ITERATIONS_TO_DELETE_CONFIRMATION_JOBS = 100;
    private static final int MAX_ROWS_TO_DELETE_FROM_CONFIRMATION_JOBS = 10;

    private static boolean isFree = true;

    public void execute(JobExecutionContext switchJobContext, JobLog log) {
        // for UI issues, the two following initializations are put here
        CONFIRMATION_COUNT_MAX = ConfigUtil.getLong(ConfigUtil.REPEAT_COUNT);
        CONFIRMATION_TIMEOUT = ConfigUtil.getLong(ConfigUtil.REPEAT_TIMEOUT);
        CONFIRMATION_SLEEP_TIME = ConfigUtil.getLong(ConfigUtil.REPEAT_SLEEP_TIME);


        logger.debug("Starting Confirmation Job");

        if (!isJobFree()) {
            logger.error("Another thread is running... Exiting from ConfirmationJob");
            log.setStatus(SwitchJobStatus.FINISHED);
            log.setExceptionMessage("Job is not free");
            return;
        }

        ProcessContext.get().init();
        int numDeleted;
        boolean echo = false;

        GeneralDao.Instance.beginTransaction();
        for (int i = 0; i < MAX_ITERATIONS_TO_DELETE_CONFIRMATION_JOBS; i++) {
            logger.debug("ConfirmationJob,Iteration number: " + i);
            numDeleted = GeneralDao.Instance.executeSqlUpdate("delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)
                    + ".job_info where job='Confirmation' and deleted=1 and rownum<=" + MAX_ROWS_TO_DELETE_FROM_CONFIRMATION_JOBS);
            if (numDeleted < MAX_ROWS_TO_DELETE_FROM_CONFIRMATION_JOBS)
                break;
        }
        GeneralDao.Instance.endTransaction();

        GeneralDao.Instance.beginTransaction();

        List<Message> requests = new ArrayList<Message>();

        try {
            // devided by two just to insure that there are enough space for TimeOut Msgs
            int maxScheduleMsg = (MessageManager.getInstance().getMaxPossibleScheduleJobs() / 10);

            List<ConfirmationJobInfo> confirmJobInfos = SchedulerService.getToBeFiredJobInfo(ConfirmationJobInfo.class, maxScheduleMsg);
            //m.rehman: will not remove expired jobs i.e. SAF/Loro
            //List<ConfirmationJobInfo> deletingJobInfos = new ArrayList<ConfirmationJobInfo>();
            logger.debug("Num messages to confirm: " + confirmJobInfos.size());

            int count = MessageManager.getInstance().getCurrentScheduledThreadQueueSize();
            long timeDelay = (switchJobContext.getNextFireTime().getTime() - System.currentTimeMillis()) / 2;

            logger.debug("maxScheduleMsg: " + maxScheduleMsg + " ,count:" + count);
            if (!echo) {
                for (ConfirmationJobInfo confirmJobInfo : confirmJobInfos) {
                    try {
                        if (count >= maxScheduleMsg) {
                            logger.debug("maxScheduleMsg reached, breaking loop...");
                            break;
                        }

                        if (count * CONFIRMATION_SLEEP_TIME > timeDelay) {
                            logger.debug("maxScheduleMsg reached, breaking loop (timeDelay)...");
                            break;
                        }

                        try {
                            GeneralDao.Instance.refresh(confirmJobInfo);
                            GeneralDao.Instance.synchObject(confirmJobInfo, LockMode.UPGRADE_NOWAIT);
                            if (Boolean.TRUE.equals(confirmJobInfo.getDeleted())) {
                                logger.info("Advice for trx: " + confirmJobInfo.getTransaction().getId() + " is ignored, deleted is TRUE!");
                                continue;
                            }
                        } catch (Exception e) {
                            logger.error(e, e);
                            continue;
                        }
                        logger.debug("try to send Advice (id): " + confirmJobInfo.getId() + ") on trx("
                                + confirmJobInfo.getTransaction().getId() + ")");
                        Message confirmationMessage = confirmation(confirmJobInfo);
                        //m.rehman: will not remove expired jobs i.e. SAF/Loro
                        //boolean done = confirmJobInfo.getCount() <= 1;
                        if (confirmationMessage != null) {
                            count++;
                            requests.add(confirmationMessage);
                            if (confirmationMessage.getPendingRequests() != null && !confirmationMessage.getPendingRequests().isEmpty()) {
                                requests.addAll(confirmationMessage.getPendingRequests());
                                confirmationMessage.setPendingRequests(null);
                            }
                        }
                        //m.rehman: will not remove expired jobs i.e. SAF/Loro
                        /*
                        else {
                            // TODO if reverseMessage wasn't generated,shall we delete
                            // its reversalJobInfo?!
                            done = true;
                        }

                        if (done) {
//					getService().createRepeatJobInfo(reverseMessage.getTransaction());
//						if(deletingJobInfos.size() < 400){

                            logger.debug("try to remove reverse job (" + confirmJobInfo.getId() + ") on trx(" + confirmJobInfo.getTransaction().getId() + ")");
                            deletingJobInfos.add(confirmJobInfo);
//						}else{
//							logger.debug("reverse job ("+reversalJobInfo.getId()+") on trx("+reversalJobInfo.getTransaction().getId()+") should be removed but there are more than 400 jobs to be deleted. delete it next time");										
//						}
                        }
                        */
                    } catch (LockAcquisitionException e) {
                        logger.warn("confirmation job (" + confirmJobInfo.getId() + ") on trx(" + confirmJobInfo.getTransaction().getId() + " was put back to the next round!", e);
                    }

                    GeneralDao.Instance.endTransaction();
                    GeneralDao.Instance.beginTransaction();
                }
            }

            //m.rehman: will not remove expired jobs i.e. SAF/Loro
            /*
            if (echo) {
//				List<Message> requests = new ArrayList<Message>(); 
                Channel channel = ChannelManager.getInstance().getChannel(581672000L, "out");
            }


            if (!deletingJobInfos.isEmpty()) {

                List<ConfirmationJobInfo> jobForQuery = new ArrayList<ConfirmationJobInfo>();
                int counter = 0;

                for (int i = 0; i < deletingJobInfos.size(); i++) {
                    jobForQuery.add(deletingJobInfos.get(i));
                    counter++;
                    if (counter == 500 || i == deletingJobInfos.size() - 1) {
                        String query = "delete from ReversalJobInfo j where j in (:ids)";
                        Map<String, Object> params = new HashMap<String, Object>(1);
                        params.put("ids", jobForQuery);
                        int numAffected = GeneralDao.Instance.executeUpdate(query, params);

                        logger.debug("Num affected jobInfo with batch update of delete: " + numAffected);

                        counter = 0;
                        jobForQuery = new ArrayList<ConfirmationJobInfo>();
                    }
                }

//				//TODO: Fix the bug on using "in" in query 
//				String query = "delete from ReversalJobInfo j where j in (:ids)";
//		        Map<String, Object> params = new HashMap<String, Object>(1);
//		        params.put("ids", deletingJobInfos);
//				GeneralDao.Instance.executeUpdate(query, params);
            }
        */
            log.setStatus(SwitchJobStatus.FINISHED);
        } catch (Exception e) {
            logger.error(e);
            log.setStatus(SwitchJobStatus.FAILED);
            log.setExceptionMessage(e.getMessage());
        } finally {
            setJobFree();
            GeneralDao.Instance.endTransaction();
        }

        if (!requests.isEmpty()) {
            MessageManager.getInstance().putRequests(requests);
        }

        logger.debug("Ending Confirmation Job");
    }

    private Message confirmation(ConfirmationJobInfo confirmJobInfo) throws LockAcquisitionException {
        Message scheduleMessage = createMessage(confirmJobInfo.getTransaction(), new Long(confirmJobInfo.getCount()));

        long delayTime = ConfirmationJob.CONFIRMATION_TIMEOUT;
        try {
            delayTime *= ((ConfirmationJob.CONFIRMATION_COUNT_MAX - confirmJobInfo.getCount()) / 5) + 1;
            delayTime = (delayTime > 3600000) ? 3600000 : delayTime;
        } catch (Exception e) {
            delayTime = ConfirmationJob.CONFIRMATION_TIMEOUT;
        }

        confirmJobInfo.setCount(confirmJobInfo.getCount() - 1);
        confirmJobInfo.setFireTime(DateTime.fromNow(delayTime));
        GeneralDao.Instance.saveOrUpdate(confirmJobInfo);

        return scheduleMessage;
    }

    private Message createMessage(Transaction transaction, Long count) {
        String scheduleConst;
        Long maxCount = CONFIRMATION_COUNT_MAX;
        if (ISOMessageTypes.isFinancialAdviceResponseMessage(transaction.getOutgoingIfx().getMti())) {
            if (count.equals(maxCount))
                scheduleConst = SchedulerConsts.ADVICE_MSG_TYPE;
            else
                scheduleConst = SchedulerConsts.ADVICE_REPEAT_MSG_TYPE;
        } else {
            if (count.equals(maxCount))
                scheduleConst = SchedulerConsts.LORO_MSG_TYPE;
            else
                scheduleConst = SchedulerConsts.LORO_REPEAT_MSG_TYPE;
        }
        return SchedulerService.createConfirmationScheduleMsg(transaction, scheduleConst);
    }

    public void interrupt() {
    }

    public void updateExecutionInfo() {
    }

    @Override
    public void submitJob() throws Exception {
        ConfirmationJob newJob = new ConfirmationJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.REPEAT);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setJobName("ConfirmationJob");
        JobServiceQuartz.submit(newJob);
    }

    @Override
    public boolean doLog() {
        return false;
    }

    public synchronized boolean isJobFree() {
        if (isFree == true) {
            isFree = false;
            return true;
        }
        return false;
    }

    public void setJobFree() {
        isFree = true;
    }
}
