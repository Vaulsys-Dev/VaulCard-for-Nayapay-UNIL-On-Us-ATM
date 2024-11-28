package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.ConfigUtil;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ProcessATMJournalForSanadPartial {
	public static final String CASH_REQUEST = "CASH REQUEST: ";
	public static final String TRANSACTION_END = "TRANSACTION END";
	public static final String CDM_ERROR = "CDM ERROR: 0000001D 20001823 00000000";
	public static final String CDM_ERROR2 = "CDM ERROR: 0000001D 20001819 00000000";
	public static final String CDM_ERROR3 = "CDM ERROR: 0000643A 00000000 0000FEC3";
	public static final String CDM_ERROR4 = "CDM ERROR: 0000001D 20001890 00000000";
	public static final String CARD_RETAINED = "CARD RETAINED";
	public static final String CARD_RETAINED2 = "IDCU ERROR: 00006434 00000042 00000000";
	public static final String CARD_RETAINED3 = "IDCU ERROR: 00000008 2800E00A 00000000";
	public static final String CASH_BEFORE_SOP = "CASH COUNTERS BEFORE SOP";
	public static final String CASH_AFTER_SOP = "CASH COUNTERS AFTER SOP";
	public static final String WITHDRAWAL = "Withdrawal:";
	public static final String PARTIAL_WITHDRAWAL = "Partial Widthdrawal:";

	public static void main(String[] args) {

		String path = "C:/journals/225569-SardarJangal1";
		String terminal = "225569";
//		BigInteger baseDisagreement = new BigInteger("3049230000"); //Motahari
//		BigInteger baseDisagreement = new BigInteger("2968740000");
		BigInteger baseDisagreement = new BigInteger("0");
//		Long configId = null;
		Boolean isTenThousConf = false;
		String startDate = "20110203";
		DateTime actualStartDate = null;
		String endDate = "20110205";
		if (args.length < 1) {
//			path = "C:/tavon-journals/313-100060-Mirdamad";
			path = "C:/journals/225569-SardarJangal1";
			terminal = "225569";
		} else {
			path = args[0];
			terminal = args[1];
			startDate = args[2];
			endDate = args[3];
			isTenThousConf = Boolean.valueOf(args[4]);
//			configId = Long.valueOf(args[4]);
			if(args.length > 5)
				baseDisagreement = new BigInteger(args[5]);
		}

		if (!path.contains(terminal)) {
			System.err.println("Terminal does not match specified path....");
			System.exit(0);
		}
		
		File folder = new File(path);
		File[] files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jrn");
			}
		});
		
		if (files == null) {
			System.exit(0);
			return;
		}
		
		Arrays.sort(files, new Comparator<File>(){
			@Override
			public int compare(File f1, File f2) {
				return f1.getName().compareToIgnoreCase(f2.getName());
			}
		});
		
		String strIn = "";
		String lastWithdrawal = "";
		String lastCashRequest = "";

		int totalC50Dispensed = 0;
		int totalC5Dispensed = 0;
		int totalC2Dispensed = 0;
		int totalC1Dispensed = 0;

		int c50Dispensed = 0;
		int c5Dispensed = 0;
		int c2Dispensed = 0;
		int c1Dispensed = 0;

		boolean errorFound = false;
		File file;
		
		int counter = 0;
		
		try {
			// Iterate on all journal files
			for (;counter<files.length;) {
				file= files[counter];
				StringTokenizer tokenizer = new StringTokenizer(file.getName(), ".");
				
				String temp = tokenizer.nextToken().trim();
				int a = Integer.parseInt(temp);
				if(a >= Integer.parseInt(startDate) && a <= Integer.parseInt(endDate) ){
					
					
					System.out.println("========================");
					System.out.println("Processing journal:" + file.getName());
					BufferedReader reader = new BufferedReader(new FileReader(file));
					
					c50Dispensed = 0;
					c5Dispensed = 0;
					c2Dispensed = 0;
					c1Dispensed = 0;
					
					while (reader.ready()) {
						if ((strIn = reader.readLine()).length() > 0) {
							if (strIn.contains(TRANSACTION_END)) {
								lastCashRequest = null;
							}
							if (strIn.contains(CASH_REQUEST)) {
								int index = strIn.indexOf(CASH_REQUEST);
								String strCashRequest = strIn.substring(index + CASH_REQUEST.length());
								lastCashRequest = strCashRequest;
								
								c50Dispensed += Integer.parseInt(strCashRequest.substring(0, 2));
								c5Dispensed += Integer.parseInt(strCashRequest.substring(2, 4));
								c2Dispensed += Integer.parseInt(strCashRequest.substring(4, 6));
								c1Dispensed += Integer.parseInt(strCashRequest.substring(6, 8));
							}
							if ((strIn.contains(CDM_ERROR) || strIn.contains(CDM_ERROR2) || strIn.contains(CDM_ERROR3) || strIn.contains(CDM_ERROR4) || 
									strIn.contains(CARD_RETAINED) || strIn.contains(CARD_RETAINED2) || strIn.contains(CARD_RETAINED3)) &&
									lastCashRequest != null) {
								// decrease last dispensed notes because of error in cdm...
								c50Dispensed -= Integer.parseInt(lastCashRequest.substring(0, 2));
								c5Dispensed -= Integer.parseInt(lastCashRequest.substring(2, 4));
								c2Dispensed -= Integer.parseInt(lastCashRequest.substring(4, 6));
								c1Dispensed -= Integer.parseInt(lastCashRequest.substring(6, 8));
								lastCashRequest = null;
							}
							if (strIn.contains(WITHDRAWAL) || strIn.contains(PARTIAL_WITHDRAWAL)) {
								lastWithdrawal = strIn;
								if(actualStartDate == null){
									int index = lastWithdrawal.indexOf("TIME:");
									lastWithdrawal = lastWithdrawal.substring(index + "TIME:".length());
									String tmpActualStartDate = lastWithdrawal.trim();
									Date dt = MyDateFormatNew.parse("yyyy/MM/dd, HH:mm:ss", tmpActualStartDate);
									actualStartDate = new DateTime(dt);
								}
								if(errorFound){								
									totalC50Dispensed += c50Dispensed;
									totalC5Dispensed += c5Dispensed;
									totalC2Dispensed += c2Dispensed;
									totalC1Dispensed += c1Dispensed;
									
									boolean err = executeSettlementQuery(lastWithdrawal, totalC50Dispensed, totalC5Dispensed, totalC2Dispensed, totalC1Dispensed, terminal, baseDisagreement, isTenThousConf, actualStartDate, endDate);
									
									if(err)
										System.exit(0);
									
									totalC50Dispensed -= c50Dispensed;
									totalC5Dispensed -= c5Dispensed;
									totalC2Dispensed -= c2Dispensed;
									totalC1Dispensed -= c1Dispensed;								
								}
							}
						}
					}
					
					totalC50Dispensed += c50Dispensed;
					totalC5Dispensed += c5Dispensed;
					totalC2Dispensed += c2Dispensed;
					totalC1Dispensed += c1Dispensed;
					
					if(Util.hasText(lastWithdrawal)){
						errorFound = executeSettlementQuery(lastWithdrawal, totalC50Dispensed, totalC5Dispensed, totalC2Dispensed, totalC1Dispensed, terminal, baseDisagreement, isTenThousConf, actualStartDate, endDate);
					}
					
					if(errorFound){
						totalC50Dispensed -= c50Dispensed;
						totalC5Dispensed -= c5Dispensed;
						totalC2Dispensed -= c2Dispensed;
						totalC1Dispensed -= c1Dispensed;	
					}else{
						counter++;
//						if(Util.hasText(lastWithdrawal)){
//							int index = lastWithdrawal.indexOf("TIME:");
//							lastWithdrawal = lastWithdrawal.substring(index + "TIME:".length());
//							String tmpActualStartDate = lastWithdrawal.trim();
//							Date dt = MyDateFormatNew.parse("yyyy/MM/dd, HH:mm:ss", tmpActualStartDate);
//							actualStartDate = new DateTime(dt);
//						}
					}
				}else{
					counter++;	
				}				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean executeSettlementQuery(String lastWithdrawal, 
			int totalC50Dispensed,
			int totalC5Dispensed,
			int totalC2Dispensed,
			int totalC1Dispensed,
			String terminal,
			BigInteger baseDisagreement,
			Boolean isTenThousConf,
			DateTime startDate,
			String endDate) throws ParseException{
		System.out.println("Last withdrawal:");
		System.out.println(lastWithdrawal);
		System.out.println("Dispensed: ");
		
		
		if(isTenThousConf != null && isTenThousConf){
			System.out.println("500000: " + totalC50Dispensed + " \t" + "100000: " + totalC5Dispensed + " \t" + "50000: " + totalC2Dispensed + " \t" + "20000: "
					+ totalC1Dispensed + " \t" );
		}
		if(isTenThousConf != null && !isTenThousConf){
			System.out.println("500000: " + totalC50Dispensed + " \t" + "50000: " + totalC5Dispensed + " \t" + "20000: "
					+ totalC2Dispensed + " \t" + "10000: " + totalC1Dispensed + " \t");
		}

		if(isTenThousConf ==  null && isTenThousendConfig(terminal)){
			System.out.println("500000: " + totalC50Dispensed + " \t" + "100000: " + totalC5Dispensed + " \t" + "50000: " + totalC2Dispensed + " \t" + "20000: "
					+ totalC1Dispensed + " \t" );
		}
		if(isTenThousConf == null && !isTenThousendConfig(terminal)){
			System.out.println("500000: " + totalC50Dispensed + " \t" + "50000: " + totalC5Dispensed + " \t" + "20000: "
					+ totalC2Dispensed + " \t" + "10000: " + totalC1Dispensed + " \t");
		}
		BigInteger total = new BigInteger((500000L * totalC50Dispensed) + "");
		
		if((isTenThousConf != null && isTenThousConf) || (isTenThousConf == null && isTenThousendConfig(terminal))){
			total = total.add(new BigInteger((100000L * totalC5Dispensed) + ""));
			total = total.add(new BigInteger((50000L * totalC2Dispensed) + ""));
			total = total.add(new BigInteger((20000L * totalC1Dispensed) + ""));
		}else{
			total = total.add(new BigInteger((50000L * totalC5Dispensed) + ""));
			total = total.add(new BigInteger((20000L * totalC2Dispensed) + ""));
			total = total.add(new BigInteger((10000L * totalC1Dispensed) + ""));
		}
		
//		if(isTenThousConf != null && isTenThousConf)
//			total = total.add(new BigInteger((50000L * totalC2Dispensed) + ""));
//		else
//			total = total.add(new BigInteger((20000L * totalC2Dispensed) + ""));
//		
//		if(isTenThousConf == null && isTenThousendConfig(terminal))
//			total = total.add(new BigInteger((100000L * totalC1Dispensed) + ""));
//		if(isTenThousConf == null && !isTenThousendConfig(terminal))
//			total = total.add(new BigInteger((10000L * totalC1Dispensed) + ""));
		
		System.out.println("Total: \t" + total);

		if(lastWithdrawal.contains(WITHDRAWAL)){
			lastWithdrawal = lastWithdrawal.substring(WITHDRAWAL.length());
		}else if(lastWithdrawal.contains(PARTIAL_WITHDRAWAL)){
			lastWithdrawal = lastWithdrawal.substring(PARTIAL_WITHDRAWAL.length());
		}
		int index = lastWithdrawal.indexOf("REF:");
		Long amount = Long.parseLong(lastWithdrawal.substring(0, index).trim());

		lastWithdrawal = lastWithdrawal.substring(index + "REF:".length());
		index = lastWithdrawal.indexOf("PAN:");
		String ref = lastWithdrawal.substring(0, index).trim();

		lastWithdrawal = lastWithdrawal.substring(index + "PAN:".length());
		index = lastWithdrawal.indexOf("TIME:");
		String pan = lastWithdrawal.substring(0, index).trim();

		lastWithdrawal = lastWithdrawal.substring(index + "TIME:".length());
		String time = lastWithdrawal.trim();
		Date dt = MyDateFormatNew.parse("yyyy/MM/dd, HH:mm:ss", time);
		DateTime recievedDt = new DateTime(dt);
		
//		Date st = MyDateFormatNew.parse("yyyy/MM/dd", startDate);
//		DateTime r= new DateTime(MyDateFormatNew.parse("yyyyMMddHHmmss", startDate+"000000"));
		
		GeneralDao.Instance.beginTransaction();
		List result = GeneralDao.Instance.executeSqlQuery("select /*+ index (i idx_ifx_term_recvdt_cix) */ sum(s.amount) " 
				+ "from "+ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".ifx i "
				+ "inner join "+ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".trx_transaxion t on t.id=i.trx "
				+ "inner join "+ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA)+".trx_flg_settlement s on s.id= t.src_stl_flg " 
				+ "where "
				+ "i.dummycol in (0,1,2,3,4,5,6,7,8,9) and "
				+ "i.direction = 1 and " 
				+ "i.request = 0 and "
				+ "s.acc_state = 2 and "
				+ "i.ifx_type=107 and " 
				+ "i.trntype=2 and " 
				+ "i.received_dt >= "+startDate.getDateTimeLong() + " and "
				+ "i.received_dt <= "+recievedDt.getDateTimeLong()+" and "
				+ "i.terminal = "+terminal );
		GeneralDao.Instance.rollback();
		System.out.println("Query: \t" + result.get(0));

		System.out.println("Disagreement: "+new BigInteger(result.get(0).toString()).subtract(total).subtract(baseDisagreement));
		
		if(new BigInteger(result.get(0).toString()).subtract(total).subtract(baseDisagreement).intValue() != 0)
			return true;
		
		return false;
	}
	
	public static boolean isTenThousendConfig(String terminal){
		GeneralDao.Instance.beginTransaction();
		String st= "select config.id from ATMConfiguration config where "+
					"config.name like '%ده هزار تومانی%' or config.name like '%ده هزارتومانی%'";
		List<Long> atmconfigs = GeneralDao.Instance.find(st);
		
		String str= "select atm from ATMTerminal atm where "+
					" atm.code = :terminal";
		
		Map<String, Object> param= new HashMap<String, Object>();
		param.put("terminal", Long.valueOf(terminal));
		ATMTerminal atm = (ATMTerminal)GeneralDao.Instance.findObject(str, param);
		GeneralDao.Instance.endTransaction();

		for(int i=0;i<atmconfigs.size();i++){
			if(atm == null)
				return false;
			else{
				
				if(atm.getConfiguration().equals( atmconfigs.get(i)))
					return true;
			}
			
		}
		return false;
	}
}
