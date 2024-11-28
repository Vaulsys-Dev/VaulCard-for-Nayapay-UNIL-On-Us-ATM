package vaulsys.scheduler.job;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.SynchronizationService;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.message.MessageManager;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.BillPaymentData;
import vaulsys.protocols.ifx.imp.EMVRqData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.scheduler.JobInfo;
import vaulsys.scheduler.JobLog;
import vaulsys.scheduler.NAJAVosoliJobInfo;
import vaulsys.scheduler.SchedulerService;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.LifeCycleStatus;
import vaulsys.transaction.Transaction;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Util;
//import vaulsys.webservices.naja.NAJAServiceHandler;
import vaulsys.wfe.ProcessContext;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.exception.LockAcquisitionException;
import org.quartz.JobExecutionContext;

import com.ghasemkiani.util.icu.PersianDateFormat;

@Entity
@DiscriminatorValue(value = "NAJA_Vosoli")
public class NAJAVosoliJob extends AbstractSwitchJob{
	private static final Logger logger = Logger.getLogger(NAJAVosoliJob.class);

	private static long VOSOLI_COUNT_MAX;
	private static long VOSOLI_TIMEOUT;
	private static long VOSOLI_SLEEP_TIME;
	private static final int MAX_VIRTUALVOSOLI_MSG = 400;
	private static final int MAX_ITERATIONS_TO_DELETE_VIRTUALVOSOLI_JOBS = 100;
	private static final int MAX_ROWS_TO_DELETE_FROM_VIRTUALVOSOLI_JOBS = 2;
	
	private static boolean isFree = true;
	
	private static final String JobTittle="NAJAVosoliJob";
	private static final String JobDiscriminator="NAJA_Vosoli";
	
	@Override
	public void execute(JobExecutionContext switchJobContext, JobLog log) {
		VOSOLI_COUNT_MAX = ConfigUtil.getLong(ConfigUtil.NAJAVOSOLI_COUNT);
		VOSOLI_TIMEOUT = ConfigUtil.getLong(ConfigUtil.NAJAVOSOLI_TIMEOUT);
		VOSOLI_SLEEP_TIME = ConfigUtil.getLong(ConfigUtil.NAJAVOSOLI_SLEEP_TIME);
		
		logger.debug("Starting "+NAJAVosoliJob.JobTittle+" Job");
				
		if(!isJobFree()){
			logger.error("Another thread is running... Exiting from "+NAJAVosoliJob.JobTittle);
			log.setStatus(SwitchJobStatus.FINISHED);
			log.setExceptionMessage("Job is not free");
			return;
		}
		
		ProcessContext.get().init();
		int numDeleted;		
		
		GeneralDao.Instance.beginTransaction();
		for(int i=0; i<MAX_ITERATIONS_TO_DELETE_VIRTUALVOSOLI_JOBS; i++){
			logger.debug(NAJAVosoliJob.JobTittle+ ",Iteration number: "+ i);
			numDeleted = GeneralDao.Instance.executeSqlUpdate("delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)
					+ ".job_info where job='"+NAJAVosoliJob.JobDiscriminator+"' and deleted=1 and rownum<=" + MAX_ROWS_TO_DELETE_FROM_VIRTUALVOSOLI_JOBS);
			if(numDeleted < MAX_ROWS_TO_DELETE_FROM_VIRTUALVOSOLI_JOBS)
				break;
		}
		GeneralDao.Instance.endTransaction();
		
		GeneralDao.Instance.beginTransaction();
		try{
			int maxScheduleMsg = (MessageManager.getInstance().getMaxPossibleScheduleJobs() / 10);
						
			List<NAJAVosoliJobInfo> najaJobInfos = SchedulerService.getToBeFiredJobInfo(NAJAVosoliJobInfo.class);
			List<NAJAVosoliJobInfo> deletingJobInfos = new ArrayList<NAJAVosoliJobInfo>();
			logger.debug("Num messages to "+NAJAVosoliJob.JobTittle+" : " + najaJobInfos.size());

			int count = MessageManager.getInstance().getCurrentScheduledThreadQueueSize();
			long timeDelay = 0L;
			if(switchJobContext.getNextFireTime() != null)
				timeDelay = (switchJobContext.getNextFireTime().getTime() - System.currentTimeMillis()) / 2;

			logger.debug("maxScheduleMsg: " + maxScheduleMsg + " ,count:" + count);
			
			String bankId = ConfigUtil.getProperty(ConfigUtil.NAJAVOSOLI_BANKID);
        	String branch = ProcessContext.get().getMyInstitution().getBranchCardCode();
        	
			for (NAJAVosoliJobInfo jobInfo : najaJobInfos) {
				try{
					if (count >= maxScheduleMsg) {
						logger.debug("maxScheduleMsg reached, breaking loop...");
						break;
					}

					if (count * VOSOLI_SLEEP_TIME > timeDelay) {
						logger.debug("maxScheduleMsg reached, breaking loop (timeDelay)...");
						break;
					}
					try{
						GeneralDao.Instance.refresh(jobInfo);
						GeneralDao.Instance.synchObject(jobInfo, LockMode.UPGRADE_NOWAIT);
						if (Boolean.TRUE.equals(jobInfo.getDeleted())) {
							logger.info("NAJAVosoli for trx: " + jobInfo.getTransaction().getId() + " is ignored, deleted is TRUE!"); 
							continue;
						}
					}catch(Exception e){
						logger.error(e,e);
						continue;
					}
					if(count >= MAX_VIRTUALVOSOLI_MSG) {
						if (!deletingJobInfos.isEmpty()) {
							String query = "delete from NAJAVosoliJobInfo j where j in (:ids)";
					        Map<String, Object> params = new HashMap<String, Object>(1);
					        params.put("ids", deletingJobInfos);
							GeneralDao.Instance.executeUpdate(query, params);
							deletingJobInfos.clear();
						}
					}
					logger.debug("try to NAJAVosoli (id): " + jobInfo.getId() + ") on trx(" + jobInfo.getTransaction().getId() + ")");
					//majid_prgtemp
					if(jobInfo.getCount() == 0){
						deletingJobInfos.add(jobInfo);
						continue;
					}
					if(ClearingState.DISAGREEMENT.equals(jobInfo.getTransaction().getSourceClearingInfo().getClearingState())){
						deletingJobInfos.add(jobInfo);
						continue;
					}
					jobInfo.setCount(jobInfo.getCount() - 1);
					GeneralDao.Instance.saveOrUpdate(jobInfo);
					//majidprginjam13
					if(jobInfo.getTransaction().getLifeCycle().getIsFullyReveresed()==null || jobInfo.getTransaction().getLifeCycle().getIsFullyReveresed().equals(LifeCycleStatus.NOTHING))
					{
						int response = getResponseFromService(jobInfo, bankId, branch);
						parseResponse(response, jobInfo);
					}else{
						deletingJobInfos.add(jobInfo);
						continue;					
					}
					
				}catch(LockAcquisitionException e) {
					logger.warn("NAJAVosoli job ("+jobInfo.getId()+") on trx("+jobInfo.getTransaction().getId()+" was put back to the next round!", e);
//					GeneralDao.Instance.saveOrUpdate(jobInfo);
//					continue;
				}
				
				GeneralDao.Instance.endTransaction();
				GeneralDao.Instance.beginTransaction();
			}
			
			if (!deletingJobInfos.isEmpty()) {
				
				List<NAJAVosoliJobInfo> jobForQuery = new ArrayList<NAJAVosoliJobInfo>();
				int counter = 0;
				
				for (int i = 0; i < deletingJobInfos.size(); i++) {
					jobForQuery.add(deletingJobInfos.get(i));
					counter++;
					if(counter == 500 || i == deletingJobInfos.size() - 1) {
						String query = "delete from NAJAVosoliJobInfo j where j in (:ids)";
				        Map<String, Object> params = new HashMap<String, Object>(1);
				        params.put("ids", jobForQuery);
						int numAffected = GeneralDao.Instance.executeUpdate(query, params);
						
						logger.debug("Num affected jobInfo with batch update of delete: "+numAffected);
						
						counter = 0;
						jobForQuery = new ArrayList<NAJAVosoliJobInfo>();
					}
				}
			}
			log.setStatus(SwitchJobStatus.FINISHED);
		} catch(Exception e){
			logger.error(e);
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e.getMessage());
		} finally {
			setJobFree();
			GeneralDao.Instance.endTransaction();
			
		}
	}
	
	private static int getResponseFromService(NAJAVosoliJobInfo jobInfo, String bankId, String branch){
		
		Long trxId = jobInfo.getTransaction().getId();
		try{
			logger.debug("Try to call naja service for trx: " + trxId);
		
//			NAJAServiceHandler najaService=new NAJAServiceHandler();
			Ifx ifx = jobInfo.getTransaction().getOutgoingIfx();
			logger.debug("call naja service");
			//ifx.getAmountPath()
			PersianDateFormat pdf = new PersianDateFormat("yyyy/MM/dd HH:mm");
	        Calendar cNow = Calendar.getInstance();
	        String strPaymentDate=pdf.format(ifx.getReceivedDt());
	        	        
			int errorCode=0; //najaService.Execute(ifx.getBillID(), ifx.getBillPaymentID(),branch,ifx.getTerminalType(),strPaymentDate, ifx.getSrc_TrnSeqCntr(),strPaymentDate,ifx.getReal_Amt().toString());
			return errorCode;
		}catch(Exception e){
			logger.error("Exception in geting response from NAJAVosoli for trx: " + trxId +" ERROR : "+ e.getMessage());
			logger.error(e);
		}finally {
			
		}	
		return -5;
	}
	
	private static void parseResponse(int response, NAJAVosoliJobInfo jobInfo){
		try{
			
			EMVRqData emvrqData_rs = jobInfo.getTransaction().getIncomingIfx().getEMVRqData();
			EMVRqData emvrqData_rq = null;
			Transaction firstTrx = jobInfo.getTransaction().getFirstTransaction();
			if(firstTrx != null)
				emvrqData_rq = firstTrx.getIncomingIfx().getEMVRqData();
			else
				logger.debug("first Trx of transaction " + jobInfo.getTransaction().getId() + " is null so najavosolistate is not save for rq trx");
			
			if(response>=0)
				jobInfo.setDeleted(true);
			
			emvrqData_rq.getSafeBillPaymentData().setVosoliState(response);
			emvrqData_rs.getSafeBillPaymentData().setVosoliState(response);
			GeneralDao.Instance.saveOrUpdate(emvrqData_rq);
			GeneralDao.Instance.saveOrUpdate(emvrqData_rs);
			GeneralDao.Instance.saveOrUpdate(jobInfo);
			
		}catch(Exception e){
			logger.error(e);
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
	
	@Override
	public void updateExecutionInfo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void submitJob() throws Exception {
		NAJAVosoliJob newJob = new NAJAVosoliJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.GENERAL);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setJobName("NAJAVosoliJob");
        JobServiceQuartz.submit(newJob);
	}

}