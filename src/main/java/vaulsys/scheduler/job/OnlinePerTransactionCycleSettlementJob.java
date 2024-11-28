package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.OnlinePerTransactionSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "Online_PerTransaction_Cycle_Settlement")
public class OnlinePerTransactionCycleSettlementJob extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		return OnlinePerTransactionSettlementServiceImpl.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		OnlinePerTransactionCycleSettlementJob newJob = new OnlinePerTransactionCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("OnlinePerTransactionCycleSettleJob_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
    	GeneralDao.Instance.saveOrUpdate(newJob);
    }
}
