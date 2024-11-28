package vaulsys.othermains.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.base.SettlementDataReport;
import vaulsys.clearing.settlement.InstitutionSettlementServiceImpl;
import vaulsys.persistence.GeneralDao;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class ShetabSettlement {
	private static final Logger logger = Logger.getLogger(ShetabSettlement.class);

	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		
		try {
			ClearingProfile clearingProfile = ClearingService.findClearingProfile(2303701L);
			boolean explicitWorkingDay = false;
			long day = 20150303L;
			long workDay = 20150303L;
			long cutofffrom = 20150302215922L;
			long cutoffto = 20150303215917L;
			if (args.length == 1) {
//				day = Long.parseLong(args[0]);
			}else if (args.length == 4) {
				day = Long.parseLong(args[0]);
				workDay = Long.parseLong(args[1]);
				cutofffrom = Long.parseLong(args[2]);
				cutoffto = Long.parseLong(args[3]);
				explicitWorkingDay = true;
			}else{
				System.err.println("Wrong number of input parameters....");
//				return;
			}
			
			DateTime settleDate = new DateTime();
			settleDate.setDayDate(new DayDate((int) (day/10000), (int) ((day%10000)/100), (int) (day%100)));
			settleDate.setDayTime(new DayTime(23, 59, 59));
			if(explicitWorkingDay){
				MonthDayDate workingDay = new MonthDayDate((int) (workDay/10000), (int) ((workDay%10000)/100), (int) (workDay%100));
				DateTime cutoffDateFrom = new DateTime(new DayDate((int) (cutofffrom/10000000000L), (int) ((cutofffrom%10000000000L)/100000000), (int) ((cutofffrom%100000000)/1000000)), new DayTime((int) ((cutofffrom%1000000)/10000), (int) ((cutofffrom%10000)/100), (int) (cutofffrom%100)));
				DateTime cutoffDateTo = new DateTime(new DayDate((int) (cutoffto/10000000000L), (int) ((cutoffto%10000000000L)/100000000), (int) ((cutoffto%100000000)/1000000)), new DayTime((int) ((cutoffto%1000000)/10000), (int) ((cutoffto%10000)/100), (int) (cutoffto%100)));
				logger.debug("Generating report for settleDate: "+settleDate+" \t workingDay: "+workingDay+"\t cutoffDateFrom: "+cutoffDateFrom+"\t cutoffDateTo:"+cutoffDateTo);
				InstitutionSettlementServiceImpl.Instance.generateDesiredSettlementReports("9000", clearingProfile, settleDate, workingDay, cutoffDateFrom, cutoffDateTo, null);
			}else{		
				InstitutionSettlementServiceImpl.Instance.generateDesiredSettlementReports(clearingProfile, settleDate);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			GeneralDao.Instance.endTransaction();
//			GeneralDao.Instance.rollback();
		}
	}
	
	public static void getZipFile(){
		GeneralDao.Instance.beginTransaction();
		byte[] b = GeneralDao.Instance.getObject(SettlementDataReport.class, 261301L).getReport();
		File shetabReportRes = new File("c:/shetab-"+System.currentTimeMillis()+".zip");
		OutputStream errors = null;
		if(!shetabReportRes.exists()){
			try {
				shetabReportRes.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			errors = new FileOutputStream(shetabReportRes);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			errors.write(b);
			errors.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GeneralDao.Instance.endTransaction();		
	}
	
	public static void createZipFile(byte[] bFile){
//		GeneralDao.Instance.beginTransaction();
//		byte[] b = GeneralDao.Instance.getObject(SettlementDataReport.class, 261301L).getReport();
		File shetabReportRes = new File("c:/shetab-"+System.currentTimeMillis()+".zip");
		OutputStream errors = null;
		if(!shetabReportRes.exists()){
			try {
				shetabReportRes.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			errors = new FileOutputStream(shetabReportRes);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			errors.write(bFile);
			errors.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		GeneralDao.Instance.endTransaction();		
	}	
}
