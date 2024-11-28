package vaulsys.scheduler.job;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.SynchronizationService;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.message.MessageManager;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.EMVRqData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.scheduler.JobLog;
import vaulsys.scheduler.MCIVirtualVosoliJobInfo;
import vaulsys.scheduler.SchedulerService;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.Transaction;
import vaulsys.util.ConfigUtil;
import vaulsys.util.Util;
//import vaulsys.webservices.mcivirtualvosoli.common.MCIVosoliState;
//import vaulsys.webservices.mcivirtualvosoli.common.VirtualVosoliRqParameters;
import vaulsys.webservice.mcivirtualvosoli.common.MCIVosoliState;
import vaulsys.wfe.ProcessContext;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.exception.LockAcquisitionException;
import org.quartz.JobExecutionContext;

import com.ghasemkiani.util.icu.PersianDateFormat;

@Entity
@DiscriminatorValue(value = "MCI_Vosoli")
public class MCIVirtualVosoliJob extends AbstractSwitchJob{
	private static final Logger logger = Logger.getLogger(MCIVirtualVosoliJob.class);

	private static long MCIVOSOLI_COUNT_MAX;
	private static long MCIVOSOLI_TIMEOUT;
	private static long MCIVOSOLI_SLEEP_TIME;
	private static final int MAX_VIRTUALVOSOLI_MSG = 400;
	private static final int MAX_ITERATIONS_TO_DELETE_VIRTUALVOSOLI_JOBS = 100;
	private static final int MAX_ROWS_TO_DELETE_FROM_VIRTUALVOSOLI_JOBS = 2;
	
	private static final int THREAD_NUM = 4;
	private static final int JOB_NUM_IN_THREAD = 25;
	
	private static boolean isFree = true;
	
	@Override
	public void execute(JobExecutionContext switchJobContext, JobLog log) {
		MCIVOSOLI_COUNT_MAX = ConfigUtil.getLong(ConfigUtil.MCIVOSOLI_COUNT);
		MCIVOSOLI_TIMEOUT = ConfigUtil.getLong(ConfigUtil.MCIVOSOLI_TIMEOUT);
		MCIVOSOLI_SLEEP_TIME = ConfigUtil.getLong(ConfigUtil.MCIVOSOLI_SLEEP_TIME);
		
		logger.debug("Starting MCIVirtualVosoli Job");
		
		if(!isJobFree()){
			logger.error("Another thread is running... Exiting from MCIVirtualVosoliJob");
			log.setStatus(SwitchJobStatus.FINISHED);
			log.setExceptionMessage("Job is not free");
			return;
		}
		
		ProcessContext.get().init();
		int numDeleted;
		
		/*************************************************************************************/
		GeneralDao.Instance.beginTransaction();
		for(int i=0; i<MAX_ITERATIONS_TO_DELETE_VIRTUALVOSOLI_JOBS; i++){
			logger.debug("MCIVirtualVosoliJob,Iteration number: "+ i);
			numDeleted = GeneralDao.Instance.executeSqlUpdate("delete from " + ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)
					+ ".job_info where job='MCI_VirtualVosoli' and deleted=1 and rownum<=" + MAX_ROWS_TO_DELETE_FROM_VIRTUALVOSOLI_JOBS);
			if(numDeleted < MAX_ROWS_TO_DELETE_FROM_VIRTUALVOSOLI_JOBS)
				break;
		}
		GeneralDao.Instance.endTransaction();
		
		/*************************************************************************************/
		GeneralDao.Instance.beginTransaction();
		try{
			// devided by two just to insure that there are enough space for TimeOut Msgs
			int maxScheduleMsg = (MessageManager.getInstance().getMaxPossibleScheduleJobs() / 10);
						
			List<MCIVirtualVosoliJobInfo> mcivosoliJobInfos = SchedulerService.getToBeFiredJobInfo(MCIVirtualVosoliJobInfo.class);
			List<MCIVirtualVosoliJobInfo> deletingJobInfos = new ArrayList<MCIVirtualVosoliJobInfo>();
			logger.debug("Num messages to MCIVirtualVosli: " + mcivosoliJobInfos.size());

			int count = MessageManager.getInstance().getCurrentScheduledThreadQueueSize();
			long timeDelay = 0L;
			if(switchJobContext.getNextFireTime() != null)
				timeDelay = (switchJobContext.getNextFireTime().getTime() - System.currentTimeMillis()) / 2;

			logger.debug("maxScheduleMsg: " + maxScheduleMsg + " ,count:" + count);
			
//			String bankId = String.valueOf(ProcessContext.get().getBank(ProcessContext.get().getMyInstitution().getBin().intValue()).getTwoDigitCode());
			String bankId = ConfigUtil.getProperty(ConfigUtil.MCI_VOSOLI_BANKID);
        	String branch = ProcessContext.get().getMyInstitution().getBranchCardCode();
        	
        	List<MCIVirtualVosoliJobInfo> shouldBeSend = new ArrayList<MCIVirtualVosoliJobInfo>();
        	
			for (MCIVirtualVosoliJobInfo jobInfo : mcivosoliJobInfos) {
				try{
					if (count >= maxScheduleMsg) {
						logger.debug("maxScheduleMsg reached, breaking loop...");
						break;
					}

					if (count * MCIVOSOLI_SLEEP_TIME > timeDelay) {
						logger.debug("maxScheduleMsg reached, breaking loop (timeDelay)...");
						break;
					}
					try{
						GeneralDao.Instance.refresh(jobInfo);
						GeneralDao.Instance.synchObject(jobInfo, LockMode.UPGRADE_NOWAIT);
						if (Boolean.TRUE.equals(jobInfo.getDeleted())) {
							logger.info("mciVirtualVosoli for trx: " + jobInfo.getTransaction().getId() + " is ignored, deleted is TRUE!"); 
							continue;
						}
					}catch(Exception e){
						logger.error(e,e);
						continue;
					}
					logger.debug("try to MCIVirtualVosoli (id): " + jobInfo.getId() + ") on trx(" + jobInfo.getTransaction().getId() + ")");
					if(jobInfo.getCount() == 0){
						deletingJobInfos.add(jobInfo);
						continue;
					}
					//if trx is reversed don't send to vosoli web service
					if(ClearingState.DISAGREEMENT.equals(jobInfo.getTransaction().getSourceClearingInfo().getClearingState())){
						deletingJobInfos.add(jobInfo);
						continue;
					}
					
					if(shouldBeSend.size() < THREAD_NUM * JOB_NUM_IN_THREAD) {
						jobInfo.setCount(jobInfo.getCount() - 1);
						GeneralDao.Instance.saveOrUpdate(jobInfo);

                        PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMdd");
                        Ifx ifx = jobInfo.getTransaction().getOutgoingIfx();
//                        VirtualVosoliRqParameters params = new VirtualVosoliRqParameters(ifx.getId(), bankId, ifx.getBillID(), branch, ifx.getTerminalType().toString(),
//                                dateFormatPers.format(ifx.getOrigDt().getDayDate().toDate()), ifx.getBillPaymentID(),
//                                ifx.getSrc_TrnSeqCntr(), dateFormatPers.format(DateTime.now().getDayDate().toDate()));
//                        jobInfo.setVirtualVosoliRqParameters(params);

                        shouldBeSend.add(jobInfo);
					}else 
						break;
					
//					String[] response = getResponseFromVirtualVosoli(jobInfo, bankId, branch);
//					parseResponse(response, jobInfo);
					
				}catch(LockAcquisitionException e) {
					logger.warn("MCIVirtualVosoli job ("+jobInfo.getId()+") on trx("+jobInfo.getTransaction().getId()+" was put back to the next round!", e);
//					GeneralDao.Instance.saveOrUpdate(jobInfo);
//					continue;
				}
				
			}
            GeneralDao.Instance.endTransaction();
            GeneralDao.Instance.beginTransaction();

			/************* Send all MCIVosoli on one socket in different thread **************/
			List<MCIVirtualVosoliJobInfo>  partialShouldBeSend;
			
			int numOfJobInThread = 0;
			int numOfThread = 0;

            Semaphore semaphore = new Semaphore(THREAD_NUM);
            for(int i = 0; i < THREAD_NUM; i++){
				
				if(shouldBeSend.size() < 1)
					break;
				
				partialShouldBeSend = new ArrayList<MCIVirtualVosoliJobInfo>();
				
				for(int j = 0; j < JOB_NUM_IN_THREAD; j++){
					
					if(shouldBeSend.size() > 0)
						partialShouldBeSend.add(shouldBeSend.remove(0));
						
				}
				
				if(partialShouldBeSend.size() > 0){
					MCIVirtualVosoliThread vosoliThread = new MCIVirtualVosoliThread(partialShouldBeSend, branch, bankId, semaphore);
					Thread thread = new Thread(vosoliThread);
                    semaphore.acquire();
                    thread.start();
				}
				
			}
            logger.info("MCIVirtualVosoliJob: Wait for all threads to finish...");
            semaphore.acquire(THREAD_NUM);

			/*********************************************************************************/
			
			if (!deletingJobInfos.isEmpty()) {
				
				List<MCIVirtualVosoliJobInfo> jobForQuery = new ArrayList<MCIVirtualVosoliJobInfo>();
				int counter = 0;
				
				for (int i = 0; i < deletingJobInfos.size(); i++) {
					jobForQuery.add(deletingJobInfos.get(i));
					counter++;
					if(counter == 500 || i == deletingJobInfos.size() - 1) {
						String query = "delete from MCIVirtualVosoliJobInfo j where j in (:ids)";
				        Map<String, Object> params = new HashMap<String, Object>(1);
				        params.put("ids", jobForQuery);
						int numAffected = GeneralDao.Instance.executeUpdate(query, params);
						
						logger.debug("Num affected jobInfo with batch update of delete: "+numAffected);
						
						counter = 0;
						jobForQuery = new ArrayList<MCIVirtualVosoliJobInfo>();
					}
				}
				/*String query = "delete from MCIVirtualVosoliJobInfo j where j in (:ids)";
		        Map<String, Object> params = new HashMap<String, Object>(1);
		        params.put("ids", deletingJobInfos);
				GeneralDao.Instance.executeUpdate(query, params);
				deletingJobInfos.clear();*/
			}
			log.setStatus(SwitchJobStatus.FINISHED);
		} catch(Exception e){
			logger.error(e);
			log.setStatus(SwitchJobStatus.FAILED);
			log.setExceptionMessage(e.getMessage());
		} finally {
			setJobFree();
			GeneralDao.Instance.endTransaction();
//			if (socket != null) {
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    logger.warn(e);
//                }
//            }
			
		}
	}
	
	public static Socket getSocket() throws Exception {
//      logger.debug("Trying get TssmSocket to: " +  + ":" + port);
      try {
          Socket socket = new Socket();
          socket.connect(new InetSocketAddress(ConfigUtil.getProperty(ConfigUtil.WIN_SREVER_IP), ConfigUtil.getInteger(ConfigUtil.WIN_SREVER_PORT)));
          /*, SSMInterface.TSSM_CONNECT_TIMEOUT*/
          socket.setSoTimeout(ConfigUtil.getInteger(ConfigUtil.MCIVOSOLI_TIMEOUT));
          /*SSMInterface.TSSM_DATA_TIMEOUT*/
          return socket;
      } catch (Throwable e) {
          logger.error(e, e);
          if (e instanceof ConnectException || e instanceof SocketTimeoutException) {
              throw new SocketTimeoutException(/*BusinessException.CONNECTING_EXCEPTION */  " SocketTimeoutException on: " /*+ ipAddress*/);
          }
          throw new Exception(e);
      }
	}
	
	private static String[] getResponseFromVirtualVosoli(MCIVirtualVosoliJobInfo jobInfo, String bankId, String branch){
		PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMdd");
		ObjectOutputStream oos;
        ObjectInputStream ois;
        Socket socket = null;
        Long trxId = jobInfo.getTransaction().getId();
		try{
			logger.debug("Try to get socket to win server for trx: " + trxId);
			socket = getSocket();
    		//request
			Ifx ifx = jobInfo.getTransaction().getOutgoingIfx();
//			VirtualVosoliRqParameters params = new VirtualVosoliRqParameters(ifx.getId(), bankId, ifx.getBillID(), branch, ifx.getTerminalType().toString(),
//        			dateFormatPers.format(ifx.getOrigDt().getDayDate().toDate()), ifx.getBillPaymentID(),
//        			ifx.getSrc_TrnSeqCntr(), dateFormatPers.format(DateTime.now().getDayDate().toDate()));
//			oos = new ObjectOutputStream(socket.getOutputStream());
//            oos.writeObject(params);
//            logger.debug("sent to mciVirtualVosoli: " + getRqString(params));
            //response
            ois = new ObjectInputStream(socket.getInputStream());
            String[] result = (String[]) ois.readObject();
//            result[0] = "-2";
            return result;
		}catch(Exception e){
			logger.error("Exception in geting response from MCIVirtualVosoli for trx: " + trxId);
			logger.error(e);
		}finally {
			if (socket != null) {
                try {
                    socket.close();
                    logger.debug("socket closed for trx: " + trxId);
                } catch (IOException e) {
                    logger.warn(e);
                }
            }
		}
		return null;
	}
	
	
	public static void parseResponse(String[] response, MCIVirtualVosoliJobInfo jobInfo/*, List<MCIVirtualVosoliJobInfo> deletingJobInfos*/){
		String code;
		String description = ""; 
		try{
            GeneralDao.Instance.refresh(jobInfo);
//			MCIVosoliState billState = null;
			
			EMVRqData emvrqData_rs = jobInfo.getTransaction().getIncomingIfx().getEMVRqData();
			EMVRqData emvrqData_rq = null;
			Transaction rsTrx = jobInfo.getTransaction()/*.getFirstTransaction()*/;
			rsTrx = GeneralDao.Instance.load(Transaction.class, rsTrx.getId());
			Transaction firstTrx = rsTrx.getFirstTransaction(); 
			if(firstTrx != null)
				emvrqData_rq = firstTrx.getIncomingIfx().getEMVRqData();
			else
				logger.debug("first Trx of transaction " + jobInfo.getTransaction().getId() + " is null so mcivosolistate is not save for rq trx");
			if(response == null || response[0] == null || !Util.hasText(response[0])){
//				billState = MCIVosoliState.NO_ANSWER;
				logger.debug("received from mciVirtualVosoli for trx: " + jobInfo.getTransaction() + " is null");
//				jobInfo.setBillState(MCIVosoliState.NO_ANSWER);
			}else {
				code = response[0];
				description = response[1]; 
//				billState = mapRsType(code);
//				billState = MCIVosoliState.INVALID_BILLID_CHECKDIGIT;
				logger.debug("received from mciVirtualVosoli: " + getRsString(response, jobInfo));
//				jobInfo.setBillState(billState);
//				if(!shouldBeRepeated(billState)) {
//					jobInfo.setDeleted(true);
////					deletingJobInfos.add(jobInfo);
//				}
			}
//			emvrqData_rq.getBillPaymentData().setMciVosoliState(billState);
//			emvrqData_rs.getBillPaymentData().setMciVosoliState(billState);
			emvrqData_rq.getBillPaymentData().setMciVosoliDesc(description);
			emvrqData_rs.getBillPaymentData().setMciVosoliDesc(description);
			GeneralDao.Instance.saveOrUpdate(emvrqData_rq);
			GeneralDao.Instance.saveOrUpdate(emvrqData_rs);
			GeneralDao.Instance.saveOrUpdate(jobInfo);
			
		}catch(Exception e){
			logger.error(e, e);
		}
	}
	
	private static Boolean shouldBeRepeated(MCIVosoliState state){
		if(state == null || MCIVosoliState.SYSTEM_ERROR.equals(state) || MCIVosoliState.NOT_SEND.equals(state))
			return true;
		else
			return false;
	}
	
	private static MCIVosoliState mapRsType(String code){
		switch (Integer.parseInt(code)) {
		
		case 0 : 
			return MCIVosoliState.SUCCESS; 
		case 8 : 
			return MCIVosoliState.INVALID_BANK_CODE;
		case 9 : 
			return MCIVosoliState.INVALID_SEND_DATE;
		case 21 :
			return MCIVosoliState.INVALID_BRANCH_CODE;
		case 22 :
			return MCIVosoliState.INVALID_BILLID_LENGTH;
		case 23 :
			return MCIVosoliState.INVALID_PAY_DATE;
		case 24 :
			return MCIVosoliState.INVALID_CHANNEL_TYPE;
		case 25 :
			return MCIVosoliState.INVALID_COMPANY_CODE;
		case 26 :
			return MCIVosoliState.ZERO_STARTED_NUM;
		case 27 :
			return MCIVosoliState.INVALID_TERM_CODE;
		case 28 :
			return MCIVosoliState.INVALID_BILLID_CHECKDIGIT;
		case 29 :
			return MCIVosoliState.INVALID_BILLPAYMENT_CHECKDIGIT_FIRST;
		case 30 :
			return MCIVosoliState.INVALID_BILLPATMENT_CHECKdIGIT_SECOND;
		case 33 :
			return MCIVosoliState.INVALID_BILLPAYMENT_VOSOLIDIGIT_TYPE;
		case -1 :
			return MCIVosoliState.REPEATED_RECORD;
		case -2 :
			return MCIVosoliState.SYSTEM_ERROR;

		default:
			return null;
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
		MCIVirtualVosoliJob newJob = new MCIVirtualVosoliJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.GENERAL);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setJobName("MCIVirtualVosoliJob");
        JobServiceQuartz.submit(newJob);
	}
	
	
	
//	public static String getRqString(VirtualVosoliRqParameters params){
//		return  "\r\n" + "ifxId: " + params.ifx_id + "\r\n "
//				+ "bank: " + params.p_bank + "\r\n"
//				+ "billId: " + params.p_bill_id + "\r\n"
//				+ "branchoCode: " + params.p_branch + "\r\n"
//				+ "terminalType: " + params.p_channel_type + "\r\n"
//				+ "trxDate: " + params.p_pay_date + "\r\n"
//				+ "paymentId: " + params.p_payment_id + "\r\n"
//				+ "srcTrnSeqCntr: " + params.p_ref_code + "\r\n"
//				+ "sendDate: " + params.p_send_date;
//	}
	
	public static String getRsString(String[] response, MCIVirtualVosoliJobInfo jobInfo) {
		return "\r\n" + "Trx: " + jobInfo.getTransaction().getId() + "\r\n"
				+ "rcCode: " + response[0] + "\r\n"
				+ "message: " + response[1];
	}
}
