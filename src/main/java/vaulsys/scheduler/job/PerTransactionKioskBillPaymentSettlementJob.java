package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.PerTransactionKioskBillPaymentSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "PerTrxKioskBillPay_Cycle_Settlement")
public class PerTransactionKioskBillPaymentSettlementJob extends CycleSettlementJob {

    @Override
    public SettlementService getSettlementService() {
        return PerTransactionKioskBillPaymentSettlementServiceImpl.Instance;
    }

    @Override
    public void submitJob() throws Exception {
        PerTransactionKioskBillPaymentSettlementJob newJob = new PerTransactionKioskBillPaymentSettlementJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setClearingProfile(this.getClearingProfile());
        newJob.setJobName("PerTrxKioskBillPayCycleSettleJob_"+this.getClearingProfile().getId());
        JobServiceQuartz.submit(newJob);
    }
}
