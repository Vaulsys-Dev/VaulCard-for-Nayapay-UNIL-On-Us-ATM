package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.SettlementService;
import vaulsys.clearing.settlement.SeveralPerDayPerTrxSettlementServiceImpl;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import vaulsys.persistence.GeneralDao;

@Entity
@DiscriminatorValue(value = "SeveralPerDayPerTrx_Settlement")
public class SeveralPerDayPerTrxSettlementJob extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		return SeveralPerDayPerTrxSettlementServiceImpl.Instance;
	}

	@Override
	public void submitJob() throws Exception {
		SeveralPerDayPerTrxSettlementJob newJob = new SeveralPerDayPerTrxSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("SeveralPerDayPerTrxSettlementJob_" + this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);

	GeneralDao.Instance.saveOrUpdate(newJob);
	}

}
