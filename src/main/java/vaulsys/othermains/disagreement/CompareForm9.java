package vaulsys.othermains.disagreement;

import vaulsys.clearing.report.ShetabDisagreementService;
import vaulsys.clearing.report.ShetabReconciliationService;
import vaulsys.clearing.report.ShetabReportConstants;
import vaulsys.clearing.report.ShetabReportRecord;
import vaulsys.util.StringFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ghasemkiani.util.icu.PersianDateFormat;
import com.ibm.icu.util.StringTokenizer;

public class CompareForm9 {

	/**
	 * @param args
	 */
	private static final Logger logger = Logger.getLogger(CompareForm9.class);
	
	public static String compareFile(String path_fromShetab, String path_toShetab, String path, boolean isPSP) {
		// TODO Auto-generated method stub
		StringTokenizer tokenizer = new StringTokenizer(path_fromShetab,"-");
		tokenizer.nextToken();
		String pathRes = path+"/comparison-"+tokenizer.nextToken()+"-"+System.currentTimeMillis()+".txt";
		
//		File acq_toShetab_file = new File(path+"/"+acq_formToShetab_path);
//		File acq_fromShetab_file = new File(path+"/"+acq_formFromShetab_path);
		
		File acq_toShetab_file = new File(path_toShetab);
		File acq_fromShetab_file = new File(path_fromShetab);
		
		File acq_disagreement_file = new File(pathRes);
		if(!acq_disagreement_file.exists()){
			try {
				acq_disagreement_file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
		
		BufferedWriter acq_disagreement_bw = null;
		try {
			acq_disagreement_bw = new BufferedWriter(new FileWriter(acq_disagreement_file));
		} catch (IOException e2) {
			e2.printStackTrace();
			logger.error(e2);
		}
		
		String acq_disagreement_str = "No error found";
		try {
			acq_disagreement_str = ShetabDisagreementService.finDisagreement(new BufferedReader(new FileReader(acq_toShetab_file)), new BufferedReader(new FileReader(acq_fromShetab_file)), isPSP, false);
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error(e1);
		}
		
		try {
			acq_disagreement_bw.append(acq_disagreement_str);
			acq_disagreement_bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return pathRes;
	}
	
	public static String generateReport9File(List<ShetabReportRecord> records, boolean isPSP){
		StringBuilder report9th = new StringBuilder();
		PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMddHHmmss");
		for(ShetabReportRecord record:records){
			report9th.append(StringFormat.formatNew(2, StringFormat.JUST_LEFT,
					ShetabReportConstants.ifxTypeToShetabTrnType.get(record.type), ' ')).append('|');
			report9th.append(dateFormatPers.format(record.origDt.toDate())).append('|');
			report9th.append(StringFormat.formatNew(6, StringFormat.JUST_RIGHT, record.trnSeqCntr, '0')).append('|');
			report9th.append(StringFormat.formatNew(19, StringFormat.JUST_LEFT, record.appPan, ' ')).append('|');
			report9th.append(StringFormat.formatNew(9, StringFormat.JUST_LEFT, record.destBankId, ' ')).append('|');
			report9th.append(StringFormat.formatNew(8, StringFormat.JUST_RIGHT, record.terminalId, '0')).append('|');
			report9th.append(StringFormat.formatNew(12, StringFormat.JUST_RIGHT, record.amount, '0')).append('|');
			report9th.append(StringFormat.formatNew(3, StringFormat.JUST_RIGHT,
					ShetabReportConstants.TerminalTypeToAcqReportTermType.get(record.terminalType)+ "", '0')).append('|');
			if(isPSP)
				report9th.append(StringFormat.formatNew(12, StringFormat.JUST_LEFT, record.merchantId, '0')).append('|');
			report9th.append("\r\n");
		}
		return report9th.toString();
	}
}
