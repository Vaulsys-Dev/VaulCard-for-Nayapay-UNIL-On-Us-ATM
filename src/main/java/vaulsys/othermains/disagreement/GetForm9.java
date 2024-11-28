package vaulsys.othermains.disagreement;

import vaulsys.clearing.report.ShetabDisagreementService;
import vaulsys.clearing.report.ShetabReconciliationService;
import vaulsys.clearing.report.ShetabReportRecord;
import vaulsys.persistence.GeneralDao;
import vaulsys.util.ConfigUtil;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GetForm9 {

	/**
	 * @param args
	 */
	private static final Logger logger = Logger.getLogger(GetForm9.class);
	public static String tuneMyForm9(String path_toShetab, String path, boolean isPSP) throws FileNotFoundException {
		String fileExt = "-acq-";
//		if(args.length < 1) {
//			System.out.println("Enter report files path as input paramater...");
//			path = "D:/disagreement/form9/900926";
//			filePath = "MR_502229.txt";
//		}else{
//			path = args[0];
//		}
		
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		GeneralDao.Instance.endTransaction();
		
		try{
			//Input file
			File file = new File(path_toShetab);
			if(file==null)
				return null;
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			//Output File
			String pathRes;
			if(isPSP)
				pathRes = path+"/"+file.getName().substring(0,file.getName().length()-4)+fileExt+"report9ToPSP.txt";
			else
				pathRes = path+"/"+file.getName().substring(0,file.getName().length()-4)+fileExt+"report9ToShetab.txt";
			File shetabReportRes = new File(pathRes);
			BufferedWriter result = new BufferedWriter(new FileWriter(shetabReportRes));
			List<ShetabReportRecord> records = new ArrayList<ShetabReportRecord>();
			records = GetForm9.extractAndParseRecord(br, isPSP);
			result.append(CompareForm9.generateReport9File(records,isPSP));
			result.flush();
			result.close();
			
			return pathRes;
		}catch(Exception e){
			logger.error(e);
		}
		return null;
	}
	public static String tuneShetabForm9(String path_fromShetab) {
		
		
		final String bankName = ConfigUtil.getProperty(ConfigUtil.BANK_NAME);   //vase tavon tav !!!!
		String fileExt = "-acq-";
//		if(args.length < 1) {
//			System.out.println("Enter report files path as input paramater...");
//			path = "D:/disagreement/form9/900926";
//		}else{
//			path = args[0];
//		}

		File folder = new File(path_fromShetab);
		File[] files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name){
				return name.toLowerCase().startsWith(bankName.toLowerCase()) && name.endsWith(".zip") && !name.contains("SANAD");
			}
		});
		
		List<File> fileName = new ArrayList<File>();
		for(int i=0 ; i< folder.list().length; i++){
			fileName.add(folder.listFiles()[i]) ;
		}
		
		if(files == null){
//			System.exit(0);
			return null;
		}

		ZipFile zipFile;
		
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		GeneralDao.Instance.endTransaction();
		
		String[] shetabReconcilationFiles = ShetabReconciliationService.getShetabReconcilationFiles(GlobalContext.getInstance().getMyInstitution().getBin());
		for(File file:files){
			logger.debug("Processing file:"+file.getName());
			try {
				String pathRes = path_fromShetab+"/"+file.getName().substring(0,file.getName().length()-4)+fileExt+"report9FromShetab.txt";
				File shetabReportRes = new File(pathRes);
				if(!shetabReportRes.exists()){
					shetabReportRes.createNewFile();
				}
				BufferedWriter result = new BufferedWriter(new FileWriter(shetabReportRes));
				zipFile = new ZipFile(file);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					for(int i=0; i<shetabReconcilationFiles.length; i++) {
						if (entry.getName().endsWith(shetabReconcilationFiles[i])){
							try{
								logger.debug("Entry:"+entry.getName());
								if(!entry.getName().endsWith(".Acq"))
									continue;
								InputStream inputStream = zipFile.getInputStream(entry);
								List<ShetabReportRecord> records = new ArrayList<ShetabReportRecord>();
								records = GetForm9.extractAndParseRecord(new BufferedReader(new InputStreamReader(inputStream)), false);
								result.append(CompareForm9.generateReport9File(records, false));
								result.flush();
							}catch(Exception e){
								e.printStackTrace();
								logger.error(e);
							}
							break;
						}
					}//for
				}//while
                zipFile.close();
					logger.debug("Processing file:" + file.getName());
					result.flush();
					result.close();
					
					return pathRes;
				
			}catch (Exception e1) {
				e1.printStackTrace();
				logger.error(e1);
			}
		}
		return null;
	}
	
	public static String tunePSPform9(String path_fromPSP, String path, String path_toPSP){
		
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
		ProcessContext.get().init();
		GeneralDao.Instance.endTransaction();
		
		try{
			File file = new File(path_fromPSP);
			if(file ==  null)
				return null;
			logger.debug("Processing file:"+file.getName());
			
			String pathRes = path+"/"+path_toPSP.substring(path_toPSP.indexOf("MR_")+"MR_".length(), path_toPSP.indexOf(".acq"))+"-report9FromPSP.txt";
			File PSPreportRes = new File(pathRes);
			if(!PSPreportRes.exists()){
				PSPreportRes.createNewFile();
			}
			BufferedWriter result = new BufferedWriter(new FileWriter(PSPreportRes));
/*			ZipEntry zipEntry;
			FileInputStream inputStream =null;
			ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
			for(zipEntry = zip.getNextEntry(); zipEntry != null; zipEntry = zip.getNextEntry()){
				if(!zipEntry.isDirectory() && zipEntry.getName().endsWith(".acq"))
					break;
			}
			FileOutputStream fos = new FileOutputStream(path+"/FromPSP.txt");
			int n=0;
			byte[] buf = new byte[5120];
			while((n = zip.read(buf,0,1024))> -1)
				fos.write(buf, 0, n);
			fos.close();
			zip.closeEntry();*/
			List<ShetabReportRecord> records = new ArrayList<ShetabReportRecord>();
			records = ShetabDisagreementService.parseRecords(new BufferedReader(new InputStreamReader(new FileInputStream(new File(path_fromPSP)))), false,ProcessContext.get().getMyInstitution().getBin(),
					ShetabReconciliationService.TRX, true);
			result.append(CompareForm9.generateReport9File(records, true));
			result.flush();
			result.close();
			
			return pathRes;
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e);
		}
		return null;
	}
	public static List<ShetabReportRecord> extractAndParsePSPrecord(BufferedReader brPSPreport) {
		// TODO Auto-generated method stub
		return null;
	}
	public static List<ShetabReportRecord> extractAndParseRecord(BufferedReader brShetabReport, boolean isPSP) throws IOException{
		List<ShetabReportRecord> records = new ArrayList<ShetabReportRecord>();
		try{
			records = ShetabDisagreementService.parseRecords(brShetabReport, false, ProcessContext.get().getMyInstitution().getBin(), ShetabReconciliationService.TRX, isPSP);
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e);
		}
		return records;
	}

}
