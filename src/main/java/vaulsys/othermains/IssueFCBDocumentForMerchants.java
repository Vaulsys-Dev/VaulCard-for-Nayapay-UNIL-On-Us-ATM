package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.job.SwitchJobStatus;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.JobLog;
import vaulsys.scheduler.SchedulerService;
import vaulsys.scheduler.job.IssuingFCBDocumentJob;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class IssueFCBDocumentForMerchants {
	private static final Logger logger = Logger.getLogger(IssueFCBDocumentForMerchants.class);

	public static void main(String[] args) {
    	boolean doLog = true;
    	JobLog log = new JobLog();
    	if(doLog) {
	    	log.setName(IssueFCBDocumentForMerchants.class.getSimpleName());
	    	log.setStatus(SwitchJobStatus.RUNNING);
	    	log.setStartTime(DateTime.now());

	    	GeneralDao.Instance.beginTransaction();
			GlobalContext.getInstance().startup();
	    	ProcessContext.get().init();
	    	
	    	GeneralDao.Instance.saveOrUpdate(log);
	    	SchedulerService.getFCBDocumentsJobInfoOfStlReport(381646402L);
	    	GeneralDao.Instance.endTransaction();
    	}

//    	GeneralDao.Instance.beginTransaction();
		
    	
		IssuingFCBDocumentJob job = new IssuingFCBDocumentJob();
		job.execute(null, log);

		
//		GeneralDao.Instance.beginTransaction();
//
//		try {
//			List<IssuingFCBDocumentJobInfo> documentJobInfos = SchedulerService.getFCBDocumentsJobInfoOfClrProf(170601L);
//			logger.debug("Num reports to be processed: " + documentJobInfos.size());
//
//			GeneralDao.Instance.endTransaction();
//
//			int count = 0;
//			
//			for (IssuingFCBDocumentJobInfo documentJobInfo : documentJobInfos) {				
//				logger.debug("try to process job (id): " + documentJobInfo.getId() + ") on report("
//						+ documentJobInfo.getReport().getId() + ")");
//				
//				
//				try {
//					GeneralDao.Instance.beginTransaction();
//					GeneralDao.Instance.refresh(documentJobInfo);
//					process(documentJobInfo);
//					String query = "delete from IssuingFCBDocumentJobInfo j where j = :id";
//					Map<String, Object> params = new HashMap<String, Object>(1);
//					params.put("id", documentJobInfo);
//					GeneralDao.Instance.executeUpdate(query, params);
//				} catch (Exception e) {
//					logger.error("Settlement Report "+documentJobInfo.getReport().getId()+"- "+e.getClass().getSimpleName()+": "+ e.getMessage());
//				}finally{
//					GeneralDao.Instance.endTransaction();
//				}
//			}
//			
//			GeneralDao.Instance.beginTransaction();			
//		} catch (Exception e) {
//			logger.warn(e,e);
//		} finally {
//			GeneralDao.Instance.endTransaction();	
//		}
//
//		logger.debug("Ending Issuing FCB Document Job");
	}
}
