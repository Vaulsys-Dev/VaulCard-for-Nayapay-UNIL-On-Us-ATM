package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.PerTransactionBillPaymentSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "PerTransactionBillPayment_Cycle_Settlement")
public class PerTransactionBillPaymentCycleSettlementJob extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		return PerTransactionBillPaymentSettlementServiceImpl.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		PerTransactionBillPaymentCycleSettlementJob newJob = new PerTransactionBillPaymentCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("PerTransactionBillPaymentCycleSettleJob_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
    }
}
