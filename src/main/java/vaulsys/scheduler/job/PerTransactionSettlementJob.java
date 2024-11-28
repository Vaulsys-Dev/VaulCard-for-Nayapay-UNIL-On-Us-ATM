package vaulsys.scheduler.job;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.PerTransactionSettlementServiceImpl;
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
@DiscriminatorValue(value = "PerTransactionSettlement")
public class PerTransactionSettlementJob extends AbstractSwitchJob {
	private static final Logger logger = Logger.getLogger(PerTransactionSettlementJob.class);

	@Override
	public void execute(JobExecutionContext switchJobContext, JobLog log) {
		logger.debug("Starting PerTransaction Settlement Job");
		ClearingProfile clearingProfile =null;
		DateTime now = null;
		DateTime afterNow = null;
		
		ProcessContext.get().init();
		
		GeneralDao.Instance.beginTransaction();

		boolean totalAccount = false;
		try{
			String query = "from "+ ClearingProfile.class.getName() +" cp where cp.settlementClass = :settlement";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("settlement", PerTransactionSettlementServiceImpl.class);
			clearingProfile = (ClearingProfile) GeneralDao.Instance.findObject(query, params);
			if (clearingProfile == null){
				GeneralDao.Instance.endTransaction();
				logger.debug("Returning PerTransaction Settlement Job");
				log.setStatus(SwitchJobStatus.FINISHED);
				return;
			}
			
			DateTime prevFireTime = new DateTime(switchJobContext.getPreviousFireTime());
			DateTime curFireTime = new DateTime(switchJobContext.getFireTime());
			
			DateTime zeroTime = new DateTime(new DayDate(), new DayTime());
	
			now = DateTime.now();
			
			afterNow = DateTime.fromNow(60000);
			
			
			if (
					(prevFireTime.getDayTime().getHour() != curFireTime.getDayTime().getHour() &&
							curFireTime.getDayTime().getHour() % 3 == 0 &&
							!prevFireTime.equals(zeroTime))
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
			PerTransactionSettlementServiceImpl.Instance.account(clearingProfile, clearingProfile.getAccountUntilTime(now), clearingProfile.getSettleUntilTime(afterNow), false, false, false, true);
			if (totalAccount) {
				logger.info("Accounting all transaction...");
				PerTransactionSettlementServiceImpl.Instance.account(clearingProfile, clearingProfile.getAccountUntilTime(now), clearingProfile.getSettleUntilTime(now), false, false, false, false);
			}
		}catch(LockAcquisitionException e1){
			logger.warn("Exception in PerTransaction Settlement",e1);
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e1.getMessage());
		}catch(Exception e){
			logger.error("Exception in PerTransaction Settlement",e);
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e.getMessage());
		}
		logger.debug("Ending PerTransaction Settlement Job");		
	}

	public void interrupt() {
	}

	public void updateExecutionInfo() {
	}

	@Override
	public void submitJob() throws Exception {
        PerTransactionSettlementJob newJob = new PerTransactionSettlementJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.ONLINESETTLEMENT);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setJobName("PerTransactionSettlementJob");
        JobServiceQuartz.submit(newJob);
        GeneralDao.Instance.saveOrUpdate(newJob);
	}
}