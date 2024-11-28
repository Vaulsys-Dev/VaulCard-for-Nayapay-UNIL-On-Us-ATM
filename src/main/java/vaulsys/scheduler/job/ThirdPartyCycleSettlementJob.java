package vaulsys.scheduler.job;

import vaulsys.clearing.settlement.SettlementService;
import vaulsys.clearing.settlement.ThirdPartySettlementServiceImpl;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "Third_Party_Cycle_Settlement")
public class ThirdPartyCycleSettlementJob extends CycleSettlementJob{
	@Override
	public SettlementService getSettlementService() {
		return ThirdPartySettlementServiceImpl.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		ThirdPartyCycleSettlementJob newJob = new ThirdPartyCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("ThirdPartyCycleSettleJob_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
    	GeneralDao.Instance.saveOrUpdate(newJob);
    }

}
