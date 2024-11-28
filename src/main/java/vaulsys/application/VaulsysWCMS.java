package vaulsys.application;

import vaulsys.clearing.settlement.BillPaymentSettlementServiceImpl;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

//import vaulsys.webservice.nayapaywebservice.webservice.WebServer;

public class VaulsysWCMS {

	public static Logger logger = Logger.getLogger(VaulsysWCMS.class);

    public static void main(String[] args) {


		//Raza for StandAlone Wallet_CMS start
		//will add things per requirement

		System.out.println("Starting up Vaulsys WCMS ...");
		logger.info("Starting up Vaulsys WCMS ...");
		BaseApp app = new Application();
		app.startup();
		app.run();
		long tm1 = System.currentTimeMillis();

		//m.rehman: commenting below as this will be manage through db
		/*
		logger.info("Starting WalletCMS Mgmt WebService");
		try {
			//WebServer.startNayaPayService();
			//System.out.println("ClassPath: " + System.getProperty("java.class.path"));

			Thread t = new Thread(new WalletCMSWSServer());
			t.setName("WalletCMSWSThread");
			t.setDaemon(false);
			t.start();

			//STDALJettyServer.start();
		}
		catch (Exception e)
		{
			System.out.println("Exception caught while starting WalletCMS Mgmt WebService");
			logger.info("Exception caught while starting WalletCMS Mgmt WebService");
			e.printStackTrace();
		}
		*/

		System.out.println("Startup in " + (System.currentTimeMillis() - tm1) / 1000. + " seconds.");
		logger.info("Startup in " + (System.currentTimeMillis() - tm1) / 1000. + " seconds.");

		//Raza for StandAlone Wallet_CMS end

		/*
		System.out.println("Starting up Fanap Switch ...");
		logger.info("Starting up Fanap Switch ...");
        long tm1 = System.currentTimeMillis();

        BaseApp app = new Application();
//        SwitchApplication.get().getGeneralDao();

        app.startup();
        app.run();

        System.out.println("Startup in " + (System.currentTimeMillis() - tm1) / 1000. + " seconds.");
		logger.info("Startup in " + (System.currentTimeMillis() - tm1) / 1000. + " seconds.");

		logger.info("Starting WalletCMS Mgmt WebService");
		try {
			//WebServer.startNayaPayService();
			//System.out.println("ClassPath: " + System.getProperty("java.class.path"));

			Thread t = new Thread(new WalletCMSWSServer());
			t.setName("WalletCMSWSThread");
			t.setDaemon(false);
			t.start();

			//STDALJettyServer.start();
		}
		catch (Exception e)
		{
			System.out.println("Exception caught while starting WalletCMS Mgmt WebService");
			logger.info("Exception caught while starting WalletCMS Mgmt WebService");
			e.printStackTrace();
		}*/

        boolean runInBg = false;
        if(args.length == 1 && args[0].equals("-bg")){
        	System.out.println("go background...");
			logger.info("go background...");
        	runInBg = true;
        }
        
        try {
        	//Mirkamali(Task131): Enable settle control panel
            /*try {
				RemoteMessageManager.get(ConfigUtil.getInteger(ConfigUtil.REMOTE_MANAGER_PORT)).startup();
			} catch (Exception e1) {
				System.err.println("RemoteMessageManager couldn't start: "+ e1);
				logger.error("RemoteMessageManager couldn't start: "+ e1.getMessage());
			}*/

            String CurLine = "";
            System.out.println("Please enter command ('quit', 'restart', 'gobackground'");
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(converter);

            while (!runInBg && !("quit".equals(CurLine))) {
                CurLine = in.readLine();
                //System.out.println("hehehe");
                
                if (CurLine == null)
                    continue;

                try {
					if (CurLine.equals("settle")) {
						System.out.println("Generating settle file.");
						//                    SwitchApplication.get().getMerchantSettlementService().settle(false);
						BillPaymentSettlementServiceImpl.Instance.settle(null, null, false, false, false);
						System.out.println("Settle file generated.");

					}
					else if (CurLine.equals("restart")) {
						CurLine = "";
						System.exit(22);
					} else if (CurLine.equals("gobackground")) {
						CurLine = "";
						System.exit(23);
//					} else if (CurLine.contains("ATM_command")) {
//						String mode = "";
//						if (CurLine.contains("in")){
//							mode = "in";
//						}else if (CurLine.contains("out"))
//							mode = "out";
//						Long ATMcode = Util.longValueOf(CurLine.substring(CurLine.indexOf("ATM_command ")
//								+ "ATM_command ".length()+mode.length()).trim());
//						if ("".equals(mode))
//							mode = "out";
//						
//						try {
//							ATMTerminal atm = TerminalService.findTerminal(ATMTerminal.class, ATMcode);
//							if (ATMConnectionStatus.CONNECTED.equals(atm.getConnection())) {
//								NDCNetworkToTerminalMsg message;
//								if ("in".equals(mode))
//									message = ATMTerminalService.generateSupplyCountersMessage(ATMcode);
//								else 
//									message = ATMTerminalService.generateGoOutOfServiceMessage(ATMcode);
//								
//								Channel channel = GlobalContext.getInstance().getChannel("channelNDCProcachInA");
//								byte[] binary = channel.getProtocol().getMapper().toBinary(message);
//								NetworkManager networkManager = NetworkManager.getInstance();
//								IoSession session = networkManager.getTerminalOpenConnection(atm.getIP());
//								session.write(binary);
//							} else {
//								CurLine = "";
//								System.err.println("ATM [" + ATMcode + "] is not connected!");
//							}
//						} catch (NotProducedProtocolToBinaryException e) {
//							CurLine = "";
//							System.out.println("Message cannot be sent to ATM [" + ATMcode + "] is not connected!");
//						}
//						CurLine = "";
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

	        //app.shutdown();
	        System.exit(0);
        }
    }
}
