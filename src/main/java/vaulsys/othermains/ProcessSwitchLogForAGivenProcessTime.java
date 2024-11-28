package vaulsys.othermains;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ProcessSwitchLogForAGivenProcessTime {
	public static void main(String[] args) {
		String strIn = "";
		String strThread = "";
		Map<String, String> lastThreadActivity = new HashMap<String, String>();
		int from;
		int to;
		long totalTime = 0;
		long totalCount = 0;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("e:/flow.2011-09-18_19-19-04"));
			while (reader.ready()) {
				if ((strIn = reader.readLine()).length() > 0) {
					if(strIn.contains("pool") && strIn.contains("-thread-")){
//						if(strIn.contains("producing protocol message ...")){
//						if(strIn.contains("referenceTransaction found:")){
						if(strIn.contains("Searching for reversal Message (BankId")){
//						if(strIn.contains("Message binder...")){
//						if(strIn.contains("getOutgoingIfxOrMessageEndpoint2")){
//						if(strIn.contains("ATMProcessor  - Message binder...")){
//						if(strIn.contains("AuthorizationComponent  - Try to authorize terminal")){
							from = strIn.indexOf("[");
							to = strIn.indexOf("]");
							strThread = strIn.substring(from+1, to);
							lastThreadActivity.put(strThread, strIn);
						}else{ 
							from = strIn.indexOf("[");
							to = strIn.indexOf("]");
							strThread = strIn.substring(from+1, to);
							if(lastThreadActivity.containsKey(strThread)){
//								if(strIn.contains("isNeedToSetSettleDate is starting...")){
//								if(strIn.contains("input msg of trx:") && strIn.contains("RQ from !SWITCH")){
//								if(strIn.contains("input msg of trx:")){
								if(strIn.contains("Try to get Lock of LifeCycle")){
//								if(strIn.contains("No Reversal request is found for Trx")){
//								if(strIn.contains("RRNForRequest set...")){
//								if(strIn.contains("after query findUniqueObject")){
//								if(strIn.contains("find incoming ifx of trx:")){
//								if(strIn.contains("Routing: # Found destiontions =")){
									long toTime = Long.parseLong(strIn.substring(0, from-1));
									String strBefore = lastThreadActivity.get(strThread);
									from = strBefore.indexOf("[");
									long fromTime = Long.parseLong(strBefore.substring(0, from-1));
									if((toTime-fromTime) > 100){
										System.out.println("Time: "+(toTime-fromTime)+"\t "+strIn);
										totalTime += (toTime-fromTime);
										totalCount++;
									}
									lastThreadActivity.remove(strThread);
								}else{
									lastThreadActivity.remove(strThread);								
								}
							}
						}
					}
				}
			}
			
			System.out.println("Avg spent time: "+(totalTime/totalCount));
//			List<String> values = new ArrayList<String>(lastThreadActivity.values());
//			String[] strValues = new String[values.size()];
//			values.toArray(strValues);
//			Arrays.sort(strValues, new Comparator<String>(){
//				@Override
//				public int compare(String f1, String f2) {
//					return f1.compareToIgnoreCase(f2);
//				}
//			});
//
//			
//			for(String str:strValues){
//				System.out.println(str);
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
