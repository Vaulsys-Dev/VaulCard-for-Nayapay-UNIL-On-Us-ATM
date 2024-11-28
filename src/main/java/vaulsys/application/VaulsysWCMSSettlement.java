package vaulsys.application;

import vaulsys.clearing.base.ClearingProfile;
import vaulsys.network.remote.RemoteMessageManager;
import vaulsys.util.ConfigUtil;
import vaulsys.util.SettlementApplication;
import vaulsys.wfe.GlobalContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class VaulsysWCMSSettlement {

    public static void main(String[] args) {
        System.out.println("Starting up Vaulsys WCMS Accounting/Settlement version 1.0.0 ....");
        long tm1 = System.currentTimeMillis();

        GlobalContext.dbUserName = ConfigUtil.DB_USERNAME_SETTLE;
        GlobalContext.dbPasswored = ConfigUtil.DB_PASSWORD_SETTLE;

        BaseApp app = new AccountingSettlementApplication();
        SettlementApplication.get().getGeneralDao();

        app.startup();
        app.run();

        System.out.println("Startup in " + (System.currentTimeMillis() - tm1) / 1000. + " seconds.");

        boolean runInBg = false;
        if(args.length == 1 && args[0].equals("-bg")){
        	System.out.println("go background...");
        	runInBg = true;
        }
        
        try {
        	//Mirkamali(Task131): Enable settle control panel
        	 try {
 				RemoteMessageManager.get(ConfigUtil.getInteger(ConfigUtil.REMOTE_MANAGER_SETTLE_PORT)).startup();
 			} catch (Exception e1) {
 				System.err.println("RemoteMessageManager couldn't start: "+ e1);
 			}
        	
        	
            String CurLine = "";
            System.out.println("Please enter command ('quit', 'settle'): ");
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(converter);

            while (!runInBg && !("quit".equals(CurLine))) {
                CurLine = in.readLine();
                System.out.println("exiting");
                
                if (CurLine == null)
                    continue;

                try {
					if (!(CurLine.equals("quit"))) {
						System.out.println("You typed: \"" + CurLine + "\"");
					}
				} catch (Exception e) {
					CurLine = "";
				}
            }
        } catch (IOException ex) {
        }

        if(!runInBg) {
	        long tm2 = System.currentTimeMillis();
	        System.out.println("milliseconds to execute : " + (tm2 - tm1));
	
	        app.shutdown();
	        System.exit(0);
        }
    }
    

    private static List<ClearingProfile> getClearingProfile(Class clazz){
    	return SettlementApplication.get().getGeneralDao().find("from ClearingProfile c where c.settlementClass = '"+ clazz.getName()+"'", null);
    }
}
