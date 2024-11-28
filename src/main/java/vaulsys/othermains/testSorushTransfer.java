package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.report.ShetabReconciliationService;
import vaulsys.message.MessageManager;
import vaulsys.message.ScheduleMessage;
import vaulsys.scheduler.job.TransferSorushJob;
import vaulsys.transfermanual.BeanDataTransfer;
import vaulsys.transfermanual.BeanDataTransferSorush;
import vaulsys.transfermanual.TransferManual;
import vaulsys.wfe.GlobalContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class testSorushTransfer {

	private static final Logger logger = Logger.getLogger(testSorushTransfer.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
			  BeanDataTransferSorush sorushFile;
	          List<BeanDataTransfer> totalRetVal;
	          List<ScheduleMessage> msgArr = new ArrayList<ScheduleMessage>();
	          int i = 0;
	          ScheduleMessage  msg ;

	          String url = "";
       
            totalRetVal = new ArrayList<BeanDataTransfer>();
            logger.error("Report file name is: " + /*"E:/1.txt"*/ url);
            File shetabReport = new File(/*"E:/1.txt"*/url);
            File shetabReportRes = new File(/*"E:/1.txt".substring(0, "E:/1.txt".length()-4)+"-Report.txt"*/url.substring(0, url.length()-4)+"-report.txt");
            try {
                totalRetVal = ShetabReconciliationService.getListOfNotTrxSorush(new BufferedReader(new FileReader(shetabReport)), new BufferedWriter(new FileWriter(shetabReportRes)), null, GlobalContext.getInstance().getSwitchUser());
                shetabReport.deleteOnExit();
            } catch (FileNotFoundException e) {
                logger.error(e, e);
            } catch (IOException e) {
                logger.error(e, e);
            } catch (Exception e) {
                logger.error(e, e);
            }
            Timestamp cur ;
            for(BeanDataTransfer tr : totalRetVal){
                try {

                    msg = new ScheduleMessage();
                    /**@author k.khodadi
                     * Message be sorat dasti shakhte mishavad
                     */
                    msg = TransferManual.getInstance().getTrxTransferSorushi(tr);
                    /**@author k.khodadi
                     * dataTime shoroe trx setr mishavat ta overTime(20 S for Sorush) rokh nadahad
                     */
                    msg.getTransaction().setBeginDateTime(DateTime.now());
                    /**
                     * Message Sent mishavad
                     */
                    MessageManager.getInstance().putRequest(msg, null, System.currentTimeMillis());
                    /**
                     * Takhir yek saniye  for send badi message
                     */
                    Thread.sleep(600);
//							cur = new Timestamp(Calendar.getInstance().getTimeInMillis() + 1000);
//							while(Calendar.getInstance().getTimeInMillis() <= cur.getTime());
							
							/*if(msg != null){
							msgArr.add(msg);
							}*/
                } catch (Exception e) {
                    logger.error(e, e);
                }


            }

        
    

	}

}
