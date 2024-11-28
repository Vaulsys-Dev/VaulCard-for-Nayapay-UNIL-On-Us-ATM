package vaulsys.othermains;

import vaulsys.clearing.report.ShetabDisagreementService;
import vaulsys.clearing.report.ShetabReconciliationService;
import vaulsys.persistence.GeneralDao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ShetabReconciliation {

	
	public static void main(String[] args) {
		
		ShetabDisagreementService shetabReconciliation = new ShetabDisagreementService();
		GeneralDao.Instance.beginTransaction();

		String path = "C:/Documents and Settings/saber/My Documents/Shetab Reports/report/bad1.txt";

		File shetabReport = new File(path);

		String pathRes = path+"bad-res-"+System.currentTimeMillis()+".txt";
		File shetabReportRes = new File(pathRes);
		BufferedWriter errors = null;
		if(!shetabReportRes.exists()){
			try {
				shetabReportRes.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			errors = new BufferedWriter(new FileWriter(shetabReportRes));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		String err = null;
		try {
			err = shetabReconciliation.reconcileByShetabReport(new BufferedReader(new FileReader(shetabReport)), true, 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(err);
		try {
			errors.append(err);
			errors.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GeneralDao.Instance.endTransaction();

		System.exit(0);
	}
//
//	public static void main(String[] args) {
//		
//		ShetabReconciliationService shetabReconciliation = new ShetabReconciliationService();
//		
//		
//		if(args.length < 1) {
//			System.out.println("Enter report file name as input paramater...");
////			System.exit(0);
//		}
//
////		String path = args[0];
////		String fileName;
//		
//		//880203
//		
//		String path = ""; 
//		if (args.length == 1) {
//			path = args[0];
//		}else{
//			path = "C:/Documents and Settings/saber/My Documents/Shetab Reports/PAS 2/Old/880203/PAS2";
//		}
//
//		String fileName;
//		
//
//		for(int i=0; i<ShetabReconciliationService.FILES.length; i++) {
//		
//			fileName = path + "/" +ShetabReconciliationService.FILES[i];
//			
//			System.out.println("Report file name is: "+fileName);
//			
//			File shetabReport = new File(fileName);
//			
//			try {
//				boolean isIssuer = ShetabReconciliationService.IS_ISSUER[i];
//				int trxType = ShetabReconciliationService.TRX_TYPES[i];
//	
//				shetabReconciliation.reconcileByShetabReport(new BufferedReader(new FileReader(shetabReport)), isIssuer, trxType);
//	
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		System.exit(0);
//	}

}
