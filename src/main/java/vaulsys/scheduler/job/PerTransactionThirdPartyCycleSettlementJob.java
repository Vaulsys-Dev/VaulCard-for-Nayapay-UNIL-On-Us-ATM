package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.PerTransactionThirdPartySettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "PerTransactionThirdParty_Cycle_Settlement")
public class PerTransactionThirdPartyCycleSettlementJob extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		return PerTransactionThirdPartySettlementServiceImpl.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		PerTransactionThirdPartyCycleSettlementJob newJob = new PerTransactionThirdPartyCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("PerTransactionThirdPartyCycleSettleJob_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
    }
}
