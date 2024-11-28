package vaulsys.scheduler.job;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.report.ShetabReconciliationService;
import vaulsys.job.AbstractSwitchJob;
import vaulsys.job.SwitchJobGroup;
import vaulsys.job.SwitchJobStatus;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.message.MessageManager;
import vaulsys.message.ScheduleMessage;
import vaulsys.persistence.GeneralDao;
import vaulsys.scheduler.JobLog;
import vaulsys.scheduler.SchedulerService;
import vaulsys.scheduler.TransferSorushJobInfo;
import vaulsys.transaction.Transaction;
import vaulsys.transfermanual.BeanDataTransfer;
import vaulsys.transfermanual.BeanDataTransferSorush;
import vaulsys.transfermanual.BeanLogSorushTrx;
import vaulsys.transfermanual.TransferManual;
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

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;

@Entity
@DiscriminatorValue(value = "TransferSorushJob")
public class TransferSorushJob extends AbstractSwitchJob {

    private static final Logger logger = Logger.getLogger(TransferSorushJob.class);

    private static boolean isFree = true;
    @Override
    public void execute(JobExecutionContext avicennaJobContext, JobLog log) {
        try {
            if(!isJobFree()){
                logger.error("Another thread is running... Exiting from TransferSorushJob");
                log.setStatus(SwitchJobStatus.FINISHED);
                log.setExceptionMessage("Job is not free");
                return;
            }

            BeanDataTransferSorush SorushFile;
            List<BeanDataTransfer> totalRetVal = new ArrayList<BeanDataTransfer>();
            List<BeanDataTransfer> totalRetValMsgI = new ArrayList<BeanDataTransfer>();
            List<ScheduleMessage> msgArr = new ArrayList<ScheduleMessage>();
            int i = 0;
            User userUI = null;
            ScheduleMessage  msg ;
            GeneralDao.Instance.beginTransaction();
            ProcessContext.get().init();
            List<TransferSorushJobInfo> sorushInfo = SchedulerService.getToBeFiredJobInfo(TransferSorushJobInfo.class);
            if(sorushInfo == null || sorushInfo.size() == 0 ){
            	GeneralDao.Instance.endTransaction();
                setJobFree();
                return ;
            }
            for(TransferSorushJobInfo b : sorushInfo){
            	
		        try {
		                if(b.getUrlFile() != null){
		                	
		                    totalRetVal = new ArrayList<BeanDataTransfer>();
		                    logger.debug("Report file name is: " + /*"E:/1.txt"*/ b.getUrlFile());
		                    File shetabReport = new File(/*"E:/1.txt"*/b.getUrlFile());
		                    
		                    File shetabReportRes = new File(/*"E:/1.txt".substring(0, "E:/1.txt".length()-4)+"-Report.txt"*/b.getUrlFile().substring(0, b.getUrlFile().length()-4)+"-report.txt");
		                    
		                    BufferedWriter logFile = new BufferedWriter(new FileWriter(shetabReportRes));
		                    try {
		                    	userUI = GeneralDao.Instance.load(User.class, b.getAmount().intValue());
							} catch (Exception e) {
								// TODO: handle exception
								userUI = null;
							}
		                    if(userUI == null ){
		                    	userUI = GlobalContext.getInstance().getSwitchUser();
		                    }
		                    try {
		                        totalRetVal = ShetabReconciliationService.getListOfTrxSorushInShetabReversalReport(new BufferedReader(new FileReader(shetabReport)), logFile, null, userUI /*GlobalContext.getInstance().getSwitchUser()*/);
		                        logger.info("totalRetVal.size() = " + totalRetVal.size());
		                        shetabReport.deleteOnExit();
		                    } catch (FileNotFoundException e) {
		                       logger.error(e);
		                    } catch (IOException e) {
		                    	logger.error(e);
		                    } catch (Exception e) {
		                    	logger.error(e);
		                    }
		                    try {
		                    	logFile.close();
							} catch (Exception e) {
								// TODO: handle exception
							}
		                    for(BeanDataTransfer tr : totalRetVal){
		                    	try {
		                    		/**
		                    		 * Transaction for insert with Type I
		                    		 */
		                    		
		                    		if( tr.getReverslSorush() != null && 
		                				tr.getReverslSorush().recordTypeInsert != null && 
		                				tr.getReverslSorush().recordTypeInsert.equalsIgnoreCase("I")){
		                    			totalRetValMsgI.add(tr);
		                    		}else{
		                    			SorushFile = new BeanDataTransferSorush(tr.getReverslSorush().row,
		                    					tr.getReverslSorush().persianDt,
		                    					tr.getReverslSorush().trnSeqCntr,
		                    					tr.getReverslSorush().appPan,
		                    					tr.getReverslSorush().amount,
		                    					tr.getReverslSorush().bankId,
		                    					tr.getReverslSorush().recordCode,
		                    					userUI,
		                    					tr.getReverslSorush().recordType,
		                    					tr.getReverslSorush().terminalId
		                    					);
		                    			SorushFile.terminalId = tr.trx.getIncomingIfx().getTerminalId();
		                    			SorushFile.trx = tr.trx;
		                    			GeneralDao.Instance.save(SorushFile);
		                    		}
								} catch (Exception e) {
									logger.debug("Problem in put request");
								}
		                    }
		                }
		
		           } catch (Exception e) {
		        	   logger.debug("Problem unKnown");
		           }
            }
            deleteJobTransfer(sorushInfo);
            GeneralDao.Instance.endTransaction();


            for(BeanDataTransfer tr : totalRetValMsgI){
            	try {
            		/**
            		 * Transaction for insert with Type I
            		 */
            		
            		if( tr.getReverslSorush() != null && 
        				tr.getReverslSorush().recordTypeInsert != null && 
        				tr.getReverslSorush().recordTypeInsert.equalsIgnoreCase("I")){
            			Thread.sleep(1000);	
            			msg = new ScheduleMessage();
            			GeneralDao.Instance.beginTransaction();
            			try {
            				
            				tr.setTrx(GeneralDao.Instance.load(Transaction.class, tr.getTrx().getId()));
            				msg = TransferManual.getInstance().getTrxTransferSorushi(tr);
            				msg.getIfx().getSafeOriginalDataElements().setRefSorushiTransaction(tr.trx);
							
						} catch (Exception e) {
							logger.error("Problem in Create TRX" );
							GeneralDao.Instance.endTransaction();
							continue;
						}
            			
            			
            			try {
            				if(msg != null && msg.getTransaction() != null){
            					
            					
            					BeanLogSorushTrx loggerSorush = new BeanLogSorushTrx();
            					loggerSorush.setTrxRef(tr.trx);                        	
            					loggerSorush.setAmountRefTrx(tr.getReverslSorush().amount);
            					loggerSorush.setAmountTotal(tr.trx.getFirstTransaction().getIncomingIfx().getAuth_Amt());
            					loggerSorush.setAmountSodSorush(tr.getReverslSorush().amount - tr.trx.getFirstTransaction().getIncomingIfx().getAuth_Amt() );
            					loggerSorush.setAppPan(tr.getReverslSorush().appPan);
            					loggerSorush.setTrnSeqCntr(tr.getReverslSorush().trnSeqCntr);
            					loggerSorush.setSorushDateTime(DateTime.now());
            					loggerSorush.setUser(userUI);
            					
            					GeneralDao.Instance.save(loggerSorush);
            				}	
							
						} catch (Exception e) {
							logger.error("Error in log in BeanLogSorushTrx ");
						}
            			
            			GeneralDao.Instance.endTransaction();
            			if(msg != null && msg.getTransaction() != null){
            				msg.getTransaction().setBeginDateTime(DateTime.now());
            				MessageManager.getInstance().putRequest(msg, null, System.currentTimeMillis());
            			}
            		}
				} catch (Exception e) {
					logger.debug("Problem in put request" ,e);
				}
            }
        } catch (Exception e) {
        	logger.error(e);
            GeneralDao.Instance.close();
        }
        setJobFree();
    }

    public void  deleteJobTransfer(List<TransferSorushJobInfo> delObj){
        try {

            if (!delObj.isEmpty()) {

                List<TransferSorushJobInfo> jobForQuery = new ArrayList<TransferSorushJobInfo>();
                int counter = 0;

                for (int i = 0; i < delObj.size(); i++) {
                    jobForQuery.add(delObj.get(i));
                    counter++;
                    if(counter == 500 || i == delObj.size() - 1) {
                        String query = "delete from TransferSorushJobInfo j where j in (:ids)";
                        Map<String, Object> params = new HashMap<String, Object>(1);
                        params.put("ids", jobForQuery);
                        int numAffected = GeneralDao.Instance.executeUpdate(query, params);

                        logger.debug("Num affected jobInfo with batch update of delete: "+numAffected);

                        counter = 0;
                        jobForQuery = new ArrayList<TransferSorushJobInfo>();
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void updateExecutionInfo() {
        // TODO Auto-generated method stub

    }

    public synchronized boolean isJobFree(){
        if(isFree == true){
            isFree = false;
            return true;
        }
        return false;
    }

    @Override
    public void submitJob() throws Exception {

        TransferSorushJob newJob = new TransferSorushJob();
        newJob.setStatus(SwitchJobStatus.NOT_STARTED);
        newJob.setGroup(SwitchJobGroup.GENERAL);
        newJob.setJobSchedule(this.getJobSchedule());
        newJob.setJobName("TransferSorushJob");
        JobServiceQuartz.submit(newJob);

    }

    public void setJobFree(){
        isFree = true;
    }

}
