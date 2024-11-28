package vaulsys.application;

import vaulsys.config.ConfigurationManager;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.message.MessageManager;
import vaulsys.network.ATMSessionManager;
import vaulsys.network.NetworkManager;
import vaulsys.persistence.GeneralDao;
import vaulsys.security.hsm.base.HSMNetworkManager;
import vaulsys.util.SwitchApplication;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.process.SwitchThreadPool;

import org.apache.log4j.Logger;

public class Application extends BaseApp {
    transient Logger logger = Logger.getLogger(Application.class);
    final String version = "1.0.0";

    @Override
    public void startup() {
        GeneralDao.Instance.beginTransaction();

        try {
            //JobServiceQuartz.init(JobServiceQuartz.SWITCH_CONFIG);

            SwitchApplication.get().startup();

            logger.info("Starting up ConfigurationManager... please wait");
            ConfigurationManager.getInstance().startup();

            logger.info("Starting up GlobalContext... please wait");
            GlobalContext.getInstance().startup();

            logger.info("Starting up ThreadPool... please wait");
            ConfigurationManager.getInstance().loadConfig(SwitchThreadPool.class);

            logger.info("Starting up MessageManager... please wait");
            MessageManager.getInstance().startup();

            ATMSessionManager.get().startup();

            logger.info("Starting up NetworkManager... please wait");
            NetworkManager.getInstance().startup();

            //m.rehman: starting hsm network
            logger.info("Starting up HSMNetworkManager... please wait");
            HSMNetworkManager.getInstance().startup();

            //logger.info("Starting up Scheduler... please wait");
            //JobServiceQuartz.start();

            GeneralDao.Instance.endTransaction();
        } catch (Exception e) {
            GeneralDao.Instance.endTransaction();
            logger.fatal(e);
            logger.info("Failure: Can't start the switch up: " + e.getMessage());
            System.exit(0);
        }
    }

    @Override
    public void shutdown() {
        logger.info("Shutting Down!");
        try {
            JobServiceQuartz.stop();
            NetworkManager.getInstance().shutdown();
            ATMSessionManager.get().shutdown();
            MessageManager.getInstance().shutdown();
            GlobalContext.getInstance().shutdown();
            ConfigurationManager.getInstance().shutdown();
        } catch (Exception e) {
            System.out.println("Couldn't shutdown the switch successfully: " + e);
        }
    }

    @Override
    public void run() {
        logger.info("********** Running Vaulsys WCMS version " + version + "! **********");
    }

}
