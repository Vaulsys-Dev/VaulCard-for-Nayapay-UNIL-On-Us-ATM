package vaulsys.othermains;

import vaulsys.clearing.report.ShetabDocumentService;
import vaulsys.persistence.GeneralDao;
import vaulsys.wfe.GlobalContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class ShetabDocument {

	public static void main(String[] args) {
		String path; 
		if(args.length < 1) {
			System.out.println("Enter report file name as input paramater...");
			path = "C:/Documents and Settings/saber/My Documents/Shetab Reports/NewReport/880314_SHETAB_SANAD/PAS2";
//			path = "/home/haddad/shetab/880212_SHETAB_SANAD/PAS2";
		}else{
			path = args[0];
		}

		String fileName;
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();

		String[] ShetabDocumentService_FILES = ShetabDocumentService.getFiles(GlobalContext.getInstance().getMyInstitution().getBin());
		for(int i=0; i<ShetabDocumentService_FILES.length; i++) {
		
			fileName = path + "/" +ShetabDocumentService_FILES[i];
			
			System.out.println("Report file name is: "+fileName);
			
			File shetabReport = new File(fileName);
			try {
				String result = ShetabDocumentService.issueShetabDocument(new BufferedReader(new FileReader(shetabReport)));
				System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				GeneralDao.Instance.endTransaction();
			}
		}
		
		System.exit(0);
	}
}
