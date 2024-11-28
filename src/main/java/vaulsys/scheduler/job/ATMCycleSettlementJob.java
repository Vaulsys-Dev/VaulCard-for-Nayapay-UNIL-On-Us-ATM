package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.ATMSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "ATM_Cycle_Settlement")
public class ATMCycleSettlementJob extends CycleSettlementJob {

	@Override
	public SettlementService getSettlementService() {
		return ATMSettlementServiceImpl.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		ATMCycleSettlementJob newJob = new ATMCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("ATMCycleSettleJob_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
		GeneralDao.Instance.saveOrUpdate(newJob);
    }
}
