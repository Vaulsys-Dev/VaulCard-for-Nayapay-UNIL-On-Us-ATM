package vaulsys.othermains;

import vaulsys.clearing.report.ShetabDisagreementService;
import vaulsys.clearing.report.ShetabReconciliationService;
import vaulsys.persistence.GeneralDao;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ShetabBatchReconciliation {
	public static void main(String[] args) {
		String path; 
		if(args.length < 1) {
			System.out.println("Enter report files path as input paramater...");
			path = "C:/Documents and Settings/saber/My Documents/Shetab Reports/report/891221";
		}else{
			path = args[0];
		}

		File folder = new File(path);
		File[] files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name){
				return name.startsWith("pas2") && name.endsWith(".zip");
			}
		});
		
		if(files == null){
			System.exit(0);
			return;
		}

		ZipFile zipFile;
		
		String pathRes = path+"/res-"+System.currentTimeMillis()+".txt";
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

		GeneralDao.Instance.beginTransaction();

		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		GeneralDao.Instance.endTransaction();
		
		String[] shetabReconcilationFiles = ShetabReconciliationService.getShetabReconcilationFiles(GlobalContext.getInstance().getMyInstitution().getBin());
		for(File file:files){	
			System.out.println("Processing file:"+file.getName());
			try {
//				errors.append("\r\n------------------\r\nProcessing file:"+file.getName()+"\r\n");
				zipFile = new ZipFile(file);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();

					for(int i=0; i<shetabReconcilationFiles.length; i++) {
						if (entry.getName().endsWith(shetabReconcilationFiles[i])){
							try {
								System.out.println("Entry:"+entry.getName());
//								errors.append("\r\nEntry:"+entry.getName()+"\r\n");
								InputStream inputStream = zipFile.getInputStream(entry);
								boolean isIssuer = ShetabReconciliationService.IS_ISSUER[i];
								int trxType = ShetabReconciliationService.TRX_TYPES[i];
								String err = ShetabDisagreementService.reconcileByShetabReport(new BufferedReader(new InputStreamReader(inputStream)), isIssuer, trxType);
								if(err != null && !err.equals("")){
									errors.append(err);
									errors.flush();
								}
							} catch (Exception e) {
								System.err.println(e);
							}
							break;
						}
					}
				}
				System.out.println("Processing file:"+file.getName());
				errors.flush();
			} catch (Exception e1) {
				System.err.println(e1);
			}		
		}
		
		try {
			errors.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
