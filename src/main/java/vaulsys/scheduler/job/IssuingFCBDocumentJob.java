package vaulsys.scheduler.job;

import vaulsys.clearing.AccountingService;
import vaulsys.clearing.base.SettlementData;
import vaulsys.clearing.base.SettlementReport;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.IssuingDocumentAction;
import vaulsys.scheduler.IssuingFCBDocumentJobInfo;
import vaulsys.scheduler.JobLog;
import vaulsys.scheduler.SchedulerService;
import vaulsys.transaction.SettledState;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.quartz.JobExecutionContext;

import com.fanap.cms.exception.BusinessException;

@Entity
@DiscriminatorValue(value = "IssuingFCBDocument")
public class IssuingFCBDocumentJob extends AbstractSwitchJob {

	private static final Logger logger = Logger.getLogger(IssuingFCBDocumentJob.class);

	private static boolean isFree = true;
	
	public void execute(JobExecutionContext switchJobContext, JobLog log) {
		logger.debug("Starting issuing FCB Document Job");

		int sizeOld = 0;
		int sizeNew = -1;
		int threadCount = 16;
		
		if(!isJobFree()){
			logger.debug("Another thread is running... Exiting from IssuingFCBDocumentJob");
			log.setStatus(SwitchJobStatus.FINISHED);
			log.setExceptionMessage("Job is not free");
			return;
		}
		
		ProcessContext.get().init();
		

		GeneralDao.Instance.beginTransaction();

		Thread[] issueThreads = null;
		
		try {
			while(sizeNew != sizeOld){
				List<IssuingFCBDocumentJobInfo> documentJobInfos = SchedulerService.getToBeFiredJobInfo(IssuingFCBDocumentJobInfo.class);
				logger.debug("Num reports to be processed: " + documentJobInfos.size());
	
				GeneralDao.Instance.endTransaction();
				
				sizeOld = documentJobInfos.size();
				
				if(documentJobInfos.size() > 400) {
					issueThreads = new Thread[threadCount];
					int subListSize = (int) Math.ceil(documentJobInfos.size()/threadCount);
					for(int i=0; i<issueThreads.length; i++){
						IssuingFCBDocumentJobThread issueFCBThread = new IssuingFCBDocumentJobThread(documentJobInfos.subList(i*subListSize, Math.min((i+1)*subListSize, documentJobInfos.size())));
						Thread issueThread = new Thread(issueFCBThread);
						issueThreads[i] = issueThread;
						logger.debug("Thread: " + issueThread.getName() + " is starting...");
						issueThread.start();
					}
					
					for(int i=0; i<issueThreads.length; i++){
						if(issueThreads != null){
							issueThreads[i].join();
							logger.debug("issueThread: "+i+"["+issueThreads[i].getName()+"] joined");
						}
					}
				}else{
					processIssueDocumentJobs(documentJobInfos);
				}
				
				GeneralDao.Instance.beginTransaction();			
				documentJobInfos = SchedulerService.getToBeFiredJobInfo(IssuingFCBDocumentJobInfo.class);
				logger.debug("Num reports after processing: " + documentJobInfos.size());
				sizeNew = documentJobInfos.size();
			}
//			GeneralDao.Instance.beginTransaction();			
			log.setStatus(SwitchJobStatus.FINISHED);
		} catch (Exception e) {
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e.getMessage());
			logger.warn(e,e);
		} finally {
			setJobFree();
			GeneralDao.Instance.endTransaction();	
		}

		logger.debug("Ending Issuing FCB Document Job");
	}

	public static void processIssueDocumentJobs(List<IssuingFCBDocumentJobInfo> documentJobInfos) {
		for (IssuingFCBDocumentJobInfo documentJobInfo : documentJobInfos) {				
			try {
				GeneralDao.Instance.beginTransaction();
				GeneralDao.Instance.refresh(documentJobInfo);
				logger.debug("try to process job (id): " + documentJobInfo.getId() + ") on report("
						+ documentJobInfo.getReport().getId() + ")");
				process(documentJobInfo);
				String query = "delete from IssuingFCBDocumentJobInfo j where j = :id";
				Map<String, Object> params = new HashMap<String, Object>(1);
				params.put("id", documentJobInfo);
				GeneralDao.Instance.executeUpdate(query, params);
			} catch (Exception e) {
				logger.warn("Settlement Report "+documentJobInfo.getReport().getId()+"- "+e.getClass().getSimpleName()+": "+ e.getMessage());
			}finally{
				GeneralDao.Instance.endTransaction();
			}
		}
	}

	public static void process(IssuingFCBDocumentJobInfo documentJobInfo) throws Exception {
		SettlementReport settlementReport = documentJobInfo.getReport();
//		SettlementState settlementState = settlementReport.getSettlementState();

		if (IssuingDocumentAction.REISSUE.equals(documentJobInfo.getAction())){

			List<SettlementData> settlementDatas = AccountingService.findSettlementData(settlementReport);

			for (SettlementData settlementData: settlementDatas) {
				
				try {
					logger.debug("Try to lock settlementData " + settlementData.getId());
					settlementData = GeneralDao.Instance.load(SettlementData.class, settlementData.getId());
					settlementData = (SettlementData) GeneralDao.Instance.synchObject(settlementData, LockMode.UPGRADE_NOWAIT);
					logger.debug("settlementData locked.... " + settlementData.getId());
				} catch (Exception e) {
					logger.error("Error in locking settlementData ("+settlementData.getId()+")", e);
					throw new Exception("Error in locking settlementData ("+settlementData.getId()+")", e);
				}
				
			}
			
			String transactionId = AccountingService.issueFCBDocument(settlementReport, false);
			
			if (transactionId!=null){				
				settlementReport.setDocumentNumber(transactionId);
				GeneralDao.Instance.saveOrUpdate(settlementReport);
				
				int updateSettlementData = AccountingService.updateSettlementData(settlementDatas, transactionId);
				logger.debug(updateSettlementData + " settlementData are settled in document-" + transactionId);
				int updateSettlementInfo = AccountingService.updateSettlementInfo(settlementDatas, SettledState.SETTLED);
				logger.debug(updateSettlementInfo + " settlementInfo are settled in document-" + transactionId);
				logger.debug("Settlement Report "+documentJobInfo.getReport().getId()+" is issued");

//				if (settlementState != null) {
//					if (AccountingService.isAllSettlementDataSettled(settlementState)) {
//						settlementState.setState(SettlementStateType.AUTOSETTLED);
////						settlementState.setSettlingUser(GlobalContext.getInstance().getSwitchUser());
//						settlementState.setSettlingUser(ProcessContext.get().getSwitchUser());
//						settlementState.setSettlementDate(DateTime.now());
//					}
//				}
			}else{
				throw new BusinessException("FCB Document couldn't be issued!");
			}
			
		}else if (IssuingDocumentAction.RETURN.equals(documentJobInfo.getAction())){
			if (!AccountingService.fullyReverseFCBDocument(settlementReport.getReferenceDocumentNumber()))
				throw new BusinessException("FCB Document couldn't be returned!");
			logger.debug("Settlement Report "+documentJobInfo.getReport().getId()+"is returned");
		}
	}
	
	public void interrupt() {
	}

	public void updateExecutionInfo() {
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
	
	@Override
	public void submitJob() throws Exception {
		IssuingFCBDocumentJob newJob = new IssuingFCBDocumentJob();
		newJob.setStatus(SwitchJobStatus.NOT_STARTED);
		newJob.setGroup(SwitchJobGroup.ISSUINGDOCUMNET);
		newJob.setJobSchedule(this.getJobSchedule());
		newJob.setJobName("IssuingFCBDocumentJob");
		JobServiceQuartz.submit(newJob);
	}
}