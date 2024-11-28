package vaulsys.othermains;

import vaulsys.clearing.report.ShetabDocumentService;
import vaulsys.persistence.GeneralDao;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class ShetabBatchDocument {

	public static void main(String[] args) {
		String path; 
		if(args.length < 1) {
			System.out.println("Enter report files path as input paramater...");
//			path = "C:/Documents and Settings/saber/My Documents/Shetab Reports/NewReport";
			path = "C:/Documents and Settings/saber/My Documents/taavon-repbal/test4";
		}else{
			path = args[0];
		}

		File folder = new File(path);
		File[] files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name){
//				return name.startsWith("pas2") && name.endsWith("_SHETAB_SANAD.zip");
				return name.startsWith("tavon") && name.endsWith("_SHETAB_SANAD.zip");
			}
		});
		
		if(files == null || files.length == 0){
			System.exit(0);
			return;
		}

		ZipFile zipFile;
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		
		for(File file:files){	
			System.out.println("Processing file:"+file.getName());
			try {
				zipFile = new ZipFile(file);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				StringBuilder builder = new StringBuilder();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					//String[] ShetabDocumentService_FILES = ShetabDocumentService.getFiles(GlobalContext.getInstance().getMyInstitution().getBin()); //Raza commenting for MutiInstitution
					String[] ShetabDocumentService_FILES = null; //Raza Adding to Remove Error Only, will FIX this

					for (int i = 0; i < ShetabDocumentService_FILES.length; i++) {
						if (entry.getName().contains(ShetabDocumentService_FILES[i]))
							try {
								InputStream inputStream = zipFile.getInputStream(entry);
//								ShetabDocumentService.issueShetabDocument(new BufferedReader(new InputStreamReader(inputStream)));
								ShetabDocumentService.issueTavonShetabDocument(new BufferedReader(new InputStreamReader(inputStream)));
								
							} catch (Exception e) {
								System.err.println(e);
							}
					}
				}
				System.out.println("Processing file:"+file.getName());
			} catch (Exception e1) {
				System.err.println(e1);
			}		
		}
		
		GeneralDao.Instance.endTransaction();
//		System.exit(0);
	}
}
