package vaulsys.application;

import vaulsys.config.ConfigurationManager;
import vaulsys.job.quartz.JobServiceQuartz;
import vaulsys.persistence.GeneralDao;
import vaulsys.util.SettlementApplication;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.process.SwitchThreadPool;

import org.apache.log4j.Logger;

public class AccountingSettlementApplication extends BaseApp {
    transient Logger logger = Logger.getLogger(AccountingSettlementApplication.class);
    final String version = "1.0.0";

    @Override
    public void startup() {
		GeneralDao.Instance.beginTransaction();
		
    	try {
    		JobServiceQuartz.init(JobServiceQuartz.SETTLE_CONFIG);
    		
    		SettlementApplication.get().startup();
            
            logger.info("Starting up ConfigurationManager... please wait");
            ConfigurationManager.getInstance().startup();

            logger.info("Starting up GlobalContext... please wait");
            GlobalContext.getInstance();
            GlobalContext.getInstance().startup();

            logger.info("Starting up ThreadPool... please wait");
            ConfigurationManager.getInstance().loadConfig(SwitchThreadPool.class);
            
            logger.info("Starting up Scheduler... please wait");
            JobServiceQuartz.start();

            GeneralDao.Instance.endTransaction();
        } catch (Exception e) {
            GeneralDao.Instance.endTransaction();
        	logger.fatal(e);
            logger.info("Failure: Can't start the switch up: " + e.getMessage(), e);
            System.exit(0);
        }
    }

    @Override
    public void shutdown() {
        logger.info("Shutting Down!");
        try {
        	JobServiceQuartz.stop();
            GlobalContext.getInstance().shutdown();
            ConfigurationManager.getInstance().shutdown();
        } catch (Exception e) {
            System.out.println("Couldn't shutdown the switch successfully: " + e);
        }
    }

    @Override
    public void run() {
        logger.info("Running Vaulsys WCMS version " + version + "!");
    }

}
