package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.InstitutionSettlementServiceImpl2;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "Shetab_Cycle_Settlement2")
public class ShetabCycleSettlementJob2 extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		return InstitutionSettlementServiceImpl2.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		ShetabCycleSettlementJob2 newJob = new ShetabCycleSettlementJob2();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("ShetabCycleSettleJob2_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
    }
}
