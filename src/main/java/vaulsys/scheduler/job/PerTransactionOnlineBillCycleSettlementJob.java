package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.PerTransactionOnlineBillSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "PerTrxOnlineBill_Cycle_Settle")
public class PerTransactionOnlineBillCycleSettlementJob extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		return PerTransactionOnlineBillSettlementServiceImpl.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		PerTransactionOnlineBillCycleSettlementJob newJob = new PerTransactionOnlineBillCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("PerTransactionOnlineBillCycleSettleJob_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
    	GeneralDao.Instance.saveOrUpdate(newJob);
    }
}
