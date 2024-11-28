package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.clearing.report.ShetabDocumentService;
import vaulsys.exception.base.SwitchBusinessException;
import vaulsys.persistence.GeneralDao;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.ghasemkiani.util.icu.PersianDateFormat;
import org.apache.log4j.Logger;



// TASK Task130 [26476] - Repbal that not used script
// TASK Task124 [16007] - New requet for Pasargad Document
public class PasargadShetabDocument {
	private static final Logger logger = Logger.getLogger(PasargadShetabDocument.class);
	
	private static final String ZIPFILE_NAME_HEADER = "pas2-";
	public static void main(String[] args) {
        String pathToday;
        String pathYesterday;
        if(args.length < 2) {
                System.out.println("Enter report file name as input paramater...");
//                pathToday = "C:/Documents and Settings/saber/My Documents/Shetab Reports/NewReport/880314_SHETAB_SANAD/PAS2";
                pathToday = "C:/Users/Kamelia/Desktop/Tasks/Task96-repbalExtra/SHETAB_SANAD/pas2-920218_SHETAB_SANAD.zip";
                pathYesterday = "C:/Users/Kamelia/Desktop/Tasks/Task96-repbalExtra/SHETAB_SANAD/pas2-920217_SHETAB_SANAD.zip";
//              path = "/home/haddad/shetab/880212_SHETAB_SANAD/PAS2";
        }else{
                pathToday = args[0];
                pathYesterday = args[1];
        }
        
        try {
			String result = issuePasargadShetabDocumentUI(pathToday, pathYesterday);
			System.out.println(result);
		} catch (SwitchBusinessException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}        

	}
	public static String issuePasargadShetabDocumentUI(String pathToday, String pathYesterday) throws SwitchBusinessException {
        String result = "";

        System.out.println("Report file name is: " + pathToday);
        System.out.println("second file for report is: " + pathYesterday);

        File todayZip = new File(pathToday);
        File yesterdayZip = new File(pathYesterday);
        
        if (!todayZip.exists()) {
            System.out.println(String.format("فایل %S وجود ندارد", todayZip.getAbsoluteFile()));
        	throw new SwitchBusinessException(String.format("فایل %S وجود ندارد", todayZip.getAbsoluteFile()));
        }
        
        if (!yesterdayZip.exists()) {
            System.out.println(String.format("فایل %S وجود ندارد", yesterdayZip.getAbsoluteFile()));
        	throw new SwitchBusinessException(String.format("فایل %S وجود ندارد", yesterdayZip.getAbsoluteFile()));
        }        

		PersianDateFormat persianDateFormat = new PersianDateFormat("yyyyMMdd");
        String todayStr = todayZip.getName().substring(ZIPFILE_NAME_HEADER.length(), ZIPFILE_NAME_HEADER.length() + 6);
        String yesterdayStr = yesterdayZip.getName().substring(ZIPFILE_NAME_HEADER.length(), ZIPFILE_NAME_HEADER.length() + 6);
        todayStr = Convert_yyMMdd_to_yyyyMMdd_PersianDateStr(todayStr);
        yesterdayStr = Convert_yyMMdd_to_yyyyMMdd_PersianDateStr(yesterdayStr);
		Date dToday = null;
		Date dYesterday = null;
		try {
			dToday = persianDateFormat.parse(todayStr);
			dYesterday = persianDateFormat.parse(yesterdayStr);
		} catch (ParseException e2) {
			
		}
		DateTime dtToday = new DateTime(dToday);
		DateTime dtYesterday = new DateTime(dYesterday);
		if (!dtYesterday.nextDay().equals(dtToday)){
			System.out.println("Input files incorrect or no sequential days");
			throw new SwitchBusinessException("فایلهای ورودی صحیح نیست یا شامل روزهای متوالی نمی باشد.");
		}
        

        GeneralDao.Instance.beginTransaction();
		if (Util.getMainClassName() != null && !Util.getMainClassName().contains("VaulsysWCMS")) {
			GlobalContext.getInstance().startup();
		}        
        ProcessContext.get().init();
        
        System.out.println("Processing file:" + todayZip.getName());
        try {
        	Long myBin = 123456L; //GlobalContext.getInstance().getMyInstitution().getBin(); //Raza commenting need to Fix
        	String repbalName =  "IRI/" + myBin + ".repbal_txt";
        	String repExtraName = "IRI/" + myBin + ".rep_extra_txt";
            ZipFile zipFile = new ZipFile(todayZip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            System.out.println("here1");
            InputStream repbalToday = null;
            InputStream extraToday = null;
            InputStream extraYesterday = null;
            while (entries.hasMoreElements()) {
            	ZipEntry entry = entries.nextElement();
                try{
                    if(entry.getName().endsWith(repbalName)){
                    	repbalToday = zipFile.getInputStream(entry);
                    }else if(entry.getName().endsWith(repExtraName)){
                    	extraToday = zipFile.getInputStream(entry);
                    }
                }catch(Throwable e){
                	System.err.println(e);
                    e.printStackTrace();
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
            if(repbalToday != null && extraToday != null && extraYesterday != null) {
            	result = ShetabDocumentService.issuePasargadShetabDocument(new BufferedReader(new InputStreamReader(repbalToday)),
            		new BufferedReader(new InputStreamReader(extraToday)), new BufferedReader(new InputStreamReader(extraYesterday)), "repbal-day:");
            } else if (repbalToday == null || extraToday == null) { 
            	logger.error(String.format("محتویات فایل %s صحیح نیست", todayZip.getAbsoluteFile()));
            	throw new SwitchBusinessException(String.format("محتویات فایل %s صحیح نیست", todayZip.getAbsoluteFile()));
            } else if (extraYesterday == null) {
            	logger.error(String.format("محتویات فایل %s صحیح نیست", yesterdayZip.getAbsoluteFile()));
            	throw new SwitchBusinessException(String.format("محتویات فایل %s صحیح نیست", yesterdayZip.getAbsoluteFile()));
            }
            System.out.println(result);
            System.out.println("Processing file:" + todayZip.getName());
        } catch (SwitchBusinessException e) {
        	throw e;
        } catch (Exception e1) {
                System.err.println(e1);
                e1.printStackTrace();
        } finally {
            GeneralDao.Instance.endTransaction();
        }
        if (result.toLowerCase().contains("duplicate"))
        	throw new SwitchBusinessException("فایل Repball ورودی قبلا اعمال شده است.");
        
       	return  String.format("شماره سند : %s", result);
	}
	
	private static String Convert_yyMMdd_to_yyyyMMdd_PersianDateStr(String persianDate){
		try{
			Integer year = Integer.valueOf(persianDate.substring(0,2));
			if (year > 50)
				return "13"+persianDate;
			else
				return "14"+persianDate;
			
		} catch(Exception e){
			return persianDate;
		}
	}
}

