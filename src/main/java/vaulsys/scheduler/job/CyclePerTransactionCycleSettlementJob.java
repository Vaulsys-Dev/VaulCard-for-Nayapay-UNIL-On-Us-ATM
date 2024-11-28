package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.CyclePerTransactionSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "Cycle_Per_Trx_Cycle_Settlement")
public class CyclePerTransactionCycleSettlementJob extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		return CyclePerTransactionSettlementServiceImpl.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		CyclePerTransactionCycleSettlementJob newJob = new CyclePerTransactionCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("CyclePerTrxCycleSettleJob_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
    	GeneralDao.Instance.saveOrUpdate(newJob);
    }

}
