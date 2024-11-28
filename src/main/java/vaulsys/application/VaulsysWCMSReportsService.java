package vaulsys.application;

import vaulsys.clearing.settlement.BillPaymentSettlementServiceImpl;
import vaulsys.network.remote.RemoteMessageManager;
import vaulsys.util.ConfigUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class VaulsysWCMSReportsService {

    public static void main(String[] args) {
        System.out.println("Starting up Vaulsys WCMS Reports Service...");
        long tm1 = System.currentTimeMillis();

        BaseApp app = new ReportsServiceApplication();

        app.startup();
        app.run();

        System.out.println("Startup in " + (System.currentTimeMillis() - tm1) / 1000. + " seconds.");

        boolean runInBg = false;
        if(args.length == 1 && args[0].equals("-bg")){
        	System.out.println("go background...");
        	runInBg = true;
        }
        
        try {

            String CurLine = "";
            System.out.println("Please enter command ('quit', 'restart', 'settle', 'gobackground'): ");
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(converter);

            while (!runInBg && !("quit".equals(CurLine))) {
                CurLine = in.readLine();
                //System.out.println("hehehe");
                
                if (CurLine == null)
                    continue;

                try {
					if (CurLine.equals("restart")) {
						CurLine = "";
						System.exit(22);
					} else if (CurLine.equals("gobackground")) {
						CurLine = "";
						System.exit(23);
					} else if (!(CurLine.equals("quit"))) {
						System.out.println("You typed: \"" + CurLine + "\"");
					}
				} catch (Exception e) {
					CurLine = "";
					// TODO: handle exception
				}
            }

        } catch (Exception ex) {
        	ex.printStackTrace();
        }

        if(!runInBg) {
	        long tm2 = System.currentTimeMillis();
	        System.out.println("milliseconds to execute : " + (tm2 - tm1));
	
	        app.shutdown();
	        System.exit(0);
        }
    }
}
