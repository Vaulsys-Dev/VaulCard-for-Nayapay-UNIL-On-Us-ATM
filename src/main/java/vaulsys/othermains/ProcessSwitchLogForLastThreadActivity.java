package vaulsys.othermains;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessSwitchLogForLastThreadActivity {

	public static void main(String[] args) {
		String strIn = "";
		String strThread = "";
		Map<String, String> lastThreadActivity = new HashMap<String, String>();
		int from;
		int to;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("d:/flow2.bad"));
			while (reader.ready()) {
				if ((strIn = reader.readLine()).length() > 0) {
					if(strIn.contains("pool") && strIn.contains("-thread-")){
//					if(strIn.contains("pool") && strIn.contains("-thread-") && strIn.contains("Begin transaction on connection with sid=[")) {
						from = strIn.indexOf("[");
						to = strIn.indexOf("]");
						strThread = strIn.substring(from+1, to);
						lastThreadActivity.put(strThread, strIn);
					}
				}
			}
			
			List<String> values = new ArrayList<String>(lastThreadActivity.values());
			String[] strValues = new String[values.size()];
			values.toArray(strValues);
			Arrays.sort(strValues, new Comparator<String>(){
				@Override
				public int compare(String f1, String f2) {
					return f1.compareToIgnoreCase(f2);
				}
			});

			
			for(String str:strValues){
				System.out.println(str);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
