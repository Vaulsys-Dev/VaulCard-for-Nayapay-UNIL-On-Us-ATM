package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.RequestBasedSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "RequestBased_Cycle_Settlement")
public class RequestBasedCycleSettlementJob extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		return RequestBasedSettlementServiceImpl.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		RequestBasedCycleSettlementJob newJob = new RequestBasedCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("RequestBasedCycleSettleJob_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
    }
}
