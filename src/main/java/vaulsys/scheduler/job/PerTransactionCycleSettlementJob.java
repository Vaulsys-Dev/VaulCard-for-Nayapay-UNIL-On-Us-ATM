package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.PerTransactionSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "PerTransaction_Cycle_Settlement")
public class PerTransactionCycleSettlementJob extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		return PerTransactionSettlementServiceImpl.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		PerTransactionCycleSettlementJob newJob = new PerTransactionCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("PerTransactionCycleSettleJob_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
    }
}
