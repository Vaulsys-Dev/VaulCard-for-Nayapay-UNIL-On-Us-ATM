package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.ATMDailyRecordSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "ATM_Daily_Record_Cycle_Settlement")
public class ATMDailyRecordCycleSettlementJob extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		return ATMDailyRecordSettlementServiceImpl.Instance;
	}

	@Override
    public void submitJob() throws Exception {
		ATMDailyRecordCycleSettlementJob newJob = new ATMDailyRecordCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("ATMDailyRecordCycleSettleJob_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
    }
}
