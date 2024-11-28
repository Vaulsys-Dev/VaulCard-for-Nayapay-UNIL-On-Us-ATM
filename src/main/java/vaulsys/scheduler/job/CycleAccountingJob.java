package vaulsys.scheduler.job;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.ATMSettlementServiceImpl;
import vaulsys.clearing.settlement.OnlineSettlementService;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.JobLog;
import vaulsys.scheduler.SchedulerService;
import vaulsys.wfe.ProcessContext;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;

@Entity
@DiscriminatorValue(value = "Cycle_Accounting")
public class CycleAccountingJob extends AbstractSwitchJob {
	private static final Logger logger = Logger.getLogger(CycleAccountingJob.class);
	private static ConcurrentHashMap<Long, String> currentlyProcessingProfiles = new ConcurrentHashMap<Long, String>();

    public void execute(JobExecutionContext switchJobContext, JobLog log) {
		try {
			logger.debug("SCHEDULER Accounting job: Start");
			ClearingProfile clearingProfile = null;
			List<CycleSettlementJob> allCycleSettlementJob = null;
			DateTime now = null;
			
			ProcessContext.get().init();
			
			GeneralDao.Instance.beginTransaction();
			try{
				allCycleSettlementJob = SchedulerService.getAllCycleSettlementJob();
				now = DateTime.now();
				GeneralDao.Instance.endTransaction();
			}catch(Exception e){
				logger.error(e);
				log.setStatus(SwitchJobStatus.FAILED);
				log.setExceptionMessage(e.getMessage());

				GeneralDao.Instance.rollback();
				GeneralDao.Instance.close();
				return;
			}
			for (CycleSettlementJob cycleSettlementJob : allCycleSettlementJob) {
				
				if (cycleSettlementJob instanceof OnlinePerTransactionCycleSettlementJob ||
						cycleSettlementJob instanceof OnlineCycleSettlementJob) {
					logger.debug("cycleSettleJob ignored! , online or onlinePerTransaction clearing profile...");
					continue;
				}
				
				try {
					clearingProfile = cycleSettlementJob.getClearingProfile();
					logger.debug("Accounting ClearingProfile: " + clearingProfile.getId());
					if(currentlyProcessingProfiles.containsKey(clearingProfile.getId())){
						logger.debug("Another thread("+currentlyProcessingProfiles.get(clearingProfile.getId())+") is processing clearingProfile: " + clearingProfile.getId()+" continue....");
						continue;
					}
					
					currentlyProcessingProfiles.put(clearingProfile.getId(), Thread.currentThread().getName());
					logger.debug("First step...");
								
					cycleSettlementJob.getSettlementService().account(clearingProfile, clearingProfile.getAccountUntilTime(now), clearingProfile.getSettleUntilTime(now), false, false, OnlineSettlementService.class.equals(clearingProfile.getSettlementClass()), false);
					
					logger.debug("Second step...");
					if (ATMSettlementServiceImpl.class.equals(clearingProfile.getSettlementClass())){
//						cycleSettlementJob.getSettlementService().account(clearingProfile, clearingProfile.getAccountUntilTime(now), clearingProfile.getSettleUntilTime(now), false, false);
						logger.debug("it is ATMSettlementServiceImpl, second step is ignored....");
					}else{
						cycleSettlementJob.getSettlementService().account(clearingProfile, clearingProfile.getAccountUntilTime(now), clearingProfile.calcNextSettleTime(now), false, false, OnlineSettlementService.class.equals(clearingProfile.getSettlementClass()), false);
					}
					
					currentlyProcessingProfiles.remove(clearingProfile.getId());
				} catch (Exception e) {
					logger.error("Exception in Accounting ClearingProfile: " + clearingProfile.getId()+": "+e, e);
					currentlyProcessingProfiles.remove(clearingProfile.getId());
				}
			}
			logger.debug("SCHEDULER Accounting job: End");
			log.setStatus(SwitchJobStatus.FINISHED);
		} catch (Exception e) {
			logger.error("Exception in CycleAccountingJob!!! " + e, e);
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e.getMessage());
		}finally{
			GeneralDao.Instance.close();
		}
	}

    public void interrupt() {
    }

    @Override
	public void submitJob() throws Exception {
        CycleAccountingJob newJob = new CycleAccountingJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.CYCLEACCOUNT);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setJobName("CycleAccountJob");
        JobServiceQuartz.submit(newJob);
	}
    
    public void updateExecutionInfo() {
    }
}