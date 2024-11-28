package vaulsys.scheduler.job;

import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.message.MessageManager;
import vaulsys.message.ScheduleMessage;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.JobLog;
import vaulsys.scheduler.SchedulerConsts;
import vaulsys.transaction.Transaction;
import vaulsys.transaction.TransactionType;
import vaulsys.wfe.ProcessContext;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;

@Entity
@DiscriminatorValue(value = "EOD")
public class EODJob extends AbstractSwitchJob {
    private static final Logger logger = Logger.getLogger(EODJob.class);

    public void execute(JobExecutionContext switchJobContext, JobLog log) {
        logger.debug("Try to execute EOD job");
        
        ProcessContext.get().init();
        
        GeneralDao.Instance.beginTransaction();
        
        MessageManager msgManager = MessageManager.getInstance();
        ScheduleMessage scheduleMessage = new ScheduleMessage(SchedulerConsts.CLEAR_MSG_TYPE, null);

        Transaction transaction = new Transaction(TransactionType.SELF_GENERATED);
        transaction.setInputMessage(scheduleMessage);
//        transaction.setStatus(TransactionStatus.RECEIVED);
        scheduleMessage.setTransaction(transaction);
        GeneralDao.Instance.saveOrUpdate(transaction);
        GeneralDao.Instance.saveOrUpdate(scheduleMessage);
        GeneralDao.Instance.saveOrUpdate(scheduleMessage.getMsgXml());
        GeneralDao.Instance.endTransaction();

        msgManager.putRequest(scheduleMessage, null, System.currentTimeMillis());
        log.setStatus(SwitchJobStatus.FINISHED);
//        return true;
    }

    public void interrupt() {
    }

    public void updateExecutionInfo() {
    }

    @Override
    public void submitJob() throws Exception {
        EODJob newEodJob = new EODJob();
        newEodJob.setGroup(SwitchJobGroup.EOD);
        newEodJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newEodJob.setJobSchedule(this.getJobSchedule());
        newEodJob.setJobName("EODJob");
        JobServiceQuartz.submit(newEodJob);
        GeneralDao.Instance.saveOrUpdate(newEodJob);
    }
}
