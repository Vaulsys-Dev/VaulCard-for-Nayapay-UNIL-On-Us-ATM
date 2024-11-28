package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.ChargeSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "CellCharge_Cycle_Settlement")
public class CellChargeCycleSettlementJob extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		return ChargeSettlementServiceImpl.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		CellChargeCycleSettlementJob newJob = new CellChargeCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("CellChargeCycleSettleJob_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
    }
}
