package vaulsys.othermains;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.GeneralDao;
import vaulsys.util.MyDateFormatNew;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class ProcessATMJournalForSanad {
	public static final String CASH_REQUEST = "CASH REQUEST: ";
	public static final String TRANSACTION_START = "TRANSACTION START";
	public static final String CDM_ERROR = "CDM ERROR: 0000001D 20001823 00000000";
	public static final String CDM_ERROR2 = "CDM ERROR: 0000001D 20001819 00000000";
	public static final String CARD_RETAINED = "CARD RETAINED";
	public static final String CARD_RETAINED2 = "IDCU ERROR: 00006434 00000042 00000000";
	public static final String CASH_BEFORE_SOP = "CASH COUNTERS BEFORE SOP";
	public static final String CASH_AFTER_SOP = "CASH COUNTERS AFTER SOP";
	public static final String WITHDRAWAL = "Withdrawal:";

	public static void main(String[] args) {

		String path = "";
		String terminal = "215630";
//		BigInteger baseDisagreement = new BigInteger("751140000");
		BigInteger baseDisagreement = new BigInteger("0");

		if (args.length < 1) {
			path = "C:/journals/215630-esfehanHakimNezami";
		} else {
			path = args[0];
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

		String strIn = "";
		String lastWithdrawal = "";
		String lastCashRequest = "";

		int c50Dispensed = 0;
		int c5Dispensed = 0;
		int c2Dispensed = 0;
		int c1Dispensed = 0;

		try {
			// Iterate on all journal files
			for (File file : files) {
				System.out.println("Processing journal:" + file.getName());
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while (reader.ready()) {
					if ((strIn = reader.readLine()).length() > 0) {
						if (strIn.contains(TRANSACTION_START)) {
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
							// System.out.println("500000: "+c50+" "+strCashRequest.substring(0,
							// 2)+"\t"+
							// "50000: "+c5+" "+strCashRequest.substring(2,
							// 4)+"\t"+
							// "20000: "+c2+" "+strCashRequest.substring(4,
							// 6)+"\t"+
							// "10000: "+c1+" "+strCashRequest.substring(6,
							// 8)+"\t"
							// );
						}
						if ((strIn.contains(CDM_ERROR) || strIn.contains(CDM_ERROR2) || 
							 strIn.contains(CARD_RETAINED) || strIn.contains(CARD_RETAINED2)) &&
								lastCashRequest != null) {
							// decrease last dispensed notes because of error in cdm...
							c50Dispensed -= Integer.parseInt(lastCashRequest.substring(0, 2));
							c5Dispensed -= Integer.parseInt(lastCashRequest.substring(2, 4));
							c2Dispensed -= Integer.parseInt(lastCashRequest.substring(4, 6));
							c1Dispensed -= Integer.parseInt(lastCashRequest.substring(6, 8));
						}
						if (strIn.contains(WITHDRAWAL)) {
							lastWithdrawal = strIn;
						}
					}
				}
			}

			System.out.println("========================");
			System.out.println("Last withdrawal:");
			System.out.println(lastWithdrawal);
			System.out.println("Dispensed: ");
			System.out.println("500000: " + c50Dispensed + " \t" + "50000: " + c5Dispensed + " \t" + "20000: "
					+ c2Dispensed + " \t" + "10000: " + c1Dispensed + " \t");
			BigInteger total = new BigInteger((500000L * c50Dispensed) + "");
			total = total.add(new BigInteger((50000L * c5Dispensed) + ""));
			total = total.add(new BigInteger((20000L * c2Dispensed) + ""));
			total = total.add(new BigInteger((10000L * c1Dispensed) + ""));
			// 500000*c50Dispensed+50000*c5Dispensed+20000*c2Dispensed+10000*c1Dispensed);
			System.out.println("Total: \t" + total);

			// Withdrawal: 800000 REF:4155 PAN:6037691017587064 TIME:2010/05/15,
			// 23:55:09
			lastWithdrawal = lastWithdrawal.substring("Withdrawal:".length());
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
			Date dt = MyDateFormatNew.parseNew("yyyy/MM/dd, HH:mm:ss", time);
			DateTime recievedDt = new DateTime(dt);

			GeneralDao.Instance.beginTransaction();
			List result = GeneralDao.Instance.executeSqlQuery("select sum(s.amount) " 
					+ "from epay.ifx i "
					+ "inner join epay.trx_transaxion t on t.id=i.trx "
					+ "inner join epay.trx_flg_settlement s on s.id= t.src_stl_flg " 
					+ "where "
					+ "i.direction = 1 and " 
					+ "i.request = 0 and "
					+ "s.acc_state = 2 and "
					+ "i.ifx_type=107 and " 
					+ "(i.recieved_date = "+recievedDt.getDayDate().toString().replace("/", "")+" and i.recieved_time <= "+recievedDt.getDayTime().toString().replace(":", "")+" or i.recieved_date < "+recievedDt.getDayDate().toString().replace("/", "")+") and "
					+ "i.terminal = "+terminal );
			GeneralDao.Instance.rollback();
			System.out.println("Query: \t" + result.get(0));

			System.out.println("Disagreement: "+new BigInteger(result.get(0).toString()).subtract(total).subtract(baseDisagreement));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
