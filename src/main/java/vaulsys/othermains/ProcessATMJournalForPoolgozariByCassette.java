package vaulsys.othermains;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;

public class ProcessATMJournalForPoolgozariByCassette {
	public static final String CASH_REQUEST = "CASH REQUEST: ";
	public static final String CASH_PRESENTED = "CASH PRESENTED";
	public static final String CASH_TAKEN = "CASH TAKEN";
	public static final String CASH = "CASH ";
	public static final String CASH_BEFORE_SOP = "CASH COUNTERS BEFORE SOP";
	public static final String CASH_AFTER_SOP = "CASH COUNTERS AFTER SOP";
	public static final String CARD_RETAINED = "CARD RETAINED";
	public static final String CARD_RETAINED2 = "IDCU ERROR: 00006434 00000042 00000000";
	public static final String CARD_RETAINED3 = "IDCU ERROR: 00000008 2800E00A 00000000";

	public static final String TRANSACTION_END = "TRANSACTION END";
	public static final String CDM_ERROR = "CDM ERROR: 0000001D 20001823 00000000";
	public static final String CDM_ERROR2 = "CDM ERROR: 0000001D 20001819 00000000";
	public static final String CDM_ERROR3 = "CDM ERROR: 0000643A 00000000 0000FEC3";
	public static final String CDM_ERROR4 = "CDM ERROR: 0000001D 20001890 00000000";

	
	public static void main(String[] args) {
		String path = "";
		if(args.length < 1){
//			path = "C:/journals/216536-shirazShomali";
			path = "C:/tavon-journals/313-100060-Mirdamad";
		}else{
			path = args[0];
		}
		
		File folder = new File(path);
		File[] files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name){
				return name.endsWith(".jrn");
			}
		});
		
		if(files == null){
			System.exit(0);
			return;
		}
		
		String lastCashRequest = "";
		String lastCashRequest2 = "";

		String strIn = "";
		int c50Dispensed = 0;
		int c5Dispensed = 0;
		int c2Dispensed = 0;
		int c1Dispensed = 0;
		
		int[] cassette = new int[6];
		
		int oldSOP50 = 0;
		int oldSOP5 = 0;
		int oldSOP2 = 0;
		int oldSOP1 = 0;

		int newSOP50 = 0;
		int newSOP5 = 0;
		int newSOP2 = 0;
		int newSOP1 = 0;

		int[] newSOPCassette = new int[6];
		int[] oldSOPCassette = new int[6];

		
		int[] rejects = new int[6];

		int rejects50 = 0;
		int rejects5 = 0;
		int rejects2 = 0;
		int rejects1 = 0;

		try {
			//Iterate on all journal files
			for(File file:files){	
				System.out.println("Processing journal:"+file.getName());
				BufferedReader reader = new BufferedReader(new FileReader(file));
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

						if (strIn.contains(CASH) && 
								!strIn.contains(CASH_REQUEST) && 
								!strIn.contains(CASH_PRESENTED) && 
								!strIn.contains(CASH_TAKEN) &&
								!strIn.contains(CASH_BEFORE_SOP) &&
								!strIn.contains(CASH_AFTER_SOP)) {
							int index = strIn.indexOf(CASH);
							String strCashRequest = strIn.substring(index + CASH.length());
							lastCashRequest2 = strCashRequest;
							
							for(int i=0; i<6; i++){
								cassette[i] += parseCassetteDispensedNotes(lastCashRequest2, i+1);
							}
						}
						if ((strIn.contains(CDM_ERROR) || strIn.contains(CDM_ERROR2) || strIn.contains(CDM_ERROR3) || strIn.contains(CDM_ERROR4) || 
							 strIn.contains(CARD_RETAINED) || strIn.contains(CARD_RETAINED2) || strIn.contains(CARD_RETAINED3)) &&
							lastCashRequest != null) {
							System.out.println(strIn);
							// decrease last dispensed notes because of error in cdm...
							c50Dispensed -= Integer.parseInt(lastCashRequest.substring(0, 2));
							c5Dispensed -= Integer.parseInt(lastCashRequest.substring(2, 4));
							c2Dispensed -= Integer.parseInt(lastCashRequest.substring(4, 6));
							c1Dispensed -= Integer.parseInt(lastCashRequest.substring(6, 8));
							
							for(int i=0; i<6; i++){
								cassette[i] -= parseCassetteDispensedNotes(lastCashRequest2, i+1);
							}

							lastCashRequest = null;
							lastCashRequest2 = null;
						}
						
						if(strIn.contains(CASH_BEFORE_SOP)){
							String strLog = strIn + "\r\n";
							strIn = reader.readLine();
							newSOP50 = 0;
							newSOP5 = 0;
							newSOP2 = 0;
							newSOP1 = 0;

							for(int i=0; i<6; i++){
								newSOPCassette[i] = 0;
							}
							
							String strCash = "";
							int index;

							for(int i=0; i<6; i++){
								strCash = "";
								if(strIn.contains("500000") || strIn.contains("500 ")){								
									index = strIn.indexOf("500000");
									if(index == -1){
										index = strIn.indexOf("500");
										strCash = strIn.substring(index+"500".length()).replace('*', ' ').trim();
									}else{
										strCash = strIn.substring(index+"500000".length()).replace('*', ' ').trim();
									}
									newSOP50 += Integer.parseInt(strCash);
								}else if(strIn.contains("50000") || strIn.contains("50 ")){
									index = strIn.indexOf("50000");
									if(index == -1){
										index = strIn.indexOf("50");
										strCash = strIn.substring(index+"50".length()).replace('*', ' ').trim();
									}else{
										strCash = strIn.substring(index+"50000".length()).replace('*', ' ').trim();
									}
									newSOP5 += Integer.parseInt(strCash);
								}else if(strIn.contains("20000") || strIn.contains("20 ")){
									index = strIn.indexOf("20000");
									if(index == -1){
										index = strIn.indexOf("20");
										strCash = strIn.substring(index+"20".length()).replace('*', ' ').trim();
									}else{
										strCash = strIn.substring(index+"20000".length()).replace('*', ' ').trim();
									}
									newSOP2 += Integer.parseInt(strCash);
								}else if(strIn.contains("10000") || strIn.contains("10 ")){
									index = strIn.indexOf("10000");
									if(index == -1){
										index = strIn.indexOf("10");
										strCash = strIn.substring(index+"10".length()).replace('*', ' ').trim();
									}else{
										strCash = strIn.substring(index+"10000".length()).replace('*', ' ').trim();
									}
									newSOP1 += Integer.parseInt(strCash);
								}else if(strIn.contains(CASH_AFTER_SOP)){
									break;
								}else{
									System.out.println("Not expected phrase: "+strIn);
								}
								if(strCash != ""){
									newSOPCassette[i] = Integer.parseInt(strCash);
								}
								strIn = reader.readLine();
							}
							
							System.out.println("Dispensed("+(500000*c50Dispensed+50000*c5Dispensed+20000*c2Dispensed+10000*c1Dispensed)+"): 500000: "+c50Dispensed+"\t50000:"+c5Dispensed+"\t20000:"+c2Dispensed+"\t10000:"+c1Dispensed);
							
							for(int i=0; i<6; i++){
								System.out.print("Cassette"+(i+1)+":"+cassette[i]+"\t");
							}
							System.out.println("\r\n");
							
							for(int i=0; i<6; i++){
								if(oldSOPCassette[i] != 0 && oldSOPCassette[i]-newSOPCassette[i] != cassette[i]){
									rejects[i] += (oldSOPCassette[i]-newSOPCassette[i]-cassette[i]);
									System.out.println(strLog + "Rejects"+(i+1)+": "+rejects[i]+"\told:"+oldSOPCassette[i]+"\tnew:"+newSOPCassette[i]+"\tdispensed:"+cassette[i]);
									strLog = "";
								}								
							}
							
							if(oldSOP50 != 0 && oldSOP50-newSOP50 != c50Dispensed){
								rejects50 += (oldSOP50-newSOP50-c50Dispensed);
								System.out.println(strLog + "500000: "+rejects50+"\told:"+oldSOP50+"\tnew:"+newSOP50+"\tdispensed:"+c50Dispensed);
								strLog = "";
							}
							if(oldSOP5 != 0 && oldSOP5-newSOP5 != c5Dispensed){
								rejects5 += (oldSOP5-newSOP5-c5Dispensed);
								System.out.println(strLog + "50000:"+rejects5+"\told:"+oldSOP5+"\tnew:"+newSOP5+"\tdispensed:"+c5Dispensed);
								strLog = "";
							}
							if(oldSOP2 != 0 && oldSOP2-newSOP2 != c2Dispensed){
								rejects2 += (oldSOP2-newSOP2-c2Dispensed);
								System.out.println(strLog + "20000:"+rejects2+"\told:"+oldSOP2+"\tnew:"+newSOP2+"\tdispensed:"+c2Dispensed);
								strLog = "";
							}
							if(oldSOP1 != 0 && oldSOP1-newSOP1 != c1Dispensed){
								rejects1 += (oldSOP1-newSOP1-c1Dispensed);
								System.out.println(strLog + "10000:"+rejects1+"\told:"+oldSOP1+"\tnew:"+newSOP1+"\tdispensed:"+c1Dispensed);
								strLog = "";
							}

							for(int i=0; i<6; i++){
								oldSOPCassette[i] = 0;
							}

							oldSOP50 = 0;
							oldSOP5 = 0;
							oldSOP2 = 0;
							oldSOP1 = 0;

							strIn = reader.readLine();
							for(int i=0; i<6; i++){
								strCash = "";
								if(strIn.contains("500000") || strIn.contains("500 ")){										
									index = strIn.indexOf("500000");
									if(index == -1){
										index = strIn.indexOf("500");
										strCash = strIn.substring(index+"500".length()).replace('*', ' ').trim();
									}else{
										strCash = strIn.substring(index+"500000".length()).replace('*', ' ').trim();
									}
									oldSOP50 += Integer.parseInt(strCash);
								}else if(strIn.contains("50000") || strIn.contains("50 ")){
									index = strIn.indexOf("50000");
									if(index == -1){
										index = strIn.indexOf("50");
										strCash = strIn.substring(index+"50".length()).replace('*', ' ').trim();
									}else{
										strCash = strIn.substring(index+"50000".length()).replace('*', ' ').trim();
									}
									oldSOP5 += Integer.parseInt(strCash);
								}else if(strIn.contains("20000") || strIn.contains("20 ")){
									index = strIn.indexOf("20000");
									if(index == -1){
										index = strIn.indexOf("20");
										strCash = strIn.substring(index+"20".length()).replace('*', ' ').trim();
									}else{
										strCash = strIn.substring(index+"20000".length()).replace('*', ' ').trim();
									}
									oldSOP2 += Integer.parseInt(strCash);
								}else if(strIn.contains("10000") || strIn.contains("10 ")){
									index = strIn.indexOf("10000");
									if(index == -1){
										index = strIn.indexOf("10");
										strCash = strIn.substring(index+"10".length()).replace('*', ' ').trim();
									}else{
										strCash = strIn.substring(index+"10000".length()).replace('*', ' ').trim();
									}
									oldSOP1 += Integer.parseInt(strCash);
								}else{
									break;
								}
								
								if(strCash != ""){
									oldSOPCassette[i] = Integer.parseInt(strCash);
								}
								strIn = reader.readLine();
							}
							if(oldSOP50 != newSOP50 || oldSOP5 != newSOP5 || oldSOP2 != newSOP2 || oldSOP1 != newSOP1){
								System.out.println(strLog + "Poolgozari: "+
										"\r\n500000 Old: "+newSOP50 + "\tNew: "+oldSOP50 + 
										"\r\n50000 Old: "+newSOP5 + "\tNew: "+oldSOP5 +
										"\r\n20000 Old: "+newSOP2 + "\tNew: "+oldSOP2 +
										"\r\n10000 Old: "+newSOP1 + "\tNew: "+oldSOP1 
										);
								System.out.println(strLog + "Sanad: "+
								"\r\n500000 "+(oldSOP50-(newSOP50+rejects50)) + 
								"\r\n50000 "+(oldSOP5-(newSOP5+rejects5)) + 
								"\r\n20000 "+(oldSOP2-(newSOP2+rejects2)) + 
								"\r\n10000 "+(oldSOP1-(newSOP1+rejects1)) +
								"\r\nTotal: "+(
										(oldSOP50-(newSOP50+rejects50))*500000+
										(oldSOP5-(newSOP5+rejects5))*50000 + 
										(oldSOP2-(newSOP2+rejects2))*20000 + 
										(oldSOP1-(newSOP1+rejects1))*10000)
								);


								System.out.println("Poolgozari: ");
								for(int i=0; i<6; i++){
									System.out.println("Cassette" +(i+1)+": Old: "+newSOPCassette[i] + "\tNew: "+oldSOPCassette[i]); 
								}

								System.out.println("Sanad: ");
								for(int i=0; i<6; i++){
									System.out.println("Cassette" +(i+1)+": "+(oldSOPCassette[i]-(newSOPCassette[i]+rejects[i])));
									rejects[i] = 0;
								}

								rejects50 = 0;
								rejects5 = 0;
								rejects2 = 0;
								rejects1 = 0;

								strLog = "";
							}
								
							
							for(int i=0; i<6; i++){
								cassette[i] = 0;
							}
							
							c50Dispensed = 0;
							c5Dispensed = 0;
							c2Dispensed = 0;
							c1Dispensed = 0;
						}
					}
				}
			}
			
//				System.out.println("Rejects: "+
//						"\r\n500000: "+rejects50 +
//						"\r\n50000: "+rejects5 +
//						"\r\n20000: "+rejects2 +
//						"\r\n10000: "+rejects1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int parseCassetteDispensedNotes(String strCashRequest, int cassette){
		int index = 0;
		int index2 = 0;
		
		index = strCashRequest.indexOf(cassette+",");
		if(index > 0){
			index2 = strCashRequest.indexOf(";", index);
			return Integer.parseInt(strCashRequest.substring(index+2, index2));
		}
		
		return 0;
	}
}
