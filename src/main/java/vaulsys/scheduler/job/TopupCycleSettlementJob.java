package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.SettlementService;
import vaulsys.clearing.settlement.TopupSettlementServiceImpl;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "Topup_Cycle_Settlement")
public class TopupCycleSettlementJob extends CycleSettlementJob{

	@Override
	public SettlementService getSettlementService() {
		return TopupSettlementServiceImpl.Instance;
	}

	@Override
	public void submitJob() throws Exception {
		TopupCycleSettlementJob newJob = new TopupCycleSettlementJob();
		newJob.setStatus(SwitchJobStatus.NOT_STARTED);
		newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
		newJob.setJobSchedule(this.getJobSchedule());
		newJob.setClearingProfile(this.getClearingProfile());
		newJob.setJobName("TopupCycleSettleJob_" + this.getClearingProfile().getId());
		JobServiceQuartz.submit(newJob);
	}

}
