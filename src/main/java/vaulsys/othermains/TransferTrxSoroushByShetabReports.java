package vaulsys.othermains;

import vaulsys.clearing.report.ShetabReconciliationService;
import vaulsys.persistence.GeneralDao;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class TransferTrxSoroushByShetabReports {

	public static void main(String[] args) {
		String path;
		String pathRes;
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		
		ProcessContext.get().init();

		System.out.println("Enter report file name as input paramater...");

		if(args.length > 0) {
			path = args[0];
			pathRes = path.substring(0, path.length()-4)+"-res.txt";
		}else{			
//			path = "C:/Documents and Settings/saber/My Documents/samimi/881106/pas2881106/isspas2881106-1092-32128.txt";
//			path = "C:/Documents and Settings/saber/My Documents/samimi/Hesab881118.txt";
//			path = "C:/NOKDUP.txt";
			path = "E:/1.txt";
			pathRes = path.substring(0, path.length()-4)+"-res.txt";
//			pathRes = "C:/Documents and Settings/saber/My Documents/samimi/13881113/BargashBeHesabtxt881113-res.txt";
		}
		GeneralDao.Instance.endTransaction();
		for(int i=0; i<ShetabReconciliationService.REVERSAL_FILES.length; i++) {
		
//			fileName = path + "/" +ShetabReconciliationService.REVERSAL_FILES[i];
			
			System.out.println("Report file name is: "+path);
			
			File shetabReport = new File(path);
			File shetabReportRes = new File(pathRes);
			
			try {
				if(!shetabReportRes.exists()){
					shetabReportRes.createNewFile();
				}
				
				ShetabReconciliationService.getListOfTrxSorushInShetabReversalReport(new BufferedReader(new FileReader(shetabReport)), new BufferedWriter(new FileWriter(shetabReportRes)), null, GlobalContext.getInstance().getSwitchUser());
			} catch (Exception e) {
				e.printStackTrace();
			}
//			GeneralDao.Instance.commit();
		}
		System.exit(0);
	}

}
