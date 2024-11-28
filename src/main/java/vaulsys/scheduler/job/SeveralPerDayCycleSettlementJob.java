package vaulsys.scheduler.job;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import vaulsys.clearing.settlement.SettlementService;
import vaulsys.clearing.settlement.SeveralPerDaySettlementServiceImpl;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;

@Entity
@DiscriminatorValue(value = "SeveralPerDay_Cycle_Settlement")
public class SeveralPerDayCycleSettlementJob extends CycleSettlementJob {
	@Override
	public SettlementService getSettlementService() {
		return SeveralPerDaySettlementServiceImpl.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		SeveralPerDayCycleSettlementJob newJob = new SeveralPerDayCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("SeveralPerDayCycleSettlementJob_" + this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);

	GeneralDao.Instance.saveOrUpdate(newJob);
    }

}
