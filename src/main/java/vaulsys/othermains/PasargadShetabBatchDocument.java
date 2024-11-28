package vaulsys.othermains;

import vaulsys.calendar.DateTime;
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

import com.ghasemkiani.util.icu.PersianDateFormat;

// TASK Task124 [16007] - New requet for Pasargad Document
public class PasargadShetabBatchDocument {
	private static final String ZIPFILE_NAME_HEADER = "pas2-";

	public static void main(String[] args) {
		String path; 
		if(args.length < 1) {
			System.out.println("Enter report files path as input paramater...");
			path = "D:/Resalat/RepbalBArgasht/Repballl/9208New/";
//			path = "C:/Documents and Settings/saber/My Documents/Shetab Reports/NewReport";
			
		}else{
			path = args[0];
			System.out.println("Parameter[1] is "+args[0]);
		}
		
		System.out.println("Start .....");

		File folder = new File(path);
		File[] files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name){
				return name.startsWith(ZIPFILE_NAME_HEADER) && name.endsWith("_SHETAB_SANAD.zip");
			}
		});
		
		if(files == null){
			System.exit(0);
			return;
		}

		ZipFile zipFile;
		GeneralDao.Instance.beginTransaction();
		GlobalContext.getInstance().startup();
	        ProcessContext.get().init();

       	Long myBin = GlobalContext.getInstance().getMyInstitution().getBin(); 
      	String repbalName =  "IRI/" + myBin + ".repbal_txt";
      	String repExtraName = "IRI/" + myBin + ".rep_extra_txt";
	    InputStream repbalToday =null;
        InputStream extraToday = null;
        InputStream extraYesterday = null;
		
		for(File file:files){	
			System.out.println("Start Processing file:"+file.getName());
			
			//PreFile Shetab
			String currentDateStr = file.getName().replace(ZIPFILE_NAME_HEADER, "").substring(0,6);
			File yesterdayZip = null;			
			try {
				Integer currentDateInt = Integer.valueOf(currentDateStr);
				System.out.println(currentDateStr);
				DateTime per = new DateTime((currentDateInt+13000000L) *1000000);
				DateTime mil = vaulsys.calendar.PersianCalendar.toGregorian(per);
				mil.increase(-1*60*24);
				PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMdd");
				String preDateStr = dateFormatPers.format(mil.toDate());			
				yesterdayZip = new File(path+ZIPFILE_NAME_HEADER+preDateStr+"_SHETAB_SANAD.zip");
				if (!yesterdayZip.exists()){
					System.out.println(String.format("WARNING : File %s is not exist ",path+ZIPFILE_NAME_HEADER+preDateStr+"_SHETAB_SANAD.zip" ));
					continue;
				}
			}catch(Exception e){
				e.printStackTrace();
				System.out.println(e.getMessage());
				break;
			}
			
			try {
				zipFile = new ZipFile(file);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				StringBuilder builder = new StringBuilder();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if (entry.getName().endsWith((repbalName))) {
						try {
							repbalToday = zipFile.getInputStream(entry);
						} catch (Exception e) {
							System.err.println(e);
							e.printStackTrace();
						}
					}
					if (entry.getName().endsWith(repExtraName)) {
						try {
							extraToday = zipFile.getInputStream(entry);
						} catch (Exception e) {
							System.err.println(e);
							e.printStackTrace();
						}
					}
				}
				
	            zipFile = new ZipFile(yesterdayZip);
	            entries = zipFile.entries();
	            while (entries.hasMoreElements()){
	            	ZipEntry entry = entries.nextElement();
	            	try{
	            		if(entry.getName().endsWith(repExtraName))
	            			extraYesterday = zipFile.getInputStream(entry);
	            	}catch(Throwable e){
	            		System.err.println(e);
	            		e.printStackTrace();
	            	}
	            }
	            String result = "";
	            if(repbalToday != null && extraToday != null && extraYesterday != null)
	            	result = ShetabDocumentService.issuePasargadShetabDocument(new BufferedReader(new InputStreamReader(repbalToday)),  
	            		new BufferedReader(new InputStreamReader(extraToday)), new BufferedReader(new InputStreamReader(extraYesterday)),"repbal-day:");
	            System.out.println(result);
				System.out.println("Finish Processing file:"+file.getName());
			} catch (Exception e1) {
				System.err.println(e1);
			}		
		}
		
		GeneralDao.Instance.endTransaction();
		System.exit(0);
		System.out.println("End .....");
	}
}

