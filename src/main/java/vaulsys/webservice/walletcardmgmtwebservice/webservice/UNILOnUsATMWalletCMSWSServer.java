package vaulsys.webservice.walletcardmgmtwebservice.webservice;

//import com.sun.jersey.spi.container.servlet.ServletContainer;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import vaulsys.wfe.GlobalContext;

/**
 * Created by Raza on 02-Feb-18.
 */

// Created By Huzaifa: 11/08/2023: FW: NAP-P5-23 ==> [ Logging email ] ==> Segregation of ATM On Us Channels Bank - UBL & BAFL
public class UNILOnUsATMWalletCMSWSServer implements Runnable {
    Logger logger = Logger.getLogger(this.getClass());
    public static long id=0;
    //public static ExecutorService executor;// = MessageManager.threadPool;

    public UNILOnUsATMWalletCMSWSServer()
    {
        //executor = MessageManager.threadPool;
    }

    public void run() {
        try {
            Thread.sleep(7000);
            logger.info("Starting UNILOnUsATMWalletCMSWSServer....!");

            ServletHolder sh = new ServletHolder(ServletContainer.class);
            sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
            sh.setInitParameter(WalletCMSWsEntity.class.getName(),"com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider");
            sh.setInitParameter("com.sun.jersey.config.property.packages", "vaulsys.webservice.walletcardmgmtwebservice.resource");
            sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

            //Server server = new Server(GlobalContext.getInstance().getChannelbyId(ChannelCodes.WALLETMEZNBIOATM).getPort());
            //new Server(9005);
            QueuedThreadPool threadPool = new QueuedThreadPool(200, 8, 1000);
            Server server = new Server(threadPool);
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(GlobalContext.getInstance().getChannelbyId(ChannelCodes.WALLETUNILONUSATM).getPort());
            server.setConnectors(new Connector[]{connector});

            ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
            context.addServlet(sh, "/*");
            //server.setHandler(context); //Raza Adding
            server.start();
            server.join();
        }
        catch (Exception e)
        {
            System.out.println("Exception caught while starting WalletCMSWSServer");
            logger.error("Exception caught while starting WalletCMSWSServer");
            e.printStackTrace();
        }
    }
}
