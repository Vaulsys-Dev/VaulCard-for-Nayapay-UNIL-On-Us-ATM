package vaulsys.scheduler.job;

import vaulsys.clearing.report.ShetabReconciliationService;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.JobLog;
import vaulsys.scheduler.SchedulerService;
import vaulsys.scheduler.TransferSorushTableJobInfo;
import vaulsys.transfermanual.BeanDataTransfer;
import vaulsys.user.User;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;


@Entity
@DiscriminatorValue(value = "TransferSorushTableJob")
public class TransferSorushTableJob extends AbstractSwitchJob {
    private static final Logger logger = Logger.getLogger(TransferSorushJob.class);
    private static final long serialVersionUID = 1L;
    private static boolean isFree = true;

    @Transient
    User userUI = null;
    @Override
    public void execute(JobExecutionContext avicennaJobContext, JobLog log) {
        try {
            if(!isJobFree()){
                logger.error("Another thread is running... Exiting from TransferSorushTableJob");
                log.setStatus(SwitchJobStatus.FINISHED);
                log.setExceptionMessage("Job is not free");
                return;
            }

            GeneralDao.Instance.beginTransaction();
//            BeanDataTransferSorush sorushFile;
            List<BeanDataTransfer> totalRetVal;
//            List<ScheduleMessage> msgArr = new ArrayList<ScheduleMessage>();
//            int i = 0;
//            ScheduleMessage  msg ;
            ProcessContext.get().init();
            List<TransferSorushTableJobInfo> sorushInfo = SchedulerService.getToBeFiredJobInfo(TransferSorushTableJobInfo.class);
            deleteJobTransfer(sorushInfo);
            
            if(sorushInfo == null || sorushInfo.size() == 0 ){
                setJobFree();
                GeneralDao.Instance.endTransaction();
                return ;
            }
            GeneralDao.Instance.endTransaction();
            for(TransferSorushTableJobInfo b : sorushInfo){
            	try {
                	GeneralDao.Instance.beginTransaction();
                	userUI = GeneralDao.Instance.load(User.class, b.getAmount().intValue());
                	 GeneralDao.Instance.endTransaction();
				} catch (Exception e) {
					// TODO: handle exception
					userUI = null;
					GeneralDao.Instance.endTransaction();
				}
                if(userUI == null ){
                	userUI = GlobalContext.getInstance().getSwitchUser();
                }
            	if(b.getTransaction() != null){
            		
            		ShetabReconciliationService.TransferSorushiFromTrx(b,userUI);
            		
            	}else if(b.getUrlFile() != null){
                    totalRetVal = new ArrayList<BeanDataTransfer>();
                    logger.info("Report file name is: " + /*"E:/1.txt"*/ b.getUrlFile());
                    File shetabReport = new File(/*"E:/1.txt"*/b.getUrlFile());
                    File shetabReportRes = new File(/*"E:/1.txt".substring(0, "E:/1.txt".length()-4)+"-Report.txt"*/b.getUrlFile().substring(0, b.getUrlFile().length()-4)+"-report.txt");
                    BufferedWriter sorushLog =  new BufferedWriter(new FileWriter(shetabReportRes));
                    
                    
                    
                    try {
                        totalRetVal = ShetabReconciliationService.getListOfNotTrxSorush(new BufferedReader(new FileReader(shetabReport)), sorushLog, null, /*GlobalContext.getInstance().getSwitchUser()*/userUI);
                        logger.info("totalRetVal.size() = " + totalRetVal.size());
                        shetabReport.deleteOnExit();
                    } catch (FileNotFoundException e) {
                        logger.error("File Sorushi Not Fount " );
                    } catch (IOException e) {
                        logger.error("Error in Read Sorushi File  " );
                    } catch (Exception e) {
                        logger.error("Error on ShetabReconciliationService.getListOfNotTrxSorush");
                    }
                    try {
                    	sorushLog.close();
					} catch (Exception e) {
					}
                   /* Timestamp cur ;
                    if(totalRetVal != null || totalRetVal.size() == 0){
                    	
                    	for(BeanDataTransfer tr : totalRetVal){
                    		try {
                    			Thread.sleep(1000);
                    			
                    			msg = new ScheduleMessage();
                    			*//**@author k.khodadi
                    			 * Message be sorat dasti shakhte mishavad
                    			 *//*
                    			msg = TransferManual.getInstance().getTrxTransferSorushi(tr);
                    			logger.info("Message sorushi create manual");
                  			
                    			if(msg != null){
                    				
                    				msgArr.add(msg);
                    			
                            		*//**@author k.khodadi
                        			 * dataTime shoroe trx setr mishavat ta overTime(20 S for Sorush) rokh nadahad
                        			 *//*
                    				msg.getTransaction().setBeginDateTime(DateTime.now());
                        			*//**
                        			 * Message Sent mishavad
                        			 *//*
                        			MessageManager.getInstance().putRequest(msg, null, System.currentTimeMillis());
                        			logger.info("Put Request Message sorushi");
                        			*//**
                        			 * Takhir yek saniye  for send badi message
                        			 *//*
                    		}
                    			
                    		} catch (Exception e) {
                    			logger.error("Error in create and send sorush: " + e, e);
                    		}
                    		
                    		
                    	}
                    	
                    }else{
                    	logger.info("NOT Found  Record Sorushi.");
                    }*/

                }
            }
            
//            deleteJobTransfer(sorushInfo);
//            GeneralDao.Instance.endTransaction();
            
           /* for(ScheduleMessage m: msgArr){
            	try {
            		*//**@author k.khodadi
        			 * dataTime shoroe trx setr mishavat ta overTime(20 S for Sorush) rokh nadahad
        			 *//*
            		GeneralDao.Instance.beginTransaction();
        				m.getTransaction().setBeginDateTime(DateTime.now());
        			GeneralDao.Instance.endTransaction();
        			*//**
        			 * Message Sent mishavad
        			 *//*
        			MessageManager.getInstance().putRequest(m, null, System.currentTimeMillis());
        			logger.info("Put Request Message sorushi");
        			*//**
        			 * Takhir yek saniye  for send badi message
        			 *//*
        			Thread.sleep(1000);
        			
				} catch (Exception e) {
					// TODO: handle exception
					logger.error("Con not Put Request Message sorushi"+ e,e);
				}
            }*/
            
        } catch (Exception e) {
            logger.error("error in job TransferSorushTableJob"+e, e);
            GeneralDao.Instance.close();
        }
        setJobFree();
    }


    public void  deleteJobTransfer(List<TransferSorushTableJobInfo> delObj){
        try {

            if (!delObj.isEmpty()) {

                List<TransferSorushTableJobInfo> jobForQuery = new ArrayList<TransferSorushTableJobInfo>();
                int counter = 0;

                for (int i = 0; i < delObj.size(); i++) {
                    jobForQuery.add(delObj.get(i));
                    counter++;
                    if(counter == 500 || i == delObj.size() - 1) {
                        String query = "delete from TransferSorushTableJobInfo j where j in (:ids)";
                        Map<String, Object> params = new HashMap<String, Object>(1);
                        params.put("ids", jobForQuery);
                        int numAffected = GeneralDao.Instance.executeUpdate(query, params);

                        logger.debug("Num affected jobInfo with batch update of delete: "+numAffected);

                        counter = 0;
                        jobForQuery = new ArrayList<TransferSorushTableJobInfo>();
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e, e);
        }
    }

    @Override
    public void updateExecutionInfo() {
        // TODO Auto-generated method stub

    }

    @Override
    public void submitJob() throws Exception {

        TransferSorushTableJob newJob = new TransferSorushTableJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.GENERAL);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setJobName("TransferSorushTableJob");
        JobServiceQuartz.submit(newJob);

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
