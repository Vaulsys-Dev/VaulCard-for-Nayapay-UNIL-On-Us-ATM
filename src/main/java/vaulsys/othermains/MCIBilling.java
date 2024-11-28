package vaulsys.othermains;

import vaulsys.util.StringFormat;
import vaulsys.wfe.GlobalContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import com.ghasemkiani.util.icu.PersianCalendar;
import com.ghasemkiani.util.icu.PersianDateFormat;

public class MCIBilling {
	private static final Logger logger = Logger.getLogger(MCIBilling.class);

	private static Time from, to;

	private static PersianCalendar persianCalendar = new PersianCalendar();
	private static PersianDateFormat persian_yyMMdd = new PersianDateFormat("yyMMdd");
	private static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat HHmmss = new SimpleDateFormat("HHmmss");
	private static SimpleDateFormat whole = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static void main(String[] args) throws Exception {
		Calendar cal = Calendar.getInstance();
		logger.info("###  M C I   B I L L I N G  > start at " + whole.format(cal.getTime()) + "  ###");
		

		String baseDir = "";
		if (args.length == 0) {
			System.err.println("Format: MCIBilling <base directory> [<start date in gregorian> <start hour>]");
			baseDir = "c:/out";
//			System.exit(1);
		}
		else
			baseDir = args[0];

		logger.info("Putting output in " + baseDir);

		Class.forName("oracle.jdbc.driver.OracleDriver");

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		int index = 0;

		if (args.length > 1) {
			String[] p = args[1].split("[/]");
			if (p.length==3) {
				try {
					cal.set(Calendar.YEAR, Integer.parseInt(p[0]));
					cal.set(Calendar.MONTH, Integer.parseInt(p[1]) - 1);
					cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(p[2]));
				} catch (NumberFormatException e) {
					logger.error("", e);
				}
			}
			
			if(args.length == 3) {
				int h = Integer.parseInt(args[2]);
				index = 4 * h;
				cal.set(Calendar.HOUR_OF_DAY, h);
			}
		}

		logger.info("Starting from: " + whole.format(cal.getTime()));

		while (true) {
			Calendar now = Calendar.getInstance();
			from = new Time(cal.getTimeInMillis());
			index ++;
			if(index == 97)
				index = 1;
			cal.add(Calendar.MINUTE, 15);
			if (cal.after(now))
				Thread.sleep(cal.getTimeInMillis() - now.getTimeInMillis() + 100000);
			to = new Time(cal.getTimeInMillis());
			persianCalendar.setTimeInMillis(from.getTime());
			String fileName = String.format("PSG%sMC%03d.091", persian_yyMMdd.format(persianCalendar), index);
			File file = new File(baseDir + "/" + fileName);
			if (!file.exists()) {
				Header header = new Header();
				StringBuilder builder = new StringBuilder();
//				Connection con = DriverManager.getConnection("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=100.0.1.116)(PORT=1521))(ADDRESS=(PROTOCOL=TCP)(HOST=100.0.1.117)(PORT=1521))(LOAD_BALANCE=YES))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=SWITCHDB)))",
				Connection con = DriverManager.getConnection("jdbc:oracle:thin:@100.0.1.114:1521:switchdb1",
						"mcibilling", "KWAHsdZBxa73q4_");
				int f = fanap(con, builder, header);
//				int n = negin(con, builder, header);
				int n = 0;
				file.getParentFile().mkdirs();
				if (builder.length() > 0) {
					FileWriter out = new FileWriter(file);
					out.append(header.toString()).append("\r\n").append(builder.toString());
					out.close();
				}
				con.close();
				logger.info(String.format("[%s]: time=[%s,%s), index=[%d], fanap=[%d], negin=[%d]",
						fileName, HHmmss.format(from), HHmmss.format(to), index, f, n));
			}
			else
				logger.info("[" + fileName + "] existed!");
		}
	}

	private static int negin(Connection con, StringBuilder builder, Header header) throws Exception {
		String branchCardCode = GlobalContext.getInstance().getMyInstitution().getBranchCardCode();
		String branchId = StringFormat.formatNew(6, StringFormat.JUST_RIGHT, branchCardCode, '0');
		
		PreparedStatement ps = con.prepareStatement("select TRPOSCCOD, TRTRACEDT, TRADDATA, PTRTRCNO, TRAMNT from nbill " +
				"where TRPRCOD=50 and TR87RSPCOD=0 and TRPOSCCOD<>59 and TRMTI=200 and (TRREVFLG is null or TRREVFLG<>1) " +
				"and TRTRACEDT>=? and TRTRACEDT<? and TRADDATA like '%0915_=%'");
		ps.setTime(1, from);
		ps.setTime(2, to);
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while (rs.next()) {
			builder.append(branchId);
			builder.append(String.format("%02d", rs.getInt("TRPOSCCOD"))); //terminal type
			persianCalendar.setTimeInMillis(rs.getDate("TRTRACEDT").getTime());
			builder.append(persian_yyMMdd.format(persianCalendar)); // recieved date, yyMMdd
			String[] billParts = rs.getString("TRADDATA").split("=");
			builder.append(String.format("%013d", Long.parseLong(billParts[0].substring(1)))); // billId
			builder.append(String.format("%013d", Long.parseLong(billParts[1].trim()))); // paymentId
			builder.append(String.format("%06d", rs.getInt("PTRTRCNO")));  // peygiri
			builder.append("\r\n");
			header.totalSize++;
			header.totalAmount += rs.getLong("TRAMNT");
			count++;
		}
		return count;
	}

	private static int fanap(Connection con, StringBuilder builder, Header header) throws Exception {
		PreparedStatement ps = con.prepareStatement(
				"select sdr.report from epay.settlement_data_report sdr inner join epay.settlement_data sd on sdr.stl_data=sd.id " +
				"where sd.clr_prof=? and sd.terminal=? and sdr.type=? " +
				"and (sd.settlement_date*1000000+sd.settlement_time)>=? " +
				"and (sd.settlement_date*1000000+sd.settlement_time)<? ");
		ps.setLong(1, 170604); // clr_prof
		ps.setLong(2, 200358); // MCI Terminal Code  
		ps.setInt(3, 1); // type
		ps.setLong(4, Integer.parseInt(yyyyMMdd.format(from))*1000000L + Integer.parseInt(HHmmss.format(from)));
		ps.setLong(5, Integer.parseInt(yyyyMMdd.format(to))*1000000L + Integer.parseInt(HHmmss.format(to)));
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while (rs.next()) {
			byte[] bytes = rs.getBytes(1);
			File fanapTempZipFile = new File("fanap_tmp"+System.currentTimeMillis()+".zip");
			FileOutputStream tmpzip = new FileOutputStream(fanapTempZipFile);
			tmpzip.write(bytes);
			tmpzip.close();
			ZipFile zipFile = new ZipFile(fanapTempZipFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				BufferedReader buffReader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)));
				header.setHeader(buffReader.readLine());
				String line;
				while ((line = buffReader.readLine()) != null) {
					builder.append(line).append("\r\n");
					count++;
				}
			}
			fanapTempZipFile.delete();
		}
		return count;
	}

	static class Header {
		String orgType_companyCode_myBankCode = "509157"; // len=1+3+2=6
		// dateTime len=6
		long totalAmount; // len=10
		long totalSize;	// len=8

		public void setHeader(String str) {
			orgType_companyCode_myBankCode = str.substring(0, 6);
			totalAmount = Long.parseLong(str.substring(12, 22)) * 1000;
			totalSize = Long.parseLong(str.substring(22));
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(orgType_companyCode_myBankCode);
			persianCalendar.setTimeInMillis(from.getTime());
			builder.append(persian_yyMMdd.format(persianCalendar));
			builder.append(String.format("%010d", totalAmount / 1000L));
			builder.append(String.format("%08d", totalSize));
			return builder.toString();
		}
	}
}
