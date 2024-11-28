package vaulsys.scheduler.job;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.settlement.MCIBillPaymentSettlementServiceImpl;
import vaulsys.clearing.settlement.SettlementService;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.JobLog;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.quartz.JobExecutionContext;

@Entity
@DiscriminatorValue(value = "MCIBill_Cycle_Settlement")
public class MCIBillPaymentCycleSettlementJob extends CycleSettlementJob {

	private static boolean isFree = true;

	@Override
	public SettlementService getSettlementService() {
		return MCIBillPaymentSettlementServiceImpl.Instance;
	}
	
	@Override
    public void submitJob() throws Exception {
		MCIBillPaymentCycleSettlementJob newJob = new MCIBillPaymentCycleSettlementJob();
    	newJob.setStatus(SwitchJobStatus.NOT_STARTED);
    	newJob.setGroup(SwitchJobGroup.CYCLESETTLEMENT);
    	newJob.setJobSchedule(this.getJobSchedule());
    	newJob.setClearingProfile(this.getClearingProfile());
    	newJob.setJobName("MCIBillPaymentCycleSettleJob_"+this.getClearingProfile().getId());
    	JobServiceQuartz.submit(newJob);
    }
	
	
	@Override
	public void execute(JobExecutionContext switchJobContext, JobLog log) {
		logger.debug("SCHEDULER Generating MCIBillPayment");
		if(!isJobFree()){
			logger.debug("Another thread is running... Exiting from MCIBillPayment");
			log.setStatus(SwitchJobStatus.FINISHED);
			log.setExceptionMessage("Job is not free");
			return;
		}
		
		try {
			ClearingProfile clearingProfile =null;
			DateTime now = null;
			DateTime accountUntilTime = null;
			DateTime settleUntilTime = null;

			ProcessContext.get().init();
			
			GeneralDao.Instance.beginTransaction();
			try{
				now = DateTime.now();
				DateTime prevFireTime = new DateTime(switchJobContext.getPreviousFireTime());
				DateTime curFireTime = new DateTime(switchJobContext.getFireTime());

				
				String query = "from "+ ClearingProfile.class.getName() +" cp where cp.settlementClass = :mciSettlement";
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("mciSettlement", MCIBillPaymentSettlementServiceImpl.class);
				clearingProfile = (ClearingProfile) GeneralDao.Instance.findObject(query, params);
				
//				Long id = (Long) switchJobContext.getJobDetail().getJobDataMap().get("ClrProfile");
//							
//				clearingProfile = ClearingService.findClearingProfile(id);
				accountUntilTime = clearingProfile.getAccountUntilTime(now);
				
				settleUntilTime = clearingProfile.getSettleUntilTime(now);
				
				GeneralDao.Instance.endTransaction();					
				
				if (prevFireTime.getDayDate().getDay() != curFireTime.getDayDate().getDay()
					|| prevFireTime.getDayDate().getMonth() != curFireTime.getDayDate().getMonth()
					|| prevFireTime.getDayDate().getYear() != curFireTime.getDayDate().getYear() ){

					accountUntilTime = settleUntilTime;
					logger.info("Different Date, previous: "+ prevFireTime +" current: "+ curFireTime);
				}
				
			}catch(Exception e){
				logger.error(e);
				log.setStatus(SwitchJobStatus.FAILED);
				log.setExceptionMessage(e.getMessage());
				GeneralDao.Instance.rollback();
				setJobFree();
//				GeneralDao.Instance.close();
				return;
			}
			
			MCIBillPaymentSettlementServiceImpl.Instance.settle(null, clearingProfile, accountUntilTime,settleUntilTime, false);
			logger.debug("SCHEDULER finished for MCIBillPayment");
			log.setStatus(SwitchJobStatus.FINISHED);
		} catch (Exception e) {
			logger.error("Exception in CycleSettlementJob!!! " + e);
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e.getMessage());
		}finally{
			setJobFree();
			GeneralDao.Instance.rollback();
//			GeneralDao.Instance.close();
		}
    }
	
	public synchronized boolean isJobFree(){
		if(isFree == true){
			isFree = false;
			return true;
		}
		return false;
	}
	
	public void setJobFree(){
		isFree = true;
	}
}
