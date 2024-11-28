package vaulsys.scheduler.job;

import vaulsys.calendar.DateTime;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.JobLog;
import vaulsys.util.ConfigUtil;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;

/**
 * 
 * @author p.moosavi
 * Task 50617 : Add New Policy for max card amount for Currency ATM
 */
@Entity
@DiscriminatorValue(value = "deletecarddata")
public class DeleteCardDataJob extends AbstractSwitchJob {
	
	private static final Logger logger = Logger.getLogger(DeleteCardDataJob.class);
	private static boolean isFree = true;

	@Override
	public void execute(JobExecutionContext avicennaJobContext, JobLog log) {
		
		logger.debug("Starting DeleteCardData Job");
		
		if(!isJobFree()){
			logger.error("Another thread is running... Exiting from DeleteCardDataJob");
			log.setStatus(SwitchJobStatus.FINISHED);
			log.setExceptionMessage("Job is not free");
			return;
		}
		
		ProcessContext.get().init();
		try{
		GeneralDao.Instance.beginTransaction();
	
		String query = "delete from "
				+ "CardData cd where " +
                "cd.fireTime.dayDate < :dayDate or " +
        		"(cd.fireTime.dayDate = :dayDate and " +
        		"cd.fireTime.dayTime <= :dayTime) ";
        Map<String, Object> params = new HashMap<String, Object>();
        DateTime now = DateTime.now();
        params.put("dayDate", now.getDayDate());
        params.put("dayTime", now.getDayTime());
        int numDeleted = GeneralDao.Instance.executeUpdate(query, params);
        logger.debug("Num card datas to delete: " + numDeleted);
        log.setStatus(SwitchJobStatus.FINISHED);
        
		} catch (Exception e) {
			logger.error(e);
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e.getMessage());
		} finally {
			setJobFree();
			GeneralDao.Instance.endTransaction();
		}
		logger.debug("Ending DeleteCardData Job");
		
	}

	@Override
	public void updateExecutionInfo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void submitJob() throws Exception {
        DeleteCardDataJob newJob = new DeleteCardDataJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.GENERAL);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setJobName("DeleteCardDataJob");
        JobServiceQuartz.submit(newJob);
		
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

}

