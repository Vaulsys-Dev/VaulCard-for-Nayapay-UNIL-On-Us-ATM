package vaulsys.scheduler.job;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.OnlineSettlementService;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.JobLog;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.log4j.Logger;
import org.hibernate.exception.LockAcquisitionException;
import org.quartz.JobExecutionContext;

@Entity
@DiscriminatorValue(value = "OnlineSettlement")
public class OnlineSettlementJob extends AbstractSwitchJob {
	private static final Logger logger = Logger.getLogger(OnlineSettlementJob.class);

	@Override
	public void execute(JobExecutionContext switchJobContext, JobLog log) {
		logger.debug("Starting Online Settlement Job");
		ClearingProfile clearingProfile =null;
		DateTime now = null;
		DateTime afterNow = null;
		
		ProcessContext.get().init();
		
		GeneralDao.Instance.beginTransaction();

		boolean totalAccount = false;
		try{
			String query = "from "+ ClearingProfile.class.getName() +" cp where cp.settlementClass = :onlineSettlement";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("onlineSettlement", OnlineSettlementService.class);
			clearingProfile = (ClearingProfile) GeneralDao.Instance.findObject(query, params);
			if (clearingProfile == null){
				GeneralDao.Instance.endTransaction();
				logger.debug("Returning Online Settlement Job");
				log.setStatus(SwitchJobStatus.FINISHED);
				return;
			}
			
			DateTime prevFireTime = new DateTime(switchJobContext.getPreviousFireTime());
			DateTime curFireTime = new DateTime(switchJobContext.getFireTime());
	
			now = DateTime.now();
			
			afterNow = DateTime.fromNow(60000);
			
			
			if (
					(prevFireTime.getDayTime().getHour() != curFireTime.getDayTime().getHour() &&
							curFireTime.getDayTime().getHour() % 3 == 0)
//					prevFireTime.getDayTime().getHour() != curFireTime.getDayTime().getHour() 
				|| prevFireTime.getDayDate().getDay() != curFireTime.getDayDate().getDay()
				|| prevFireTime.getDayDate().getMonth() != curFireTime.getDayDate().getMonth()
				|| prevFireTime.getDayDate().getYear() != curFireTime.getDayDate().getYear() ){
//				afterNow = now;
				totalAccount = true;
				logger.info("Different Date, previous: "+ prevFireTime +" current: "+ curFireTime);
			}
			GeneralDao.Instance.endTransaction();
			log.setStatus(SwitchJobStatus.FINISHED);
		}catch(Exception e){
			logger.error(e);
			GeneralDao.Instance.rollback();
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e.getMessage());
			return;
		}

		try{
			OnlineSettlementService.Instance.account(clearingProfile, clearingProfile.getAccountUntilTime(now), clearingProfile.getSettleUntilTime(afterNow), false, false, false, true);
			if (totalAccount) {
				logger.info("Accounting all transaction...");
				OnlineSettlementService.Instance.account(clearingProfile, clearingProfile.getAccountUntilTime(now), clearingProfile.getSettleUntilTime(now), false, false, false, false);
			}
		}catch(LockAcquisitionException e1){
			logger.warn("Exception in Online Settlement",e1);
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e1.getMessage());
		}catch(Exception e){
			logger.error("Exception in Online Settlement",e);
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e.getMessage());
		}
		logger.debug("Ending Online Settlement Job");		
	}

	public void interrupt() {
	}

	public void updateExecutionInfo() {
	}

	@Override
	public void submitJob() throws Exception {
        OnlineSettlementJob newJob = new OnlineSettlementJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.ONLINESETTLEMENT);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setJobName("OnlineSettlementJob");
        JobServiceQuartz.submit(newJob);
	}
}