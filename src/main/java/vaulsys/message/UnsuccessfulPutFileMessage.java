package vaulsys.message;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UnsuccessfulPutFileMessage {
	
	private static final Logger logger = Logger.getLogger(UnsuccessfulPutFileMessage.class);
	
	private static final String MY_NAME = "Pasargad";
	
	private static List<String> NUMBERS = Arrays.asList("09125849557", "09126227331", "09379659158", "09126207143");
	//Mehdi Honarmand ,  Mehdi Torki, Kamelia MirKamali, Sahar Kia
	
	
	public static void process(String exception, String masterName){
		try {
			logger.info("Unsuccessful in putting file for " + masterName);
			try {
				String msg = String.format("Unsuccessful in putting file for " + masterName 
											+ "\nServer: " + MY_NAME 
											+ "\n%1$tH:%1$tM:%1$tS"
											+ "\nException: " + exception
											+  "\n*Fanap Monitoring*\n"
											, new Date());
				for(String num : NUMBERS)
					SwitchRestarter.sendSMS(num, msg);
			} catch (Exception e) {
				logger.error("Sending SMS: ", e);
			}
		} catch (Exception e) {
			logger.error("Unsuccessful put file  SMS Execution Problem: ", e);
		}
	}
}
